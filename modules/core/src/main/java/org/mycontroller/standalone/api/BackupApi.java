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
package org.mycontroller.standalone.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.BackupRestore;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.jaxrs.json.BackupFile;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class BackupApi {
    private static final Logger _logger = LoggerFactory.getLogger(BackupApi.class.getName());

    public List<BackupFile> getBackupList() {
        String[] filter = { "zip" };
        Collection<File> zipFiles = FileUtils.listFiles(
                FileUtils.getFile(McObjectManager.getAppProperties().getBackupSettings().getBackupLocation()),
                filter, true);
        List<BackupFile> backupFiles = new ArrayList<BackupFile>();
        for (File zipFile : zipFiles) {
            if (zipFile.getName().contains(BackupRestore.FILE_NAME_IDENTITY)) {
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
        return backupFiles;
    }

    public void backupNow() {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Backup triggered.");
        }
        BackupRestore.backup("on-demand");
    }

    public void deleteBackup(BackupFile backupFile) throws IOException {
        FileUtils.forceDelete(FileUtils.getFile(backupFile.getAbsolutePath()));
    }

    public void restore(BackupFile backupFile) throws IOException, McBadRequestException {
        if (backupFile != null) {
            _logger.info("Restore triggered.");
            BackupRestore.restore(backupFile);
        } else {
            throw new McBadRequestException("backup file should not be null");
        }
    }
}
