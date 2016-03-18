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
package org.mycontroller.standalone.api.jaxrs.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.MycUtils;
import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.api.jaxrs.mapper.LogFileJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.experimental.UtilityClass;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@UtilityClass
public class McServerLogFile {
    static final Logger _logger = LoggerFactory.getLogger(McServerLogFile.class.getName());
    private static final String LOG_FILE_LOCATION = "../logs/";
    private static File appLogFile = FileUtils.getFile("../logs/mycontroller.log");
    private final static StringBuilder logBuilder = new StringBuilder();
    private static final long MAX_POSITION_LIMIT = 100000;

    public static LogFileJson getLogUpdate(Long lastKnownPosition, Long lastNPosition) {
        if (lastNPosition != null && appLogFile.length() > lastNPosition) {
            lastKnownPosition = appLogFile.length() - lastNPosition;
        } else if (lastKnownPosition != null && appLogFile.length() <= lastKnownPosition) {
            return LogFileJson.builder().lastKnownPosition(lastKnownPosition).build();
        }
        if (lastKnownPosition == null) {
            lastKnownPosition = 0l;
        }

        //Set maximum limit
        if ((appLogFile.length() - lastKnownPosition) > MAX_POSITION_LIMIT) {
            lastKnownPosition = appLogFile.length() - MAX_POSITION_LIMIT;
        }

        logBuilder.setLength(0);
        // Reading and writing file
        RandomAccessFile readFileAccess = null;
        try {
            readFileAccess = new RandomAccessFile(appLogFile, "r");
            readFileAccess.seek(lastKnownPosition);
            String log = null;
            while ((log = readFileAccess.readLine()) != null) {
                logBuilder.append(log).append("\n");
            }
            lastKnownPosition = readFileAccess.getFilePointer();
        } catch (FileNotFoundException ex) {
            _logger.error("Error,", ex);
        } catch (IOException ex) {
            _logger.error("Error,", ex);
        } finally {
            if (readFileAccess != null) {
                try {
                    readFileAccess.close();
                } catch (IOException ex) {
                    _logger.error("Error,", ex);
                }
            }
        }
        return LogFileJson.builder().lastKnownPosition(lastKnownPosition).data(logBuilder.toString()).build();
    }

    public static String getLogsZipFile() throws IOException {
        String fileName = ObjectManager.getAppProperties().getTmpLocation() + "mc-logs-"
                + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + ".zip";
        File[] files = FileUtils.getFile(LOG_FILE_LOCATION).listFiles();
        final ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(fileName));
        for (File file : files) {
            MycUtils.addToZipFile(file.getAbsolutePath(), outZip);
        }
        //compress all the files
        outZip.close();
        _logger.debug("zip file creation done for logs");
        return fileName;
    }
}
