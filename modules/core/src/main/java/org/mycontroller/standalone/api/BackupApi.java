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
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.json.BackupFile;
import org.mycontroller.standalone.backup.BRCommons;
import org.mycontroller.standalone.backup.Backup;
import org.mycontroller.standalone.backup.Restore;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class BackupApi {

    public List<BackupFile> getBackupList() {
        String[] filter = { "zip" };
        Collection<File> zipFiles = FileUtils.listFiles(
                FileUtils.getFile(AppProperties.getInstance().getBackupSettings().getBackupLocation()),
                filter, true);
        List<BackupFile> backupFiles = new ArrayList<BackupFile>();
        for (File zipFile : zipFiles) {
            if (zipFile.getName().contains(BRCommons.FILE_NAME_IDENTITY)) {
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

    public String backupNow(String backupFilePrefix) throws McException, IOException {
        _logger.debug("Backup triggered.");
        return Backup.backup(backupFilePrefix);
    }

    public String backupNow() throws McException, IOException {
        return backupNow("on-demand");
    }

    public void deleteBackup(BackupFile backupFile) throws IOException {
        FileUtils.forceDelete(FileUtils.getFile(backupFile.getAbsolutePath()));
    }

    public void restore(BackupFile backupFile) throws IOException, McBadRequestException {
        if (backupFile != null) {
            new Thread(new Restore(backupFile)).start();
            _logger.info("Restore triggered.");
        } else {
            throw new McBadRequestException("backup file should not be null");
        }
    }
}
