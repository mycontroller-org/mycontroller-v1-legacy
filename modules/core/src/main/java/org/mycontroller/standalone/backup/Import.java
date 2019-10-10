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
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.BadRequestException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.StartApp;
import org.mycontroller.standalone.api.jaxrs.model.McFile;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;

import com.fasterxml.jackson.databind.JavaType;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */

@Slf4j
public class Import implements Runnable {

    private McFile sourceFile;

    public Import(McFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    private void startImport() throws IOException {
        if (!sourceFile.getName().contains(Commons.EXPORT_FILE_NAME_IDENTITY)) {
            throw new BadRequestException("export file name should contain '" + Commons.EXPORT_FILE_NAME_IDENTITY
                    + "'. Your input:"
                    + sourceFile.getName());
        }

        _logger.info("About to restore a export, {}", sourceFile);

        String extractedLocation = AppProperties.getInstance().getTmpLocation()
                + sourceFile.getName().replaceAll(".zip", "");

        try {
            //Extract zip file
            _logger.debug("Zip file:{}", sourceFile.getCanonicalPath());

            McFileUtils.extractZipFile(sourceFile.getCanonicalPath(), extractedLocation);
            _logger.debug("All the files extracted to '{}'", extractedLocation);

            //Validate required files
            if (!FileUtils.getFile(extractedLocation + File.separator + Commons.APP_PROPERTIES_FILENAME).exists()) {
                _logger.error("Unable to continue restore opration! selected file not found! File:{}",
                        extractedLocation + File.separator + Commons.APP_PROPERTIES_FILENAME);
                return;
            }

            // import database files
            importFiles(extractedLocation + File.separator + Commons.DATABASE_FILES);

            //Stop all services
            StartApp.stopServices(false);

            // DO NOT RESTORE PROPERTIES FILE AND CERTIFICATES. But it is available on the export zip

            //Restore resources directory
            McFileUtils.restoreResourceFiles(extractedLocation);
            _logger.info("Export completed successfully. Start '{}' server manually", AppProperties.APPLICATION_NAME);
        } finally {
            //clean tmp file
            FileUtils.deleteQuietly(FileUtils.getFile(extractedLocation));
            _logger.debug("Tmp location[{}] clean success", extractedLocation);
        }
        // stop the application
        System.exit(0);
    }

    private void importFiles(String directoryLocation) {
        long startTime = System.currentTimeMillis();
        _logger.info("Import data job started...");
        // get list of files
        List<File> dataFiles = Arrays.asList(
                FileUtils.getFile(directoryLocation).listFiles());
        for (String key : Commons.EXPORT_MAP.keySet()) {
            ExportMap item = Commons.EXPORT_MAP.get(key);
            try {
                loadFile(dataFiles, item);
                _logger.debug("Completed for: {}, {}", key, item.getFileName());

            } catch (Exception ex) {
                _logger.error("Exception,", ex);
            }
        }
        _logger.info("Import data job completed in {} ms", System.currentTimeMillis() - startTime);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void loadFile(List<File> dataFiles, ExportMap item) {
        _logger.info("Processing import for the file: {}*.json", item.getFileName());
        int deletedItemCount = item.getDao().deleteAll();
        _logger.info("Removed existing data from the table. Deleted count: {}", deletedItemCount);
        for (File file : dataFiles) {
            if (file.getName().startsWith(item.getFileName())) {
                Object data = JsonUtils.loads(getType(List.class, item.getClazz()), file);
                int createdCount = item.getDao().createBulk((List) data);
                _logger.info("Insertion data completed for the file: {}, records count: {}",
                        file.getName(), createdCount);
            }
        }
    }

    private JavaType getType(@SuppressWarnings("rawtypes") Class<? extends List> collectionClazz, Class<?> clazz) {
        JavaType clazzType = RestUtils.getObjectMapper().getTypeFactory().constructType(clazz);
        return RestUtils.getObjectMapper().getTypeFactory().constructCollectionType(collectionClazz, clazzType);
    }

    @Override
    public void run() {
        if (Commons.IS_IMPORT_EXPORT_RUNNING.get()) {
            _logger.warn("Another instance of import or export job is in progress.");
            return;
        }
        Commons.IS_IMPORT_EXPORT_RUNNING.set(true);
        try {
            this.startImport();
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
        } finally {
            Commons.IS_IMPORT_EXPORT_RUNNING.set(false);
            StartApp.stopServices();
            System.exit(0);
        }
    }
}
