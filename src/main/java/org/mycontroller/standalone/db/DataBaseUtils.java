/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
import java.util.ArrayList;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.MC_TIME_FORMAT;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.mycontroller.standalone.alarm.jobs.AlarmDefinitionDampeningActiveTimeJob;
import org.mycontroller.standalone.auth.AuthUtils.PERMISSION_TYPE;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.db.tables.SystemJob;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.jobs.MidNightJobs;
import org.mycontroller.standalone.jobs.NodeAliveStatusJob;
import org.mycontroller.standalone.jobs.ResourcesLogsAggregationJob;
import org.mycontroller.standalone.metrics.jobs.MetricsAggregationJob;
import org.mycontroller.standalone.settings.EmailSettings;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.settings.MetricsSettings;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.settings.MySensorsSettings;
import org.mycontroller.standalone.settings.SettingsUtils;
import org.mycontroller.standalone.settings.SmsSettings;
import org.mycontroller.standalone.settings.Unit;
import org.mycontroller.standalone.settings.UnitsSettings;
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
            ObjectFactory.getAppProperties().loadPropertiesFromDb();
            upgradeSchema();
            //After update schema, reload settings again
            ObjectFactory.getAppProperties().loadPropertiesFromDb();
            //create or update static json file used for GUI before login
            SettingsUtils.updateStaticJsonInformationFile();
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
        MyControllerSettings controllerSettings = ObjectFactory.getAppProperties().getControllerSettings();
        int dbVersion = 0;
        if (controllerSettings.getDbVersion() != null) {
            dbVersion = controllerSettings.getDbVersion();
        }
        _logger.debug("MC DB Version:{}", dbVersion);

        if (dbVersion < 6 && dbVersion > 0) {
            throw new RuntimeException("This version of " + AppProperties.APPLICATION_NAME
                    + " database not supported! Workaround: stop " + AppProperties.APPLICATION_NAME
                    + " server, delete existing database, start " + AppProperties.APPLICATION_NAME + " server.");
        }
        if (dbVersion == 0) { // Update version 1 schema
            // Metric or Imperial to sensors
            ArrayList<Unit> unitVariables = new ArrayList<Unit>();

            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_CURRENT.getText(), "A", "A"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_CUSTOM.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_DIRECTION.getText(), "°", "°"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_DISTANCE.getText(), "cm", "cm"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_FLOW.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_GUST.getText(), "mph", "mph"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_HUM.getText(), "%", "%"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_COOL.getText(), "°C", "°F"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_HEAT.getText(), "°C", "°F"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_IMPEDANCE.getText(), "Ω", "Ω"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_KWH.getText(), "kWh", "kWh"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_LEVEL.getText(), "%", "%"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL.getText(), "%", "%"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_PERCENTAGE.getText(), "%", "%"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_POSITION.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_PRESSURE.getText(), "psi", "psi"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_RAIN.getText(), "mm", "mm"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_RAINRATE.getText(), "mm/hr", "mm/hr"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_TEMP.getText(), "°C", "°F"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_UV.getText(), "mj/cm2", "mj/cm2"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_VAR1.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_VAR2.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_VAR3.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_VAR4.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_VAR5.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_VOLTAGE.getText(), "V", "V"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_VOLUME.getText(), "", ""));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_WATT.getText(), "W", "W"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_WEIGHT.getText(), "kg", "kg"));
            unitVariables.add(new Unit(MESSAGE_TYPE_SET_REQ.V_WIND.getText(), "mph", "mph"));

            //Update unit values into table
            UnitsSettings.builder().variables(unitVariables).build().save();

            //Update location settings
            LocationSettings.builder()
                    .name("Namakkal")
                    .latitude("11.2333")
                    .longitude("78.1667").build().save();

            LocationSettings.builder()
                    .sunriseTime(0l)
                    .sunsetTime(0l).build().updateInternal();

            //Update MySensors settings
            MySensorsSettings.builder()
                    .defaultFirmware(null)
                    .enbaledDefaultOnNoFirmware(false).build().save();

            //Update email settings
            EmailSettings.builder()
                    .smtpHost(null)
                    .smtpPort(null)
                    .enableSsl(false)
                    .fromAddress(null)
                    .smtpUsername(null)
                    .smtpPassword(null).build().save();

            //Update SMS settings
            SmsSettings.builder()
                    .vendor(null)
                    .authSid(null)
                    .authToken(null)
                    .fromNumber(null).build().save();

            // Add System Jobs
            createSystemJob("Metrics aggregate job", "05 * * * * ? *", true, MetricsAggregationJob.class);
            createSystemJob("ResourcesLogs Aggregation Job", "45 * * * * ? *", true, ResourcesLogsAggregationJob.class);
            createSystemJob("Daily once job", "30 3 0 * * ? *", true, MidNightJobs.class);

            // Add a job to monitor Alarm definitions with active time dampening
            // run this job every 30 seconds once
            createSystemJob("Alarm definition dampening active time", "25,55 * * * * ? *", true,
                    AlarmDefinitionDampeningActiveTimeJob.class);

            //Add super admin role
            DaoUtils.getRoleDao().create(Role.builder()
                    .name("Super admin role")
                    .description("created by system on installation")
                    .permission(PERMISSION_TYPE.SUPER_ADMIN)
                    .build());

            // Add default User          
            DaoUtils.getUserDao().create(User.builder()
                    .username("admin")
                    .enabled(true)
                    .fullName("Admin")
                    .email("admin@localhost.com")
                    .password("admin")
                    .build());
            //Get created role
            Role superAdminRole = DaoUtils.getRoleDao().getByRoleName("Super admin role");
            //Get created user
            User adminUser = DaoUtils.getUserDao().getByUsername("admin");
            //Map role and user
            DaoUtils.getRoleUserMapDao().create(RoleUserMap.builder()
                    .user(adminUser)
                    .role(superAdminRole)
                    .build());

            //Update controller default settings
            MyControllerSettings.builder()
                    .language(MC_LANGUAGE.EN_US.getText())
                    .aliveCheckInterval(NodeAliveStatusJob.DEFAULT_ALIVE_CHECK_INTERVAL)
                    .timeFormat(MC_TIME_FORMAT.HOURS_12.getText())
                    .unitConfig(UNIT_CONFIG.METRIC.getText())
                    .loginMessage("Default username: <b>admin</b>, password: <b>admin<b>")
                    .build().save();

            //Update Metrics reference data
            MetricsSettings.builder()
                    .lastAggregationRawData(System.currentTimeMillis())
                    .lastAggregationOneMinute(System.currentTimeMillis())
                    .lastAggregationFiveMinutes(System.currentTimeMillis())
                    .lastAggregationOneHour(System.currentTimeMillis())
                    .lastAggregationSixHours(System.currentTimeMillis())
                    .lastAggregationTwelveHours(System.currentTimeMillis())
                    .lastAggregationOneDay(System.currentTimeMillis())
                    .build().updateInternal();

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
            createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC, MESSAGE_TYPE_SET_REQ.V_HVAC_SPEED);
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

            dbVersion = 8;
            // update version information
            MyControllerSettings.builder()
                    .version("0.0.2-alpha7-SNAPSHOT")
                    .dbVersion(dbVersion)
                    .build().updateInternal();
            _logger.info("MC DB version[{}]", dbVersion);

        }

    }

    private static void createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION sensorType,
            MESSAGE_TYPE_SET_REQ variableType) {
        DaoUtils.getSensorsVariablesMapDao().create(sensorType, variableType);
    }

    private static void createSystemJob(String name, String cronExpression, boolean isEnabled, Class<?> clazz) {
        DaoUtils.getSystemJobDao().create(
                SystemJob.builder().name(name).cron(cronExpression).enabled(isEnabled).className(clazz.getName())
                        .build());
    }

    // http://www.h2database.com/html/tutorial.html#upgrade_backup_restore
    public static void backupDb() {

    }
}
