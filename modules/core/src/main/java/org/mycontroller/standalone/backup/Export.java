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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.SystemApi;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */

@Slf4j
public class Export implements Runnable {

    private static final long ROW_LIMIT = 1000;

    private long rowLimit;
    private String prefix;

    public Export(String prefix) {
        this.prefix = prefix;
        rowLimit = ROW_LIMIT;
    }

    public Export(String prefix, Long rowLimit) {
        this.prefix = prefix;
        if (rowLimit == null) {
            this.rowLimit = ROW_LIMIT;
        } else {
            this.rowLimit = rowLimit;
        }
    }

    private String startExport() throws IOException {
        String preZipDirectory = AppProperties.getInstance().getExportSettings().getExportLocation()
                + prefix + Commons.EXPORT_FILE_NAME_IDENTITY + "_"
                + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
        //Create parent dir if not exist
        FileUtils.deleteQuietly(FileUtils.getFile(preZipDirectory));
        FileUtils.forceMkdir(FileUtils.getFile(preZipDirectory));

        // copy version details
        copyVersionDetails(preZipDirectory);

        // copy database data as json
        copyDatabaseData(preZipDirectory, rowLimit);

        //copy static files
        return McFileUtils.copyStaticFilesAndCreateZipFile(preZipDirectory);
    }

    private void jsonDump(Object data, String preZipDirectory, String fileName) {
        JsonUtils.dumps(data, preZipDirectory, Commons.DATABASE_FILES, fileName);
    }

    private void copyDatabaseData(String preZipDirectory, long rowLimit) {
        try {
            for (String key : Commons.EXPORT_MAP.keySet()) {
                ExportMap item = Commons.EXPORT_MAP.get(key);
                try {
                    // get total count
                    long total = item.getDao().countOf();
                    long startRow = 0;
                    int iteration = 1;
                    if (total > rowLimit) {
                        do {
                            String fileName = String.format("%s_%d_%d.json", item.getFileName(), rowLimit, iteration);
                            //dump(item.getDao().getAll(item.getOrderBy(), startRow, rowLimit), fileName);
                            jsonDump(item.getDao().getAll(null, startRow, rowLimit), preZipDirectory, fileName);
                            startRow = startRow + rowLimit;
                            iteration++;
                        } while (startRow < total);
                    } else {
                        jsonDump(item.getDao().getAll(), preZipDirectory, item.getFileName() + ".json");
                    }
                    _logger.debug("Completed for: {}, {}", key, item.getFileName());
                } catch (Exception ex) {
                    _logger.error("Exception, item:{}", item, ex);
                }
            }
        } catch (Exception exM) {
            _logger.error("Exception,", exM);
        }
    }

    private void copyVersionDetails(String preZipDirectory) {
        JsonUtils.dumps(new SystemApi().getAbout(), preZipDirectory, "version.json");
    }

    @Override
    public void run() {
        if (Commons.IS_IMPORT_EXPORT_RUNNING.get()) {
            _logger.warn("Another instance of import or export job is in progress.");
            return;
        }
        Commons.IS_IMPORT_EXPORT_RUNNING.set(true);
        try {
            long startTime = System.currentTimeMillis();
            String fileName = this.startExport();
            _logger.debug("Export job completed in {}ms, filename: {}",
                    System.currentTimeMillis() - startTime, fileName);
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        } finally {
            Commons.IS_IMPORT_EXPORT_RUNNING.set(false);
        }
    }

}
