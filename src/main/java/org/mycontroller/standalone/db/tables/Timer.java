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
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.TIMER)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
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
    private FREQUENCY_TYPE frequencyType;

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

    public void setFrequencyData(String frequencyData) {
        if (frequencyData != null && frequencyData.length() > 0) {
            this.frequencyData = frequencyData;
        }
    }

    public void setValidityFrom(Long validFrom) {
        this.validityFrom = validFrom;
    }

    public void setValidityTo(Long validTo) {
        this.validityTo = validTo;
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

    public String getResource() {
        ResourceModel resourceModel = new ResourceModel(resourceType, resourceId);
        return resourceModel.getResourceLessDetails();
    }

}
