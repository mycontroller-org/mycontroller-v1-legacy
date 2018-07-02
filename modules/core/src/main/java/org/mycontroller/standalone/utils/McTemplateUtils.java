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
package org.mycontroller.standalone.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.TemplatesHandler;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.model.McTemplate;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;
import org.mycontroller.standalone.scripts.McScriptEngineUtils;
import org.mycontroller.standalone.scripts.McScriptEngineUtils.SCRIPT_TYPE;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class McTemplateUtils {

    //mc script file filter
    private static final String[] MC_SCRIPT_SUFFIX_FILTER = { "html" };

    @SuppressWarnings("unchecked")
    public static QueryResponse get(Query query) throws IOException {
        //Check less info available and true, if true set less info return
        Boolean lessInfo = (Boolean) query.getFilters().get(TemplatesHandler.KEY_LESS_INFO);
        if (lessInfo) {
            query.setPageLimit(-1);
        }

        String locationCanonicalPath = McUtils.getDirectoryLocation(
                FileUtils.getFile(AppProperties.getInstance().getTemplatesLocation()).getCanonicalPath());

        if (FileUtils.getFile(locationCanonicalPath).exists()) {
            List<McTemplate> files = new ArrayList<McTemplate>();
            List<String> filesString = new ArrayList<String>();

            //Filters
            //Extension filter
            String[] extensionSuffixFilter = null;
            if (query.getFilters().get(TemplatesHandler.KEY_EXTENSION) != null) {
                if (Arrays.asList(MC_SCRIPT_SUFFIX_FILTER).contains(
                        query.getFilters().get(TemplatesHandler.KEY_EXTENSION))) {
                    extensionSuffixFilter = new String[] {
                            (String) query.getFilters().get(TemplatesHandler.KEY_EXTENSION) };
                }
            }

            if (extensionSuffixFilter == null) {
                extensionSuffixFilter = MC_SCRIPT_SUFFIX_FILTER;
            }

            SuffixFileFilter extensionFilter = new SuffixFileFilter(extensionSuffixFilter, IOCase.INSENSITIVE);

            //name filter
            IOFileFilter nameFileFilter = null;
            List<String> fileNames = (List<String>) query.getFilters().get(TemplatesHandler.KEY_NAME);
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
            IOFileFilter templatesFileFilter = null;
            if (nameFileFilter != null) {
                templatesFileFilter = FileFilterUtils.and(extensionFilter, nameFileFilter);
            } else {
                templatesFileFilter = extensionFilter;
            }
            List<File> templateFiles = new ArrayList<File>(FileUtils.listFiles(
                    FileUtils.getFile(locationCanonicalPath),
                    templatesFileFilter, TrueFileFilter.INSTANCE));
            query.setFilteredCount((long) templateFiles.size());
            //Get total items without filter
            query.setTotalItems((long) FileUtils.listFiles(FileUtils.getFile(locationCanonicalPath),
                    new SuffixFileFilter(MC_SCRIPT_SUFFIX_FILTER, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE).size());

            int fileFrom;
            int fileTo;
            if (query.getPageLimit() == -1) {
                fileTo = templateFiles.size();
                fileFrom = 0;
            } else {
                fileFrom = query.getStartingRow().intValue();
                fileTo = (int) (query.getPage() * query.getPageLimit());
            }
            for (; fileFrom < fileTo; fileFrom++) {
                if (templateFiles.size() > fileFrom) {
                    File templateFile = templateFiles.get(fileFrom);
                    String name = templateFile.getCanonicalPath().replace(locationCanonicalPath, "");
                    if (lessInfo) {
                        filesString.add(name);
                    } else {
                        files.add(McTemplate.builder()
                                .name(name)
                                .extension(FilenameUtils.getExtension(templateFile.getCanonicalPath()))
                                .size(templateFile.length())
                                .lastModified(templateFile.lastModified())
                                .build());
                    }
                } else {
                    break;
                }
            }
            if (lessInfo) {
                return QueryResponse.builder().data(filesString).query(query).build();
            } else {
                return QueryResponse.builder().data(files).query(query).build();
            }

        } else {
            throw new FileNotFoundException("File location not found: " + locationCanonicalPath);
        }
    }

    public static void delete(List<String> templateFiles) throws IOException {
        String templatesLocation = McUtils.getDirectoryLocation(FileUtils.getFile(
                AppProperties.getInstance().getTemplatesLocation()).getCanonicalPath());
        for (String templateFile : templateFiles) {
            String fileFullPath = templatesLocation + templateFile;
            if (McUtils.isInScope(templatesLocation, fileFullPath)) {
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

    public static McTemplate get(String templateName) throws IOException, IllegalAccessException,
            McBadRequestException {
        String templatesLocation = McUtils.getDirectoryLocation(FileUtils.getFile(
                AppProperties.getInstance().getTemplatesLocation()).getCanonicalPath());
        String fileFullPath = templatesLocation + templateName;
        if (McUtils.isInScope(templatesLocation, fileFullPath)) {
            if (!FileUtils.getFile(fileFullPath).exists()) {
                throw new McBadRequestException("File not found! " + fileFullPath);
            }
            File fileScript = FileUtils.getFile(fileFullPath);

            McTemplate mcTemplate = McTemplate.builder()
                    .extension(FilenameUtils.getExtension(fileScript.getCanonicalPath()))
                    .size(fileScript.length())
                    .lastModified(fileScript.lastModified())
                    .data(FileUtils.readFileToString(fileScript, StandardCharsets.UTF_8))
                    .canonicalPath(fileScript.getCanonicalPath())
                    .name(FilenameUtils.getBaseName(fileScript.getCanonicalPath().replace(templatesLocation, "")))
                    .build();
            return mcTemplate;
        } else {
            _logger.warn("Trying to get file from outside scope! Filepath:{}, CanonicalPath:{}",
                    fileFullPath,
                    FileUtils.getFile(fileFullPath).getCanonicalPath());
            throw new IllegalAccessException("Trying to get file from outside scope!");
        }
    }

    public static String execute(String templateName, String scriptName, HashMap<String, Object> bindings)
            throws Exception {
        if (scriptName == null || scriptName.length() == 0) {
            McTemplate mcTemplate = McTemplateUtils.get(templateName);
            return mcTemplate.getData();
        }
        HashMap<String, Object> bindingsFinal = McScriptFileUtils.executeScript(scriptName, bindings);
        bindingsFinal.put(McScriptEngineUtils.MC_SCRIPT_NAME, scriptName);
        return execute(templateName, bindingsFinal);
    }

    public static String execute(String templateName, HashMap<String, Object> bindings) throws Exception {
        McTemplate mcTemplate = McTemplateUtils.get(templateName);
        if (mcTemplate == null) {
            throw new McBadRequestException("Template[" + templateName + "] not available!");
        }
        McScript mcTemplateScript = null;
        String templateResult = null;
        //Map and execute script result with template
        try {
            mcTemplateScript = McScript.builder()
                    .canonicalPath(mcTemplate.getCanonicalPath())
                    .type(SCRIPT_TYPE.OPERATION)
                    .engineName(McScriptEngineUtils.MC_TEMPLATE_ENGINE)
                    .bindings(bindings)
                    .build();
            McScriptEngine templateEngine = new McScriptEngine(mcTemplateScript);
            templateResult = (String) templateEngine.executeScript();
        } catch (Exception ex) {
            if (ex.getMessage() == null) {
                templateResult = "Exception: null";
            } else {
                templateResult = ex.getMessage();
            }
            _logger.error("Exception:{},", mcTemplateScript, ex);
            throw ex;
        }
        return templateResult;
    }

    public static void upload(McTemplate mcTemplate) throws IOException, IllegalAccessException,
            McBadRequestException {
        if (mcTemplate == null
                || mcTemplate.getData() == null
                || mcTemplate.getExtension() == null
                || mcTemplate.getName() == null) {
            throw new McBadRequestException("Required parameter(s) missing!");
        }
        String fileFullPath = AppProperties.getInstance().getTemplatesLocation()
                + mcTemplate.getName() + "." + mcTemplate.getExtension();
        FileUtils.writeStringToFile(FileUtils.getFile(fileFullPath), (String) mcTemplate.getData(), false);
        _logger.debug("Write success! File:{}", fileFullPath);
    }
}
