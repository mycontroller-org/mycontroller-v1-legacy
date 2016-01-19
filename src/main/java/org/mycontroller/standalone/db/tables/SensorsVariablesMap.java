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

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.db.DB_TABLES;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@DatabaseTable(tableName = DB_TABLES.SENSOR_VARIABLES_MAP)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorsVariablesMap {
    public static final String KEY_ID = "id";
    public static final String KEY_SENSOR_TYPE = "sensorType";
    public static final String KEY_VARIABLE_TYPE = "variableType";

    public SensorsVariablesMap() {
    }

    public SensorsVariablesMap(MESSAGE_TYPE_PRESENTATION sensorType, MESSAGE_TYPE_SET_REQ variableType) {
        this.sensorType = sensorType;
        this.variableType = variableType;
    }

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, index = true, uniqueCombo = true, columnName = KEY_SENSOR_TYPE, dataType = DataType.ENUM_STRING)
    private MESSAGE_TYPE_PRESENTATION sensorType;

    @DatabaseField(canBeNull = false, index = true, uniqueCombo = true, columnName = KEY_VARIABLE_TYPE, dataType = DataType.ENUM_STRING)
    private MESSAGE_TYPE_SET_REQ variableType;

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

    public void setId(Integer id) {
        this.id = id;
    }

    public MESSAGE_TYPE_PRESENTATION getSensorType() {
        return sensorType;
    }

    public void setSensorType(MESSAGE_TYPE_PRESENTATION sensorType) {
        this.sensorType = sensorType;
    }

    public MESSAGE_TYPE_SET_REQ getVariableType() {
        return variableType;
    }

    public void setVariableType(MESSAGE_TYPE_SET_REQ variableType) {
        this.variableType = variableType;
    }
}
