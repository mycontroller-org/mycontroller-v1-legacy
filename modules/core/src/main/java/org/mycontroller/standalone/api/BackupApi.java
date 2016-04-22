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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.api.jaxrs.json.BackupFile;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
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
    public static final String KEY_NAME = "name";
    public static final String[] BACKUP_FILE_SUFFIX_FILTER = { "zip", "ZIP" };

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
                        .canonicalPath(zipFile.getAbsolutePath())
                        .build());
            }

        }
        //Do order reverse
        Collections.sort(backupFiles, Collections.reverseOrder());
        return backupFiles;
    }

    public QueryResponse getBackupFiles(HashMap<String, Object> filters) throws IOException {
        Query query = Query.get(filters);

        String locationCanonicalPath = McUtils.getDirectoryLocation(FileUtils.getFile(
                AppProperties.getInstance().getBackupSettings().getBackupLocation()).getCanonicalPath());

        if (FileUtils.getFile(locationCanonicalPath).exists()) {
            List<BackupFile> files = new ArrayList<BackupFile>();

            //Filters
            //Extension filter
            SuffixFileFilter extensionFilter = new SuffixFileFilter(BACKUP_FILE_SUFFIX_FILTER, IOCase.INSENSITIVE);

            //name filter
            IOFileFilter nameFileFilter = null;
            @SuppressWarnings("unchecked")
            List<String> fileNames = (List<String>) query.getFilters().get(KEY_NAME);
            if (fileNames != null && !fileNames.isEmpty()) {
                for (String fileName : fileNames) {
                    if (nameFileFilter == null) {
                        nameFileFilter = FileFilterUtils.and(
                                new WildcardFileFilter("*" + fileName + "*", IOCase.INSENSITIVE));
                    } else {
                        nameFileFilter = FileFilterUtils.and(nameFileFilter,
                                new WildcardFileFilter("*" + fileName + "*", IOCase.INSENSITIVE));
                    }
                }
            }
            //Combine all filters
            IOFileFilter finalFileFilter = null;
            if (nameFileFilter != null) {
                finalFileFilter = FileFilterUtils.and(extensionFilter, nameFileFilter);
            } else {
                finalFileFilter = extensionFilter;
            }
            List<File> backupFiles = new ArrayList<File>(FileUtils.listFiles(FileUtils.getFile(locationCanonicalPath),
                    finalFileFilter, TrueFileFilter.INSTANCE));
            query.setFilteredCount((long) backupFiles.size());
            //Get total items without filter
            query.setTotalItems((long) FileUtils.listFiles(FileUtils.getFile(locationCanonicalPath),
                    new SuffixFileFilter(BACKUP_FILE_SUFFIX_FILTER, IOCase.INSENSITIVE),
                    TrueFileFilter.INSTANCE).size());
            int fileFrom;
            int fileTo;
            if (query.getPageLimit() == -1) {
                fileTo = backupFiles.size();
                fileFrom = 0;
            } else {
                fileFrom = query.getStartingRow().intValue();
                fileTo = (int) (query.getPage() * query.getPageLimit());
            }
            for (File backupFile : backupFiles) {
                String name = backupFile.getCanonicalPath().replace(locationCanonicalPath, "");
                files.add(BackupFile.builder()
                        .name(name)
                        .size(backupFile.length())
                        .timestamp(backupFile.lastModified())
                        .canonicalPath(backupFile.getCanonicalPath())
                        .build());
            }

            if (!files.isEmpty()) {
                //Do order reverse
                Collections.sort(files, Collections.reverseOrder());
                if (fileFrom < files.size()) {
                    files = files.subList(Math.max(0, fileFrom), Math.min(fileTo, files.size()));
                }
            }
            return QueryResponse.builder().data(files).query(query).build();

        } else {
            throw new FileNotFoundException("File location not found: " + locationCanonicalPath);
        }
    }

    public void deleteBackupFiles(List<String> backupFiles) throws IOException {
        String backupFileLocation = McUtils.getDirectoryLocation(FileUtils.getFile(
                AppProperties.getInstance().getBackupSettings().getBackupLocation()).getCanonicalPath());
        for (String backupFile : backupFiles) {
            String fileFullPath = backupFileLocation + backupFile;
            if (McUtils.isInScope(backupFileLocation, fileFullPath)) {
                if (FileUtils.deleteQuietly(FileUtils.getFile(fileFullPath))) {
                    _logger.debug("File deleted successfully! {}", fileFullPath);
                } else {
                    _logger.warn("File deletion failed! {}", fileFullPath);
                }
            } else {
                _logger.warn("Trying to delete file from outside scope! Filepath:{}, CanonicalPath:{}",
                        fileFullPath, FileUtils.getFile(fileFullPath).getCanonicalPath());
            }
        }
    }

    public String backupNow(String backupFilePrefix) throws McException, IOException {
        _logger.debug("Backup triggered.");
        return Backup.backup(backupFilePrefix);
    }

    public String backupNow() throws McException, IOException {
        return backupNow("on-demand");
    }

    public void restore(String fileName) throws IOException, McBadRequestException {
        if (fileName == null) {
            throw new McBadRequestException("backup file should not be null");
        }
        String backupCanonicalPath = FileUtils.getFile(
                AppProperties.getInstance().getBackupSettings().getBackupLocation()).getCanonicalPath();
        String fileFullName = AppProperties.getInstance().getBackupSettings().getBackupLocation() + fileName;
        if (McUtils.isInScope(backupCanonicalPath, fileFullName)) {
            File bkpFile = FileUtils.getFile(fileFullName);
            BackupFile backupFile = BackupFile.builder()
                    .name(bkpFile.getName())
                    .canonicalPath(bkpFile.getCanonicalPath())
                    .timestamp(bkpFile.lastModified())
                    .size(bkpFile.length())
                    .build();
            new Thread(new Restore(backupFile)).start();
            _logger.info("Restore triggered.");
        } else {
            throw new McBadRequestException("Trying to restore file from outside backup scope");
        }
    }
}
