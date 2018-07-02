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
package org.mycontroller.standalone.units;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class UnitUtils {
    public static HashMap<UNIT_TYPE, Unit> unitsMetric = new HashMap<>();
    public static HashMap<UNIT_TYPE, Unit> unitsImperial = new HashMap<>();
    static {
        //Update units Metric
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_CURRENT, "A", "mA", "A", 1.0, 1000.0, 1000.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_DIRECTION, "°", "°", "°", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_DISTANCE, "cm", "mm", "m", 1.0, 100.0, 10.0, 100.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_EC, "μS/cm", "μS/cm", "μS/cm", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_FLOW, "m", "m", "m", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_GUST, "km/h", "km/h", "km/h", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_HUMIDITY, "%", "%", "%", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_IMPEDANCE, "Ω", "Ω", "KΩ", 1.0, 1000.0, 1.0, 1000.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_KWH, "kWh", "kWh", "kWh", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_LIGHT_LEVEL, "%", "%", "%", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_ORP, "mV", "mV", "mV", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_PERCENTAGE, "%", "%", "%", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_PH, "PH", "PH", "PH", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_POWER_FACTOR, "PF", "PF", "PF", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_PRESSURE, "hPa", "hPa", "hPa", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_PRESSURE_BARO, "inHg", "inHg", "inHg", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_RAIN, "mm", "mm", "cm", 1.0, 10.0, 1.0, 10.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_RAINRATE, "mm/hr", "mm/hr", "cm/hr", 1.0, 10.0, 1.0, 10.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_TEMPERATURE, "°C", "°C", "°C", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_UV, "mj/cm2", "mj/cm2", "mj/cm2", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_VA, "VA", "VA", "VA", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_VAR, "VAR", "VAR", "VAR", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_VOLTAGE, "V", "mV", "V", 1.0, 1.0, 1000.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_WATT, "W", "mW", "W", 1.0, 1.0, 1000.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_WEIGHT, "kg", "g", "kg", 1.0, 1.0, 1000.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_WIND, "km/h", "km/h", "km/h", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsMetric, Unit.get(UNIT_TYPE.U_NONE, "", "", "", 1.0, 1.0, 1.0, 1.0));

        //Update units Imperial
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_CURRENT, "A", "mA", "A", 1.0, 1000.0, 1000.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_DIRECTION, "°", "°", "°", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_DISTANCE, "cm", "mm", "m", 1.0, 100.0, 10.0, 100.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_EC, "μS/cm", "μS/cm", "μS/cm", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_FLOW, "m", "m", "m", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_GUST, "mph", "mph", "mph", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_HUMIDITY, "%", "%", "%", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_IMPEDANCE, "Ω", "Ω", "KΩ", 1.0, 1000.0, 1.0, 1000.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_KWH, "kWh", "kWh", "kWh", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_LIGHT_LEVEL, "%", "%", "%", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_ORP, "mV", "mV", "mV", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_PERCENTAGE, "%", "%", "%", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_PH, "PH", "PH", "PH", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_POWER_FACTOR, "PF", "PF", "PF", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_PRESSURE, "psi", "psi", "psi", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_PRESSURE_BARO, "inHg", "inHg", "inHg", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_RAIN, "mm", "mm", "cm", 1.0, 10.0, 1.0, 10.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_RAINRATE, "mm/hr", "mm/hr", "cm/hr", 1.0, 10.0, 1.0, 10.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_TEMPERATURE, "°F", "°F", "°F", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_UV, "mj/cm2", "mj/cm2", "mj/cm2", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_VA, "VA", "VA", "VA", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_VAR, "VAR", "VAR", "VAR", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_VOLTAGE, "V", "mV", "V", 1.0, 1.0, 1000.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_WATT, "W", "mW", "W", 1.0, 1.0, 1000.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_WEIGHT, "kg", "g", "kg", 1.0, 1.0, 1000.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_WIND, "mph", "mph", "mph", 1.0, 1.0, 1.0, 1.0));
        addUnit(unitsImperial, Unit.get(UNIT_TYPE.U_NONE, "", "", "", 1.0, 1.0, 1.0, 1.0));
    }

    private static void addUnit(HashMap<UNIT_TYPE, Unit> unitsMap, Unit unit) {
        unitsMap.put(unit.getType(), unit);
    }

    public enum UNIT_TYPE {
        U_TEMPERATURE("Temperature"),
        U_HUMIDITY("Humidity"),
        U_PERCENTAGE("Percentage"),
        U_PRESSURE("Pressure"),
        U_PRESSURE_BARO("Pressure baro"),
        U_RAIN("Rain"),
        U_RAINRATE("Rainrate"),
        U_WIND("Wind"),
        U_GUST("Gust"),
        U_DIRECTION("Direction"),
        U_UV("UV"),
        U_WEIGHT("Weight"),
        U_DISTANCE("Distance"),
        U_IMPEDANCE("Impedance"),
        U_WATT("Watt"),
        U_KWH("kWh"),
        U_LIGHT_LEVEL("Light level"),
        U_FLOW("Flow"),
        U_VOLTAGE("Voltage"),
        U_CURRENT("Current"),
        U_PH("PH"),
        U_ORP("ORP"),
        U_EC("EC"),
        U_VAR("VAR"),
        U_VA("VA"),
        U_POWER_FACTOR("Power factor"),
        U_NONE("None");

        private final String type;

        private UNIT_TYPE(String type) {
            this.type = type;
        }

        public String getText() {
            return this.type;
        }

        public static UNIT_TYPE get(int id) {
            for (UNIT_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static UNIT_TYPE fromString(String text) {
            if (text != null) {
                for (UNIT_TYPE type : UNIT_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static Unit getUnit(UNIT_TYPE type) {
        if (AppProperties.getInstance().getControllerSettings().getUnitConfig().equals(UNIT_CONFIG.METRIC.getText())) {
            return unitsMetric.get(type);
        } else {
            return unitsImperial.get(type);
        }
    }

    public static UNIT_TYPE getUnit(MESSAGE_TYPE_SET_REQ mType) {
        switch (mType) {
            case V_TEMP:
            case V_HVAC_SETPOINT_COOL:
            case V_HVAC_SETPOINT_HEAT:
                return UNIT_TYPE.U_TEMPERATURE;
            case V_HUM:
                return UNIT_TYPE.U_HUMIDITY;
            case V_PERCENTAGE:
                return UNIT_TYPE.U_PERCENTAGE;
            case V_PRESSURE:
                return UNIT_TYPE.U_PRESSURE;
            case V_RAIN:
                return UNIT_TYPE.U_RAIN;
            case V_RAINRATE:
                return UNIT_TYPE.U_RAINRATE;
            case V_WIND:
                return UNIT_TYPE.U_WIND;
            case V_GUST:
                return UNIT_TYPE.U_GUST;
            case V_DIRECTION:
                return UNIT_TYPE.U_DIRECTION;
            case V_UV:
                return UNIT_TYPE.U_UV;
            case V_WEIGHT:
                return UNIT_TYPE.U_WEIGHT;
            case V_DISTANCE:
                return UNIT_TYPE.U_DISTANCE;
            case V_IMPEDANCE:
                return UNIT_TYPE.U_IMPEDANCE;
            case V_WATT:
                return UNIT_TYPE.U_WATT;
            case V_KWH:
                return UNIT_TYPE.U_KWH;
            case V_LIGHT_LEVEL:
                return UNIT_TYPE.U_LIGHT_LEVEL;
            case V_FLOW:
                return UNIT_TYPE.U_FLOW;
            case V_VOLTAGE:
                return UNIT_TYPE.U_VOLTAGE;
            case V_CURRENT:
                return UNIT_TYPE.U_CURRENT;
            default:
                return UNIT_TYPE.U_NONE;
        }
    }
}
