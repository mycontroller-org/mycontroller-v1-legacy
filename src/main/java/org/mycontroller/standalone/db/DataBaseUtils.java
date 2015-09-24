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
import java.util.List;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.SystemJob;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.jobs.MidNightJob;
import org.mycontroller.standalone.jobs.SensorLogAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsFiveMinutesAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsOneDayAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsOneHourAggregationJob;
import org.mycontroller.standalone.jobs.metrics.MetricsOneMinuteAggregationJob;
import org.mycontroller.standalone.mysensors.MyMessages;
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
    //private static ConnectionSource connectionSource = null;
    private static JdbcPooledConnectionSource connectionPooledSource = null;
    // this uses h2 by default but change to match your database
    //private static String databaseUrl = "jdbc:h2:mem:account";
    private static String databaseUrl = "jdbc:h2:file:" + ObjectFactory.getAppProperties().getH2DbLocation();

    //private static String databaseUrl = "jdbc:sqlite:/tmp/mysensors.db";

    public static ConnectionSource getConnectionSource() throws DbException {
        if (connectionPooledSource != null) {
            _logger.debug("DatabaseConnectionPool Connections,Count[open:{},close:{}],"
                    + "CurrentConnections[free:{},managed:{}],"
                    + "MaxConnectionsEverUsed:{},TestLoopCount:{}",
                    connectionPooledSource.getOpenCount(),
                    connectionPooledSource.getCloseCount(),
                    connectionPooledSource.getCurrentConnectionsFree(),
                    connectionPooledSource.getCurrentConnectionsManaged(),
                    connectionPooledSource.getMaxConnectionsEverUsed(),
                    connectionPooledSource.getTestLoopCount());
            return connectionPooledSource;
        } else {
            throw new DbException("Database connection should be inilized before to call me..");
        }
    }

    public static synchronized void loadDatabase() throws SQLException, ClassNotFoundException {
        if (!isDbLoaded) {
            //Class.forName("org.sqlite.JDBC");
            /* // create a connection source to our database
             connectionSource = new JdbcConnectionSource(databaseUrl);
             */
            // pooled connection source
            connectionPooledSource =
                    new JdbcPooledConnectionSource(databaseUrl);
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
            updateSchema();
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

    public static void updateSchema() {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.MC_DB_VERSION);
        int dbVersion = 0;
        if (settings != null) {
            dbVersion = Integer.valueOf(settings.getValue());
        }
        _logger.debug("MC DB Version:{}", dbVersion);

        if (dbVersion < 1) { //Update version 1 schema
            //System Settings
            DaoUtils.getSettingsDao().create(new Settings(Settings.MC_VERSION, "0.0.1", "MC Version"));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.DEFAULT_UNIT_TEMPERATURE, MyMessages.UNITS_TEMPERATURE.CELSIUS.value(),
                            "Temperature"));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.DEFAULT_UNIT_DISTANCE, MyMessages.UNITS_DISTANCE.CENTIMETER.value(),
                            "Distance"));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.DEFAULT_UNIT_PERCENTAGE, MyMessages.UNITS_PERCENTAGE.PERCENTAGE.value(),
                            "Percentage"));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.CITY_LATITUDE, "11.2333", "City Latitude", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.CITY_LONGITUDE, "78.1667", "City Longitude", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.SUNRISE_TIME, "0", "Sunrise Time"));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.SUNSET_TIME, "0", "Sunset Time"));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.AUTO_NODE_ID, "0", "Auto Node Id (MySensors)", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.DEFAULT_FIRMWARE, null, "Default Firmware", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.ENABLE_NOT_AVAILABLE_TO_DEFAULT_FIRMWARE, "false",
                            "If requested firmware is not available, redirect to default", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.EMAIL_SMTP_HOST, null, "SMTP Host", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.EMAIL_SMTP_PORT, null, "SMTP Port", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.EMAIL_FROM, null, "From address", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.EMAIL_SMTP_USERNAME, null, "Username", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.EMAIL_SMTP_PASSWORD, null, "Password", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.EMAIL_ENABLE_SSL, null, "Enable SSL", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.SMS_AUTH_ID, null, "Auth Id", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.SMS_AUTH_TOKEN, null, "Auth Token", true));
            DaoUtils.getSettingsDao().create(
                    new Settings(Settings.SMS_FROM_PHONE_NUMBER, null, "From phone number", true));

            //Add System Jobs
            DaoUtils.getSystemJobDao().create(
                    new SystemJob(
                            "Aggregate One Minute Data", "58 * * * * ? *",
                            true, MetricsOneMinuteAggregationJob.class.getName()));
            DaoUtils.getSystemJobDao().create(
                    new SystemJob(
                            "Aggregate Five Minutes Data", "0 0/5 * * * ? *",
                            true, MetricsFiveMinutesAggregationJob.class.getName()));
            DaoUtils.getSystemJobDao().create(
                    new SystemJob(
                            "Aggregate One Hour Data", "5 0 0/1 * * ? *",
                            true, MetricsOneHourAggregationJob.class.getName()));
            DaoUtils.getSystemJobDao().create(
                    new SystemJob(
                            "Aggregate One Day Data", "5 0 0 * * ? *",
                            //One day aggregation table takes previous date, if you change here change there also
                            true, MetricsOneDayAggregationJob.class.getName()));
            DaoUtils.getSystemJobDao().create(
                    new SystemJob(
                            "SensorLog Aggregation Job", "45 * * * * ? *",
                            true, SensorLogAggregationJob.class.getName()));

            DaoUtils.getSystemJobDao().create(
                    new SystemJob(
                            "Daily once job", "30 3 0 * * ? *",
                            true, MidNightJob.class.getName()));

            //Add default User
            User adminUser = new User("admin");
            adminUser.setPassword("admin");
            adminUser.setEmail("admin@localhost.com");
            adminUser.setRoleId(USER_ROLE.ADMIN.ordinal());
            adminUser.setFullName("Admin");
            DaoUtils.getUserDao().create(adminUser);

            DaoUtils.getSettingsDao().create(new Settings(Settings.MC_DB_VERSION, "1", "Database Schema Revision"));
            _logger.info("MC DB version[{}] upgraded to version[{}]", dbVersion, 1);
            dbVersion = 1;
        }
        if (dbVersion < 5) {
            List<SystemJob> systemJobs = DaoUtils.getSystemJobDao().getAll();
            for (SystemJob systemJob : systemJobs) {
                systemJob.setClassName(systemJob.getClassName().replaceAll(
                        "org.mycontroller.standalone.scheduler.jobs.Metrics",
                        "org.mycontroller.standalone.jobs.metrics.Metrics"));
                systemJob.setClassName(systemJob.getClassName().replaceAll(
                        "org.mycontroller.standalone.scheduler.jobs",
                        "org.mycontroller.standalone.jobs"));
                DaoUtils.getSystemJobDao().update(systemJob);
            }

            //Add Line chart default interpolate type
            settings = new Settings(Settings.GRAPH_INTERPOLATE_TYPE, "linear", "Interpolate Type", true);
            DaoUtils.getSettingsDao().create(settings);
            upgradeVersion("0.0.2-alpha4", dbVersion, dbVersion + 1);
            dbVersion = 5;
        }
    }

    private static void upgradeVersion(String appVersion, int dbVersionOld, int dbVersionNew) {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.MC_VERSION);
        settings.setValue(appVersion);
        DaoUtils.getSettingsDao().update(settings);

        settings = DaoUtils.getSettingsDao().get(Settings.MC_DB_VERSION);
        settings.setValue(String.valueOf(dbVersionNew));
        DaoUtils.getSettingsDao().update(settings);
        _logger.info("MC DB version[{}] upgraded to version[{}]", dbVersionOld, dbVersionNew);
    }

    //http://www.h2database.com/html/tutorial.html#upgrade_backup_restore
    public static void backupDb() {

    }
}
