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
package org.mycontroller.standalone.settings;

import java.io.File;
import java.util.List;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.json.About;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SettingsUtils {
    private static final Logger _logger = LoggerFactory.getLogger(SettingsUtils.class.getName());

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
        String fileLocation = ObjectFactory.getAppProperties().getWebFileLocation() + "configMyController.json";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            _logger.debug("controller information static file location:[{}]", fileLocation);
            objectMapper.writeValue(new File(fileLocation), new About());
        } catch (Exception ex) {
            _logger.error("Unable to write static json information file! location:[{}]", fileLocation, ex);
        }
    }

    public static void updateAllSettings() {
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
        updateStaticJsonInformationFile();
    }
}
