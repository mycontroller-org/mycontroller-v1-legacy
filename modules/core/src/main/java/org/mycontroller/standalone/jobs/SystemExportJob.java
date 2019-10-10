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
package org.mycontroller.standalone.jobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.model.McFile;
import org.mycontroller.standalone.backup.Export;
import org.mycontroller.standalone.settings.ExportSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */
@Slf4j
public class SystemExportJob extends Job {
    private void removeOldFiles(ExportSettings exportSettings) throws IOException {
        String[] filter = { "zip" };
        Collection<File> zipFiles = FileUtils.listFiles(
                FileUtils.getFile(exportSettings.getExportLocation()),
                filter, true);

        List<McFile> backupFiles = new ArrayList<McFile>();
        for (File zipFile : zipFiles) {
            if (zipFile.getName().startsWith(exportSettings.getPrefix())) {//Filter with file name
                backupFiles.add(McFile.builder()
                        .name(zipFile.getName())
                        .size(zipFile.length())
                        .timestamp(zipFile.lastModified())
                        .canonicalPath(zipFile.getCanonicalPath())
                        .build());
            }
        }
        //Do order reverse
        Collections.sort(backupFiles, Collections.reverseOrder());

        if (backupFiles.size() > exportSettings.getRetainMax()) {
            _logger.debug("Available export files:{}, Maximum files retain:{}", backupFiles.size(),
                    exportSettings.getRetainMax());
            for (int deleteIndex = exportSettings.getRetainMax(); deleteIndex < backupFiles.size(); deleteIndex++) {
                try {
                    FileUtils.forceDelete(FileUtils.getFile(backupFiles.get(deleteIndex).getCanonicalPath()));
                    _logger.debug("Export file deleted, {}", backupFiles.get(deleteIndex));
                } catch (Exception ex) {
                    _logger.error("Export file deletion failed", ex);
                }
            }
        } else {
            _logger.debug("Available backup files:{}", backupFiles.size());
        }
    }

    @Override
    public void doRun() throws JobInterruptException {
        try {
            _logger.debug("Export auto job triggered");
            ExportSettings settings = AppProperties.getInstance().getExportSettings();
            Export export = new Export(settings.getPrefix(), settings.getRowLimit());
            export.run();
            removeOldFiles(settings);//Retain max backup
            _logger.debug("Export auto job completed");
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }
}
