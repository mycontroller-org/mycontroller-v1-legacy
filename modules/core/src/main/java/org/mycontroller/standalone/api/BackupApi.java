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
package org.mycontroller.standalone.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import org.mycontroller.standalone.api.jaxrs.model.BackupFile;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.backup.BRCommons;
import org.mycontroller.standalone.backup.Backup;
import org.mycontroller.standalone.backup.Restore;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McException;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * This class used to call system backup and restore functions.
 * <p>Like list of available backup files, run new backup, to restore a backup
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class BackupApi {
    public static final String KEY_NAME = "name";
    public static final String[] BACKUP_FILE_SUFFIX_FILTER = { "zip", "ZIP" };

    /**
     * Call this method to get list of available backup files.
     * <p><b>Filter(s):</b>
     * <p>name - {@link List} of backup file names
     * <p><b>Page filter(s):</b>
     * <p>pageLimit - Set number of items per page
     * <p>page - Request page number
     * <p>order - Set order. <b>Option:</b> asc, desc
     * <p>orderBy - column name for order. <b>Option:</b> name
     * @param filters Supports various filter options.
     * @return QueryResponse Contains input filter and response
     * @throws IOException throws when problem with backup location
     */
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
            if (fileNames == null) {
                fileNames = new ArrayList<String>();
            }
            fileNames.add(BRCommons.FILE_NAME_IDENTITY);

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
                    extensionFilter, TrueFileFilter.INSTANCE).size());
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

    /**
     * Call this method to delete list of backup files
     * @param backupFiles List of backup file names with extension
     * @throws IOException throws when problem with backup location
     */
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

    /**
     * Call this method to run immediate system backup
     * @param backupFilePrefix backup file prefix name
     * @return backed up file name
     * @throws IOException throws when problem with backup location
     * @throws McException internal issue
     */
    public String backupNow(String backupFilePrefix) throws McException, IOException {
        _logger.debug("Backup triggered.");
        return Backup.backup(backupFilePrefix);
    }

    /**
     * Call this method to run immediate system backup
     * <p> calls {@link #backupNow(String)} with 'on-demand' string
     * @return backed up file name
     * @throws IOException throws when problem with backup location
     * @throws McException internal issue
     */
    public String backupNow() throws McException, IOException {
        return backupNow("on-demand");
    }

    /**
     * Call this method to restore a backup file
     * @param fileName backup file name to restore
     * <p><b>Warning:</b> After successful restore you have to start the server manually
     * @throws IOException throws when problem with backup location
     * @throws McBadRequestException given file name not available
     */
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
