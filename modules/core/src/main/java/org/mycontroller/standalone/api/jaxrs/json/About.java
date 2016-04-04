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
package org.mycontroller.standalone.api.jaxrs.json;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.McObjectManager;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Data
@ToString(includeFieldNames = true)
public class About {
    private String timezone;
    private Integer timezoneMilliseconds;
    private String timezoneString;
    private String appVersion;
    private Date systemDate;
    private String appName;
    private String languageId;
    private String language;
    private String dateFormat;
    private String dateFormatWithoutSeconds;
    private String timeFormat;
    private String timeFormatWithoutSeconds;
    private String timeFormatSet;
    private String loginMessage;
    private Long globalPageRefreshTime;
    private Integer dashboardLimit;
    private Integer tableRowsLimit;

    public About() {
        Date date = new Date();
        appName = AppProperties.APPLICATION_NAME;
        timezoneMilliseconds = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        systemDate = date;
        timezone = new SimpleDateFormat("Z").format(date);
        timezoneString = new SimpleDateFormat("z").format(date);
        appVersion = McObjectManager.getAppProperties().getControllerSettings().getVersion();
        languageId = McObjectManager.getAppProperties().getLanguage().toString().toLowerCase();
        language = McObjectManager.getAppProperties().getLanguage().getText();
        loginMessage = McObjectManager.getAppProperties().getControllerSettings().getLoginMessage();
        dateFormat = McObjectManager.getAppProperties().getDateFormat();
        dateFormatWithoutSeconds = McObjectManager.getAppProperties().getDateFormatWithoutSeconds();
        timeFormat = McObjectManager.getAppProperties().getTimeFormat();
        timeFormatWithoutSeconds = McObjectManager.getAppProperties().getTimeFormatWithoutSeconds();
        timeFormatSet = McObjectManager.getAppProperties().getControllerSettings().getTimeFormat();
        globalPageRefreshTime = McObjectManager.getAppProperties().getControllerSettings()
                .getGlobalPageRefreshTime();
        dashboardLimit = McObjectManager.getAppProperties().getControllerSettings().getDashboardLimit();
        tableRowsLimit = McObjectManager.getAppProperties().getControllerSettings().getTableRowsLimit();
    }
}
