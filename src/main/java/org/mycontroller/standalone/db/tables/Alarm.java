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

import org.mycontroller.standalone.db.AlarmUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@DatabaseTable(tableName = "alarm")
public class Alarm {
    public static final String SENSOR_REF_ID = "sensor_ref_id";
    public static final String ENABLED = "enabled";
    public static final String TRIGGERED = "triggered";

    public Alarm() {
        this.triggered = false;
    }

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private String name;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = SENSOR_REF_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Sensor sensor;

    @DatabaseField(canBeNull = true)
    private Long timestamp;

    @DatabaseField(canBeNull = true)
    private Long lastTrigger;

    @DatabaseField(canBeNull = true, defaultValue = "true")
    private Boolean ignoreDuplicate;

    @DatabaseField(canBeNull = true)
    private Long lastNotification;

    @DatabaseField(canBeNull = false, columnName = TRIGGERED)
    private Boolean triggered;

    @DatabaseField(canBeNull = false)
    private Integer occurrenceCount = 0;

    @DatabaseField(canBeNull = false)
    private Integer evaluationCount = 0;

    @DatabaseField(canBeNull = false)
    private Integer type;

    @DatabaseField(canBeNull = false)
    private Integer trigger;

    @DatabaseField(canBeNull = false)
    private String thresholdValue;

    @DatabaseField(canBeNull = false)
    private Integer dampeningType;

    @DatabaseField(canBeNull = true)
    private String dampeningVar1;

    @DatabaseField(canBeNull = true)
    private String dampeningVar2;

    @DatabaseField(canBeNull = true)
    private String variable1;

    @DatabaseField(canBeNull = true)
    private String variable2;

    @DatabaseField(canBeNull = true)
    private String variable3;

    @DatabaseField(canBeNull = true)
    private String variable4;

    @DatabaseField(canBeNull = true)
    private String variable5;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getType() {
        return type;
    }

    public String getTypeString() {
        if (type != null) {
            return AlarmUtils.TYPE.get(type).value();
        }
        return null;
    }

    //To ignore json serialization
    public void setTypeString(String typeString) {

    }

    public String getVariable1() {
        return variable1;
    }

    public String getVariable2() {
        return variable2;
    }

    public String getVariable3() {
        return variable3;
    }

    public String getVariable4() {
        return variable4;
    }

    public String getVariable5() {
        return variable5;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setVariable1(String variable1) {
        this.variable1 = variable1;
    }

    public void setVariable2(String variable2) {
        this.variable2 = variable2;
    }

    public void setVariable3(String variable3) {
        this.variable3 = variable3;
    }

    public void setVariable4(String variable4) {
        this.variable4 = variable4;
    }

    public void setVariable5(String variable5) {
        this.variable5 = variable5;
    }

    public Integer getTrigger() {
        return trigger;
    }

    public String getTriggerString() {
        if (trigger != null) {
            return AlarmUtils.TRIGGER.get(trigger).value();
        }
        return null;
    }

    public void setTrigger(Integer trigger) {
        this.trigger = trigger;
    }

    //Ignore, just for JSON
    public void setTriggerString(String triggerString) {

    }

    public String getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(String thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getNotificationString() {
        return AlarmUtils.getNotificationString(this);
    }

    //To ignore json serialization
    public void setNotificationString(String notificationString) {

    }

    public Long getLastTrigger() {
        return lastTrigger;
    }

    public void setLastTrigger(Long lastTrigger) {
        this.lastTrigger = lastTrigger;
    }

    public Long getLastNotification() {
        return lastNotification;
    }

    public void setLastNotification(Long lastNotification) {
        this.lastNotification = lastNotification;
    }

    public Boolean getTriggered() {
        return triggered;
    }

    public void setTriggered(Boolean triggered) {
        this.triggered = triggered;
    }

    public Integer getDampeningType() {
        return dampeningType;
    }

    public String getDampeningVar1() {
        return dampeningVar1;
    }

    public String getDampeningVar2() {
        return dampeningVar2;
    }

    public void setDampeningType(Integer dampeningType) {
        this.dampeningType = dampeningType;
    }

    public void setDampeningVar1(String dampeningVar1) {
        this.dampeningVar1 = dampeningVar1;
    }

    public void setDampeningVar2(String dampeningVar2) {
        this.dampeningVar2 = dampeningVar2;
    }

    //To ignore json serialization error
    public void setDampeningString(String dampeningString) {

    }

    public String getDampeningString() {
        return AlarmUtils.getDampeningString(this);
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public Integer getEvaluationCount() {
        return evaluationCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public void setEvaluationCount(Integer evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public Boolean getIgnoreDuplicate() {
        return ignoreDuplicate;
    }

    public void setIgnoreDuplicate(Boolean ignoreDuplicate) {
        this.ignoreDuplicate = ignoreDuplicate;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Enabled:").append(this.enabled);
        builder.append(", Name:").append(this.name);
        builder.append(", Sensor:[").append(this.sensor).append("]");
        builder.append(", timestamp:").append(this.timestamp);
        builder.append(", ignoreDuplicate:").append(this.ignoreDuplicate);
        builder.append(", lastTrigger:").append(this.lastTrigger);
        builder.append(", lastNotification:").append(this.lastNotification);
        builder.append(", triggered:").append(this.triggered);
        builder.append(", occurrenceCount:").append(this.occurrenceCount);
        builder.append(", evaluationCount:").append(this.evaluationCount);
        builder.append(", type:").append(this.type);
        builder.append(", trigger:").append(this.trigger);
        builder.append(", thresholdValue:").append(this.thresholdValue);
        builder.append(", dampeningType:").append(this.dampeningType);
        builder.append(", dampeningVar1:").append(this.dampeningVar1);
        builder.append(", dampeningVar2:").append(this.dampeningVar2);
        builder.append(", variable1:").append(this.variable1);
        builder.append(", variable2:").append(this.variable2);
        builder.append(", variable3:").append(this.variable3);
        builder.append(", variable4:").append(this.variable4);
        builder.append(", variable5:").append(this.variable5);
        return builder.toString();
    }

}
