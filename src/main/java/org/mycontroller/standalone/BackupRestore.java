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
package org.mycontroller.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.BadRequestException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.api.jaxrs.mapper.BackupFile;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class BackupRestore {
    private static final Logger _logger = LoggerFactory.getLogger(BackupRestore.class.getName());
    private static final String DATABASE_FILENAME = "database_backup.zip";
    private static final String APP_PROPERTIES_FILENAME = "mycontroller.properties";
    private static final String APP_CONF_LOCATION = "../conf/";
    private static String KEY_STORE_FILE = null;

    private static boolean isbackupRestoreRunning = false;

    public static synchronized void backup() {
        //backup database
        //backup configuration file
        //backup certificates
        //backup logback xml file

        if (isbackupRestoreRunning) {
            throw new BadRequestException("A backup or restore is running");
        }

        isbackupRestoreRunning = true;
        String applicationBackupDir = ObjectFactory.getAppProperties().getBackupLocation() + "mycontroller_backup-"
                + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
        //Create parent dir if not exist
        try {
            FileUtils.forceMkdir(FileUtils.getFile(applicationBackupDir));
            String databaseBackup = ObjectFactory.getAppProperties().getTmpLocation() + DATABASE_FILENAME;
            if (DataBaseUtils.backupDatabase(databaseBackup)) {
                //Copy database file
                FileUtils.moveFile(
                        FileUtils.getFile(databaseBackup),
                        FileUtils.getFile(applicationBackupDir + "/" + DATABASE_FILENAME));
                //copy static files
                copyStaticFiles(applicationBackupDir);
                _logger.debug("Copied all the files");
                final ZipOutputStream outZip = new ZipOutputStream(
                        new FileOutputStream(applicationBackupDir + ".zip"));
                //add database file
                addToZipFile(applicationBackupDir + "/" + DATABASE_FILENAME, outZip);
                //add properties file
                addToZipFile(applicationBackupDir + "/" + APP_PROPERTIES_FILENAME, outZip);
                //add keystore file, if https enabled
                if (ObjectFactory.getAppProperties().isWebHttpsEnabled() && KEY_STORE_FILE != null) {
                    addToZipFile(KEY_STORE_FILE, outZip);
                }
                //compress all the files
                outZip.close();
                _logger.debug("zip file creation done");
                //clean temporary files
                FileUtils.deleteDirectory(FileUtils.getFile(applicationBackupDir));
            } else {
                //Throw exception
            }
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
        } finally {
            isbackupRestoreRunning = false;
        }

    }

    public static void restore(BackupFile backupFile) throws IOException {
        if (isbackupRestoreRunning) {
            throw new BadRequestException("A backup or restore is running");
        }

        isbackupRestoreRunning = true;

        String extractedLocation = ObjectFactory.getAppProperties().getTmpLocation()
                + backupFile.getName().replaceAll(".zip", "");
        try {

            String oldDatabaseLocation = ObjectFactory.getAppProperties().getDbH2DbLocation();
            //Extract zip file
            _logger.debug("Zip file:{}", backupFile.getAbsolutePath());

            extractZipFile(backupFile.getAbsolutePath(), extractedLocation);
            _logger.debug("All the files extracted to '{}'", extractedLocation);

            //Validate required files
            if (!FileUtils.getFile(extractedLocation + "/" + DATABASE_FILENAME).exists()) {
                _logger.error("Unable to continue restore opration! selected file not found! File:{}",
                        extractedLocation + "/" + DATABASE_FILENAME);
                return;
            }

            //Stop all services
            StartApp.stopServices();

            //Remove old properties file
            FileUtils.deleteQuietly(FileUtils.getFile(APP_CONF_LOCATION + APP_PROPERTIES_FILENAME));

            //Restore properties file
            FileUtils.moveFile(
                    FileUtils.getFile(extractedLocation + "/" + APP_PROPERTIES_FILENAME),
                    FileUtils.getFile(APP_CONF_LOCATION + APP_PROPERTIES_FILENAME));
            //Load initial properties
            StartApp.loadInitialProperties();

            //Remove old files
            FileUtils.deleteQuietly(FileUtils.getFile(ObjectFactory.getAppProperties().getWebSslKeystoreFile()));

            if (ObjectFactory.getAppProperties().isWebHttpsEnabled()) {
                //restore key store file
                FileUtils.moveFile(
                        FileUtils.getFile(extractedLocation + "/" + FileUtils.getFile(
                                ObjectFactory.getAppProperties().getWebSslKeystoreFile()).getName()),
                        FileUtils.getFile(ObjectFactory.getAppProperties().getWebSslKeystoreFile()));
            }

            //remove old database
            if (FileUtils.deleteQuietly(FileUtils.getFile(oldDatabaseLocation + ".h2.db"))) {
                _logger.debug("Old database removed successfully");
            } else {
                _logger.warn("Unable to remove old database");
            }
            //restore database
            DataBaseUtils.restoreDatabase(extractedLocation + "/" + DATABASE_FILENAME);

            _logger.info("Resore files completed successfully. Start application manually");
        } finally {
            //clean tmp file
            FileUtils.deleteQuietly(FileUtils.getFile(extractedLocation));
            _logger.debug("Tmp location[{}] clean success", extractedLocation);
            isbackupRestoreRunning = false;
        }
        //Stop application
        System.exit(0);
    }

    private static void copyStaticFiles(String applicationBackupDir) {
        //Copy mycontroller.properties file
        try {
            FileUtils.copyFile(
                    FileUtils.getFile(System.getProperty("mc.conf.file")),
                    FileUtils.getFile(applicationBackupDir + "/" + APP_PROPERTIES_FILENAME));
            if (ObjectFactory.getAppProperties().isWebHttpsEnabled()) {
                KEY_STORE_FILE = applicationBackupDir + "/"
                        + FileUtils.getFile(ObjectFactory.getAppProperties().getWebSslKeystoreFile()).getName();
                FileUtils.copyFile(
                        FileUtils.getFile(ObjectFactory.getAppProperties().getWebSslKeystoreFile()),
                        FileUtils.getFile(KEY_STORE_FILE));
            }

        } catch (IOException ex) {
            _logger.error("Static file backup failed!", ex);
        }
    }

    private static void addToZipFile(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {
        _logger.debug("Writing '{}' to zip file", fileName);
        File file = FileUtils.getFile(fileName);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(FileUtils.getFile(fileName).getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    private static void extractZipFile(String zipFileName, String destination)
            throws FileNotFoundException, IOException {
        ZipFile zipFile = new ZipFile(zipFileName);
        Enumeration<?> enu = zipFile.entries();
        //create destination if not exists
        FileUtils.forceMkdir(FileUtils.getFile(destination));
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();
            String name = zipEntry.getName();
            long size = zipEntry.getSize();
            long compressedSize = zipEntry.getCompressedSize();
            _logger.debug("name:{} | size:{} | compressed size:{}", name, size, compressedSize);
            File file = FileUtils.getFile(destination + "/" + name);
            //Create destination if it's not available
            if (name.endsWith("/")) {
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();
        }
        zipFile.close();
    }
}
