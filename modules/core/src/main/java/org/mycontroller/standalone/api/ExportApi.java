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
import org.mycontroller.standalone.backup.Export;
import org.mycontroller.standalone.backup.Import;
import org.mycontroller.standalone.backup.McFileUtils;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */
@Slf4j
public class ExportApi {

    public QueryResponse getExportFiles(HashMap<String, Object> filters) throws IOException {
        return McFileUtils.getMcFiles(
                AppProperties.getInstance().getExportSettings().getExportLocation(),
                Commons.EXPORT_FILE_NAME_IDENTITY, filters);
    }

    public void deleteExportFiles(List<String> backupFiles) throws IOException {
        McFileUtils.deleteMcFiles(AppProperties.getInstance().getExportSettings().getExportLocation(), backupFiles);
    }

    public void exportNow(String exportFilePrefix, long rowLimit) throws McException, IOException {
        _logger.debug("Export triggered.");
        McThreadPoolFactory.execute(new Export(exportFilePrefix, rowLimit));
    }

    public void exportNow(long rowLimit) throws McException, IOException {
        exportNow("on-demand", rowLimit);
    }

    public void importNow(String fileName) throws IOException, McBadRequestException {
        McFile exportFile = McFileUtils.getMcFile(
                AppProperties.getInstance().getExportSettings().getExportLocation(), fileName);
        McThreadPoolFactory.execute(new Import(exportFile));
        _logger.info("Import triggered.");
    }
}
