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
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@DatabaseTable(tableName = "sensors_variables_map")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorsVariablesMap {
    public static final String SENSOR_TYPE = "sensor_type";
    public static final String VARIABLE_TYPE = "variable_type";

    public SensorsVariablesMap() {
    }

    public SensorsVariablesMap(Integer sensorType, Integer variableType) {
        this.sensorType = sensorType;
        this.variableType = variableType;
    }

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, index = true, uniqueCombo = true, columnName = SENSOR_TYPE)
    private Integer sensorType;

    @DatabaseField(canBeNull = false, index = true, uniqueCombo = true, columnName = VARIABLE_TYPE)
    private Integer variableType;

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Sensor Type:").append(this.sensorType);
        builder.append(", Variable Type:").append(this.variableType);
        return builder.toString();
    }

    public Integer getId() {
        return id;
    }

    public Integer getSensorType() {
        return sensorType;
    }

    public Integer getVariableType() {
        return variableType;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSensorType(Integer sensorType) {
        this.sensorType = sensorType;
    }

    public void setVariableType(Integer variableType) {
        this.variableType = variableType;
    }

    public String getSensorTypeString() {
        return MESSAGE_TYPE_PRESENTATION.get(this.sensorType).toString();
    }

    public String getVariableTypeString() {
        return MESSAGE_TYPE_SET_REQ.get(this.variableType).toString();
    }
}
