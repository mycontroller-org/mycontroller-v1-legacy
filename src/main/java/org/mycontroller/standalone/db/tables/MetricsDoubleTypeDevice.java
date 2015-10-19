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

import org.apache.commons.lang.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsDoubleTypeDevice {
    public static final String SENSOR_VALUE_REF_ID = "sensor_value_ref_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String AGGREGATION_TYPE = "aggregation_type";

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = SENSOR_VALUE_REF_ID)
    private SensorValue sensorValue;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = TIMESTAMP)
    private Long timestamp;

    @DatabaseField(canBeNull = false)
    private Integer samples;

    @DatabaseField
    private Double min;

    @DatabaseField
    private Double max;

    @DatabaseField(canBeNull = false)
    private Double avg;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = AGGREGATION_TYPE)
    private Integer aggregationType;

    private Long timestampFrom;
    private Long timestampTo;

    public MetricsDoubleTypeDevice(SensorValue sensorValue, Integer aggregationType) {
        this(sensorValue, aggregationType, null, null, aggregationType);
    }

    public MetricsDoubleTypeDevice(SensorValue sensorValue, Integer aggregationType, Long timestamp) {
        this(sensorValue, aggregationType, timestamp, null, aggregationType);
    }

    public MetricsDoubleTypeDevice(SensorValue sensorValue, Integer aggregationType, Long timestamp, Double avg,
            Integer samples) {
        this.sensorValue = sensorValue;
        this.aggregationType = aggregationType;
        this.timestamp = timestamp;
        this.avg = avg;
        this.samples = samples;
    }

    public MetricsDoubleTypeDevice() {

    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public SensorValue getSensorValue() {
        return this.sensorValue;
    }

    public void setSensorValue(SensorValue sensorValue) {
        this.sensorValue = sensorValue;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getSamples() {
        return samples;
    }

    public void setSamples(Integer samples) {
        this.samples = samples;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Integer getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(Integer aggregationType) {
        this.aggregationType = aggregationType;
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
}
