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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.api.jaxrs.mapper.KeyValueJson;
import org.mycontroller.standalone.api.jaxrs.mapper.KeyValueJson.TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.SensorStatus;
import org.mycontroller.standalone.api.jaxrs.mapper.SensorsGuiButton;
import org.mycontroller.standalone.db.TypeUtils.METRIC_TYPE;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class SensorUtils {
    private static final Logger _logger = LoggerFactory.getLogger(SensorUtils.class);

    private SensorUtils() {

    }

    public static final String VARIABLE_TYPE_SPLITER = ", ";

    public static String getVariableTypes(Sensor sensor) {
        StringBuilder builder = new StringBuilder();
        List<SensorValue> sensorValues = DaoUtils.getSensorValueDao().getAll(sensor.getId());
        if (sensorValues != null) {
            for (SensorValue sensorValue : sensorValues) {
                if (builder.length() != 0) {
                    builder.append(VARIABLE_TYPE_SPLITER);
                }
                builder.append(MESSAGE_TYPE_SET_REQ.get(sensorValue.getVariableType()));
            }
        }
        return builder.toString();
    }

    public static List<SensorStatus> getStatus(Sensor sensor) {
        ArrayList<SensorStatus> statusList = new ArrayList<SensorStatus>();
        List<SensorValue> sensorValues = DaoUtils.getSensorValueDao().getAll(sensor.getId());
        for (SensorValue sensorValue : sensorValues) {
            SensorStatus sensorStatus = new SensorStatus(sensorValue.getVariableTypeString(),
                    sensorValue.getLastValue(), sensorValue.getUnit(), getValue(sensorValue),
                    sensorValue.getTimestamp());
            statusList.add(sensorStatus);
        }
        return statusList;
    }

    public static SensorsGuiButton getGuiButtonsStatus(Sensor sensor) {
        List<SensorValue> sensorValues = DaoUtils.getSensorValueDao().getAll(sensor.getId());
        SensorsGuiButton guiButton = new SensorsGuiButton();
        for (SensorValue sensorValue : sensorValues) {
            setGuiButton(sensorValue, guiButton);
        }
        return guiButton;
    }

    public static void setGuiButton(SensorValue sensorValue, SensorsGuiButton guiButton) {
        //Update Graphical Report Status
        if (sensorValue.getMetricType() != METRIC_TYPE.NONE.ordinal()) {
            guiButton.getGraph().setShow(true);
        }
        switch (MESSAGE_TYPE_SET_REQ.get(sensorValue.getVariableType())) {
            case V_TEMP:
            case V_HUM:
                break;
            case V_STATUS:
                guiButton.getOnOff().setShow(true);
                guiButton.getOnOff().setValue(sensorValue.getLastValue() == null ? "0" : sensorValue.getLastValue());
                break;
            case V_PERCENTAGE:
                guiButton.getIncreaseDecrease().setShow(true);
                guiButton.getIncreaseDecrease().setValue(sensorValue.getLastValue());
                guiButton.getIncreaseDecrease().setVariableType(MESSAGE_TYPE_SET_REQ.V_PERCENTAGE.ordinal());
                break;
            case V_PRESSURE:
            case V_FORECAST:
            case V_RAIN:
            case V_RAINRATE:
            case V_WIND:
            case V_GUST:
            case V_DIRECTION:
            case V_UV:
            case V_WEIGHT:
            case V_DISTANCE:
            case V_IMPEDANCE:
                break;
            case V_ARMED:
                guiButton.getArmed().setShow(true);
                guiButton.getArmed().setValue(sensorValue.getLastValue() == null ? "0" : sensorValue.getLastValue());
                break;
            case V_TRIPPED:
                guiButton.getTripped().setShow(true);
                guiButton.getTripped().setValue(sensorValue.getLastValue() == null ? "0" : sensorValue.getLastValue());
                break;
            case V_WATT:
            case V_KWH:
            case V_SCENE_ON:
            case V_SCENE_OFF:
                break;
            case V_HVAC_FLOW_STATE:
                guiButton.getHvacFlowState().setShow(true);
                guiButton.getHvacFlowState().setValue(sensorValue.getLastValue());
                break;
            case V_HVAC_SPEED:
                guiButton.getHvacSpeed().setShow(true);
                guiButton.getHvacSpeed().setValue(sensorValue.getLastValue());
                break;
            case V_LIGHT_LEVEL:
                guiButton.getIncreaseDecrease().setShow(true);
                guiButton.getIncreaseDecrease().setValue(sensorValue.getLastValue());
                guiButton.getIncreaseDecrease().setVariableType(MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL.ordinal());
                break;
            case V_VAR1:
            case V_VAR2:
            case V_VAR3:
            case V_VAR4:
            case V_VAR5:
                break;
            case V_UP:
            case V_DOWN:
            case V_STOP:
                guiButton.getCover().setShow(true);
                guiButton.getCover().setValue(sensorValue.getLastValue());
                break;
            case V_IR_SEND:
            case V_IR_RECEIVE:
            case V_FLOW:
            case V_VOLUME:
                break;
            case V_LOCK_STATUS:
                guiButton.getLockStatus().setShow(true);
                guiButton.getLockStatus().setValue(sensorValue.getLastValue());
                break;
            case V_LEVEL:
                guiButton.getIncreaseDecrease().setShow(true);
                guiButton.getIncreaseDecrease().setValue(sensorValue.getLastValue());
                guiButton.getIncreaseDecrease().setVariableType(MESSAGE_TYPE_SET_REQ.V_LEVEL.ordinal());
                break;
            case V_VOLTAGE:
            case V_CURRENT:
                break;
            case V_RGB:
                guiButton.getRgb().setShow(true);
                guiButton.getRgb().setValue(
                        sensorValue.getLastValue() != null ? "#" + sensorValue.getLastValue() : null);
                guiButton.getRgb().setVariableType(MESSAGE_TYPE_SET_REQ.V_RGB.ordinal());
                break;
            case V_RGBW:
                guiButton.getRgbw().setShow(true);
                guiButton.getRgbw().setValue(getRgbaFromHex(sensorValue.getLastValue()));
                guiButton.getRgbw().setVariableType(MESSAGE_TYPE_SET_REQ.V_RGBW.ordinal());
                break;
            case V_ID:
            case V_UNIT_PREFIX:
            case V_HVAC_SETPOINT_COOL:
            case V_HVAC_SETPOINT_HEAT:
                break;
            case V_HVAC_FLOW_MODE:
                guiButton.getHvacFlowMode().setShow(true);
                guiButton.getHvacFlowMode().setValue(sensorValue.getLastValue());
                break;
            case V_TEXT:
                break;
            default:
                break;
        }
    }

    public static String getHexFromRgba(String rgba) {
        if (rgba == null) {
            return null;
        }
        //rgba(120,24,24,0.31)
        String[] value = rgba.replace("rgba(", "").replace(")", "").split(",");
        if (value.length == 4) {
            int white = (int) ((1.0 - Double.valueOf(value[3])) * 255.0); // a-b=x, x*255=yy
            return String.format("%02x%02x%02x%02x",
                    Integer.valueOf(value[0]),
                    Integer.valueOf(value[1]),
                    Integer.valueOf(value[2]),
                    white);
        } else {
            //Throw Exception
            return null;
        }
    }

    public static String getRgbaFromHex(String hex) {
        if (hex == null) {
            return null;
        }
        //RRGGBBWW
        int red = Integer.valueOf(hex.substring(0, 2), 16);
        int blue = Integer.valueOf(hex.substring(2, 4), 16);
        int green = Integer.valueOf(hex.substring(4, 6), 16);
        double white = 1.0 - (Integer.valueOf(hex.substring(6, 8), 16) / 255.0);// x=yy/255, b=a-x
        StringBuilder builder = new StringBuilder();
        builder.append("rgba(")
                .append(red).append(",")
                .append(blue).append(",")
                .append(green).append(",")
                .append(new DecimalFormat("#.#").format(white))
                .append(")");
        return builder.toString();
    }

    public static String getValue(SensorValue sensorValue) {
        String data = null;
        switch (MESSAGE_TYPE_SET_REQ.get(sensorValue.getVariableType())) {
            case V_TEMP:
            case V_HUM:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_STATUS:
                data = NumericUtils.getStatusAsString(sensorValue.getLastValue());
                break;
            case V_PERCENTAGE:
            case V_PRESSURE:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_FORECAST:
                data = sensorValue.getLastValue();
                break;
            case V_RAIN:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_RAINRATE:
                data = sensorValue.getLastValue();
                break;
            case V_WIND:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_GUST:
            case V_DIRECTION:
                data = sensorValue.getLastValue();
                break;
            case V_UV:
            case V_WEIGHT:
            case V_DISTANCE:
            case V_IMPEDANCE:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_ARMED:
                data = NumericUtils.getArmedAsString(sensorValue.getLastValue());
                break;
            case V_TRIPPED:
                data = NumericUtils.getTrippedAsString(sensorValue.getLastValue());
                break;
            case V_WATT:
            case V_KWH:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_SCENE_ON:
            case V_SCENE_OFF:
            case V_HVAC_FLOW_STATE:
            case V_HVAC_SPEED:
                data = sensorValue.getLastValue();
                break;
            case V_LIGHT_LEVEL:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_VAR1:
            case V_VAR2:
            case V_VAR3:
            case V_VAR4:
            case V_VAR5:
                data = sensorValue.getLastValue();
                break;
            case V_UP:
            case V_DOWN:
            case V_STOP:
            case V_IR_SEND:
            case V_IR_RECEIVE:
            case V_FLOW:
            case V_VOLUME:
                data = sensorValue.getLastValue();
                break;
            case V_LOCK_STATUS:
                data = NumericUtils.getLockStatusAsString(sensorValue.getLastValue());
                break;
            case V_LEVEL:
            case V_VOLTAGE:
            case V_CURRENT:
                data = NumericUtils.getDoubleAsString(sensorValue.getLastValue());
                break;
            case V_RGB:
            case V_RGBW:
            case V_ID:
                data = sensorValue.getLastValue();
                break;
            case V_UNIT_PREFIX:
                data = "";
                break;
            case V_HVAC_SETPOINT_COOL:
            case V_HVAC_SETPOINT_HEAT:
            case V_HVAC_FLOW_MODE:
            case V_TEXT:
                data = sensorValue.getLastValue();
                break;
            default:
                data = null;
                break;
        }
        if (data == null) {
            return null;
        } else {
            return data + " " + sensorValue.getUnit();
        }
    }

    public static String getUnitString(Integer variableType) {
        Settings settings = DaoUtils.getSettingsDao().get(
                Settings.DEFAULT_UNIT + MESSAGE_TYPE_SET_REQ.get(variableType).toString());
        if (settings != null && settings.getValue() != null) {
            return settings.getValue();
        }
        return null;
    }

    public static String getLastSeen(Long timestamp) {
        if (timestamp != null) {
            return NumericUtils.getDifferenceFriendlyTime(timestamp);
        }
        return "Never";

    }

    public static void updateSensorValues(Sensor sensor) {
        Sensor sensorOld = DaoUtils.getSensorDao().get(sensor.getId());
        List<String> newVariables = Arrays.asList(sensor.getVariableTypes().split(VARIABLE_TYPE_SPLITER));
        List<String> oldVariables = Arrays.asList(sensorOld.getVariableTypes().split(VARIABLE_TYPE_SPLITER));
        _logger.debug("Sensor Variables: Old Variables:[{}], New Variables:[{}]", oldVariables, newVariables);
        for (String newVariable : newVariables) {
            if (!oldVariables.contains(newVariable)) {
                _logger.debug("Creating entry for variable:{}", newVariable);
                //Create new entry
                DaoUtils.getSensorValueDao().create(
                        new SensorValue(sensor, MESSAGE_TYPE_SET_REQ.valueOf(newVariable.trim()).ordinal()));
            }
        }
        //Remove left items
        if (oldVariables != null && !oldVariables.isEmpty()) {
            for (String removeVariable : oldVariables) {
                if (!newVariables.contains(removeVariable)) {
                    _logger.debug("Removing entry for variable:{}", removeVariable);
                    DeleteResourceUtils.deleteSensorValue(DaoUtils.getSensorValueDao().get(sensor.getId(),
                            MESSAGE_TYPE_SET_REQ.valueOf(removeVariable.trim()).ordinal()));
                }
            }
        }
    }

    public static boolean getSendPayloadEnabled(Sensor sensor) {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.ENABLE_SEND_PAYLOAD);
        if (settings != null && settings.getValue().equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    public static void updateOthers(Integer sensorRefId, List<KeyValueJson> keyValues) {

        for (KeyValueJson keyValue : keyValues) {
            if (keyValue.getType() == TYPE.UNIT) {
                SensorValue sensorValue = DaoUtils.getSensorValueDao().get(sensorRefId,
                        MESSAGE_TYPE_SET_REQ.valueOf(keyValue.getKey()).ordinal());
                sensorValue.setUnit(keyValue.getValue() != null && keyValue.getValue().trim().length() > 0 ?
                        keyValue.getValue() : "");
                DaoUtils.getSensorValueDao().update(sensorValue);
            } else if (keyValue.getType() == TYPE.ENABLE_PAYLOAD) {
                Sensor sensor = DaoUtils.getSensorDao().get(sensorRefId);
                sensor.setEnableSendPayload(keyValue.getValue() != null && keyValue.getValue().length() > 0 ?
                        keyValue.getValue().equalsIgnoreCase("true") : null);
                DaoUtils.getSensorDao().updateWithEnableSendPayload(sensor);
            }
        }
    }

    public static List<KeyValueJson> getOthers(Integer sensorRefId) {
        List<KeyValueJson> keyValues = new ArrayList<KeyValueJson>();
        //Add sensor Units
        List<SensorValue> sensorValues = DaoUtils.getSensorValueDao().getAll(sensorRefId);
        for (SensorValue sensorValue : sensorValues) {
            keyValues.add(new KeyValueJson(
                    MESSAGE_TYPE_SET_REQ.get(sensorValue.getVariableType()).toString(),
                    sensorValue.getUnit(),
                    KeyValueJson.TYPE.UNIT));
        }
        //Add Enable send payload
        Sensor sensor = DaoUtils.getSensorDao().get(sensorRefId);
        keyValues.add(new KeyValueJson(
                "Enable Payload",
                sensor.getEnableSendPayloadRaw() != null ? String.valueOf(sensor.getEnableSendPayloadRaw()) : null,
                KeyValueJson.TYPE.ENABLE_PAYLOAD));
        return keyValues;
    }
}
