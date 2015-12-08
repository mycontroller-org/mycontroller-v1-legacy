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

import java.util.List;

import org.mycontroller.standalone.api.jaxrs.mapper.SensorStatus;
import org.mycontroller.standalone.api.jaxrs.mapper.SensorsGuiButton;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = "sensor")
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private String name;

    @DatabaseField
    private Long updateTime;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = NODE_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Node node;

    @DatabaseField(canBeNull = true)
    private Boolean enableSendPayload;

    private String variableTypes;

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

    public void setName(String name) {
        this.name = name;
    }

    public List<SensorStatus> getStatus() {
        return SensorUtils.getStatus(this);
    }

    public SensorsGuiButton getGuiButtons() {
        return SensorUtils.getGuiButtonsStatus(this);
    }

    public String getVariableTypes() {
        if (this.variableTypes == null) {
            this.variableTypes = SensorUtils.getVariableTypes(this);
        }
        return this.variableTypes;
    }

    public void setVariableTypes(String variableTypes) {
        this.variableTypes = variableTypes;
    }

    public String getLastSeen() {
        return SensorUtils.getLastSeen(this.updateTime);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", SensorId:").append(this.sensorId);
        builder.append(", Name:").append(this.name);
        builder.append(", SensorType:").append(this.type);
        builder.append(", VariableTypes:").append(getVariableTypes());
        builder.append(", UpdateTime:").append(this.updateTime);
        builder.append(", Node:[").append(this.node).append("]");
        builder.append(", Status:[").append(this.getStatus()).append("]");
        builder.append(", IsSendPayloadEnabled?:").append(this.getEnableSendPayload());
        return builder.toString();
    }

    public Boolean getEnableSendPayload() {
        if (this.enableSendPayload == null) {
            return SensorUtils.getSendPayloadEnabled(this);
        }
        return enableSendPayload;
    }

    public Boolean getEnableSendPayloadRaw() {
        return enableSendPayload;
    }

    public void setEnableSendPayload(Boolean enableSendPayload) {
        this.enableSendPayload = enableSendPayload;
    }
}
