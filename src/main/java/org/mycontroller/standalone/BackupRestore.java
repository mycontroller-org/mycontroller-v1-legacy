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

import org.apache.commons.io.FileUtils;
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
    private static String KEY_STORE_FILE = null;

    public static synchronized void backup() {
        //backup database
        //backup configuration file
        //backup certificates
        //backup logback xml file

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
                //Restore test
                restore(applicationBackupDir + ".zip");
            } else {
                //Throw exception
            }
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
        }

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

    private static void restore(String zipFileName) throws IOException {
        FileUtils.getFile(zipFileName).getName();
        _logger.debug("Zip file name:{}", FileUtils.getFile(zipFileName).getName());
        String extractedLocation = ObjectFactory.getAppProperties().getTmpLocation()
                + FileUtils.getFile(zipFileName).getName().replaceAll(".zip", "");
        extractZipFile(zipFileName, extractedLocation);
        _logger.debug("All the files extracted to '{}'", extractedLocation);
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
