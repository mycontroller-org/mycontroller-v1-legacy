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

    private static void loadCSSHeaderFiles(HtmlHeaderFiles htmlHeaderFiles) {
        String cssImports = AppProperties.getInstance().getWebConfigurationsLocation() + "additional.css";
        _logger.debug("CSS imports file location:[{}], data:{}", cssImports, htmlHeaderFiles);
        StringBuilder builder = new StringBuilder();

        try {
            //add css files
            for (String cssLink : htmlHeaderFiles.getLinks()) {
                if (cssLink.trim().length() > 0) {
                    builder.append("@import url(\"")
                           .append(cssLink.trim())
                           .append("\");\n")
                    ;
                }
            }
            FileUtils.writeStringToFile(FileUtils.getFile(cssImports), builder.toString());
        } catch (Exception ex) {
            _logger.error("Unable to write static CSS imports file! location:[{}]", cssImports, ex);
        }
    }

    private static void loadJSHeaderFiles(HtmlHeaderFiles htmlHeaderFiles) {
        String jsImports = AppProperties.getInstance().getWebConfigurationsLocation() + "additional.js";
        _logger.debug("JS additional headers file location:[{}], data:{}", jsImports, htmlHeaderFiles);
        StringBuilder builder = new StringBuilder();

        builder.append(
            "$.when(\n" +
            "    //this list is automatically generated by\n" +
            "    //Utilites > HTML additional headers > Script files\n"
        );

        try {
            //add css files
            for (String scriptFile : htmlHeaderFiles.getScripts()) {
                if (scriptFile.trim().length() > 0) {
                    builder.append("    $.getScript('")
                           .append(scriptFile.trim())
                           .append("'),\n")
                    ;
                }
            }

            builder.append(
                "    $.Deferred(function(deferred) { $(deferred.resolve);})\n" +
                ").done(function() {\n" +
                "    //this list is automatically generated by\n" +
                "    //Utilities > HTML additional headers > Additional AngularJS modules to load\n" +
                "    let aModules = [\n"
            );

            if(htmlHeaderFiles.getAdditionalAngularjsModulesToLoad() != null)
            {
                for (String angularModule : htmlHeaderFiles.getAdditionalAngularjsModulesToLoad()) {
                    builder.append("        '")
                           .append(angularModule)
                           .append("',\n")
                    ;
                }
            }
            builder.append(
                "    ];\n" +
                "\n" +
                "    //test each module name to prevent AngularJS from failing to load\n" +
                "    let aWorkingModules = [];\n" +
                "    for(let i = 0; i < aModules.length; i++)\n" +
                "    {\n" +
                "        try {\n" +
                "            angular.module(aModules[i]); //test if module loads\n" +
                "            aWorkingModules.push(aModules[i]); //put working module on list\n" +
                "        }\n" +
                "        //throw an error, if module can not be loaded\n" +
                "        catch(ex) { console.log('unable to load module ' + aModules[i]); }\n" +
                "    }\n" +
                "    //resume bootstrap process which was halted with /defer_angular_bootstrap.js\n" +
                "    //and load working modules\n" +
                "    angular.resumeBootstrap(aWorkingModules);\n" +
                "});"
            );

            FileUtils.writeStringToFile(FileUtils.getFile(jsImports), builder.toString());
        } catch (Exception ex) {
            _logger.error("Unable to write static JS additional header information file! location:[{}]", jsImports, ex);
        }
    }

    private static void loadHtmlHeaderFiles(HtmlHeaderFiles htmlHeaderFiles) {
        String angularCustomControllers = AppProperties.getInstance().getWebConfigurationsLocation()
            + "custom-controllers.js";

        loadCSSHeaderFiles(htmlHeaderFiles);
        loadJSHeaderFiles(htmlHeaderFiles);

        try {
            _logger.debug("angular controllers file location:[{}], data:{}", angularCustomControllers, htmlHeaderFiles);
            //Add custom controllers file in to www location
            FileUtils.writeStringToFile(
                    FileUtils.getFile(angularCustomControllers),
                    htmlHeaderFiles.getAngularjsCustomControllers()
            );
        } catch (Exception ex) {
            _logger.error("unable to write angular controllers file! location:[{}]", angularCustomControllers, ex);
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
