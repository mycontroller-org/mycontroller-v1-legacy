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
package org.mycontroller.standalone.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.model.HtmlHeaderFiles;
import org.mycontroller.standalone.api.jaxrs.model.McGuiSettings;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SettingsUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    private SettingsUtils() {

    }

    public static String getValue(String key) {
        return getValue(key, key);
    }

    public static String getValue(Integer userId, String key, String subKey) {
        Settings settings = DaoUtils.getSettingsDao().get(userId, key, subKey);
        return settings != null ? settings.getValue() : null;
    }

    public static String getValue(String key, String subKey) {
        return getValue(null, key, subKey);
    }

    public static void updateSettings(Settings settings) {
        updateSettings(settings, false);
    }

    public static void updateSettings(Settings settings, boolean forceCreate) {
        Settings oldSettings = null;
        if (settings.getId() != null) {
            oldSettings = DaoUtils.getSettingsDao().getById(settings.getId());
            if (oldSettings.getUserId() != settings.getUserId()) {
                //different user id? trying to hack?
                _logger.warn("Cannot update this settings, different user id'd found!"
                        + " Old settings:[{}], new settings:[{}]", oldSettings, settings);
            } else {
                DaoUtils.getSettingsDao().update(settings);
            }
        } else {
            if (forceCreate) {
                DaoUtils.getSettingsDao().create(settings);
            } else {
                oldSettings = getSettings(settings.getUserId(), settings.getKey(), settings.getSubKey());
                if (oldSettings == null) {
                    DaoUtils.getSettingsDao().create(settings);
                } else {
                    settings.setId(oldSettings.getId());
                    DaoUtils.getSettingsDao().update(settings);
                }
            }
        }
    }

    public static Settings getSettings(Integer userId, String key, String subKey) {
        return DaoUtils.getSettingsDao().get(userId, key, subKey);
    }

    public static Settings getSettings(String key, String subKey) {
        return getSettings(null, key, subKey);
    }

    public static List<Settings> getSettingsList(Integer userId, String key) {
        return DaoUtils.getSettingsDao().getAll(userId, key);
    }

    public static void updateValue(String key, Object value) {
        updateValue(key, key, value);
    }

    public static void updateValue(String key, String subKey, Object value) {
        DaoUtils.getSettingsDao().update(key, subKey, value != null ? String.valueOf(value) : null);
    }

    public static void updateValue(String key, String subKey, Object value, Object altValue) {
        DaoUtils.getSettingsDao().update(key, subKey, value != null ? String.valueOf(value) : null,
                altValue != null ? String.valueOf(altValue) : null);
    }

    //When reloading configuration write this static file
    //This file used in GUI side before login
    //As all of our REST API basic authentication,
    //without authentication we need to serve some information about our controller
    public static void updateStaticJsonInformationFile() {
        String fileLocation = AppProperties.getInstance().getWebConfigurationsLocation() + "mycontroller-configs.json";
        try {
            _logger.debug("controller information static file location:[{}]", fileLocation);
            OBJECT_MAPPER.writeValue(new File(fileLocation), new McGuiSettings());
            loadHtmlHeaderFiles(getHtmlIncludeFiles());
        } catch (Exception ex) {
            _logger.error("Unable to write static json information file! location:[{}]", fileLocation, ex);
        }
    }

    public static void updateAllSettings() {
        AppProperties.getInstance().loadPropertiesFromDb();
        updateStaticJsonInformationFile();
    }

    public static HtmlHeaderFiles getHtmlIncludeFiles() {
        File htmlIncludeFile = FileUtils.getFile(AppProperties.getInstance().getHtmlHeadersFile());
        if (htmlIncludeFile.exists()) {
            try {
                return OBJECT_MAPPER.readValue(htmlIncludeFile, HtmlHeaderFiles.class);
            } catch (IOException ex) {
                _logger.error("Exception, ", ex);
            }
        }
        return HtmlHeaderFiles.builder()
                .lastUpdate(System.currentTimeMillis())
                .scripts(new ArrayList<String>())
                .links(new ArrayList<String>())
                .angularjsCustomControllers("")
                .build();
    }

    private static void loadHtmlHeaderFiles(HtmlHeaderFiles htmlHeaderFiles) {
        String htmlAdditionalHeaders = AppProperties.getInstance().getWebConfigurationsLocation()
                + "additional-headers.html";
        String angularJsCustomControllers = AppProperties.getInstance().getWebConfigurationsLocation()
                + "custom-controllers.js";
        try {
            _logger.debug("Html additional headers file location:[{}], data:{}", htmlAdditionalHeaders,
                    htmlHeaderFiles);
            StringBuilder builder = new StringBuilder();
            //Add css files
            for (String cssLink : htmlHeaderFiles.getLinks()) {
                if (cssLink.trim().length() > 0) {
                    builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"")
                            .append(cssLink.trim()).append("\">\n");
                }
            }
            //Add js files
            for (String scriptFile : htmlHeaderFiles.getScripts()) {
                if (scriptFile.trim().length() > 0) {
                    builder.append("<script src=\"").append(scriptFile.trim()).append("\"></script>\n");
                }
            }
            FileUtils.writeStringToFile(FileUtils.getFile(htmlAdditionalHeaders), builder.toString());
            //Add custom controllers file in to www location
            FileUtils.writeStringToFile(FileUtils.getFile(angularJsCustomControllers),
                    htmlHeaderFiles.getAngularjsCustomControllers());
        } catch (Exception ex) {
            _logger.error("Unable to write static html header information file! location:[{}]",
                    htmlAdditionalHeaders, ex);
        }
    }

    public static void saveHtmlIncludeFiles(HtmlHeaderFiles htmlHeaderFiles) {
        try {
            OBJECT_MAPPER.writeValue(FileUtils.getFile(AppProperties.getInstance().getHtmlHeadersFile()),
                    htmlHeaderFiles);
            loadHtmlHeaderFiles(htmlHeaderFiles);
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
        }
    }
}
