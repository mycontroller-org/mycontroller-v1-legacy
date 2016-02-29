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
package org.mycontroller.standalone.notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.model.ResourceModel;

import lombok.Getter;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Getter
public class AlarmNotification {
    public static final String KEY_ALARM_DEFINITION_NAME = Pattern.quote("${aName}");
    public static final String KEY_ALARM_RESOURCE = Pattern.quote("${aResource}");
    public static final String KEY_ALARM_CONDITION = Pattern.quote("${aCondition}");
    public static final String KEY_ACTUAL_VALUE = Pattern.quote("${aValue}");
    public static final String KEY_ALARM_TRIGGERED_AT = Pattern.quote("${aTriggeredAt}");
    public static final String KEY_NOTIFICATION_NAME = Pattern.quote("${notificationName}");

    private String aName;
    private String aResource;
    private String aCondition;
    private String aValue;
    private String aTriggeredAt;

    private String notificationName;

    public AlarmNotification(AlarmDefinition alarmDefinition, String actualValue) {
        this.aName = alarmDefinition.getName();
        this.aCondition = AlarmUtils.getConditionString(alarmDefinition)
                + AlarmUtils.getSensorUnit(alarmDefinition, true);
        this.aValue = actualValue + AlarmUtils.getSensorUnit(alarmDefinition, false);
        this.aResource = new ResourceModel(alarmDefinition.getResourceType(), alarmDefinition.getResourceId())
                .getResourceLessDetails();
        this.aTriggeredAt = new SimpleDateFormat(ObjectFactory.getAppProperties().getDateFormatWithTimezone())
                .format(new Date(System.currentTimeMillis()));
    }

    public String toString(String spaceVariable) {
        StringBuilder builder = new StringBuilder();
        builder.append("Alarm definition: ").append(aName);
        builder.append(spaceVariable).append("Resource: ").append(aResource);
        builder.append(spaceVariable).append("Condition: ").append(aCondition);
        builder.append(spaceVariable).append("Present value: ").append(aValue);
        if (notificationName != null) {
            builder.append(spaceVariable).append("Notification: ").append(notificationName);
        }
        builder.append(spaceVariable).append("Triggered at: ").append(aTriggeredAt);
        builder.append(spaceVariable).append("--- www.mycontroller.org");
        return builder.toString();
    }

    public String toString() {
        return toString("\n");
    }

    public String updateReferances(String source) {
        return source.replaceAll(KEY_ALARM_DEFINITION_NAME, aName)
                .replaceAll(KEY_ALARM_RESOURCE, aResource)
                .replaceAll(KEY_ALARM_CONDITION, aCondition)
                .replaceAll(KEY_ACTUAL_VALUE, aValue)
                .replaceAll(KEY_ALARM_TRIGGERED_AT, aTriggeredAt)
                .replaceAll(KEY_NOTIFICATION_NAME, notificationName);
    }

}
