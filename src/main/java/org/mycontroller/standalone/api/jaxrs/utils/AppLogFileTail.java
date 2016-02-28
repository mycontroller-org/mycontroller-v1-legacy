package org.mycontroller.standalone.api.jaxrs.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.api.jaxrs.mapper.LogFileJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.experimental.UtilityClass;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@UtilityClass
public class AppLogFileTail {
    static final Logger _logger = LoggerFactory.getLogger(AppLogFileTail.class.getName());
    private static File appLogFile = FileUtils.getFile("../logs/mycontroller.log");
    private final static StringBuilder logBuilder = new StringBuilder();

    public static LogFileJson getUpdate(Long lastKnownPosition, Long lastNPosition) {
        if (lastNPosition != null && appLogFile.length() > lastNPosition) {
            lastKnownPosition = appLogFile.length() - lastNPosition;
        } else if (lastKnownPosition != null && appLogFile.length() <= lastKnownPosition) {
            return LogFileJson.builder().lastKnownPosition(lastKnownPosition).build();
        }
        if (lastKnownPosition == null) {
            lastKnownPosition = 0l;
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
}
