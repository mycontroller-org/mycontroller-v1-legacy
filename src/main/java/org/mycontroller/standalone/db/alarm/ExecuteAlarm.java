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
package org.mycontroller.standalone.db.alarm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.AlarmUtils;
import org.mycontroller.standalone.db.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.db.AlarmUtils.THRESHOLD_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.PayloadSpecialOperation;
import org.mycontroller.standalone.db.PayloadSpecialOperationUtils;
import org.mycontroller.standalone.db.PayloadSpecialOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.db.SensorLogUtils;
import org.mycontroller.standalone.db.tables.Alarm;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.mycontroller.standalone.email.EmailUtils;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.mycontroller.standalone.sms.SMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ExecuteAlarm implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(ExecuteAlarm.class);

    private List<Alarm> alarms;
    private SensorValue sensorValue;

    public ExecuteAlarm(List<Alarm> alarms, SensorValue sensorValue) {
        this.alarms = alarms;
        this.sensorValue = sensorValue;
    }

    public void runAlarm(Alarm alarm) throws Exception {
        boolean triggerAlarm = false;
        String thresholdValue = null;
        switch (THRESHOLD_TYPE.get(alarm.getThresholdType())) {
            case VALUE:
                thresholdValue = alarm.getThresholdValue();
                break;
            case SENSOR:
                SensorValue thresholdSensorValue = DaoUtils.getSensorValueDao().get(
                        NumericUtils.getInteger(alarm.getThresholdValue()));
                if (thresholdSensorValue != null) {
                    thresholdValue = thresholdSensorValue.getLastValue();
                }
                break;
            default:
                break;

        }
        if (thresholdValue == null) {
            _logger.warn("Could not execute this alarm, it does not have threshold value! Alarm:[{}]", alarm);
            return;
        }
        switch (AlarmUtils.TRIGGER.get(alarm.getTrigger())) {
            case EQUAL:
                if (sensorValue.getLastValue().equals(thresholdValue)) {
                    triggerAlarm = true;
                }
                break;
            case GREATER_THAN:
                if (Double.parseDouble(sensorValue.getLastValue()) > Double.parseDouble(thresholdValue)) {
                    triggerAlarm = true;
                }
                break;
            case GREATER_THAN_EQUAL:
                if (Double.parseDouble(sensorValue.getLastValue()) >= Double.parseDouble(thresholdValue)) {
                    triggerAlarm = true;
                }
                break;
            case LESSER_THAN:
                if (Double.parseDouble(sensorValue.getLastValue()) < Double.parseDouble(thresholdValue)) {
                    triggerAlarm = true;
                }
                break;
            case LESSER_THAN_EQUAL:
                if (Double.parseDouble(sensorValue.getLastValue()) <= Double.parseDouble(thresholdValue)) {
                    triggerAlarm = true;
                }
                break;
            case NOT_EQUAL:
                if (!sensorValue.getLastValue().equals(thresholdValue)) {
                    triggerAlarm = true;
                }
                break;
            default:
                break;
        }
        //Dampening Mode
        switch (DAMPENING_TYPE.get(alarm.getDampeningType())) {
            case NONE:
                alarmTriggerCall(alarm, triggerAlarm);
                break;
            case CONSECUTIVE:
                if (triggerAlarm) {
                    alarm.setOccurrenceCount(alarm.getOccurrenceCount() + 1);
                } else {
                    alarm.setOccurrenceCount(0);
                    alarmTriggerCall(alarm, triggerAlarm);
                }
                if (alarm.getOccurrenceCount() >= AlarmUtils.getDampeningConsecutive(alarm).getOccurrences()) {
                    alarm.setOccurrenceCount(0);
                    alarmTriggerCall(alarm, triggerAlarm);
                }
                break;
            case LAST_N_EVALUATIONS:
                DampeningLastNEvaluations nEvaluations = AlarmUtils.getDampeningLastNEvaluations(alarm);
                alarm.setEvaluationCount(alarm.getEvaluationCount() + 1);
                if (triggerAlarm) {
                    alarm.setOccurrenceCount(alarm.getOccurrenceCount() + 1);
                }
                if (alarm.getEvaluationCount() >= nEvaluations.getEvaluations()) {
                    if (alarm.getOccurrenceCount() >= nEvaluations.getOccurrences()) {
                        alarmTriggerCall(alarm, triggerAlarm);
                    } else {
                        alarmTriggerCall(alarm, false);
                    }
                    alarm.setEvaluationCount(0);
                    alarm.setOccurrenceCount(0);
                }
                break;
            default:
                break;
        }
        DaoUtils.getAlarmDao().update(alarm);

    }

    private void alarmTriggerCall(Alarm alarm, boolean triggerAlarm) throws Exception {
        if (triggerAlarm) {
            if (!alarm.getTriggered() || !alarm.getIgnoreDuplicate()) {
                switch (AlarmUtils.TYPE.get(alarm.getType())) {
                    case SEND_PAYLOAD:
                        alarmSendPayLoad(alarm);
                        break;
                    case SEND_EMAIL:
                        alarmSendEmail(alarm);
                        break;
                    case SEND_SMS:
                        alarmSendSMS(alarm);
                        break;
                    default:
                        _logger.info("Not implemented this type:{}", AlarmUtils.TYPE.get(alarm.getType()));
                        break;
                }
                alarm.setTriggered(true);
                //Alarm Triggered Message, Log message data
                SensorLogUtils.setAlarmLog(alarm, triggerAlarm, null);
                //Update Last Trigger Time
                alarm.setLastTrigger(new Date().getTime());
                DaoUtils.getAlarmDao().update(alarm);
            }
        } else {
            if (alarm.getTriggered()) {
                alarm.setTriggered(false);
                DaoUtils.getAlarmDao().update(alarm);
            }
        }
    }

    private void alarmSendPayLoad(Alarm alarm) {
        SendPayLoad sendPayLoad = AlarmUtils.getSendPayLoad(alarm);
        Sensor sensor = DaoUtils.getSensorDao().get(sendPayLoad.getSensorRefId());
        SensorValue sensorValuedestination = null;
        if (sensor != null) {
            sensorValuedestination = DaoUtils.getSensorValueDao().get(sensor.getId(), sendPayLoad.getVariableType());
        }
        PayloadSpecialOperation specialOperation = null;
        _logger.debug("Sesnor: ", sensor);
        if (sensor != null) {
            specialOperation = new PayloadSpecialOperation(sendPayLoad.getPayLoad());
            if (specialOperation.getOperationType() != null) {
                _logger.debug("Special Operation:[{}]", specialOperation);
                if (sensorValuedestination != null && sensorValuedestination.getLastValue() != null) {
                    sendPayLoad.setPayLoad(PayloadSpecialOperationUtils.getPayload(
                            specialOperation, sensorValuedestination.getLastValue()));
                } else {
                    _logger.warn("Cannot run Special Operations, there is no reference value on target sensor");
                    return;
                }
            } else {
                _logger.debug("Payload to be sent:{}", sendPayLoad.getPayLoad());
                if (sensorValuedestination != null && sensorValuedestination.getLastValue() != null) {
                    if (sendPayLoad.getPayLoad().equals(sensorValuedestination.getLastValue())) {
                        _logger.debug("Already destination with the same payload. Skipped...Destination[{}]",
                                sendPayLoad);
                        return;
                    }
                }
            }
        } else {
            _logger.warn("Target not available!");
            return;
        }

        RawMessage rawMessage;
        if (specialOperation.getOperationType() != null
                && specialOperation.getOperationType() == SEND_PAYLOAD_OPERATIONS.REBOOT) {
            rawMessage = new RawMessage(
                    sensor.getNode().getId(),
                    255,
                    MESSAGE_TYPE.C_INTERNAL.ordinal(), //messageType
                    0, //ack
                    MESSAGE_TYPE_INTERNAL.I_REBOOT.ordinal(),//subType
                    "",
                    true);// isTxMessage
        } else {
            rawMessage = new RawMessage(
                    sensor.getNode().getId(),
                    sensor.getSensorId(),
                    MESSAGE_TYPE.C_SET.ordinal(), //messageType
                    0, //ack
                    sendPayLoad.getVariableType(),//subType
                    sendPayLoad.getPayLoad(),
                    true);// isTxMessage
        }
        ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
    }

    private void alarmSendEmail(Alarm alarm) throws EmailException {
        StringBuilder builder = new StringBuilder();

        builder.append("Alarm: [").append(alarm.getName()).append("] triggered! Sensor:[")
                .append(alarm.getSensor().getNameWithNode()).append("]");
        String subject = builder.toString();

        builder.setLength(0);

        builder.append("<table border='0'>");

        builder.append("<tr>");
        builder.append("<td>").append("Alarm Name").append("</td>");
        builder.append("<td>").append(": ").append(alarm.getName()).append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Condition").append("</td>");
        builder.append("<td>").append(": ").append(AlarmUtils.getConditionString(alarm)).append(this.getSensorUnit(alarm, true))
                .append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Dampening").append("</td>");
        builder.append("<td>")
                .append(": ").append(alarm.getDampeningString()).append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Sensor").append("</td>");
        builder.append("<td>")
                .append(": Name:").append(alarm.getSensor().getNameWithNode()).append(", Id:[Node:")
                .append(alarm.getSensor().getNode().getId()).append(", Sensor:")
                .append(alarm.getSensor().getSensorId()).append("]").append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Sensor Value").append("</td>");
        builder.append("<td>")
                .append(": ").append(sensorValue.getLastValue()).append(this.getSensorUnit(alarm, false))
                .append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Triggered at").append("</td>");
        builder.append("<td>")
                .append(": ").append(new SimpleDateFormat(ObjectFactory.getAppProperties().getJavaDateFormat()).format(new Date()))
                .append("</td>");
        builder.append("<tr>");

        builder.append("</table>");

        String message = null;
        try {
            message = new String(Files.readAllBytes(Paths.get(AppProperties.EMAIL_TEMPLATE_ALARM)),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
            message = ex.getMessage();
        }

        EmailUtils.sendSimpleEmail(AlarmUtils.getSendEmail(alarm), subject,
                message.replaceAll(EmailUtils.ALARM_INFO, builder.toString()));
    }

    private void alarmSendSMS(Alarm alarm) throws Exception {
        StringBuilder builder = new StringBuilder();

        builder.append("Alarm: [")
                .append(alarm.getName())
                .append("], Cond: ").append(AlarmUtils.getConditionString(alarm))
                .append(this.getSensorUnit(alarm, true))
                .append(", Present Value:").append(sensorValue.getLastValue())
                .append(this.getSensorUnit(alarm, false))
                .append(", Node:[")
                .append(alarm.getSensor().getNameWithNode())
                .append("],id[N:").append(alarm.getSensor().getNode().getId()).append(",S:")
                .append(alarm.getSensor().getSensorId())
                .append(", T:").append(MESSAGE_TYPE_SET_REQ.get(sensorValue.getVariableType()).toString()).append("]")
                .append("\nwww.mycontroller.org");
        SMSUtils.sendSMS(AlarmUtils.getSendSMS(alarm).getToPhoneNumber(), builder.toString());
    }

    private String getSensorUnit(Alarm alarm, boolean isCondition) {
        String unit = "";
        if (isCondition) {
            if (alarm.getThresholdType() != THRESHOLD_TYPE.VALUE.ordinal()) {
                return unit;
            }
        }

        if (sensorValue.getUnit() != null && sensorValue.getUnit().length() > 0) {
            unit = " " + sensorValue.getUnit();
        }

        return unit;
    }

    @Override
    public void run() {
        for (Alarm alarm : this.alarms) {
            try {
                if (alarm.getEnabled()) {
                    runAlarm(alarm);
                } else {
                    _logger.debug("Alarm[{}] disabled, no action needed.");
                }
            } catch (Exception ex) {
                _logger.error("failed to execute alarm:[{}],", alarm, ex);
                SensorLogUtils.setAlarmLog(alarm, null, ex.getMessage());
            }
        }
    }

}
