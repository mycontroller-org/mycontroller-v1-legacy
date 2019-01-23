/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.flywaydb.core.api.FlywayException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.DB_TYPE;
import org.mycontroller.standalone.api.SystemApi;
import org.mycontroller.standalone.api.jaxrs.model.McAbout;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.utils.McUtils;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class DataBaseUtils {
    private DataBaseUtils() {
    }

    private static boolean dbMigrationStatus = false;
    // private static ConnectionSource connectionSource = null;
    private static JdbcPooledConnectionSource connectionPooledSource = null;
    private static final String DB_MIGRATION_SCRIPT_LOCATION = "org/mycontroller/standalone/db/migration";
    private static final int DB_MAX_FREE_CONNECTION = 3;

    //private static final String APP_VERSION = "0.0.3-alpha2-SNAPSHOT";

    // private static String databaseUrl = "jdbc:sqlite:/tmp/mysensors.db";

    public static ConnectionSource getConnectionSource() throws SQLException {
        return getConnectionSource(false);
    }

    public static ConnectionSource getConnectionSource(boolean reload) throws SQLException {
        if (reload || connectionPooledSource == null) {
            stop();
            // Class.forName("org.sqlite.JDBC");
            /*
             * // create a connection source to our database connectionSource =
             * new JdbcConnectionSource(databaseUrl);
             */

            // pooled connection source
            connectionPooledSource = new JdbcPooledConnectionSource(AppProperties.getInstance().getDbUrl(),
                    AppProperties.getInstance().getDbUsername(), AppProperties.getInstance().getDbPassword());
            // only keep the connections open for 5 minutes
            connectionPooledSource.setMaxConnectionAgeMillis(McUtils.FIVE_MINUTES);
            // change the check-every milliseconds from 30 seconds to 60
            connectionPooledSource.setCheckConnectionsEveryMillis(McUtils.THREE_MINUTES);
            // Maximum free connections you want to keep
            connectionPooledSource.setMaxConnectionsFree(DB_MAX_FREE_CONNECTION);
            // for extra protection, enable the testing of connections
            // right before they are handed to the user
            connectionPooledSource.setTestBeforeGet(true);
            _logger.debug("Database ConnectionSource loaded. Database Url:[{}]", AppProperties.getInstance()
                    .getDbUrl());

        }
        _logger.debug(
                "DatabaseConnectionPool Connections,Count[open:{},close:{}],"
                        + "CurrentConnections[free:{},managed:{}]," + "MaxConnectionsEverUsed:{},TestLoopCount:{}",
                connectionPooledSource.getOpenCount(), connectionPooledSource.getCloseCount(),
                connectionPooledSource.getCurrentConnectionsFree(),
                connectionPooledSource.getCurrentConnectionsManaged(),
                connectionPooledSource.getMaxConnectionsEverUsed(), connectionPooledSource.getTestLoopCount());
        return connectionPooledSource;

    }

    public static synchronized void runDatabaseMigration() throws SQLException, ClassNotFoundException {
        if (!dbMigrationStatus) {
            // Steps to migrate database
            // Create the Flyway instance
            Flyway flyway = new Flyway();
            flyway.setTable("schema_version");
            // Point it to the database
            flyway.setDataSource(AppProperties.getInstance().getDbUrl(),
                    AppProperties.getInstance().getDbUsername(), AppProperties.getInstance().getDbPassword());
            flyway.setLocations(DB_MIGRATION_SCRIPT_LOCATION);
            flyway.setBaselineOnMigrate(true);
            // Start the migration
            int migrationsCount = 0;
            try {
                _logger.info("Checking migration...");
                migrationsCount = flyway.migrate();
            } catch (FlywayException fEx) {
                _logger.error("Migration exception, ", fEx);
                if (fEx.getMessage().contains("contains a failed migration")) {
                    flyway.repair();
                    migrationsCount = flyway.migrate();
                }
            }
            //Close opened connection
            try {
                if (!flyway.getDataSource().getConnection().isClosed()) {
                    flyway.getDataSource().getConnection().close();
                    _logger.debug("Closed flyway database connection.");
                }
            } catch (Exception ex) {
                _logger.error("Unable to close flyway connection", ex);
            }
            //Load Dao's if not loaded already
            if (!DaoUtils.isDaoInitialized()) {
                DaoUtils.loadAllDao();
            }

            //set recent migration version to application table.
            //load/reload settings
            AppProperties.getInstance().loadPropertiesFromDb();
            McAbout mcAbout = new SystemApi().getAbout();
            if (migrationsCount > 0) {
                MyControllerSettings
                        .builder()
                        .version(mcAbout.getGitVersion())
                        .dbVersion(flyway.info().current().getVersion()
                                + " - " + flyway.info().current().getDescription())
                        .build().updateInternal();
            } else {
                MyControllerSettings
                        .builder()
                        .version(mcAbout.getGitVersion())
                        .build().updateInternal();
            }

            //After executed migration, reload settings again
            AppProperties.getInstance().loadPropertiesFromDb();

            if (migrationsCount > 0) {
                _logger.info("Number of migrations done:{}", migrationsCount);
            } else {
                _logger.debug("Number of migrations done:{}", migrationsCount);
            }

            mcAbout = new SystemApi().getAbout();
            _logger.info(
                    "Application information: [Version:{}, Database(type:{}, version:{}, schema version:{}),"
                            + " Built on:{}, Git commit:{}:{}]", mcAbout.getApplicationVersion(),
                    mcAbout.getDatabaseType(), mcAbout.getDatabaseVersion(), mcAbout.getApplicationDbVersion(),
                    mcAbout.getGitBuiltOn(), mcAbout.getGitCommit(), mcAbout.getGitBranch());
            dbMigrationStatus = true;
            reloadDao();
        } else {
            _logger.warn("Database ConnectionSource already created. Nothing to do. Database Url:[{}]", AppProperties
                    .getInstance().getDbUrl());
        }

    }

    public static synchronized void loadDao() {
        //Load Dao's if not loaded already
        if (!DaoUtils.isDaoInitialized()) {
            DaoUtils.loadAllDao();
        }

        //Load properties from database
        AppProperties.getInstance().loadPropertiesFromDb();
    }

    public static synchronized void reloadDao() {
        _logger.debug("Reload DAO triggered...");
        try {
            //reload connection source
            getConnectionSource(true);
        } catch (SQLException ex) {
            _logger.error("Unable to reload database connection source.", ex);
        }
        loadDao();
    }

    public static void stop() {
        if (connectionPooledSource != null && connectionPooledSource.isOpen(null)) {
            try {
                connectionPooledSource.closeQuietly();
                _logger.debug("Database service stopped.");
                DaoUtils.setIsDaoInitialized(false);
            } catch (Exception ioEx) {
                _logger.error("Unable to stop database service, ", ioEx);
            }
        } else {
            _logger.debug("Database service is not running.");
        }
    }

    public static void createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION sensorType,
            MESSAGE_TYPE_SET_REQ variableType) {
        DaoUtils.getSensorsVariablesMapDao().create(sensorType, variableType);
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
            conn = DriverManager.getConnection(AppProperties.getInstance().getDbUrl(),
                    AppProperties.getInstance().getDbUsername(), AppProperties.getInstance().getDbPassword());
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

    public static String getDatabaseVersionQuery() {
        switch (AppProperties.getInstance().getDbType()) {
            case H2DB:
            case H2DB_EMBEDDED:
                return "SELECT VALUE as version FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME='info.VERSION'";
            default:
                return "SELECT version()";

        }
    }

    public static synchronized boolean restoreDatabase(String databaseRestoreScript) {
        Connection conn = null;
        try {
            String restoreFileFullPath = FileUtils.getFile(databaseRestoreScript).getCanonicalPath();
            _logger.debug("database backup triggered...");
            conn = DriverManager.getConnection(AppProperties.getInstance().getDbUrl(),
                    AppProperties.getInstance().getDbUsername(), AppProperties.getInstance().getDbPassword());
            if (AppProperties.getInstance().getDbType() == DB_TYPE.H2DB
                    || AppProperties.getInstance().getDbType() == DB_TYPE.H2DB_EMBEDDED) {
                //Drop everything
                PreparedStatement dropAllObjects = conn.prepareStatement("DROP ALL OBJECTS");
                dropAllObjects.execute();
            }
            //Restore database
            PreparedStatement restoreScript = conn.prepareStatement("RUNSCRIPT FROM ? COMPRESSION ZIP");
            restoreScript.setString(1, restoreFileFullPath);
            restoreScript.execute();
            _logger.info("Database restore completed. Database url:{}, Restored file name:{}",
                    AppProperties.getInstance().getDbUrl(), restoreFileFullPath);
            return true;
        } catch (SQLException | IOException ex) {
            _logger.error("Exception, restore failed!", ex);
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
