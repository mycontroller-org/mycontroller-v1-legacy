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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.flywaydb.core.Flyway;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.db.tables.SystemJob;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.settings.SettingsUtils;
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
    public static final String DB_URL_PREFIX = "jdbc:h2:file:";
    private static String DB_URL = null;
    private static final String DB_USERNAME = "mycontroller";
    private static final String DB_PASSWORD = "mycontroller";
    private static final String DB_MIGRATION_LOCATION = "org/mycontroller/standalone/db/migration";
    private static final String APP_VERSION = "0.0.3-alpha1";

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

            //Update Database url
            DB_URL = DB_URL_PREFIX + ObjectManager.getAppProperties().getDbH2DbLocation();

            // pooled connection source
            connectionPooledSource = new JdbcPooledConnectionSource(DB_URL, DB_USERNAME, DB_PASSWORD);
            // only keep the connections open for 5 minutes
            connectionPooledSource.setMaxConnectionAgeMillis(TIME_REF.FIVE_MINUTES);
            // change the check-every milliseconds from 30 seconds to 60
            connectionPooledSource.setCheckConnectionsEveryMillis(TIME_REF.THREE_MINUTES);
            // for extra protection, enable the testing of connections
            // right before they are handed to the user
            connectionPooledSource.setTestBeforeGet(true);
            isDbLoaded = true;
            _logger.debug("Database ConnectionSource loaded. Database Url:[{}]", DB_URL);

            //Steps to migrate database
            // Create the Flyway instance
            Flyway flyway = new Flyway();
            // Point it to the database
            flyway.setDataSource(DB_URL, DB_USERNAME, DB_PASSWORD);
            flyway.setLocations(DB_MIGRATION_LOCATION);
            // Start the migration
            int migrationsCount = flyway.migrate();

            //Load Dao's if not loaded already
            if (!DaoUtils.isDaoInitialized()) {
                DaoUtils.loadAllDao();
            }

            //set recent migration version to application table.
            if (migrationsCount > 0) {
                MyControllerSettings
                        .builder()
                        .version(APP_VERSION)
                        .dbVersion(flyway.info().current().getVersion()
                                + " - " + flyway.info().current().getDescription())
                        .build().updateInternal();
            } else {
                MyControllerSettings
                        .builder()
                        .version(APP_VERSION)
                        .build().updateInternal();
            }

            //After executed migration, reload settings again
            ObjectManager.getAppProperties().loadPropertiesFromDb();

            _logger.info("Number of migrations done:{}", migrationsCount);
            _logger.info("Application information: [Version:{}, Database version:{}]",
                    ObjectManager.getAppProperties().getControllerSettings().getVersion(),
                    ObjectManager.getAppProperties().getControllerSettings().getDbVersion());

            //create or update static json file used for GUI before login
            SettingsUtils.updateStaticJsonInformationFile();
        } else {
            _logger.warn("Database ConnectionSource already created. Nothing to do. Database Url:[{}]", DB_URL);
        }
    }

    public static void stop() {
        if (connectionPooledSource != null && connectionPooledSource.isOpen()) {
            try {
                connectionPooledSource.close();
                _logger.debug("Database service stopped.");
                isDbLoaded = false;
                DaoUtils.setIsDaoInitialized(false);
            } catch (SQLException sqlEx) {
                _logger.error("Unable to stop database service, ", sqlEx);
            }
        } else {
            _logger.debug("Database service not running.");
        }
    }

    public static void createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION sensorType,
            MESSAGE_TYPE_SET_REQ variableType) {
        DaoUtils.getSensorsVariablesMapDao().create(sensorType, variableType);
    }

    public static void createSystemJob(String name, String cronExpression, boolean isEnabled, Class<?> clazz) {
        DaoUtils.getSystemJobDao().create(
                SystemJob.builder().name(name).cron(cronExpression).enabled(isEnabled).className(clazz.getName())
                        .build());
    }

    public static synchronized boolean backupDatabase(String databaseBackup) {
        Connection conn = null;
        try {
            _logger.debug("database backup triggered...");
            File backupFile = new File(databaseBackup);
            //Create parent dir if not exist
            FileUtils.forceMkdir(backupFile.getParentFile());
            //Delete file is exists
            FileUtils.deleteQuietly(backupFile);
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement statement = conn.prepareStatement("SCRIPT TO ? COMPRESSION ZIP");
            statement.setString(1, backupFile.getAbsolutePath());
            statement.execute();
            _logger.debug("database backup completed. File name:{}", backupFile.getAbsolutePath());
            return true;
        } catch (SQLException ex) {
            _logger.error("Exception, backup failed!", ex);
        } catch (IOException ex) {
            _logger.error("Parent folder creation failed", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    _logger.error("Unable to close backup database connection!", ex);
                }
                _logger.debug("Database connection closed...");
            }
        }
        return false;
    }

    public static synchronized boolean restoreDatabase(String databaseRestoreScript) {
        Connection conn = null;
        try {
            _logger.debug("database backup triggered...");
            conn = DriverManager.getConnection(DB_URL_PREFIX + ObjectManager.getAppProperties().getDbH2DbLocation(),
                    DB_USERNAME, DB_PASSWORD);
            PreparedStatement statement = conn.prepareStatement("RUNSCRIPT FROM ? COMPRESSION ZIP");
            statement.setString(1, databaseRestoreScript);
            statement.execute();
            _logger.info("Database restore completed. Database location:{}, Restored file name:{}",
                    ObjectManager.getAppProperties().getDbH2DbLocation(), databaseRestoreScript);
            return true;
        } catch (SQLException ex) {
            _logger.error("Exception, backup failed!", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    _logger.error("Unable to close backup database connection!", ex);
                }
                _logger.debug("Database connection closed...");
            }
        }
        return false;
    }
}
