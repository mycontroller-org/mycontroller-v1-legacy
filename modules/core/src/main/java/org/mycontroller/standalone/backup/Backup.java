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
package org.mycontroller.standalone.backup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.BadRequestException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.DB_TYPE;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.mycontroller.standalone.exceptions.McException;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class Backup {
    private static String KEY_STORE_FILE = null;

    public static synchronized String backup(String prefix) throws McException, IOException {
        //backup database
        //backup configuration file
        //backup certificates
        //backup logback xml file
        String backupZipFileName = null;
        if (BRCommons.isBackupRestoreRunning()) {
            throw new BadRequestException("A backup or restore is running");
        }

        BRCommons.setBackupRestoreRunning(true);
        String applicationBackupDir = AppProperties.getInstance().getBackupSettings().getBackupLocation()
                + prefix + BRCommons.FILE_NAME_IDENTITY + "_"
                + AppProperties.getInstance().getDbType().name().toLowerCase()
                + new SimpleDateFormat("-yyyy_MM_dd-HH_mm_ss").format(new Date());
        //Create parent dir if not exist
        try {
            FileUtils.forceMkdir(FileUtils.getFile(applicationBackupDir));
            boolean includeDdBackup = false;
            if (AppProperties.getInstance().getDbType() == DB_TYPE.H2DB_EMBEDDED) {
                includeDdBackup = true;
            } else if (AppProperties.getInstance().getDbType() == DB_TYPE.H2DB
                    && AppProperties.getInstance().includeDbBackup()) {
                includeDdBackup = true;
            }

            if (includeDdBackup) {
                String databaseBackup = AppProperties.getInstance().getTmpLocation() + BRCommons.DATABASE_FILENAME;

                if (DataBaseUtils.backupDatabase(databaseBackup)) {
                    //Copy database file
                    FileUtils.moveFile(
                            FileUtils.getFile(databaseBackup),
                            FileUtils.getFile(applicationBackupDir + File.separator + BRCommons.DATABASE_FILENAME));
                    //copy static files
                } else {
                    throw new McException("Database backup failed!");
                }
            }

            //copy static files
            copyStaticFiles(applicationBackupDir);
            _logger.debug("Copied all the files");
            //create zip file
            McUtils.createZipFile(applicationBackupDir, applicationBackupDir + ".zip");
            _logger.debug("zip file creation done");
            //clean temporary files
            FileUtils.deleteDirectory(FileUtils.getFile(applicationBackupDir));
            return backupZipFileName;

        } catch (IOException ex) {
            _logger.error("Exception,", ex);
            throw ex;
        } finally {
            BRCommons.setBackupRestoreRunning(false);
        }

    }

    private static void copyStaticFiles(String applicationBackupDir) {
        //Copy mycontroller.properties file
        try {
            FileUtils.copyFile(
                    FileUtils.getFile(System.getProperty("mc.conf.file")),
                    FileUtils.getFile(applicationBackupDir + File.separator + BRCommons.APP_PROPERTIES_FILENAME),
                    true);
            if (AppProperties.getInstance().isWebHttpsEnabled()) {
                KEY_STORE_FILE = applicationBackupDir + File.separator
                        + FileUtils.getFile(AppProperties.getInstance().getWebSslKeystoreFile()).getName();
                FileUtils.copyFile(
                        FileUtils.getFile(AppProperties.getInstance().getWebSslKeystoreFile()),
                        FileUtils.getFile(KEY_STORE_FILE),
                        true);
            }
            //Copy resources directory
            FileUtils.copyDirectory(
                    FileUtils.getFile(AppProperties.getInstance().getResourcesLocation()),
                    FileUtils.getFile(applicationBackupDir + File.separator + BRCommons.RESOURCES_LOCATION),
                    true);

        } catch (IOException ex) {
            _logger.error("Static file backup failed!", ex);
        }
    }

}
