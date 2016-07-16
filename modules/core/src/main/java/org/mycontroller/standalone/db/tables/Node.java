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
package org.mycontroller.standalone.db.tables;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.NodeUtils.NODE_REGISTRATION_STATE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.NODE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class Node {
    public static final String KEY_FIRMWARE_ID = "firmwareId";
    public static final String KEY_GATEWAY_ID = "gatewayId";
    public static final String KEY_GATEWAY_NAME = "gatewayName";
    public static final String KEY_STATE = "state";
    public static final String KEY_EUI = "eui";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_VERSION = "version";
    public static final String KEY_LIB_VERSION = "libVersion";
    public static final String KEY_BATTERY_LEVEL = "batteryLevel";
    public static final String KEY_ERASE_CONFIG = "eraseConfig";
    public static final String KEY_LAST_SEEN = "laseSeen";
    public static final String KEY_RSSI = "rssi";
    public static final String KEY_PROPERTIES = "properties";
    public static final String KEY_PARENT_ID = "parentId";
    public static final String KEY_REGISTRATION_STATE = "registrationState";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = KEY_EUI)
    private String eui;

    @DatabaseField(uniqueCombo = true, canBeNull = true, columnName = KEY_GATEWAY_ID, foreign = true,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private GatewayTable gatewayTable;

    @DatabaseField(columnName = KEY_NAME)
    private String name;

    @DatabaseField(columnName = KEY_VERSION)
    private String version;

    @DatabaseField(dataType = DataType.ENUM_STRING, columnName = KEY_TYPE)
    private MESSAGE_TYPE_PRESENTATION type;

    @DatabaseField(columnName = KEY_LIB_VERSION)
    private String libVersion;

    @DatabaseField(columnName = KEY_BATTERY_LEVEL)
    private String batteryLevel;

    @DatabaseField(canBeNull = true, columnName = KEY_ERASE_CONFIG)
    private Boolean eraseConfig;

    @DatabaseField(canBeNull = true, columnName = KEY_FIRMWARE_ID, foreign = true,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Firmware firmware;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_STATE)
    private STATE state = STATE.UNAVAILABLE;

    @DatabaseField(canBeNull = true, columnName = KEY_LAST_SEEN)
    private Long lastSeen;

    @DatabaseField(canBeNull = true, columnName = KEY_RSSI)
    private String rssi;

    @DatabaseField(canBeNull = true, columnName = KEY_PROPERTIES, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Object> properties;

    @DatabaseField(canBeNull = true, columnName = KEY_PARENT_ID)
    private String parentId;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_REGISTRATION_STATE)
    private NODE_REGISTRATION_STATE registrationState = NODE_REGISTRATION_STATE.NEW;

    public HashMap<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties;
    }

}