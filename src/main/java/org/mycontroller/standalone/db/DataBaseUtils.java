/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.db;

import java.sql.SQLException;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.SystemJob;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.jobs.MidNightJob;
import org.mycontroller.standalone.jobs.SensorLogAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsFiveMinutesAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsOneDayAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsOneHourAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsOneMinuteAggregationJob;
import org.mycontroller.standalone.jobs.mysensors.HeartbeatJob;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class DataBaseUtils {
    private DataBaseUtils() {
    }

    private static final Logger _logger = LoggerFactory.getLogger(DataBaseUtils.class.getName());
    private static boolean isDbLoaded = false;
    // private static ConnectionSource connectionSource = null;
    private static JdbcPooledConnectionSource connectionPooledSource = null;
    // this uses h2 by default but change to match your database
    // private static String databaseUrl = "jdbc:h2:mem:account";
    private static String databaseUrl = "jdbc:h2:file:" + ObjectFactory.getAppProperties().getDbH2DbLocation();

    // private static String databaseUrl = "jdbc:sqlite:/tmp/mysensors.db";

    public static ConnectionSource getConnectionSource() throws DbException {
        if (connectionPooledSource != null) {
            _logger.debug(
                    "DatabaseConnectionPool Connections,Count[open:{},close:{}],"
                            + "CurrentConnections[free:{},managed:{}]," + "MaxConnectionsEverUsed:{},TestLoopCount:{}",
                    connectionPooledSource.getOpenCount(), connectionPooledSource.getCloseCount(),
                    connectionPooledSource.getCurrentConnectionsFree(),
                    connectionPooledSource.getCurrentConnectionsManaged(),
                    connectionPooledSource.getMaxConnectionsEverUsed(), connectionPooledSource.getTestLoopCount());
            return connectionPooledSource;
        } else {
            throw new DbException("Database connection should be inilized before to call me..");
        }
    }

    public static synchronized void loadDatabase() throws SQLException, ClassNotFoundException {
        if (!isDbLoaded) {
            // Class.forName("org.sqlite.JDBC");
            /*
             * // create a connection source to our database connectionSource =
             * new JdbcConnectionSource(databaseUrl);
             */
            // pooled connection source
            connectionPooledSource = new JdbcPooledConnectionSource(databaseUrl);
            // only keep the connections open for 5 minutes
            connectionPooledSource.setMaxConnectionAgeMillis(TIME_REF.FIVE_MINUTES);
            // change the check-every milliseconds from 30 seconds to 60
            connectionPooledSource.setCheckConnectionsEveryMillis(TIME_REF.THREE_MINUTES);
            // for extra protection, enable the testing of connections
            // right before they are handed to the user
            connectionPooledSource.setTestBeforeGet(true);
            isDbLoaded = true;
            _logger.debug("Database ConnectionSource loaded. Database Url:[{}]", databaseUrl);
            DaoUtils.loadAllDao();
            upgradeSchema();
        } else {
            _logger.info("Database ConnectionSource already created. Nothing to do. Database Url:[{}]", databaseUrl);
        }
    }

    public static void stop() {
        if (connectionPooledSource != null && connectionPooledSource.isOpen()) {
            try {
                connectionPooledSource.close();
                _logger.debug("Database service stopped.");
                isDbLoaded = false;
            } catch (SQLException sqlEx) {
                _logger.error("Unable to stop database service, ", sqlEx);
            }
        } else {
            _logger.debug("Database service not running.");
        }
    }

    public static void upgradeSchema() {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.MC_DB_VERSION);
        int dbVersion = 0;
        if (settings != null) {
            dbVersion = Integer.valueOf(settings.getValue());
        }
        _logger.debug("MC DB Version:{}", dbVersion);

        if (dbVersion < 6 && dbVersion > 0) {
            throw new RuntimeException("This version of " + AppProperties.APPLICATION_NAME
                    + " database not supported! Workaround: stop " + AppProperties.APPLICATION_NAME
                    + " server, delete existing database, start " + AppProperties.APPLICATION_NAME + " server.");
        }
        if (dbVersion == 0) { // Update version 1 schema
            // System Settings
            DaoUtils.getSettingsDao().create(new Settings(Settings.MC_VERSION, "0.0.2-alpha5", "MC Version"));

            // Metric or Imperial to sensors
            DaoUtils.getSettingsDao()
                    .create(new Settings(Settings.MY_SENSORS_CONFIG, "Metric", "MySensors Config", true));

            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_TEMP, "°C", "Temperature", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_HUM, "%", "Humidity", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_PERCENTAGE, "%", "Percentage", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_PRESSURE, "psi", "Pressure", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_RAIN, "mm", "Rain", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_RAINRATE, "mm/hr", "Rain Rate", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_WIND, "mph", "Wind", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_GUST, "mph", "Gust", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_DIRECTION, "°", "Direction", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_UV, "mj/cm2", "UV", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_WEIGHT, "kg", "Weight", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_DISTANCE, "cm", "Distance", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_IMPEDANCE, "Ω", "Impedance", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_WATT, "W", "Watt", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_KWH, "kWh", "KWH", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL, "%", "Light Level", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_LEVEL, "%", "Level", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_VOLTAGE, "V", "Voltage", true);
            createSettings(Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.V_CURRENT, "A", "Current", true);

            createSettings(Settings.CITY_NAME, "Namakkal", "City Name", true);
            createSettings(Settings.CITY_LATITUDE, "11.2333", "City Latitude", true);
            createSettings(Settings.CITY_LONGITUDE, "78.1667", "City Longitude", true);
            createSettings(Settings.SUNRISE_TIME, "0", "Sunrise Time");
            createSettings(Settings.SUNSET_TIME, "0", "Sunset Time");

            createSettings(Settings.AUTO_NODE_ID, "0", "Auto Node Id (MySensors)", true);
            createSettings(Settings.DEFAULT_FIRMWARE, null, "Default Firmware", true);
            createSettings(Settings.ENABLE_NOT_AVAILABLE_TO_DEFAULT_FIRMWARE, "false",
                    "If requested firmware is not available, redirect to default", true);
            createSettings(Settings.ENABLE_SEND_PAYLOAD, "false", "Enable Send Payload", true);

            createSettings(Settings.EMAIL_SMTP_HOST, null, "SMTP Host", true);
            createSettings(Settings.EMAIL_SMTP_PORT, null, "SMTP Port", true);
            createSettings(Settings.EMAIL_FROM, null, "From address", true);
            createSettings(Settings.EMAIL_SMTP_USERNAME, null, "Username", true);
            createSettings(Settings.EMAIL_SMTP_PASSWORD, null, "Password", true);
            createSettings(Settings.EMAIL_ENABLE_SSL, null, "Enable SSL", true);

            createSettings(Settings.SMS_AUTH_ID, null, "Auth Id", true);
            createSettings(Settings.SMS_AUTH_TOKEN, null, "Auth Token", true);
            createSettings(Settings.SMS_FROM_PHONE_NUMBER, null, "From phone number", true);

            // Graph type
            createSettings(Settings.GRAPH_INTERPOLATE_TYPE, "linear", "Interpolate Type", true);

            // Add System Jobs
            createSystemJob("Aggregate One Minute Data", "58 * * * * ? *", true, MetricsOneMinuteAggregationJob.class);
            createSystemJob("Aggregate Five Minutes Data", "0 0/5 * * * ? *", true,
                    MetricsFiveMinutesAggregationJob.class);
            createSystemJob("Aggregate One Hour Data", "5 0 0/1 * * ? *", true, MetricsOneHourAggregationJob.class);
            // One day aggregation table takes previous date, if you change here
            // change there also
            createSystemJob("Aggregate One Day Data", "5 0 0 * * ? *", true, MetricsOneDayAggregationJob.class);
            createSystemJob("SensorLog Aggregation Job", "45 * * * * ? *", true, SensorLogAggregationJob.class);
            createSystemJob("Daily once job", "30 3 0 * * ? *", true, MidNightJob.class);

            // Add default User
            User adminUser = new User("admin");
            adminUser.setPassword("admin");
            adminUser.setEmail("admin@localhost.com");
            adminUser.setRoleId(USER_ROLE.ADMIN.ordinal());
            adminUser.setFullName("Admin");
            DaoUtils.getUserDao().create(adminUser);

            // Update Sensor Type and Variables Type mapping

            // Door sensor, V_TRIPPED, V_ARMED
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DOOR, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DOOR, MESSAGE_TYPE_SET_REQ.V_ARMED);

            // Motion sensor, V_TRIPPED, V_ARMED
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOTION, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOTION, MESSAGE_TYPE_SET_REQ.V_ARMED);

            // Smoke sensor, V_TRIPPED, V_ARMED
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SMOKE, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SMOKE, MESSAGE_TYPE_SET_REQ.V_ARMED);

            // Binary light or relay, V_STATUS (or V_LIGHT), V_WATT
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BINARY, MESSAGE_TYPE_SET_REQ.V_STATUS);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BINARY, MESSAGE_TYPE_SET_REQ.V_WATT);

            // Dimmable light or fan device, V_STATUS (on/off),
            // V_DIMMER(V_PERCENTAGE) (dimmer level 0-100), V_WATT
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DIMMER, MESSAGE_TYPE_SET_REQ.V_STATUS);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DIMMER, MESSAGE_TYPE_SET_REQ.V_PERCENTAGE);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DIMMER, MESSAGE_TYPE_SET_REQ.V_WATT);

            // Blinds or window cover, V_UP, V_DOWN, V_STOP, V_DIMMER
            // (open/close to a percentage)
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER, MESSAGE_TYPE_SET_REQ.V_UP);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER, MESSAGE_TYPE_SET_REQ.V_DOWN);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER, MESSAGE_TYPE_SET_REQ.V_STOP);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER, MESSAGE_TYPE_SET_REQ.V_PERCENTAGE);

            // Temperature sensor, V_TEMP
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_TEMP, MESSAGE_TYPE_SET_REQ.V_TEMP);

            // Humidity sensor, V_HUM
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HUM, MESSAGE_TYPE_SET_REQ.V_HUM);

            // Barometer sensor, V_PRESSURE, V_FORECAST
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BARO, MESSAGE_TYPE_SET_REQ.V_PRESSURE);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BARO, MESSAGE_TYPE_SET_REQ.V_FORECAST);

            // Wind sensor, V_WIND, V_GUST
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WIND, MESSAGE_TYPE_SET_REQ.V_WIND);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WIND, MESSAGE_TYPE_SET_REQ.V_GUST);

            // Rain sensor, V_RAIN, V_RAINRATE
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RAIN, MESSAGE_TYPE_SET_REQ.V_RAIN);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RAIN, MESSAGE_TYPE_SET_REQ.V_RAINRATE);

            // Uv sensor, V_UV
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_UV, MESSAGE_TYPE_SET_REQ.V_UV);

            // Personal scale sensor, V_WEIGHT, V_IMPEDANCE
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WEIGHT, MESSAGE_TYPE_SET_REQ.V_WEIGHT);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WEIGHT, MESSAGE_TYPE_SET_REQ.V_IMPEDANCE);

            // Power meter, V_WATT, V_KWH
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_POWER, MESSAGE_TYPE_SET_REQ.V_WATT);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_POWER, MESSAGE_TYPE_SET_REQ.V_KWH);

            // Header device, V_HVAC_SETPOINT_HEAT, V_HVAC_FLOW_STATE, V_TEMP
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HEATER, MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_HEAT);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HEATER, MESSAGE_TYPE_SET_REQ.V_HVAC_FLOW_STATE);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HEATER, MESSAGE_TYPE_SET_REQ.V_TEMP);

            // Distance sensor, V_DISTANCE
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DISTANCE, MESSAGE_TYPE_SET_REQ.V_DISTANCE);

            // Light level sensor, V_LIGHT_LEVEL (uncalibrated in percentage),
            // V_LEVEL (light level in lux)
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_LIGHT_LEVEL, MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_LIGHT_LEVEL, MESSAGE_TYPE_SET_REQ.V_LEVEL);

            // Lock device, V_LOCK_STATUS
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_LOCK, MESSAGE_TYPE_SET_REQ.V_LOCK_STATUS);

            // Ir device, V_IR_SEND, V_IR_RECEIVE
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_IR, MESSAGE_TYPE_SET_REQ.V_IR_SEND);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_IR, MESSAGE_TYPE_SET_REQ.V_IR_RECEIVE);

            // Water meter, V_FLOW, V_VOLUME
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER, MESSAGE_TYPE_SET_REQ.V_FLOW);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER, MESSAGE_TYPE_SET_REQ.V_VOLUME);

            // Air quality sensor, V_LEVEL
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_AIR_QUALITY, MESSAGE_TYPE_SET_REQ.V_LEVEL);

            // Custom sensor
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR1);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR2);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR3);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR4);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR5);

            // Dust sensor, V_LEVEL
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DUST, MESSAGE_TYPE_SET_REQ.V_LEVEL);

            // Scene controller device, V_SCENE_ON, V_SCENE_OFF.
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SCENE_CONTROLLER, MESSAGE_TYPE_SET_REQ.V_SCENE_ON);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SCENE_CONTROLLER, MESSAGE_TYPE_SET_REQ.V_SCENE_OFF);

            // RGB light. Send color component data using V_RGB. Also supports
            // V_WATT
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGB_LIGHT, MESSAGE_TYPE_SET_REQ.V_RGB);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGB_LIGHT, MESSAGE_TYPE_SET_REQ.V_WATT);

            // RGB light with an additional White component. Send data using
            // V_RGBW. Also supports V_WATT
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGBW_LIGHT, MESSAGE_TYPE_SET_REQ.V_RGBW);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGBW_LIGHT, MESSAGE_TYPE_SET_REQ.V_WATT);

            // Color sensor, send color information using V_RGB
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COLOR_SENSOR, MESSAGE_TYPE_SET_REQ.V_RGB);

            // Thermostat/HVAC device. V_HVAC_SETPOINT_HEAT,
            // V_HVAC_SETPOINT_COOL, V_HVAC_FLOW_STATE, V_HVAC_FLOW_MODE, V_TEMP
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC, MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_HEAT);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC, MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_COOL);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC, MESSAGE_TYPE_SET_REQ.V_HVAC_FLOW_STATE);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC, MESSAGE_TYPE_SET_REQ.V_HVAC_FLOW_MODE);
            // Multimeter device, V_VOLTAGE, V_CURRENT, V_IMPEDANCE
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MULTIMETER, MESSAGE_TYPE_SET_REQ.V_VOLTAGE);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MULTIMETER, MESSAGE_TYPE_SET_REQ.V_CURRENT);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MULTIMETER, MESSAGE_TYPE_SET_REQ.V_IMPEDANCE);

            // Sprinkler, V_STATUS (turn on/off), V_TRIPPED (if fire detecting
            // device)
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SPRINKLER, MESSAGE_TYPE_SET_REQ.V_STATUS);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SPRINKLER, MESSAGE_TYPE_SET_REQ.V_TRIPPED);

            // Water leak sensor, V_TRIPPED, V_ARMED
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER_LEAK, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER_LEAK, MESSAGE_TYPE_SET_REQ.V_ARMED);

            // Sound sensor, V_TRIPPED, V_ARMED, V_LEVEL (sound level in dB)
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SOUND, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SOUND, MESSAGE_TYPE_SET_REQ.V_ARMED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SOUND, MESSAGE_TYPE_SET_REQ.V_LEVEL);

            // Vibration sensor, V_TRIPPED, V_ARMED, V_LEVEL (vibration in Hz)
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_VIBRATION, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_VIBRATION, MESSAGE_TYPE_SET_REQ.V_ARMED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_VIBRATION, MESSAGE_TYPE_SET_REQ.V_LEVEL);

            // Moisture sensor, V_TRIPPED, V_ARMED, V_LEVEL (water content or
            // moisture in percentage?)
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOISTURE, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOISTURE, MESSAGE_TYPE_SET_REQ.V_ARMED);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOISTURE, MESSAGE_TYPE_SET_REQ.V_LEVEL);

            // LCD text device / Simple information device on controller, V_TEXT
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_INFO, MESSAGE_TYPE_SET_REQ.V_TEXT);

            // Gas meter, V_FLOW, V_VOLUME
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_GAS, MESSAGE_TYPE_SET_REQ.V_FLOW);
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_GAS, MESSAGE_TYPE_SET_REQ.V_VOLUME);

            dbVersion = 6;
            createSettings(Settings.MC_DB_VERSION, String.valueOf(dbVersion), "Database Schema Revision");
            _logger.info("MC DB version[{}]", dbVersion);

        }

        if (dbVersion == 6) {
            createSettings(Settings.MC_LANGUAGE, String.valueOf(MC_LANGUAGE.EN_US.ordinal()), "Language", true);
            createSettings(Settings.MC_TIME_12_24_FORMAT, "12", "Time Format", true);
            createSettings(Settings.MYS_HEARTBEAT_INTERVAL, String.valueOf(HeartbeatJob.DEFAULT_HEARTBEAT_INTERVAL),
                    "Heartbeat Interval(Minutes)", true);

            upgradeVersion("0.0.2-alpha6-SNAPSHOT", dbVersion, dbVersion + 1);
            dbVersion = 7;
        }

    }

    private static void createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION sensorType,
            MESSAGE_TYPE_SET_REQ variableType) {
        DaoUtils.getSensorsVariablesMapDao().create(sensorType.ordinal(), variableType.ordinal());
    }

    private static void createSettings(String key, String value, String friendlyName) {
        createSettings(key, value, friendlyName, false);
    }

    private static void createSettings(String key, String value, String friendlyName, boolean isEditable) {
        DaoUtils.getSettingsDao().create(new Settings(key, value, friendlyName, isEditable));
    }

    private static void createSystemJob(String name, String cronExpression, boolean isEnabled, Class<?> clazz) {
        DaoUtils.getSystemJobDao().create(new SystemJob(name, cronExpression, isEnabled, clazz.getName()));
    }

    private static void upgradeVersion(String appVersion, int dbVersionOld, int dbVersionNew) {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.MC_VERSION);
        settings.setValue(appVersion);
        DaoUtils.getSettingsDao().update(settings);

        settings = DaoUtils.getSettingsDao().get(Settings.MC_DB_VERSION);
        settings.setValue(String.valueOf(dbVersionNew));
        DaoUtils.getSettingsDao().update(settings);
        _logger.info("MC DB version[{}] upgraded to version[{}], Application version:{}", dbVersionOld, dbVersionNew,
                appVersion);
    }

    // http://www.h2database.com/html/tutorial.html#upgrade_backup_restore
    public static void backupDb() {

    }
}
