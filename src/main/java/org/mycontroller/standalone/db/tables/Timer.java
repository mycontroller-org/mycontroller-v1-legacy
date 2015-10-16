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

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.db.PayloadSpecialOperation;
import org.mycontroller.standalone.db.TimerUtils;
import org.mycontroller.standalone.db.PayloadSpecialOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timer {
    public static final String SENSOR_REF_ID = "sensor_ref_id";
    public static final String SENSOR_VARIABLE_TYPE = "sensor_var_type";
    public static final String ENABLED = "enabled";

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private String name;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = SENSOR_REF_ID)
    private Sensor sensor;

    @DatabaseField(canBeNull = false, columnName = SENSOR_VARIABLE_TYPE)
    private Integer sensorVariableType;

    @DatabaseField(canBeNull = false)
    private Integer type;

    @DatabaseField(canBeNull = true)
    private Integer frequency;

    @DatabaseField(canBeNull = true)
    private String frequencyData;

    @DatabaseField(canBeNull = true)
    private Long time;

    @DatabaseField(canBeNull = true)
    private Long validFrom;

    @DatabaseField(canBeNull = true)
    private Long validTo;

    @DatabaseField(canBeNull = false)
    private String payload;

    @DatabaseField(canBeNull = true)
    private Long timestamp;

    public Integer getId() {
        return id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Integer getType() {
        return type;
    }

    public String getTimerDataString() {
        return TimerUtils.getTimerDataString(this);
    }

    public String getValidityString() {
        return TimerUtils.getValidityString(this);
    }

    public Integer getFrequency() {
        return frequency;
    }

    public String getFrequencyData() {
        return frequencyData;
    }

    public Long getTime() {
        return time;
    }

    public Long getValidFrom() {
        return validFrom;
    }

    public Long getValidTo() {
        return validTo;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setTypeString() {
        // To ignore JSON serialization
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public void setFrequencyDataString(String frequencyDataString) {
        if (frequencyDataString != null && frequencyDataString.length() > 0) {
            this.frequencyData = frequencyDataString;
        }
    }

    public void setFrequencyData(List<Integer> frequencyData) {
        if (frequencyData != null && !frequencyData.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Integer frequencyD : frequencyData) {
                if (builder.length() > 0) {
                    builder.append(",").append(frequencyD);
                } else {
                    builder.append(frequencyD);
                }
            }
            this.frequencyData = builder.toString();
        }
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setTimeString(String time) {
        this.time = TimerUtils.getTime(time);
    }

    public void setValidFrom(Long validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidFromString(String validFromString) {
        this.validFrom = TimerUtils.getValidFromToTime(validFromString + ":00");
    }

    public void setValidToString(String validToString) {
        this.validTo = TimerUtils.getValidFromToTime(validToString + ":59");
    }

    public void setValidTo(Long validTo) {
        this.validTo = validTo;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayloadFormatted() {
        StringBuilder builder = new StringBuilder();
        PayloadSpecialOperation specialOperation = new PayloadSpecialOperation(this.payload);
        if (specialOperation.getOperationType() != null) {
            if (specialOperation.getOperationType() == SEND_PAYLOAD_OPERATIONS.REBOOT) {
                builder.append(" ").append(specialOperation.getOperationType().value());
            } else {
                if (specialOperation.getValue() != null) {
                    builder.append(" {sen.value} ")
                            .append(specialOperation.getOperationType().value())
                            .append(" ")
                            .append(NumericUtils.getDoubleAsString(specialOperation.getValue()));
                } else {
                    builder.append(" ")
                            .append(specialOperation.getOperationType().value())
                            .append(" {sen.value}");
                }
            }
        } else {
            builder.append(this.payload);
        }
        return builder.toString();
    }

    public void setPayloadFormatted(String payload) {
        // To ignore JSON serialization
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name:").append(name)
                .append(", Type:").append(getTimerDataString());
        builder.append(", Variable Type:").append(this.getSensorVariableTypeString());
        builder.append(", PayLoad:").append(this.getPayloadFormatted());
        builder.append(", Validity:").append(getValidityString());
        return builder.toString();
    }

    public Integer getSensorVariableType() {
        return sensorVariableType;
    }

    public String getSensorVariableTypeString() {
        if (sensorVariableType != null) {
            return MESSAGE_TYPE_SET_REQ.get(sensorVariableType).toString();
        }
        return "";
    }

    public void setSensorVariableType(Integer sensorVariableType) {
        this.sensorVariableType = sensorVariableType;
    }
}
