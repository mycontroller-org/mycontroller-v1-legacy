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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.PayloadOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.TIMER)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timer {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_TIMER_TYPE = "timerType";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_LAST_FIRED = "lastFired";

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_NAME)
    private String name;

    @DatabaseField(dataType = DataType.ENUM_INTEGER, canBeNull = false, columnName = KEY_RESOURCE_TYPE)
    private RESOURCE_TYPE resourceType;

    @DatabaseField(canBeNull = false, columnName = KEY_RESOURCE_ID)
    private Integer resourceId;

    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false, columnName = KEY_TIMER_TYPE)
    private TIMER_TYPE timerType;

    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = true, columnName = KEY_FREQUENCY)
    private FREQUENCY frequency;

    @DatabaseField(canBeNull = true)
    private String frequencyData;

    @DatabaseField(canBeNull = true)
    private Long triggerTime;

    @DatabaseField(canBeNull = true)
    private Long validityFrom;

    @DatabaseField(canBeNull = true)
    private Long validityTo;

    @DatabaseField(canBeNull = false)
    private String payload;

    @DatabaseField(canBeNull = true, columnName = KEY_LAST_FIRED)
    private Long lastFired;

    @DatabaseField(canBeNull = true)
    private String internalVariable1;

    public Timer() {

    }

    public Timer(boolean enabled, String name, RESOURCE_TYPE resourceType, Integer resourceId, TIMER_TYPE timerType) {
        this.enabled = enabled;
        this.name = name;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.timerType = timerType;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Enabled:").append(this.enabled);
        builder.append(", Name:").append(this.name);
        builder.append(", ResourceType:").append(this.resourceType);
        builder.append(", ResourceId:").append(this.resourceId);
        builder.append(", TriggerType:").append(this.timerType.getText());
        builder.append(", Frequency:").append(this.frequency != null ? this.frequency.getText() : "-");
        builder.append(", TriggerTime:").append(this.triggerTime);
        builder.append(", ValidFrom:").append(this.validityFrom);
        builder.append(", ValidTo:").append(this.validityTo);
        builder.append(", Payload:").append(this.payload);
        builder.append(", Timestamp:").append(this.lastFired);
        return builder.toString();
    }

    @JsonGetter(value = "id")
    public Integer getId() {
        return id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getTimerDataString() {
        try {
            return TimerUtils.getTimerDataString(this);
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    public String getValidityString() {
        return TimerUtils.getValidityString(this);
    }

    public String getFrequencyData() {
        return frequencyData;
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public Long getValidityFrom() {
        return validityFrom;
    }

    public Long getValidityTo() {
        return validityTo;
    }

    @JsonIgnore
    public void setId(Integer id) {
        this.id = id;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFrequencyData(String frequencyData) {
        if (frequencyData != null && frequencyData.length() > 0) {
            this.frequencyData = frequencyData;
        }
    }

    //@JsonIgnore
    public void setTriggerTime(Long time) {
        this.triggerTime = time;
    }

    /*    @JsonSetter(value = "triggerTime")
        private void setTriggerTime(String time) {
            if (time != null) {
                this.triggerTime = TimerUtils.getTime(time);
            }
        }*/

    public void setValidityFrom(Long validFrom) {
        this.validityFrom = validFrom;
    }

    public void setValidFromString(String validFromString) {
        if (validFromString != null) {
            this.validityFrom = TimerUtils.getValidFromToTime(validFromString + ":00");
        }
    }

    public void setValidToString(String validToString) {
        if (validToString != null) {
            this.validityTo = TimerUtils.getValidFromToTime(validToString + ":59");
        }
    }

    public void setValidityTo(Long validTo) {
        this.validityTo = validTo;
    }

    public Long getLastFired() {
        return lastFired;
    }

    public void setLastFired(Long timestamp) {
        this.lastFired = timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayloadFormatted() {
        StringBuilder builder = new StringBuilder();
        PayloadOperation specialOperation = new PayloadOperation(this.payload);
        if (specialOperation.getOperationType() != null) {
            if (specialOperation.getOperationType() == SEND_PAYLOAD_OPERATIONS.REBOOT) {
                builder.append(" ").append(specialOperation.getOperationType().getText());
            } else {
                if (specialOperation.getValue() != null) {
                    builder.append(" {resource.value} ")
                            .append(specialOperation.getOperationType().getText())
                            .append(" ")
                            .append(NumericUtils.getDoubleAsString(specialOperation.getValue()));
                } else {
                    builder.append(specialOperation.getOperationType().getText());
                }
            }
        } else {
            builder.append(this.payload);
        }
        return builder.toString();
    }

    @JsonIgnore
    public RESOURCE_TYPE getResourceType() {
        return resourceType;
    }

    @JsonGetter(value = "resourceType")
    private String getResourceTypeString() {
        if (resourceType != null) {
            return resourceType.getText();
        }
        return null;
    }

    @JsonIgnore
    public void setResourceType(RESOURCE_TYPE resourceType) {
        this.resourceType = resourceType;
    }

    @JsonSetter(value = "resourceType")
    private void setResourceType(String resourceType) {
        if (resourceType != null) {
            this.resourceType = org.mycontroller.standalone.AppProperties.RESOURCE_TYPE.fromString(resourceType);
        }
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    @JsonIgnore
    public TIMER_TYPE getTimerType() {
        return timerType;
    }

    @JsonIgnore
    public void setTimerType(TIMER_TYPE timerType) {
        this.timerType = timerType;
    }

    @JsonGetter(value = "timerType")
    private String getTimerTypeString() {
        if (timerType != null) {
            return timerType.getText();
        }
        return null;
    }

    @JsonSetter(value = "timerType")
    private void setTimerType(String timerType) {
        if (timerType != null) {
            this.timerType = TIMER_TYPE.fromString(timerType);
        }
    }

    @JsonIgnore
    public FREQUENCY getFrequency() {
        return frequency;
    }

    @JsonIgnore
    public void setFrequency(FREQUENCY frequency) {
        this.frequency = frequency;
    }

    @JsonGetter(value = "frequency")
    private String getFrequencyString() {
        if (frequency != null) {
            return frequency.getText();
        }
        return null;
    }

    @JsonSetter(value = "frequency")
    private void setFrequency(String frequency) {
        if (frequency != null) {
            this.frequency = FREQUENCY.fromString(frequency);
        }
    }

    @JsonIgnore
    public String getInternalVariable1() {
        return internalVariable1;
    }

    @JsonIgnore
    public void setInternalVariable1(String internalVariable1) {
        this.internalVariable1 = internalVariable1;
    }

    @JsonGetter(value = "resource")
    private String getResource() {
        ResourceModel resourceModel = new ResourceModel(resourceType, resourceId);
        return resourceModel.getResourceLessDetails();
    }

}
