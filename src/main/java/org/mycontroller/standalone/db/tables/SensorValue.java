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

import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.TypeUtils.METRIC_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@DatabaseTable(tableName = "sensor_value")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorValue {
    public static final String ID = "id";
    public static final String SENSOR_REF_ID = "sensor_ref_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String VARIABLE_TYPE = "variable_type";
    public static final String LAST_VALUE = "last_value";
    public static final String METRIC = "metric_type";
    public static final String UNIT = "unit";

    public SensorValue() {
    }

    public SensorValue(Sensor sensor, Integer variableType, String lastvalue, Long timestamp,
            Integer metricType, String unit) {
        this.sensor = sensor;
        this.variableType = variableType;
        this.timestamp = timestamp;
        this.lastValue = lastvalue;
        this.metricType = metricType;
        this.unit = (unit == null ? "" : unit);
    }

    public SensorValue(Sensor sensor, Integer variableType, String lastvalue, Long timestamp, Integer metricType) {
        this(sensor, variableType, lastvalue, timestamp,
                metricType, SensorUtils.getUnitString(variableType));
    }

    public SensorValue(Sensor sensor, Integer variableType, String lastvalue, Integer metricType) {
        this(sensor, variableType, lastvalue, System.currentTimeMillis(),
                metricType, SensorUtils.getUnitString(variableType));
    }

    public SensorValue(Sensor sensor, Integer variableType, String lastvalue) {
        this(sensor, variableType, lastvalue, System.currentTimeMillis(), null
                , SensorUtils.getUnitString(variableType));
    }

    public SensorValue(Sensor sensor, Integer variableType, Integer metricType) {
        this(sensor, variableType, null, null, metricType, SensorUtils.getUnitString(variableType));
    }

    public SensorValue(Sensor sensor, Integer variableType) {
        this(sensor, variableType, null, null, MyMessages.getMetricType(MyMessages.getPayLoadType(MESSAGE_TYPE_SET_REQ
                .get(variableType))).ordinal(), SensorUtils.getUnitString(variableType));
    }

    @DatabaseField(generatedId = true, columnName = ID)
    private Integer id;

    @DatabaseField(columnName = SENSOR_REF_ID, canBeNull = false, uniqueCombo = true, foreign = true, maxForeignAutoRefreshLevel = 0)
    private Sensor sensor;

    @DatabaseField(columnName = VARIABLE_TYPE, canBeNull = false, uniqueCombo = true)
    private Integer variableType;

    @DatabaseField(columnName = METRIC, canBeNull = false)
    private Integer metricType = METRIC_TYPE.NONE.ordinal();

    @DatabaseField(columnName = TIMESTAMP, canBeNull = true)
    private Long timestamp;

    @JsonProperty("value")
    @DatabaseField(columnName = LAST_VALUE, canBeNull = true)
    private String lastValue;

    @DatabaseField(columnName = UNIT, canBeNull = true)
    private String unit;

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Sensor:").append("[").append(sensor).append("]");
        builder.append(", Variable Type:").append(getVariableTypeString())
                .append("(").append(this.variableType).append(")");
        builder.append(", Metric Type:").append(METRIC_TYPE.get(this.metricType))
                .append("(").append(this.metricType).append(")");
        builder.append(", Last Value:").append(this.lastValue);
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

    public Integer getVariableType() {
        return variableType;
    }

    public String getVariableTypeString() {
        return MESSAGE_TYPE_SET_REQ.get(this.variableType).toString();
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void setVariableType(Integer variableType) {
        this.variableType = variableType;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public Integer getMetricType() {
        return metricType;
    }

    public void setMetricType(Integer metricType) {
        this.metricType = metricType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
