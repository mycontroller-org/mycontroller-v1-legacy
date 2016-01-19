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

import org.mycontroller.standalone.MYCMessages;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.metrics.TypeUtils.METRIC_TYPE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@DatabaseTable(tableName = DB_TABLES.SENSOR_VARIABLE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorVariable {
    public static final String KEY_ID = "id";
    public static final String KEY_SENSOR_DB_ID = "sensorDbId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_VARIABLE_TYPE = "variableType";
    public static final String KEY_VALUE = "value";
    public static final String KEY_METRIC = "metricType";
    public static final String KEY_UNIT = "unit";

    public SensorVariable() {
    }

    public SensorVariable(Integer id) {
        this.id = id;
    }

    public SensorVariable(Sensor sensor, MESSAGE_TYPE_SET_REQ variableType, String lastvalue, Long timestamp,
            METRIC_TYPE metricType, String unit) {
        this.sensor = sensor;
        this.variableType = variableType;
        this.timestamp = timestamp;
        this.value = lastvalue;
        this.metricType = metricType;
        this.unit = (unit == null ? "" : unit);
    }

    public SensorVariable(Sensor sensor, MESSAGE_TYPE_SET_REQ variableType, String lastvalue, Long timestamp,
            METRIC_TYPE metricType) {
        this(sensor, variableType, lastvalue, timestamp, metricType, SensorUtils.getUnit(variableType));
    }

    public SensorVariable(Sensor sensor, MESSAGE_TYPE_SET_REQ variableType, String lastvalue, METRIC_TYPE metricType) {
        this(sensor, variableType, lastvalue, System.currentTimeMillis(), metricType,
                SensorUtils.getUnit(variableType));
    }

    public SensorVariable(Sensor sensor, MESSAGE_TYPE_SET_REQ variableType, String lastvalue) {
        this(sensor, variableType, lastvalue, System.currentTimeMillis(), null, SensorUtils.getUnit(variableType));
    }

    public SensorVariable(Sensor sensor, MESSAGE_TYPE_SET_REQ variableType, METRIC_TYPE metricType) {
        this(sensor, variableType, null, null, metricType, SensorUtils.getUnit(variableType));
    }

    public SensorVariable(Sensor sensor, MESSAGE_TYPE_SET_REQ variableType) {
        this(sensor, variableType, null, null, MYCMessages.getMetricType(
                MYCMessages.getPayLoadType(variableType)), SensorUtils.getUnit(variableType));
    }

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(columnName = KEY_SENSOR_DB_ID, canBeNull = false, uniqueCombo = true, foreign = true,
            maxForeignAutoRefreshLevel = 3, foreignAutoRefresh = true)
    private Sensor sensor;

    @DatabaseField(columnName = KEY_VARIABLE_TYPE, canBeNull = false, uniqueCombo = true, dataType = DataType.ENUM_STRING)
    private MESSAGE_TYPE_SET_REQ variableType;

    @DatabaseField(columnName = KEY_METRIC, canBeNull = false, dataType = DataType.ENUM_STRING)
    private METRIC_TYPE metricType = METRIC_TYPE.NONE;

    @DatabaseField(columnName = KEY_TIMESTAMP, canBeNull = true)
    private Long timestamp;

    @DatabaseField(columnName = KEY_VALUE, canBeNull = true)
    private String value;

    @DatabaseField(columnName = KEY_UNIT, canBeNull = true)
    private String unit;

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Sensor:").append("[").append(sensor).append("]");
        builder.append(", Variable Type:").append(this.variableType.getText())
                .append("(").append(this.variableType).append(")");
        builder.append(", Metric Type:").append(this.metricType.toString())
                .append("(").append(this.metricType).append(")");
        builder.append(", Value:").append(this.value);
        builder.append(", Timestamp:").append(this.timestamp);
        builder.append(", Unit:").append(this.unit);
        return builder.toString();
    }

    public Integer getId() {
        return id;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(String lastValue) {
        this.value = lastValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public MESSAGE_TYPE_SET_REQ getVariableType() {
        return variableType;
    }

    public void setVariableType(MESSAGE_TYPE_SET_REQ variableType) {
        this.variableType = variableType;
    }

    public METRIC_TYPE getMetricType() {
        return metricType;
    }

    public void setMetricType(METRIC_TYPE metricType) {
        this.metricType = metricType;
    }
}
