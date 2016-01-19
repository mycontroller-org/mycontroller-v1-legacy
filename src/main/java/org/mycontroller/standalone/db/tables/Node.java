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
package org.mycontroller.standalone.db.tables;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.NumericUtils;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.NODE)
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

    public Node() {
    }

    public Node(Gateway gateway, String eui, String name, String version) {
        this.eui = eui;
        this.name = name;
        this.version = version;
        this.gateway = gateway;
    }

    public Node(Gateway gateway, String eui, String name) {
        this(gateway, eui, name, null);
    }

    public Node(String eui, Gateway gateway) {
        this(gateway, eui, null, null);
    }

    public Node(Integer gatewayId, String eui) {
        this(new Gateway(gatewayId), eui, null, null);
    }

    public Node(Integer id) {
        this.id = id;
    }

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;
    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = KEY_EUI)
    private String eui;
    @DatabaseField(uniqueCombo = true, canBeNull = true, columnName = KEY_GATEWAY_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Gateway gateway;
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
    @DatabaseField(canBeNull = true, columnName = KEY_FIRMWARE_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Firmware firmware;
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_STATE)
    private STATE state = STATE.UNAVAILABLE;
    @DatabaseField(canBeNull = true, columnName = KEY_LAST_SEEN)
    private Long lastSeen;

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getEui() {
        return eui;
    }

    public Integer getEuiInt() {
        return NumericUtils.getInteger(eui);
    }

    public void setEui(String eui) {
        this.eui = eui;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLibVersion() {
        return libVersion;
    }

    public String getBatteryLevel() {
        if (batteryLevel == null) {
            return "-";
        }
        return batteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Firmware getFirmware() {
        return firmware;
    }

    public void setFirmware(Firmware firmware) {
        this.firmware = firmware;
    }

    public Boolean getEraseConfig() {
        return eraseConfig;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public void setLibVersion(String libVersion) {
        this.libVersion = libVersion;
    }

    public void setEraseConfig(Boolean eraseConfig) {
        this.eraseConfig = eraseConfig;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public MESSAGE_TYPE_PRESENTATION getType() {
        return type;
    }

    public void setType(MESSAGE_TYPE_PRESENTATION type) {
        this.type = type;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Eui:").append(this.eui);
        builder.append(", Name:").append(this.name);
        builder.append(", Type:").append(this.type);
        builder.append(", Version:").append(this.version);
        builder.append(", MySensorsVersion:").append(this.libVersion);
        builder.append(", BatteryLevel:").append(this.batteryLevel);
        builder.append(", EraseEEPROM:").append(this.eraseConfig);
        builder.append(", Firmware:").append(this.firmware);
        builder.append(", Status:").append(this.state);
        builder.append(", LastSeen:").append(this.lastSeen);
        return builder.toString();
    }
}
