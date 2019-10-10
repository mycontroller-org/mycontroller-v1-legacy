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
package org.mycontroller.standalone.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.McThreadPoolFactory;
import org.mycontroller.standalone.api.jaxrs.model.McFile;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.backup.Commons;
import org.mycontroller.standalone.backup.Backup;
import org.mycontroller.standalone.backup.McFileUtils;
import org.mycontroller.standalone.backup.Restore;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McException;

import lombok.extern.slf4j.Slf4j;

/**
 * This class used to call system backup and restore functions.
 * <p>Like list of available backup files, run new backup, to restore a backup
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class BackupApi {
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
        return McFileUtils.getMcFiles(
                AppProperties.getInstance().getBackupSettings().getBackupLocation(),
                Commons.BACKUP_FILE_NAME_IDENTITY, filters);
    }

    /**
     * Call this method to delete list of backup files
     * @param backupFiles List of backup file names with extension
     * @throws IOException throws when problem with backup location
     */
    public void deleteBackupFiles(List<String> backupFiles) throws IOException {
        McFileUtils.deleteMcFiles(AppProperties.getInstance().getBackupSettings().getBackupLocation(), backupFiles);
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
        McFile backupFile = McFileUtils.getMcFile(
                AppProperties.getInstance().getBackupSettings().getBackupLocation(), fileName);
        McThreadPoolFactory.execute(new Restore(backupFile));
        _logger.info("Restore triggered.");
    }
}
