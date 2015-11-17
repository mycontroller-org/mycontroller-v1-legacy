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
package org.mycontroller.standalone.db.tables;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = "settings")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
	public static final String AUTO_NODE_ID = "auto-node-key";
	public static final String MC_VERSION = "mc-version";
	public static final String MC_DB_VERSION = "mc-db-version";
	public static final String LAST_ONE_DAY_AGGREGATION = "last_one_day_aggregation";
	public static final String SUNRISE_TIME = "timer-sunrise";
	public static final String SUNSET_TIME = "timer-sunset";
	public static final String CITY_NAME = "city_name";
	public static final String CITY_LATITUDE = "city_latitude";
	public static final String CITY_LONGITUDE = "city_longitude";
	public static final String DEFAULT_FIRMWARE = "default_firmware";
	public static final String ENABLE_NOT_AVAILABLE_TO_DEFAULT_FIRMWARE = "not_available_to_default_firmware";

	public static final String EMAIL_SMTP_HOST = "email_smtp_host";
	public static final String EMAIL_SMTP_PORT = "email_smtp_port";
	public static final String EMAIL_SMTP_USERNAME = "email_smtp_username";
	public static final String EMAIL_SMTP_PASSWORD = "email_smtp_password";
	public static final String EMAIL_ENABLE_SSL = "email_enable_ssl";
	public static final String EMAIL_FROM = "email_from";

	public static final String SMS_AUTH_ID = "sms_auth_id";
	public static final String SMS_AUTH_TOKEN = "sms_auth_token";
	public static final String SMS_FROM_PHONE_NUMBER = "sms_from_phone_number";

	public static final String SERIALPORT_NAME = "serialport_name";
	public static final String SERIALPORT_BAUD_RATE = "serialport_baud_rate";
	public static final String SERIALPORT_DRIVER_TYPE = "serialport_driver_type";

	public static final String GRAPH_INTERPOLATE_TYPE = "graph_interpolate_type";

	public static final String DEFAULT_UNIT = "du_";

	public static final String ENABLE_SEND_PAYLOAD = "enable_send_payload";

	public static final String MY_SENSORS_CONFIG = "mys_config";

	public static final String MC_LANGUAGE = "mc_language";

	public Settings() {

	}

	public Settings(String key, String value, String frindlyName) {
		this(key, value, frindlyName, null);
	}

	public Settings(String key, String value, String frindlyName, Boolean userEditable) {
		this.key = key;
		this.value = value;
		this.frindlyName = frindlyName;
		if (userEditable == null) {
			this.userEditable = false;
		} else {
			this.userEditable = userEditable;
		}

	}

	public Settings(String key) {
		this.key = key;
	}

	@DatabaseField(generatedId = true)
	private Integer id;
	@DatabaseField(canBeNull = false, unique = true)
	private String key;
	@DatabaseField(canBeNull = false)
	private String frindlyName;
	@DatabaseField(canBeNull = true)
	private String value;
	@DatabaseField(canBeNull = true)
	private Boolean userEditable = false;

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String id) {
		this.key = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFrindlyName() {
		return frindlyName;
	}

	public void setFrindlyName(String frindlyName) {
		this.frindlyName = frindlyName;
	}

	public Boolean getUserEditable() {
		return userEditable;
	}

	public void setUserEditable(Boolean userEditable) {
		this.userEditable = userEditable;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
