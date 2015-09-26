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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.mysensors.MyMessages;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = "sensors")
public class Sensor {
    public static final String SENSOR_ID = "sensor_id";
    public static final String NODE_ID = "node_id";

    public Sensor() {
    }

    public Sensor(Integer sensorId, Integer type, String name) {
        this.sensorId = sensorId;
        this.type = type;
        this.name = name;
        this.updateTime = System.currentTimeMillis();
    }

    public Sensor(Integer sensorId) {
        this.sensorId = sensorId;
    }

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false, index = true, uniqueCombo = true, columnName = SENSOR_ID)
    private Integer sensorId;
    @DatabaseField
    private Integer type;
    @DatabaseField
    private Integer messageType;
    @DatabaseField
    private String name;
    @DatabaseField
    private Long updateTime;
    @DatabaseField
    private String status;
    @DatabaseField
    private String lastValue;
    @DatabaseField
    private String unit;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = NODE_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Node node;

    public void updateDefault() {
        if (this.unit == null && this.messageType != null) {
            switch (MyMessages.getSensorUnit(MESSAGE_TYPE_SET_REQ.get(this.messageType))) {
                case DISTANCE:
                    this.unit = DaoUtils.getSettingsDao().get(Settings.DEFAULT_UNIT_DISTANCE).getValue();
                    break;
                case TEMPERATURE:
                    this.unit = DaoUtils.getSettingsDao().get(Settings.DEFAULT_UNIT_TEMPERATURE).getValue();
                    break;
                case PERCENTAGE:
                    this.unit = DaoUtils.getSettingsDao().get(Settings.DEFAULT_UNIT_PERCENTAGE).getValue();
                    break;
                default:
                    this.unit = "";
                    break;
            }
        }
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeString() {
        if (this.type != null) {
            return MESSAGE_TYPE_PRESENTATION.get(this.type).toString();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getNameWithNode() {
        if (node != null && node.getName() != null) {
            if (name == null) {
                return node.getName() + ":-";
            }
            return node.getName() + ":" + name;
        }
        return name;
    }

    public void setNameWithNode(String nameWithNode) {
        //For now to ignore setter for json
    }

    public void setName(String name) {
        this.name = name;
    }

    //Ignore, JSON serialization support
    public void setTypeString(String typeString) {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public String getMessageTypeString() {
        if (this.messageType != null) {
            return MESSAGE_TYPE_SET_REQ.get(this.messageType).toString();
        }
        return null;
    }

    //Ignore, only for JSON serilization
    public void setMessageTypeString(String messageTypeString) {

    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Integer getMetricType() {
        if (this.messageType != null) {
            return MyMessages.getPayLoadType(MESSAGE_TYPE_SET_REQ.get(this.messageType)).ordinal();
        }
        return null;
    }

    public void setMetricType(Integer metrictype) {
        //Just ignore, to avoid setter from JSON converter
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String scale) {
        this.unit = scale;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", SensorId:").append(this.sensorId);
        builder.append(", Name:").append(this.name);
        builder.append(", MessageType:").append(this.messageType);
        builder.append(", Type:").append(this.type);
        builder.append(", LastValue:").append(this.lastValue);
        builder.append(", Status:").append(this.status);
        builder.append(", Unit:").append(this.unit);
        builder.append(", UpdateTime:").append(this.updateTime);
        builder.append(", Node:[").append(this.node).append("]");
        return builder.toString();
    }
}
