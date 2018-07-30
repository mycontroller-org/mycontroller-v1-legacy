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
package org.mycontroller.standalone.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.BadRequestException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SizeFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.model.ImageFile;
import org.mycontroller.standalone.api.jaxrs.model.LogFile;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@UtilityClass
@Slf4j
public class McServerFileUtils {
    private static String LOGS_DIRECTORY_LOCATION = null;
    private static File appLogFile = null;
    private static final StringBuilder logBuilder = new StringBuilder();
    private static final long MAX_POSITION_LIMIT = 100000;

    //Image file filters
    private static final String[] IMAGE_DISPLAY_SUFFIX_FILTER = { "jpg", "jpeg", "png", "gif" };
    //1 MB limit max file size allowed.
    //If we allow more than this, should increase heap space of VM.
    private static final long IMAGE_DISPLAY_WIDGET_FILE_SIZE_LIMIT = McUtils.MB * 7;
    private static final long MAX_FILES_LIMIT = 500;

    public static void updateApplicationLogLocation() {
        String applicationLogFile = null;
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                Object enumElement = index.next();
                if (enumElement instanceof FileAppender) {
                    FileAppender<?> fileAppender = (FileAppender<?>) enumElement;
                    File clientLogFile = new File(fileAppender.getFile());
                    try {
                        applicationLogFile = clientLogFile.getCanonicalPath();
                    } catch (Exception ex) {
                        _logger.error("Unable to get log file path,", ex);
                    }
                    break;
                }
            }
        }

        if (applicationLogFile == null) {
            applicationLogFile = "../logs/mycontroller.log";
        }
        // update log file details
        appLogFile = FileUtils.getFile(applicationLogFile);
        LOGS_DIRECTORY_LOCATION = appLogFile.getParent();
        _logger.debug("Application log file location: {}", applicationLogFile);
    }

    public static LogFile getLogUpdate(Long lastKnownPosition, Long lastNPosition) {
        if (lastNPosition != null && appLogFile.length() > lastNPosition) {
            lastKnownPosition = appLogFile.length() - lastNPosition;
        } else if (lastKnownPosition != null && appLogFile.length() <= lastKnownPosition) {
            return LogFile.builder().lastKnownPosition(lastKnownPosition).build();
        }
        if (lastKnownPosition == null) {
            lastKnownPosition = 0L;
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
        return LogFile.builder().lastKnownPosition(lastKnownPosition).data(logBuilder.toString()).build();
    }

    public static String getLogsZipFile() throws IOException {
        String zipFileName = AppProperties.getInstance().getTmpLocation() + "mc-logs-"
                + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + ".zip";
        McUtils.createZipFile(LOGS_DIRECTORY_LOCATION, zipFileName);
        _logger.debug("zip file creation done for logs");
        return zipFileName;
    }

    public static List<String> getImageFilesList() throws IOException {
        String filesLocation = AppProperties.getInstance().getControllerSettings()
                .getWidgetImageFilesLocation();
        String locationCanonicalPath = FileUtils.getFile(filesLocation).getCanonicalPath();
        if (!locationCanonicalPath.endsWith(File.separator)) {
            locationCanonicalPath += File.separator;
        }
        if (FileUtils.getFile(filesLocation).exists()) {
            List<String> files = new ArrayList<String>();
            IOFileFilter ioFileFilter =
                    FileFilterUtils.and(new SuffixFileFilter(IMAGE_DISPLAY_SUFFIX_FILTER, IOCase.INSENSITIVE),
                            new SizeFileFilter(IMAGE_DISPLAY_WIDGET_FILE_SIZE_LIMIT, false));
            Collection<File> imageFiles = FileUtils.listFiles(FileUtils.getFile(filesLocation), ioFileFilter,
                    TrueFileFilter.INSTANCE);
            for (File imageFile : imageFiles) {
                files.add(imageFile.getCanonicalPath().replace(locationCanonicalPath, ""));
                if (files.size() >= MAX_FILES_LIMIT) {
                    break;
                }
            }
            return files;
        } else {
            throw new FileNotFoundException("File location not found: " + locationCanonicalPath);
        }
    }

    public static synchronized ImageFile getImageFile(String imageFileName)
            throws IOException, IllegalAccessException {
        String filesLocation = AppProperties.getInstance().getControllerSettings()
                .getWidgetImageFilesLocation();
        if (!getImageFilesList().contains(imageFileName)) {
            throw new IllegalAccessException(
                    "You do not have access (or) file not found (or) "
                            + "file size exceeded the allowed limit of 7 MB. File name: '"
                            + imageFileName + "'");
        }
        if (FileUtils.getFile(filesLocation).exists()) {
            File imageFile = FileUtils.getFile(filesLocation + imageFileName);
            if (imageFile.exists()) {
                if (imageFile.length() > IMAGE_DISPLAY_WIDGET_FILE_SIZE_LIMIT) {
                    throw new BadRequestException("File size exceeded the allowed limit of 7 MB, actual size: " +
                            imageFile.length() / McUtils.MB + " MB");
                }
                return ImageFile
                        .builder()
                        .size(imageFile.length())
                        .timestamp(imageFile.lastModified())
                        .name(imageFileName)
                        .canonicalPath(imageFile.getCanonicalPath())
                        .extension(FilenameUtils.getExtension(imageFileName).toLowerCase())
                        .data(FileUtils.readFileToByteArray(imageFile)).build();
            } else {
                throw new FileNotFoundException("File not found: " + imageFileName);
            }
        } else {
            throw new FileNotFoundException("File location not found: " + filesLocation);
        }
    }
}
