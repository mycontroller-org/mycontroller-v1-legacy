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

import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = "node")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
    public static final String FIRMWARE_ID = "firmware_id";

    public Node() {
    }

    public Node(Integer id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.updateTime = System.currentTimeMillis();
    }

    public Node(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.updateTime = System.currentTimeMillis();
    }

    public Node(Integer id) {
        this.id = id;
    }

    @DatabaseField(id = true, unique = true)
    private Integer id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String version;
    @DatabaseField
    private Long updateTime;
    @DatabaseField
    private Integer type;
    @DatabaseField
    private String mySensorsVersion;
    @DatabaseField
    private String batteryLevel;
    @DatabaseField(canBeNull = true)
    private Boolean eraseEEPROM;
    @DatabaseField(canBeNull = true, columnName = FIRMWARE_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Firmware firmware;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getMySensorsVersion() {
        return mySensorsVersion;
    }

    public void setMySensorsVersion(String mySensorsVersion) {
        this.mySensorsVersion = mySensorsVersion;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeString() {
        if (type != null) {
            return MESSAGE_TYPE_PRESENTATION.get(type).toString();
        }
        return null;
    }

    //Ignore, for JSON serialization support
    public void setTypeString(String typeString) {

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

    public Boolean getEraseEEPROM() {
        return eraseEEPROM;
    }

    public void setEraseEEPROM(Boolean eraseEEPROM) {
        this.eraseEEPROM = eraseEEPROM;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Name:").append(this.name);
        builder.append(", Type:").append(this.type);
        builder.append(", Version:").append(this.version);
        builder.append(", MySensorsVersion:").append(this.mySensorsVersion);
        builder.append(", BatteryLevel:").append(this.batteryLevel);
        builder.append(", EraseEEPROM:").append(this.eraseEEPROM);
        builder.append(", Firmware:").append(this.firmware);
        builder.append(", UpdateTime:").append(this.updateTime);
        return builder.toString();
    }

}
