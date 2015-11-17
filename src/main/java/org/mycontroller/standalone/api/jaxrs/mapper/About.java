/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

public class About {
	private String timezone;
	private int timezoneMilliseconds;
	private String timezoneString;
	private String appVersion;
	private Date systemDate;
	private String appName;
	private String language;

	public About() {
		Date date = new Date();
		this.appName = AppProperties.APPLICATION_NAME;
		this.timezoneMilliseconds = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		this.systemDate = date;
		this.timezone = new SimpleDateFormat("Z").format(date);
		this.timezoneString = new SimpleDateFormat("z").format(date);
		this.appVersion = DaoUtils.getSettingsDao().get(Settings.MC_VERSION).getValue();
		this.language = ObjectFactory.getAppProperties().getLanguage().toString().replaceAll("_", "-").toLowerCase();
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getTimezoneString() {
		return timezoneString;
	}

	public Date getSystemDate() {
		return systemDate;
	}

	public String getAppName() {
		return appName;
	}

	public int getTimezoneMilliseconds() {
		return timezoneMilliseconds;
	}

	public String getLanguage() {
		return language;
	}
}
