/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.BackupRestore;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.jaxrs.json.BackupFile;
import org.mycontroller.standalone.settings.BackupSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class SystemBackupJob extends Job {
    private static final Logger _logger = LoggerFactory.getLogger(SystemBackupJob.class);
    private static boolean isBackupRunning = false;

    private void removeOldFiles(BackupSettings backupSettings) {
        String[] filter = { "zip" };
        Collection<File> zipFiles = FileUtils.listFiles(
                FileUtils.getFile(McObjectManager.getAppProperties().getBackupSettings().getBackupLocation()),
                filter, true);

        List<BackupFile> backupFiles = new ArrayList<BackupFile>();
        for (File zipFile : zipFiles) {
            if (zipFile.getName().startsWith(backupSettings.getPrefix())) {//Filter with file name
                backupFiles.add(BackupFile.builder()
                        .name(zipFile.getName())
                        .size(zipFile.length())
                        .timestamp(zipFile.lastModified())
                        .absolutePath(zipFile.getAbsolutePath())
                        .build());
            }
        }
        //Do order reverse
        Collections.sort(backupFiles, Collections.reverseOrder());

        if (backupFiles.size() > backupSettings.getRetainMax()) {
            _logger.info("Available backup files:{}, Maximum files retain:{}", backupFiles.size(),
                    backupSettings.getRetainMax());
            for (int deleteIndex = backupSettings.getRetainMax(); deleteIndex < backupFiles.size(); deleteIndex++) {
                try {
                    FileUtils.forceDelete(FileUtils.getFile(backupFiles.get(deleteIndex).getAbsolutePath()));
                    _logger.info("Backup file deleted, {}", backupFiles.get(deleteIndex));
                } catch (Exception ex) {
                    _logger.error("Backup file deletion failed", ex);
                }
            }
        } else {
            _logger.debug("Available backup files:{}", backupFiles.size());
        }
    }

    @Override
    public void doRun() throws JobInterruptException {
        if (isBackupRunning) {
            _logger.warn("A backup already running.");
            return;
        }
        try {
            isBackupRunning = true;
            _logger.debug("Backup job triggered");
            BackupRestore.backup(McObjectManager.getAppProperties().getBackupSettings().getPrefix());
            removeOldFiles(McObjectManager.getAppProperties().getBackupSettings());//Retain max backup
            _logger.debug("Backup job completed");
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        } finally {
            isBackupRunning = false;
        }
    }
}
