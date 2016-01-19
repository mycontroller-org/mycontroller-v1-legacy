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

import org.mycontroller.standalone.db.DB_TABLES;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.METRICS_BINARY_TYPE_DEVICE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsBinaryTypeDevice {
    public static final String KEY_SENSOR_VARIABLE_ID = "sensorVariableId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_STATE = "state";

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_SENSOR_VARIABLE_ID)
    private SensorVariable sensorVariable;
    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = KEY_TIMESTAMP)
    private Long timestamp;
    @DatabaseField(canBeNull = false, columnName = KEY_STATE)
    private Boolean state;

    private Long timestampFrom;
    private Long timestampTo;

    public MetricsBinaryTypeDevice() {

    }

    public MetricsBinaryTypeDevice(SensorVariable sensorVariable) {
        this(sensorVariable, null, null);
    }

    public MetricsBinaryTypeDevice(SensorVariable sensorVariable, Long timestamp) {
        this(sensorVariable, timestamp, null);
    }

    public MetricsBinaryTypeDevice(SensorVariable sensorVariable, Long timestamp, Boolean state) {
        this.sensorVariable = sensorVariable;
        this.state = state;
        this.timestamp = timestamp;
    }

    public SensorVariable getSensorValue() {
        return sensorVariable;
    }

    public void setSensorValue(SensorVariable sensorVariable) {
        this.sensorVariable = sensorVariable;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Long getTimestampFrom() {
        return timestampFrom;
    }

    public void setTimestampFrom(Long timestampFrom) {
        this.timestampFrom = timestampFrom;
    }

    public Long getTimestampTo() {
        return timestampTo;
    }

    public void setTimestampTo(Long timestampTo) {
        this.timestampTo = timestampTo;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}