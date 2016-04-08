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

import org.mycontroller.standalone.McUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyControllerSettings {
    public static final String KEY_MY_CONTROLLER = "myController";
    public static final String SKEY_LANGUAGE = "language";
    public static final String SKEY_TIME_FORMAT = "timeFormat";
    public static final String SKEY_VERSION = "version";
    public static final String SKEY_DB_VERSION = "dbVersion";
    public static final String SKEY_ALIVE_CHECK_INTERVAL = "aliveCheckInterval";
    public static final String SKEY_UNIT_CONFIG = "unitConfig";
    public static final String SKEY_LOGIN_MESSAGE = "loginMessage";
    public static final String SKEY_GRANT_ACCESS_TO_CHILD_RESOURCES = "grantAccessToChildResources";
    public static final String SKEY_RESOURCES_LOG_LEVEL = "resourcesLogLevel";
    public static final String SKEY_GLOBAL_PAGE_REFRESH_TIME = "globalPageRefreshTime";
    public static final String SKEY_DASHBOARD_LIMIT = "dashboardLimit";
    public static final String SKEY_WIDGET_IMAGE_FILES_LOCATION = "widgetImageFilesLocation";
    public static final String SKEY_TABLE_ROWS_LIMIT = "tableRowsLimit";

    private String language;
    private String timeFormat;
    private String version;
    private String dbVersion;
    private Long aliveCheckInterval;
    private String unitConfig;
    private String loginMessage;
    private Boolean grantAccessToChildResources;
    private String resourcesLogLevel;
    private Long globalPageRefreshTime;
    private Integer dashboardLimit;
    private String widgetImageFilesLocation;
    private Integer tableRowsLimit;

    public static MyControllerSettings get() {
        return MyControllerSettings.builder()
                .language(getValue(SKEY_LANGUAGE))
                .timeFormat(getValue(SKEY_TIME_FORMAT))
                .version(getValue(SKEY_VERSION))
                .dbVersion(getValue(SKEY_DB_VERSION))
                .aliveCheckInterval(McUtils.getLong(getValue(SKEY_ALIVE_CHECK_INTERVAL)))
                .unitConfig(getValue(SKEY_UNIT_CONFIG))
                .loginMessage(getValue(SKEY_LOGIN_MESSAGE))
                .grantAccessToChildResources(McUtils.getBoolean(getValue(SKEY_GRANT_ACCESS_TO_CHILD_RESOURCES)))
                .resourcesLogLevel(getValue(SKEY_RESOURCES_LOG_LEVEL))
                .globalPageRefreshTime(McUtils.getLong(getValue(SKEY_GLOBAL_PAGE_REFRESH_TIME)))
                .dashboardLimit(McUtils.getInteger(getValue(SKEY_DASHBOARD_LIMIT)))
                .widgetImageFilesLocation(getValue(SKEY_WIDGET_IMAGE_FILES_LOCATION))
                .tableRowsLimit(McUtils.getInteger(getValue(SKEY_TABLE_ROWS_LIMIT)))
                .build();
    }

    public void save() {
        if (language != null) {
            updateValue(SKEY_LANGUAGE, language);
        }
        if (timeFormat != null) {
            updateValue(SKEY_TIME_FORMAT, timeFormat);
        }
        if (aliveCheckInterval != null) {
            updateValue(SKEY_ALIVE_CHECK_INTERVAL, aliveCheckInterval);
        }
        if (unitConfig != null) {
            updateValue(SKEY_UNIT_CONFIG, unitConfig);
        }
        if (loginMessage != null) {
            updateValue(SKEY_LOGIN_MESSAGE, loginMessage);
        }
        if (grantAccessToChildResources != null) {
            updateValue(SKEY_GRANT_ACCESS_TO_CHILD_RESOURCES, grantAccessToChildResources);
        }
        if (resourcesLogLevel != null) {
            updateValue(SKEY_RESOURCES_LOG_LEVEL, resourcesLogLevel);
        }
        if (globalPageRefreshTime != null) {
            updateValue(SKEY_GLOBAL_PAGE_REFRESH_TIME, globalPageRefreshTime);
        }
        if (dashboardLimit != null) {
            updateValue(SKEY_DASHBOARD_LIMIT, dashboardLimit);
        }
        if (widgetImageFilesLocation != null) {
            updateValue(SKEY_WIDGET_IMAGE_FILES_LOCATION, McUtils.getDirectoryLocation(widgetImageFilesLocation));
        }
        if (tableRowsLimit != null) {
            updateValue(SKEY_TABLE_ROWS_LIMIT, tableRowsLimit);
        }
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_MY_CONTROLLER, subKey);
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_MY_CONTROLLER, subKey, value);
    }

    @JsonIgnore
    public void updateInternal() {
        if (version != null) {
            updateValue(SKEY_VERSION, version);
        }
        if (dbVersion != null) {
            updateValue(SKEY_DB_VERSION, dbVersion);
        }
    }
}