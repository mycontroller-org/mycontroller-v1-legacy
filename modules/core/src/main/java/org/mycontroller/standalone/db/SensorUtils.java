/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;

import org.mycontroller.standalone.api.GoogleAnalyticsApi;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.units.Unit;
import org.mycontroller.standalone.units.UnitUtils;
import org.mycontroller.standalone.units.UnitUtils.UNIT_TYPE;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class SensorUtils {

    private SensorUtils() {

    }

    public static final String VARIABLE_TYPE_SPLITER = ", ";

    public static List<String> getVariableTypes(Sensor sensor) {
        List<String> variableTypes = new ArrayList<String>();
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensor.getId());
        if (sensorVariables != null) {
            for (SensorVariable sensorVariable : sensorVariables) {
                variableTypes.add(sensorVariable.getVariableType().getText());
            }
        }
        return variableTypes;
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

    public static String getValue(SensorVariable sensorVariable) {
        String data = null;
        try {
            switch (sensorVariable.getVariableType()) {
                case V_TEMP:
                case V_HUM:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_STATUS:
                    data = McUtils.getStatusAsString(sensorVariable.getValue());
                    break;
                case V_PERCENTAGE:
                case V_PRESSURE:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_FORECAST:
                    data = sensorVariable.getValue();
                    break;
                case V_RAIN:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_RAINRATE:
                    data = sensorVariable.getValue();
                    break;
                case V_WIND:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_GUST:
                case V_DIRECTION:
                    data = sensorVariable.getValue();
                    break;
                case V_UV:
                case V_WEIGHT:
                case V_DISTANCE:
                case V_IMPEDANCE:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_ARMED:
                    data = McUtils.getArmedAsString(sensorVariable.getValue());
                    break;
                case V_TRIPPED:
                    data = McUtils.getTrippedAsString(sensorVariable.getValue());
                    break;
                case V_WATT:
                case V_KWH:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_SCENE_ON:
                case V_SCENE_OFF:
                case V_HVAC_FLOW_STATE:
                case V_HVAC_SPEED:
                    data = sensorVariable.getValue();
                    break;
                case V_LIGHT_LEVEL:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_VAR1:
                case V_VAR2:
                case V_VAR3:
                case V_VAR4:
                case V_VAR5:
                    data = sensorVariable.getValue();
                    break;
                case V_UP:
                case V_DOWN:
                case V_STOP:
                case V_IR_SEND:
                case V_IR_RECEIVE:
                case V_FLOW:
                case V_VOLUME:
                    data = sensorVariable.getValue();
                    break;
                case V_LOCK_STATUS:
                    data = McUtils.getLockStatusAsString(sensorVariable.getValue());
                    break;
                case V_LEVEL:
                case V_VOLTAGE:
                case V_CURRENT:
                    data = McUtils.getDoubleAsString(sensorVariable.getValue());
                    break;
                case V_RGB:
                case V_RGBW:
                case V_ID:
                    data = sensorVariable.getValue();
                    break;
                case V_UNIT_PREFIX:
                    data = "";
                    break;
                case V_HVAC_SETPOINT_COOL:
                case V_HVAC_SETPOINT_HEAT:
                case V_HVAC_FLOW_MODE:
                case V_TEXT:
                    data = sensorVariable.getValue();
                    break;
                default:
                    data = null;
                    break;
            }
        } catch (NumberFormatException ex) {
            _logger.warn("Exception,", ex);
            data = null;
        }

        if (data == null || data.equals("-")) {
            return null;
        } else {
            if (sensorVariable.getUnitType() == UNIT_TYPE.U_NONE) {
                return data;
            } else if (sensorVariable.getMetricType() == METRIC_TYPE.DOUBLE) {
                Unit unit = UnitUtils.getUnit(sensorVariable.getUnitType());
                Double orgData = McUtils.getDouble(data);
                if (orgData == null) {
                    return null;
                }
                if (orgData < unit.getLimitLow()) {
                    return McUtils.getDoubleAsString(orgData * unit.getMtplLow()) + " " + unit.getUnitLow();
                } else if (orgData > unit.getLimitHigh()) {
                    return McUtils.getDoubleAsString(orgData * unit.getDivHigh()) + " " + unit.getUnitHigh();
                } else {
                    return data + " " + unit.getUnit();
                }
            } else {
                return data + " " + UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit();
            }
        }
    }

    public static String getLastSeen(Long timestamp) {
        if (timestamp != null) {
            return McUtils.getDifferenceFriendlyTime(timestamp);
        }
        return "Never";

    }

    public static void updateSensorVariables(Sensor sensor) {
        Sensor sensorOld = DaoUtils.getSensorDao().getById(sensor.getId());
        _logger.debug("Sensor Variables: Old Variables:[{}], New Variables:[{}]", sensorOld.getVariableTypes(),
                sensor.getVariableTypes());
        for (String newVariable : sensor.getVariableTypes()) {
            if (!sensorOld.getVariableTypes().contains(newVariable)) {
                _logger.debug("Creating entry for variable:{}", newVariable);
                //Create new entry
                DaoUtils.getSensorVariableDao().create(
                        SensorVariable.builder().sensor(sensor)
                                .variableType(MESSAGE_TYPE_SET_REQ.fromString(newVariable)).build()
                                .updateUnitAndMetricType());
                GoogleAnalyticsApi.instance().trackSensorVariableCreation(newVariable);
            }
        }
        //Remove left items
        if (sensorOld.getVariableTypes() != null && !sensorOld.getVariableTypes().isEmpty()) {
            for (String removeVariable : sensorOld.getVariableTypes()) {
                if (!sensor.getVariableTypes().contains(removeVariable)) {
                    _logger.debug("Removing entry for variable:{}", removeVariable);
                    DeleteResourceUtils.deleteSensorVariable(DaoUtils.getSensorVariableDao().get(sensor.getId(),
                            MESSAGE_TYPE_SET_REQ.fromString(removeVariable)));
                }
            }
        }
    }
}
