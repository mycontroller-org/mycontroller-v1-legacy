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
package org.mycontroller.standalone.mysensors;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MyMessages {
    // Message types
    public enum MESSAGE_TYPE {
        C_PRESENTATION,
        C_SET,
        C_REQ,
        C_INTERNAL,
        C_STREAM; // For Firmware and other larger chunks of data that need to be divided into pieces 
        public static MESSAGE_TYPE get(int id) {
            for (MESSAGE_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    // Type of sensor data (for set/req/ack messages)
    public enum MESSAGE_TYPE_SET_REQ {
        V_TEMP, V_HUM, V_STATUS, V_PERCENTAGE, V_PRESSURE, V_FORECAST, V_RAIN, V_RAINRATE,
        V_WIND, V_GUST, V_DIRECTION, V_UV, V_WEIGHT, V_DISTANCE, V_IMPEDANCE, V_ARMED,
        V_TRIPPED, V_WATT, V_KWH, V_SCENE_ON, V_SCENE_OFF, V_HVAC_FLOW_STATE, V_HVAC_SPEED,
        V_LIGHT_LEVEL, V_VAR1, V_VAR2, V_VAR3, V_VAR4, V_VAR5, V_UP, V_DOWN, V_STOP,
        V_IR_SEND, V_IR_RECEIVE, V_FLOW, V_VOLUME, V_LOCK_STATUS, V_LEVEL,
        V_VOLTAGE, V_CURRENT, V_RGB, V_RGBW, V_ID, V_UNIT_PREFIX,
        V_HVAC_SETPOINT_COOL, V_HVAC_SETPOINT_HEAT, V_HVAC_FLOW_MODE;
        public static MESSAGE_TYPE_SET_REQ get(int id) {
            for (MESSAGE_TYPE_SET_REQ type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    // Type of internal messages (for internal messages)
    public enum MESSAGE_TYPE_INTERNAL {
        I_BATTERY_LEVEL, I_TIME, I_VERSION, I_ID_REQUEST, I_ID_RESPONSE,
        I_INCLUSION_MODE, I_CONFIG, I_FIND_PARENT, I_FIND_PARENT_RESPONSE,
        I_LOG_MESSAGE, I_CHILDREN, I_SKETCH_NAME, I_SKETCH_VERSION,
        I_REBOOT, I_GATEWAY_READY, I_REQUEST_SIGNING, I_GET_NONCE, I_GET_NONCE_RESPONSE;
        public static MESSAGE_TYPE_INTERNAL get(int id) {
            for (MESSAGE_TYPE_INTERNAL type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    // Type of sensor  (for presentation message)
    public enum MESSAGE_TYPE_PRESENTATION {
        S_DOOR, S_MOTION, S_SMOKE, S_BINARY, S_DIMMER, S_COVER, S_TEMP, S_HUM, S_BARO, S_WIND, S_RAIN, S_UV,
        S_WEIGHT, S_POWER, S_HEATER, S_DISTANCE, S_LIGHT_LEVEL, S_ARDUINO_NODE, S_ARDUINO_REPEATER_NODE,
        S_LOCK, S_IR, S_WATER, S_AIR_QUALITY, S_CUSTOM, S_DUST, S_SCENE_CONTROLLER, S_RGB_LIGHT, S_RGBW_LIGHT,
        S_COLOR_SENSOR, S_HVAC, S_MULTIMETER, S_SPRINKLER, S_WATER_LEAK, S_SOUND, S_VIBRATION, S_MOISTURE;
        public static MESSAGE_TYPE_PRESENTATION get(int id) {
            for (MESSAGE_TYPE_PRESENTATION type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    // Type of data stream  (for streamed message)
    public enum MESSAGE_TYPE_STREAM {
        ST_FIRMWARE_CONFIG_REQUEST, ST_FIRMWARE_CONFIG_RESPONSE, ST_FIRMWARE_REQUEST,
        ST_FIRMWARE_RESPONSE, ST_SOUND, ST_IMAGE;
        public static MESSAGE_TYPE_STREAM get(int id) {
            for (MESSAGE_TYPE_STREAM type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    public enum PAYLOAD {
        P_STRING, P_BYTE, P_INT16, P_UINT16, P_LONG32, P_ULONG32, P_CUSTOM, P_FLOAT32;
    }

    public enum PAYLOAD_TYPE {
        PL_DOUBLE, PL_BOOLEAN, PL_INTEGER, PL_FLOAT, PL_BYTE, PL_HEX, PL_STRING;
    }

    public static PAYLOAD_TYPE getPayLoadType(MESSAGE_TYPE_SET_REQ type_set_req) {
        switch (type_set_req) {
            case V_TEMP:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_HUM:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_STATUS:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_PERCENTAGE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_PRESSURE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_FORECAST:
                return PAYLOAD_TYPE.PL_STRING;
            case V_RAIN:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_RAINRATE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_WIND:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_GUST:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_DIRECTION:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_UV:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_WEIGHT:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_DISTANCE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_IMPEDANCE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_ARMED:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_TRIPPED:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_WATT:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_KWH:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_SCENE_ON:
            case V_SCENE_OFF:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_HVAC_FLOW_STATE:
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_SPEED:
                return PAYLOAD_TYPE.PL_STRING;
            case V_LIGHT_LEVEL:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_VAR1:
            case V_VAR2:
            case V_VAR3:
            case V_VAR4:
            case V_VAR5:
                return PAYLOAD_TYPE.PL_STRING;
            case V_UP:
            case V_DOWN:
            case V_STOP:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_IR_SEND:
            case V_IR_RECEIVE:
                return PAYLOAD_TYPE.PL_HEX;
            case V_FLOW:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_VOLUME:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_LOCK_STATUS:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_LEVEL:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_VOLTAGE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_CURRENT:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_RGB:
            case V_RGBW:
                return PAYLOAD_TYPE.PL_HEX;
            case V_ID:
                return PAYLOAD_TYPE.PL_STRING;
            case V_UNIT_PREFIX:
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_SETPOINT_COOL:
                return PAYLOAD_TYPE.PL_INTEGER;
            case V_HVAC_SETPOINT_HEAT:
                return PAYLOAD_TYPE.PL_INTEGER;
            case V_HVAC_FLOW_MODE:
                return PAYLOAD_TYPE.PL_STRING;
            default:
                return null;
        }
    }

    public enum SENSOR_UNITS {
        DISTANCE,
        TEMPERATURE,
        PERCENTAGE,
        OTHERS;
        public static SENSOR_UNITS get(int id) {
            for (SENSOR_UNITS type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    public static SENSOR_UNITS getSensorUnit(MESSAGE_TYPE_SET_REQ type_set_req) {
        switch (type_set_req) {
            case V_TEMP:
                return SENSOR_UNITS.TEMPERATURE;
            case V_HUM:
                return SENSOR_UNITS.PERCENTAGE;
            case V_STATUS:
                return SENSOR_UNITS.OTHERS;
            case V_PERCENTAGE:
                return SENSOR_UNITS.PERCENTAGE;
            case V_PRESSURE:
                return SENSOR_UNITS.OTHERS;
            case V_FORECAST:
                return SENSOR_UNITS.OTHERS;
            case V_RAIN:
                return SENSOR_UNITS.OTHERS;
            case V_RAINRATE:
                return SENSOR_UNITS.OTHERS;
            case V_WIND:
                return SENSOR_UNITS.OTHERS;
            case V_GUST:
                return SENSOR_UNITS.OTHERS;
            case V_DIRECTION:
                return SENSOR_UNITS.OTHERS;
            case V_UV:
                return SENSOR_UNITS.OTHERS;
            case V_WEIGHT:
                return SENSOR_UNITS.OTHERS;
            case V_DISTANCE:
                return SENSOR_UNITS.OTHERS;
            case V_IMPEDANCE:
                return SENSOR_UNITS.OTHERS;
            case V_ARMED:
                return SENSOR_UNITS.OTHERS;
            case V_TRIPPED:
                return SENSOR_UNITS.OTHERS;
            case V_WATT:
                return SENSOR_UNITS.OTHERS;
            case V_KWH:
                return SENSOR_UNITS.OTHERS;
            case V_SCENE_ON:
            case V_SCENE_OFF:
                return SENSOR_UNITS.OTHERS;
            case V_HVAC_FLOW_STATE:
                return SENSOR_UNITS.OTHERS;
            case V_HVAC_SPEED:
                return SENSOR_UNITS.OTHERS;
            case V_LIGHT_LEVEL:
                return SENSOR_UNITS.OTHERS;
            case V_VAR1:
            case V_VAR2:
            case V_VAR3:
            case V_VAR4:
            case V_VAR5:
                return SENSOR_UNITS.OTHERS;
            case V_UP:
            case V_DOWN:
            case V_STOP:
                return SENSOR_UNITS.OTHERS;
            case V_IR_SEND:
            case V_IR_RECEIVE:
                return SENSOR_UNITS.OTHERS;
            case V_FLOW:
                return SENSOR_UNITS.OTHERS;
            case V_VOLUME:
                return SENSOR_UNITS.OTHERS;
            case V_LOCK_STATUS:
                return SENSOR_UNITS.OTHERS;
            case V_LEVEL:
                return SENSOR_UNITS.OTHERS;
            case V_VOLTAGE:
                return SENSOR_UNITS.OTHERS;
            case V_CURRENT:
                return SENSOR_UNITS.OTHERS;
            case V_RGB:
            case V_RGBW:
                return SENSOR_UNITS.OTHERS;
            case V_ID:
                return SENSOR_UNITS.OTHERS;
            case V_UNIT_PREFIX:
                return SENSOR_UNITS.OTHERS;
            case V_HVAC_SETPOINT_COOL:
                return SENSOR_UNITS.OTHERS;
            case V_HVAC_SETPOINT_HEAT:
                return SENSOR_UNITS.OTHERS;
            case V_HVAC_FLOW_MODE:
                return SENSOR_UNITS.OTHERS;
            default:
                break;
        }
        return SENSOR_UNITS.OTHERS;
    }
    
    // Scale types
    public enum UNITS_DISTANCE {
        KILOMETER("km"),
        METER("m"),
        DECIMETER("dm"),
        CENTIMETER("cm"),
        MILLIMETER("mm"),
        MILE("mi"),
        YARD("yd"),
        FOOT("ft"),
        INCH("in");
        public static UNITS_DISTANCE get(int id) {
            for (UNITS_DISTANCE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String scale;

        private UNITS_DISTANCE(String scale) {
            this.scale = scale;
        }

        public String value() {
            return this.scale;
        }
    }

    public enum UNITS_TEMPERATURE {
        CELSIUS("°C"),
        FAHRENHEIT("°F"),
        KELVIN("K");
        public static UNITS_TEMPERATURE get(int id) {
            for (UNITS_TEMPERATURE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String scale;

        private UNITS_TEMPERATURE(String scale) {
            this.scale = scale;
        }

        public String value() {
            return this.scale;
        }
    }

    public enum UNITS_PERCENTAGE {
        PERCENTAGE("%");
        public static UNITS_PERCENTAGE get(int id) {
            for (UNITS_PERCENTAGE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String scale;

        private UNITS_PERCENTAGE(String scale) {
            this.scale = scale;
        }

        public String value() {
            return this.scale;
        }
    }

}
