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
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.NOTIFICATION_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.THRESHOLD_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.TRIGGER_TYPE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.model.ResourceModel;

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
@DatabaseTable(tableName = DB_TABLES.ALARM_DEFINITION)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlarmDefinition {
    public static final String KEY_ID = "id";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_NAME = "name";
    public static final String KEY_TRIGGERED = "triggered";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_LAST_TRIGGER = "lastTrigger";

    public AlarmDefinition() {
        this.triggered = false;
    }

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_NAME)
    private String name;

    @DatabaseField(dataType = DataType.ENUM_STRING, columnName = KEY_RESOURCE_TYPE)
    private RESOURCE_TYPE resourceType;

    @DatabaseField(canBeNull = false, columnName = KEY_RESOURCE_ID)
    private Integer resourceId;

    @DatabaseField(canBeNull = true)
    private Long timestamp;

    @DatabaseField(canBeNull = true, columnName = KEY_LAST_TRIGGER)
    private Long lastTrigger;

    @DatabaseField(canBeNull = true, defaultValue = "true")
    private Boolean ignoreDuplicate;

    @DatabaseField(canBeNull = true)
    private Long lastNotification;

    @DatabaseField(canBeNull = false, columnName = KEY_TRIGGERED)
    private Boolean triggered;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING)
    private THRESHOLD_TYPE thresholdType;

    @DatabaseField(canBeNull = false)
    private String thresholdValue;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING)
    private TRIGGER_TYPE triggerType;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING)
    private DAMPENING_TYPE dampeningType;

    @DatabaseField(canBeNull = true)
    private String dampeningVar1;

    @DatabaseField(canBeNull = true)
    private String dampeningVar2;

    @DatabaseField(canBeNull = true)
    private String dampeningInternal1;

    @DatabaseField(canBeNull = true)
    private String dampeningInternal2;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING)
    private NOTIFICATION_TYPE notificationType;

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

    @DatabaseField(canBeNull = true)
    private String variable6;

    @DatabaseField(canBeNull = true)
    private String variable7;

    @JsonGetter(value = "id")
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getTimestamp() {
        return timestamp;
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

    @JsonIgnore
    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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

    public String getDampeningVar1() {
        return dampeningVar1;
    }

    public String getDampeningVar2() {
        return dampeningVar2;
    }

    public void setDampeningVar1(String dampeningVar1) {
        this.dampeningVar1 = dampeningVar1;
    }

    public void setDampeningVar2(String dampeningVar2) {
        this.dampeningVar2 = dampeningVar2;
    }

    public String getDampeningString() {
        return AlarmUtils.getDampeningString(this);
    }

    public Boolean getIgnoreDuplicate() {
        return ignoreDuplicate;
    }

    public void setIgnoreDuplicate(Boolean ignoreDuplicate) {
        this.ignoreDuplicate = ignoreDuplicate;
    }

    @JsonIgnore
    public RESOURCE_TYPE getResourceType() {
        return resourceType;
    }

    @JsonGetter(value = "resourceType")
    private String getResourceTypeString() {
        return resourceType.getText();
    }

    @JsonSetter(value = "resourceType")
    private void setResourceType(String resourceType) {
        if (resourceType != null) {
            this.resourceType = RESOURCE_TYPE.fromString(resourceType);
        }
    }

    public void setResourceType(RESOURCE_TYPE resourceType) {
        this.resourceType = resourceType;
    }

    @JsonIgnore
    public NOTIFICATION_TYPE getNotificationType() {
        return notificationType;
    }

    @JsonGetter(value = "notificationType")
    private String getNotificationTypeString() {
        return notificationType.getText();
    }

    @JsonSetter(value = "notificationType")
    private void setNotificationType(String notificationType) {
        if (notificationType != null) {
            this.notificationType = NOTIFICATION_TYPE.fromString(notificationType);
        }
    }

    public void setNotificationType(NOTIFICATION_TYPE notificationType) {
        this.notificationType = notificationType;
    }

    @JsonIgnore
    public TRIGGER_TYPE getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TRIGGER_TYPE triggerType) {
        this.triggerType = triggerType;
    }

    @JsonGetter(value = "triggerType")
    private String getTriggerTypeString() {
        return triggerType.getText();
    }

    @JsonSetter(value = "triggerType")
    private void setTriggerType(String triggerType) {
        if (triggerType != null) {
            this.triggerType = TRIGGER_TYPE.fromString(triggerType);
        }
    }

    @JsonIgnore
    public THRESHOLD_TYPE getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(THRESHOLD_TYPE thresholdType) {
        this.thresholdType = thresholdType;
    }

    @JsonGetter(value = "thresholdType")
    private String getThresholdTypeString() {
        return thresholdType.getText();
    }

    @JsonSetter(value = "thresholdType")
    private void setThresholdType(String thresholdType) {
        if (thresholdType != null) {
            this.thresholdType = THRESHOLD_TYPE.fromString(thresholdType);
        }
    }

    @JsonIgnore
    public DAMPENING_TYPE getDampeningType() {
        return dampeningType;
    }

    public void setDampeningType(DAMPENING_TYPE dampeningType) {
        this.dampeningType = dampeningType;
    }

    @JsonGetter(value = "dampeningType")
    private String getDampeningTypeString() {
        return dampeningType.getText();
    }

    @JsonSetter(value = "dampeningType")
    private void setDampeningType(String dampeningType) {
        if (dampeningType != null) {
            this.dampeningType = DAMPENING_TYPE.fromString(dampeningType);
        }
    }

    public String getVariable6() {
        return variable6;
    }

    public void setVariable6(String variable6) {
        this.variable6 = variable6;
    }

    public String getVariable7() {
        return variable7;
    }

    public void setVariable7(String variable7) {
        this.variable7 = variable7;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Enabled:").append(this.enabled);
        builder.append(", Name:").append(this.name);
        builder.append(", timestamp:").append(this.timestamp);
        builder.append(", ignoreDuplicate:").append(this.ignoreDuplicate);
        builder.append(", lastTrigger:").append(this.lastTrigger);
        builder.append(", lastNotification:").append(this.lastNotification);
        builder.append(", triggered:").append(this.triggered);
        builder.append(", dampeningInternal1:").append(this.dampeningInternal1);
        builder.append(", dampeningInternal2:").append(this.dampeningInternal2);
        builder.append(", notificationType:").append(this.notificationType.getText());
        builder.append(", triggerType:").append(this.triggerType.getText());
        builder.append(", thresholdType:").append(this.thresholdType);
        builder.append(", thresholdValue:").append(this.thresholdValue);
        builder.append(", dampeningType:").append(this.dampeningType);
        builder.append(", dampeningVar1:").append(this.dampeningVar1);
        builder.append(", dampeningVar2:").append(this.dampeningVar2);
        builder.append(", variable1:").append(this.variable1);
        builder.append(", variable2:").append(this.variable2);
        builder.append(", variable3:").append(this.variable3);
        builder.append(", variable4:").append(this.variable4);
        builder.append(", variable5:").append(this.variable5);
        builder.append(", variable6:").append(this.variable6);
        builder.append(", variable7:").append(this.variable7);
        return builder.toString();
    }

    public String getConditionString() {
        return AlarmUtils.getConditionString(this);
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceTypeId) {
        this.resourceId = resourceTypeId;
    }

    @JsonGetter(value = "resource")
    private String getResource() {
        ResourceModel resourceModel = new ResourceModel(resourceType, resourceId);
        return resourceModel.getResourceLessDetails();
    }

    @JsonIgnore
    public String getDampeningInternal1() {
        return dampeningInternal1;
    }

    @JsonIgnore
    public void setDampeningInternal1(String dampeningInternal1) {
        this.dampeningInternal1 = dampeningInternal1;
    }

    @JsonIgnore
    public String getDampeningInternal2() {
        return dampeningInternal2;
    }

    @JsonIgnore
    public void setDampeningInternal2(String dampeningInternal2) {
        this.dampeningInternal2 = dampeningInternal2;
    }

}
