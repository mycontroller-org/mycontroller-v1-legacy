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

import java.util.Date;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.AlarmUtils;
import org.mycontroller.standalone.db.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.db.AlarmUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.SensorLogUtils;
import org.mycontroller.standalone.db.tables.Alarm;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.email.EmailUtils;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.MyMessages;
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

    public ExecuteAlarm(List<Alarm> alarms) {
        this.alarms = alarms;
    }

    public void runAlarm(Alarm alarm) throws Exception {
        boolean triggerAlarm = false;
        Sensor sensor = DaoUtils.getSensorDao().get(alarm.getSensor().getId());
        switch (AlarmUtils.TRIGGER.get(alarm.getTrigger())) {
            case EQUAL:
                if (sensor.getLastValue().equals(alarm.getThresholdValue())) {
                    triggerAlarm = true;
                }
                break;
            case GREATER_THAN:
                if (Double.parseDouble(sensor.getLastValue()) > Double.parseDouble(alarm.getThresholdValue())) {
                    triggerAlarm = true;
                }
                break;
            case GREATER_THAN_EQUAL:
                if (Double.parseDouble(sensor.getLastValue()) >= Double.parseDouble(alarm.getThresholdValue())) {
                    triggerAlarm = true;
                }
                break;
            case LESSER_THAN:
                if (Double.parseDouble(sensor.getLastValue()) < Double.parseDouble(alarm.getThresholdValue())) {
                    triggerAlarm = true;
                }
                break;
            case LESSER_THAN_EQUAL:
                if (Double.parseDouble(sensor.getLastValue()) <= Double.parseDouble(alarm.getThresholdValue())) {
                    triggerAlarm = true;
                }
                break;
            case NOT_EQUAL:
                if (!sensor.getLastValue().equals(alarm.getThresholdValue())) {
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
        _logger.debug("Sesnor: ", sensor);
        boolean checkPreviosValue = false;
        if (sensor != null) {
            String modifiedPayLoad = sendPayLoad.getPayLoad();
            SEND_PAYLOAD_OPERATIONS OperationType = AlarmUtils.SEND_PAYLOAD_OPERATIONS.findByValue(sendPayLoad
                    .getPayLoad().toLowerCase());
            if (OperationType == null) {
                checkPreviosValue = true;
                switch (MyMessages.getPayLoadType(MESSAGE_TYPE_SET_REQ.get(sensor.getMessageType()))) {
                    case PL_BOOLEAN:
                        modifiedPayLoad = sendPayLoad.getPayLoad().trim().equalsIgnoreCase("1") ? "ON" : "OFF";
                        break;
                    case PL_DOUBLE:
                        modifiedPayLoad = String.valueOf(NumericUtils.round(
                                Double.valueOf(sendPayLoad.getPayLoad()),
                                NumericUtils.DOUBLE_ROUND));
                        break;
                    default:
                        break;
                }
            } else {
                switch (OperationType) {
                    case INVERT:
                        sendPayLoad.setPayLoad(String.valueOf(Double.valueOf(sensor.getLastValue()) > 0 ? 0 : 1));
                        break;
                    case INCREMENT:
                        sendPayLoad.setPayLoad(String.valueOf(Double.valueOf(sensor.getLastValue()) + 1));
                        break;
                    case DECREMENT:
                        sendPayLoad.setPayLoad(String.valueOf(Double.valueOf(sensor.getLastValue()) - 1));
                        break;
                    default:
                        _logger.warn("Selected Operation not implemented:[{}]", OperationType.value());
                        break;
                }
            }

            _logger.debug("Original Payload:{}, Modified Payload:{}", sendPayLoad.getPayLoad(), modifiedPayLoad);
            if (modifiedPayLoad.equals(sensor.getStatus()) && checkPreviosValue) {
                _logger.debug("Already destination with the same payload. Skipped...Destination[{}]", sendPayLoad);
                return;
            }
        }
        RawMessage rawMessage = new RawMessage(
                sensor.getNode().getId(),
                sensor.getSensorId(),
                MESSAGE_TYPE.C_SET.ordinal(), //messageType
                0, //ack
                sensor.getMessageType(),//subType
                sendPayLoad.getPayLoad(),
                true);// isTxMessage
        ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
    }

    private void alarmSendEmail(Alarm alarm) throws EmailException {
        StringBuilder builder = new StringBuilder();

        builder.append("Alarm: [").append(alarm.getName()).append("] triggered! Node:[")
                .append(alarm.getSensor().getNameWithNode()).append("]");
        String subject = builder.toString();

        builder.setLength(0);

        String unit = " ";
        if (alarm.getSensor().getUnit() != null && alarm.getSensor().getUnit().length() > 0) {
            unit += alarm.getSensor().getUnit();
        }

        builder.append("Dear User,\nThere is an alarm triggered for you!\n")
                .append(String.format("\n\t%-30s%-2s", "Alarm Name", ":")).append(alarm.getName())
                .append(String.format("\n\t%-30s%-2s", "Condition", ":")).append("if {Sensor Value} ")
                .append(alarm.getTriggerString())
                .append(" ").append(alarm.getThresholdValue()).append(unit)
                .append(String.format("\n\t%-30s%-2s", "Sensor", ":")).append(alarm.getSensor().getNameWithNode())
                .append(String.format("\n\t%-30s%-2s", "Id", ":")).append("Node Id:")
                .append(alarm.getSensor().getNode().getId())
                .append(", Sensor Id:")
                .append(alarm.getSensor().getSensorId())
                .append(String.format("\n\t%-30s%-2s", "Sensor Present Value", ":"))
                .append(alarm.getSensor().getLastValue())
                .append(unit)
                .append("\n\n\n-- Powered by www.mycontroller.org");
        EmailUtils.sendSimpleEmail(AlarmUtils.getSendEmail(alarm), subject, builder.toString());
    }

    private void alarmSendSMS(Alarm alarm) throws Exception {
        StringBuilder builder = new StringBuilder();
        String unit = "";
        if (alarm.getSensor().getUnit() != null && alarm.getSensor().getUnit().length() > 0) {
            unit = " " + alarm.getSensor().getUnit();
        }
        builder.append("Alarm: [")
                .append(alarm.getName())
                .append("], Cond: if VAL ")
                .append(alarm.getTriggerString())
                .append(" ").append(alarm.getThresholdValue()).append(unit)
                .append(", Present Value:").append(alarm.getSensor().getLastValue()).append(unit)
                .append(", Node:[")
                .append(alarm.getSensor().getNameWithNode())
                .append("],id[N:").append(alarm.getSensor().getNode().getId()).append(",S:")
                .append(alarm.getSensor().getSensorId()).append("]")
                .append("\nwww.mycontroller.org");
        SMSUtils.sendSMS(AlarmUtils.getSendSMS(alarm).getToPhoneNumber(), builder.toString());
    }

    @Override
    public void run() {
        for (Alarm alarm : this.alarms) {
            try {
                runAlarm(alarm);
            } catch (Exception ex) {
                SensorLogUtils.setAlarmLog(alarm, null, ex.getMessage());
                _logger.error("failed to execute alarm,", ex);
            }
        }
    }

}
