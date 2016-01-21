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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.ToString;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.ALARM_DEFINITION)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class AlarmDefinition {
    public static final String KEY_ID = "id";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_NAME = "name";
    public static final String KEY_TRIGGERED = "triggered";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_LAST_TRIGGER = "lastTrigger";

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
    private Boolean triggered = false;

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

    public String getNotificationString() {
        return AlarmUtils.getNotificationString(this);
    }

    public String getDampeningString() {
        return AlarmUtils.getDampeningString(this);
    }

    public String getConditionString() {
        return AlarmUtils.getConditionString(this);
    }

    public String getResource() {
        ResourceModel resourceModel = new ResourceModel(resourceType, resourceId);
        return resourceModel.getResourceLessDetails();
    }

}
