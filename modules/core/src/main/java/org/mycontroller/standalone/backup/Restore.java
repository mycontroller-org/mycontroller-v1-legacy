/*
 * Copyright 2015-2019 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.backup;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.BadRequestException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.DB_TYPE;
import org.mycontroller.standalone.StartApp;
import org.mycontroller.standalone.api.jaxrs.model.McFile;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class Restore implements Runnable {
    private McFile backupFile;

    public Restore(McFile backupFile) {
        this.backupFile = backupFile;
    }

    private static void loadDefaultProperties() {
        StartApp.loadInitialProperties(System.getProperty("mc.conf.file"));
    }

    public static void restore(McFile backupFile) throws IOException {
        if (Commons.IS_BACKUP_RESTORE_RUNNING.get()) {
            throw new BadRequestException("A backup or restore is running");
        }

        if (!backupFile.getName().contains(Commons.BACKUP_FILE_NAME_IDENTITY)) {
            throw new BadRequestException("backup file name should contain '" + Commons.BACKUP_FILE_NAME_IDENTITY
                    + "'. Your input:" + backupFile.getName());
        }

        Commons.IS_BACKUP_RESTORE_RUNNING.set(true);

        _logger.info("About to restore a backup, {}", backupFile);

        String extractedLocation = AppProperties.getInstance().getTmpLocation()
                + backupFile.getName().replaceAll(".zip", "");
        try {
            //Extract zip file
            _logger.debug("Zip file:{}", backupFile.getCanonicalPath());

            McFileUtils.extractZipFile(backupFile.getCanonicalPath(), extractedLocation);
            _logger.debug("All the files extracted to '{}'", extractedLocation);
            //Validate required files
            if (!FileUtils.getFile(extractedLocation + File.separator + Commons.APP_PROPERTIES_FILENAME).exists()) {
                _logger.error("Unable to continue restore opration! selected file not found! File:{}",
                        extractedLocation + File.separator + Commons.APP_PROPERTIES_FILENAME);
                return;
            }

            //Load initial properties
            if (!StartApp.loadInitialProperties(extractedLocation + File.separator + Commons.APP_PROPERTIES_FILENAME)) {
                loadDefaultProperties();
                _logger.error("Failed to load properties file from '{}'", extractedLocation + File.separator
                        + Commons.APP_PROPERTIES_FILENAME);
                return;
            }
            boolean executeDbBackup = false;
            if (AppProperties.getInstance().getDbType() == DB_TYPE.H2DB_EMBEDDED) {
                executeDbBackup = true;
            } else if (AppProperties.getInstance().getDbType() == DB_TYPE.H2DB
                    && AppProperties.getInstance().includeDbBackup()) {
                executeDbBackup = true;
            }

            if (executeDbBackup) {
                //Validate required files
                if (!FileUtils.getFile(extractedLocation + File.separator + Commons.BACKUP_DATABASE_FILENAME).exists()) {
                    _logger.error("Unable to continue restore opration! selected file not found! File:{}",
                            extractedLocation + File.separator + Commons.BACKUP_DATABASE_FILENAME);
                    loadDefaultProperties();
                    return;
                }
            }

            //Stop all services
            StartApp.stopServices(false);

            //Restore properties file
            //Remove old properties file
            FileUtils.deleteQuietly(FileUtils.getFile(Commons.APP_CONF_LOCATION + Commons.APP_PROPERTIES_FILENAME));

            FileUtils.copyFile(
                    FileUtils.getFile(extractedLocation + File.separator + Commons.APP_PROPERTIES_FILENAME),
                    FileUtils.getFile(Commons.APP_CONF_LOCATION + Commons.APP_PROPERTIES_FILENAME));

            if (AppProperties.getInstance().isWebHttpsEnabled()) {
                //Remove old files
                FileUtils.deleteQuietly(FileUtils.getFile(AppProperties.getInstance().getWebSslKeystoreFile()));
                //restore key store file
                FileUtils.copyFile(
                        FileUtils.getFile(extractedLocation + File.separator + FileUtils.getFile(
                                AppProperties.getInstance().getWebSslKeystoreFile()).getName()),
                        FileUtils.getFile(AppProperties.getInstance().getWebSslKeystoreFile()));
            }

            McFileUtils.restoreResourceFiles(extractedLocation);
            if (executeDbBackup) {
                //restore database
                if (!DataBaseUtils.restoreDatabase(extractedLocation + File.separator
                        + Commons.BACKUP_DATABASE_FILENAME)) {
                    _logger.error("Database restore failed:{}", extractedLocation + File.separator
                            + Commons.BACKUP_DATABASE_FILENAME);
                    return;
                }
            } else {
                _logger.info("Database file not included on restore...");
            }
            _logger.info("Restore completed successfully. Start '{}' server manually", AppProperties.APPLICATION_NAME);
        } finally {
            //clean tmp file
            FileUtils.deleteQuietly(FileUtils.getFile(extractedLocation));
            _logger.debug("Tmp location[{}] clean success", extractedLocation);
            Commons.IS_BACKUP_RESTORE_RUNNING.set(false);
        }
        //Stop application
        System.exit(0);
    }

    @Override
    public void run() {
        try {
            //wait few seconds to response for caller REST API
            Thread.sleep(McUtils.ONE_SECOND * 3);
            restore(backupFile);
        } catch (Exception ex) {
            _logger.error("Restore failed!", ex);
        }
    }

    public static String getDbLocation(String dbUrl) {
        String finalPath = "../conf/mycontroller";
        if (dbUrl.startsWith("jdbc:h2:file:")) {
            String databaseUrl = dbUrl;
            databaseUrl = databaseUrl.replace("jdbc:h2:file:", "");
            int toIndex = databaseUrl.indexOf(';');
            if (toIndex == -1) {
                toIndex = databaseUrl.length();
            }
            finalPath = databaseUrl.substring(0, toIndex);
        }
        _logger.debug("Database url:[], location:[]", dbUrl, finalPath);
        return finalPath;
    }
}
