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
package org.mycontroller.standalone.alarm;

import java.util.List;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.scheduler.SchedulerUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class AlarmUtils {
    public static final String ALARM_TIMER_JOB = "timer_by_alarm_definition_";

    private AlarmUtils() {

    }

    public enum THRESHOLD_TYPE {
        VALUE("Value"),
        SENSOR_VARIABLE("Sensor variable"),
        GATEWAY_STATE("Gateway state"),
        NODE_STATE("Node state");
        public static THRESHOLD_TYPE get(int id) {
            for (THRESHOLD_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private THRESHOLD_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static THRESHOLD_TYPE fromString(String text) {
            if (text != null) {
                for (THRESHOLD_TYPE type : THRESHOLD_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum NOTIFICATION_TYPE {
        SEND_PAYLOAD("Send payload"),
        SEND_SMS("Send SMS"),
        SEND_EMAIL("Send email");
        public static NOTIFICATION_TYPE get(int id) {
            for (NOTIFICATION_TYPE notification_type : values()) {
                if (notification_type.ordinal() == id) {
                    return notification_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private NOTIFICATION_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static NOTIFICATION_TYPE fromString(String text) {
            if (text != null) {
                for (NOTIFICATION_TYPE type : NOTIFICATION_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum TRIGGER_TYPE {
        GREATER_THAN(">"),
        GREATER_THAN_EQUAL(">="),
        LESSER_THAN("<"),
        LESSER_THAN_EQUAL("<="),
        EQUAL("=="),
        NOT_EQUAL("!=");

        public static TRIGGER_TYPE get(int id) {
            for (TRIGGER_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private TRIGGER_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static TRIGGER_TYPE fromString(String text) {
            if (text != null) {
                for (TRIGGER_TYPE type : TRIGGER_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum DAMPENING_TYPE {
        NONE("None"),
        CONSECUTIVE("Consecutive"),
        LAST_N_EVALUATIONS("Last N evaluations"),
        ACTIVE_TIME("Active time");
        public static DAMPENING_TYPE get(int id) {
            for (DAMPENING_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private DAMPENING_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static DAMPENING_TYPE fromString(String text) {
            if (text != null) {
                for (DAMPENING_TYPE type : DAMPENING_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static String getAlarmTimerJobName(AlarmDefinition alarmDefinition) {
        return ALARM_TIMER_JOB + alarmDefinition.getId();
    }

    public static String getSensorUnit(AlarmDefinition alarmDefinition, boolean isCondition) {
        String unit = "";
        switch (alarmDefinition.getResourceType()) {
            case SENSOR_VARIABLE:
                if (isCondition) {
                    if (alarmDefinition.getThresholdType() == THRESHOLD_TYPE.VALUE) {
                        return unit;
                    }
                }
                SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(alarmDefinition.getResourceId());
                if (sensorVariable.getUnit() != null && sensorVariable.getUnit().length() > 0) {
                    unit = " " + sensorVariable.getUnit();
                }

                break;

            default:
                break;
        }

        return unit;
    }

    public static String getNotificationString(AlarmDefinition alarmDefinition) {
        StringBuilder builder = new StringBuilder();
        builder.append(alarmDefinition.getNotificationType().getText()).append(": ");
        switch (alarmDefinition.getNotificationType()) {
            case SEND_PAYLOAD:
                NotificationSendPayLoad sendPayLoad = new NotificationSendPayLoad(alarmDefinition);
                builder.append(sendPayLoad.toString());
                break;
            case SEND_EMAIL:
                NotificationEmail notificationEmail = new NotificationEmail(alarmDefinition);
                builder.append(notificationEmail.getToEmailAddress());
                break;
            case SEND_SMS:
                NotificationSMS notificationSMS = new NotificationSMS(alarmDefinition);
                builder.append(notificationSMS.getToPhoneNumber());
                break;
            default:
                builder.append("-");
        }
        return builder.toString();
    }

    //----------------review-----------------
    public static String getDampeningString(AlarmDefinition alarmDefinition) {
        StringBuilder builder = new StringBuilder();
        builder.append(alarmDefinition.getDampeningType().getText());
        switch (alarmDefinition.getDampeningType()) {
            case NONE:
                break;
            case CONSECUTIVE:
                DampeningConsecutive dampeningConsecutive = new DampeningConsecutive(alarmDefinition);
                builder.append(": ").append(dampeningConsecutive.getConsecutiveMax());
                break;
            case LAST_N_EVALUATIONS:
                DampeningLastNEvaluations lastNEvaluations = new DampeningLastNEvaluations(alarmDefinition);
                builder.append(": ").append(lastNEvaluations.getOccurrencesMax()).append(" out of ")
                        .append(lastNEvaluations.getEvaluationsMax());
                break;
            case ACTIVE_TIME:
                DampeningActiveTime dampeningActiveTime = new DampeningActiveTime(alarmDefinition);
                builder.append(": ").append(
                        dampeningActiveTime.getActiveTime() != 0 ? NumericUtils.getFriendlyTime(
                                dampeningActiveTime.getActiveTime(), true) : "-");
                break;
            default:
                return "-";
        }
        return builder.toString();
    }

    public static String getResourceString(AlarmDefinition alarmDefinition, boolean isDetailed) {
        StringBuilder builder = new StringBuilder();
        switch (alarmDefinition.getResourceType()) {
            case GATEWAY:
                Gateway gateway = DaoUtils.getGatewayDao().getById(alarmDefinition.getResourceId());
                if (isDetailed) {
                    builder.append("[Name:").append(gateway.getName());
                    builder.append(", Type:").append(gateway.getType().getText());
                    builder.append(", Network type:").append(gateway.getNetworkType().getText());
                    builder.append("]");
                } else {
                    builder.append(":").append(gateway.getName());
                }
                break;
            case NODE:
                Node node = DaoUtils.getNodeDao().get(alarmDefinition.getResourceId());
                if (isDetailed) {
                    builder.append("[Name:").append(node.getName());
                    builder.append(", Eui:").append(node.getEui());
                    builder.append(", Gateway:").append(node.getGateway().getName());
                    builder.append("]");
                } else {
                    builder.append(":").append(node.getName()).append(", ").append(node.getEui());
                }
                break;
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(alarmDefinition.getResourceId());
                if (isDetailed) {
                    builder.append("[Sensor:").append(sensorVariable.getSensor().getName());
                    builder.append(", Type:").append(sensorVariable.getVariableType().getText());
                    builder.append("]");
                } else {
                    builder.append(":").append(sensorVariable.getSensor().getName()).append(", ")
                            .append(sensorVariable.getVariableType().getText());
                }
                break;
            default:
                break;
        }

        return builder.toString();
    }

    public static String getConditionString(AlarmDefinition alarmDefinition) {
        StringBuilder builder = new StringBuilder();
        builder.append("if {resource} ");
        builder.append(alarmDefinition.getTriggerType().getText()).append(" {");
        switch (alarmDefinition.getThresholdType()) {
            case VALUE:
                builder.append(alarmDefinition.getThresholdValue());
                break;
            case SENSOR_VARIABLE:
                ResourceModel resourceModel = new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE,
                        NumericUtils.getInteger(alarmDefinition.getThresholdValue()));
                builder.append(resourceModel.getResourceLessDetails());
                break;
            case GATEWAY_STATE:
                resourceModel = new ResourceModel(RESOURCE_TYPE.GATEWAY,
                        NumericUtils.getInteger(alarmDefinition.getThresholdValue()));
                builder.append(resourceModel.getResourceLessDetails());
                break;
            case NODE_STATE:
                resourceModel = new ResourceModel(RESOURCE_TYPE.NODE,
                        NumericUtils.getInteger(alarmDefinition.getThresholdValue()));
                builder.append(resourceModel.getResourceLessDetails());
                break;
            default:
                break;
        }
        builder.append("}");
        return builder.toString();
    }

    public static void enableAlarmDefinition(AlarmDefinition alarmDefinition) {
        alarmDefinition.setEnabled(true);
        alarmDefinition.setTriggered(false);
        alarmDefinition.setDampeningInternal1(null);
        alarmDefinition.setDampeningInternal2(null);
        DaoUtils.getAlarmDefinitionDao().update(alarmDefinition);
    }

    public static void disableAlarmDefinition(AlarmDefinition alarmDefinition) {
        //Unload timer job
        Timer timer = new Timer();
        timer.setName(getAlarmTimerJobName(alarmDefinition));
        SchedulerUtils.unloadTimerJob(timer);
        //Disable
        alarmDefinition.setEnabled(false);
        DaoUtils.getAlarmDefinitionDao().update(alarmDefinition);
    }

    public static void enableAlarmDefinitions(List<Integer> ids) {
        List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll(ids);
        for (AlarmDefinition alarmDefinition : alarmDefinitions) {
            enableAlarmDefinition(alarmDefinition);
        }
    }

    public static void disableAlarmDefinitions(List<Integer> ids) {
        List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll(ids);
        for (AlarmDefinition alarmDefinition : alarmDefinitions) {
            disableAlarmDefinition(alarmDefinition);
        }
    }
}
