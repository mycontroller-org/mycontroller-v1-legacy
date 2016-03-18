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
package org.mycontroller.standalone.db;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.db.tables.Timer;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ResourcesLogsUtils {
    private ResourcesLogsUtils() {

    }

    public enum LOG_LEVEL {
        TRACE("Trace"),
        NOTICE("Notice"),
        INFO("Info"),
        WARNING("Warning"),
        ERROR("Error");
        public static LOG_LEVEL get(int id) {
            for (LOG_LEVEL type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private LOG_LEVEL(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static LOG_LEVEL fromString(String text) {
            if (text != null) {
                for (LOG_LEVEL type : LOG_LEVEL.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum LOG_DIRECTION {
        INTERNAL("Internal"),
        SENT("Sent"),
        RECEIVED("Received");
        public static LOG_DIRECTION get(int id) {
            for (LOG_DIRECTION type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private LOG_DIRECTION(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static LOG_DIRECTION fromString(String text) {
            if (text != null) {
                for (LOG_DIRECTION type : LOG_DIRECTION.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static boolean isLevel(LOG_LEVEL logLevel) {
        if (LOG_LEVEL.fromString(ObjectManager.getAppProperties().getControllerSettings().getResourcesLogLevel())
                .ordinal() <= logLevel.ordinal()) {
            return true;
        }
        return false;
    }

    public static void setAlarmLog(LOG_LEVEL logLevel, AlarmDefinition alarmDefinition, Boolean triggered,
            String errorMsg) {
        StringBuilder builder = new StringBuilder();
        if (triggered) {
            builder.append("Triggered: ");
        } else {
            builder.append("Failed: ");
        }
        builder.append("Name: ").append(alarmDefinition.getName())
                .append(", Condition: ").append(AlarmUtils.getConditionString(alarmDefinition))
                .append(", Resource:").append(AlarmUtils.getResourceString(alarmDefinition, true))
                .append(", Notifications: ").append(alarmDefinition.getNotifications());

        if (errorMsg != null) {
            builder.append(", Error: ").append(errorMsg);
        }

        ResourcesLogs resourcesLogs = ResourcesLogs.builder()
                .timestamp(System.currentTimeMillis())
                .resourceType(RESOURCE_TYPE.ALARM_DEFINITION)
                .resourceId(alarmDefinition.getId())
                .logLevel(logLevel)
                .logDirection(LOG_DIRECTION.INTERNAL)
                .message(builder.toString()).build();
        DaoUtils.getResourcesLogsDao().add(resourcesLogs);
    }

    public static void setTimerLog(LOG_LEVEL logLevel, Timer timer, String errorMsg) {
        StringBuilder builder = new StringBuilder();
        if (errorMsg != null) {
            builder.append("Failed: ").append(timer.getTimerDataString())
                    .append(", Error: ").append(errorMsg);
        } else {
            builder.append("Fired: ").append(timer.getTimerDataString());
        }

        ResourcesLogs resourcesLogs = ResourcesLogs.builder()
                .timestamp(System.currentTimeMillis())
                .resourceType(RESOURCE_TYPE.TIMER)
                .resourceId(timer.getId())
                .logLevel(logLevel)
                .logDirection(LOG_DIRECTION.INTERNAL)
                .message(builder.toString()).build();
        DaoUtils.getResourcesLogsDao().add(resourcesLogs);
    }

    public static void recordSensorsResourcesLog(RESOURCE_TYPE resourceType, Integer resourceId, LOG_LEVEL logLevel,
            MESSAGE_TYPE messageType, boolean isTxMessage, String message) {
        ResourcesLogs resourcesLogs = ResourcesLogs.builder()
                .timestamp(System.currentTimeMillis())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .messageType(messageType)
                .logLevel(logLevel)
                .logDirection(isTxMessage == true ? LOG_DIRECTION.SENT : LOG_DIRECTION.RECEIVED)
                .message(message).build();
        DaoUtils.getResourcesLogsDao().add(resourcesLogs);
    }

    public static void deleteResourcesLog(RESOURCE_TYPE resourceType, Integer resourceId) {
        DaoUtils.getResourcesLogsDao().deleteAll(resourceType, resourceId);
    }

}
