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

import java.util.List;

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.api.jaxrs.mapper.SensorsGuiButton;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.SensorUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.SENSOR)
public class Sensor {
    public static final String KEY_ID = "id";
    public static final String KEY_SENSOR_ID = "sensorId";
    public static final String KEY_NODE_ID = "nodeId";
    public static final String KEY_NODE_NAME = "nodeName";
    public static final String KEY_NAME = "name";
    public static final String KEY_LAST_SEEN = "lastSeen";
    public static final String KEY_TYPE = "type";

    public Sensor() {
    }

    public Sensor(Integer sensorId, MESSAGE_TYPE_PRESENTATION type, String name) {
        this.sensorId = sensorId;
        this.type = type;
        this.name = name;
        this.lastSeen = System.currentTimeMillis();
    }

    public Sensor(Integer sensorId) {
        this.sensorId = sensorId;
    }

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, index = true, uniqueCombo = true, columnName = KEY_SENSOR_ID)
    private Integer sensorId;

    @DatabaseField(dataType = DataType.ENUM_STRING, columnName = KEY_TYPE)
    private MESSAGE_TYPE_PRESENTATION type;

    @DatabaseField(columnName = KEY_NAME)
    private String name;

    @DatabaseField(columnName = KEY_LAST_SEEN)
    private Long lastSeen;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_NODE_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Node node;

    private List<String> variableTypes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public MESSAGE_TYPE_PRESENTATION getType() {
        return type;
    }

    public void setType(MESSAGE_TYPE_PRESENTATION type) {
        this.type = type;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SensorsGuiButton getGuiButtons() {
        return SensorUtils.getGuiButtonsStatus(this);
    }

    public List<String> getVariableTypes() {
        if (this.variableTypes == null) {
            this.variableTypes = SensorUtils.getVariableTypes(this);
        }
        return this.variableTypes;
    }

    public void setVariableTypes(List<String> variableTypes) {
        this.variableTypes = variableTypes;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    //This method is used to create variables for this sensor,
    //But in mixins(SensorMixin) we need id of this sensor, if we use we 'getId'cannot generate 'id'
    //So added this ugly workaround.
    @JsonGetter
    private Integer getIdforVariables() {
        return this.id;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", SensorId:").append(this.sensorId);
        builder.append(", Name:").append(this.name);
        builder.append(", SensorType:").append(this.type);
        builder.append(", VariableTypes:").append(getVariableTypes());
        builder.append(", lastSeen:").append(this.lastSeen);
        builder.append(", Node:[").append(this.node).append("]");
        return builder.toString();
    }
}
