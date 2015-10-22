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
package org.mycontroller.standalone.db;

import org.mycontroller.standalone.db.tables.Alarm;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorLog;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.mysensors.RawMessage;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SensorLogUtils {
    private SensorLogUtils() {

    }

    public enum LOG_TYPE {
        ALARM("Alarm"),
        TIMER("Timer"),
        SENSOR("Sensor"),
        SENSOR_INTERNAL("Sensor Internal"),
        SENSOR_PRESENTATION("Sensor Presentation"),
        SENSOR_STREAM("Sensor Stream");
        public static LOG_TYPE get(int id) {
            for (LOG_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private LOG_TYPE(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public static void setAlarmLog(Alarm alarm, Boolean triggered, String errorMsg) {
        SensorLog sensorLog = new SensorLog(
                alarm.getSensor().getId(),
                System.currentTimeMillis(),
                LOG_TYPE.ALARM.ordinal());
        sensorLog.setSensor(alarm.getSensor());
        StringBuffer buffer = new StringBuffer();
        if (triggered == null) {
            buffer.append("Name: ").append(alarm.getName())
                    .append(", Condition: {Value} ").append(alarm.getTriggerString())
                    .append(alarm.getThresholdValue())
                    .append(", Notification: ").append(alarm.getNotificationString())
                    .append(", Error: ").append(errorMsg);
        } else if (triggered) {
            buffer.append("Triggered: Name: ").append(alarm.getName())
                    .append(", Condition: {Value} ").append(alarm.getTriggerString())
                    .append(alarm.getThresholdValue())
                    .append(", Notification: ").append(alarm.getNotificationString());
        } else {
            buffer.append("Failed: Name: ").append(alarm.getName())
                    .append(", Condition: {Value} ").append(alarm.getTriggerString())
                    .append(alarm.getThresholdValue())
                    .append(", Notification: ").append(alarm.getNotificationString())
                    .append(", Error: ").append(errorMsg);
        }
        sensorLog.setLog(buffer.toString());
        DaoUtils.getSensorLogDao().add(sensorLog);
    }

    public static void setTimerLog(Timer timer, String errorMsg) {
        SensorLog sensorLog = new SensorLog(
                timer.getSensor().getId(),
                System.currentTimeMillis(),
                LOG_TYPE.TIMER.ordinal());
        sensorLog.setSensor(timer.getSensor());
        StringBuffer buffer = new StringBuffer();

        if (errorMsg != null) {
            buffer.append("Failed: ").append(timer.toString())
                    .append(", Error: ").append(errorMsg);
        } else {
            buffer.append("Fired: ").append(timer.toString());
        }

        sensorLog.setLog(buffer.toString());
        DaoUtils.getSensorLogDao().add(sensorLog);
    }

    public static void setSensorData(Sensor sensor, Boolean dataSent, SensorValue sensorValue, String errorMsg) {
        SensorLog sensorLog = new SensorLog(
                sensor.getId(),
                System.currentTimeMillis(),
                LOG_TYPE.SENSOR.ordinal(),
                dataSent);
        sensorLog.setSensor(sensor);

        StringBuffer buffer = new StringBuffer();

        if (sensor.getNameWithNode() != null) {
            buffer.append(sensor.getNameWithNode()).append(", ");
        }
        buffer.append("[NodeId:").append(sensor.getNode().getId())
                .append(", SensorId:").append(sensor.getSensorId())
                .append(", Type:").append(MESSAGE_TYPE_SET_REQ.get(sensorValue.getVariableType()))
                .append(", PayLoad:").append(sensorValue.getLastValue()).append("]");
        if (errorMsg != null) {
            buffer.append(", Error:").append(errorMsg);
        }
        sensorLog.setLog(buffer.toString());
        DaoUtils.getSensorLogDao().add(sensorLog);
    }

    public static void setSensorOtherData(LOG_TYPE logType, String subType, String extraInfo,
            RawMessage rawMessage, String errorMsg) {
        Sensor sensor = DaoUtils.getSensorDao().get(rawMessage.getNodeId(), rawMessage.getChildSensorId());
        SensorLog sensorLog = new SensorLog(
                sensor != null ? sensor.getId() : null,
                System.currentTimeMillis(),
                logType.ordinal(),
                rawMessage.isTxMessage());
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(subType.toString());
        buffer.append("]");
        buffer.append(", [NodeId:").append(rawMessage.getNodeId()).append(", SensorId:")
                .append(rawMessage.getChildSensorId())
                .append(", PayLoad: ").append(rawMessage.getPayload()).append("]");
        if (extraInfo != null) {
            buffer.append(", ").append(extraInfo);
        }
        if (errorMsg != null) {
            buffer.append(", Error: ").append(errorMsg);

        }

        sensorLog.setLog(buffer.toString());
        DaoUtils.getSensorLogDao().add(sensorLog);
    }

    public static void setSensorOtherData(LOG_TYPE logType, String subType, RawMessage rawMessage, String errorMsg) {
        setSensorOtherData(logType, subType, null, rawMessage, errorMsg);
    }

    public static void setSensorInternalData(MESSAGE_TYPE_INTERNAL typeInternal, RawMessage rawMessage, String errorMsg) {
        setSensorOtherData(LOG_TYPE.SENSOR_INTERNAL, typeInternal.toString(), rawMessage, errorMsg);
    }

    public static void setSensorPresentationData(MESSAGE_TYPE_PRESENTATION typePresentation, RawMessage rawMessage,
            String errorMsg) {
        setSensorOtherData(LOG_TYPE.SENSOR_PRESENTATION, typePresentation.toString(), rawMessage, errorMsg);
    }

    public static void setSensorStreamData(MESSAGE_TYPE_STREAM typeStream, RawMessage rawMessage, String errorMsg) {
        setSensorOtherData(LOG_TYPE.SENSOR_STREAM, typeStream.toString(), rawMessage, errorMsg);
    }

    public static void setSensorStreamData(MESSAGE_TYPE_STREAM typeStream, String extraInfo, RawMessage rawMessage,
            String errorMsg) {
        setSensorOtherData(LOG_TYPE.SENSOR_STREAM, typeStream.toString(), extraInfo, rawMessage, errorMsg);
    }

}
