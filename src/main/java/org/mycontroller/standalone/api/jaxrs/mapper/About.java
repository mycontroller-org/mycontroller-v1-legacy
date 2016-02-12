/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.api.jaxrs.mapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.ObjectFactory;

import lombok.ToString;

import lombok.Data;

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

    public About() {
        Date date = new Date();
        this.appName = AppProperties.APPLICATION_NAME;
        this.timezoneMilliseconds = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        this.systemDate = date;
        this.timezone = new SimpleDateFormat("Z").format(date);
        this.timezoneString = new SimpleDateFormat("z").format(date);
        this.appVersion = ObjectFactory.getAppProperties().getControllerSettings().getVersion();
        this.languageId = ObjectFactory.getAppProperties().getLanguage().toString().toLowerCase();
        this.language = ObjectFactory.getAppProperties().getLanguage().getText();
        this.loginMessage = ObjectFactory.getAppProperties().getControllerSettings().getLoginMessage();
        this.dateFormat = ObjectFactory.getAppProperties().getDateFormat();
        this.dateFormatWithoutSeconds = ObjectFactory.getAppProperties().getDateFormatWithoutSeconds();
        this.timeFormat = ObjectFactory.getAppProperties().getTimeFormat();
        this.timeFormatWithoutSeconds = ObjectFactory.getAppProperties().getTimeFormatWithoutSeconds();
        this.timeFormatSet = ObjectFactory.getAppProperties().getControllerSettings().getTimeFormat();
        this.globalPageRefreshTime = ObjectFactory.getAppProperties().getControllerSettings()
                .getGlobalPageRefreshTime();
    }
}
