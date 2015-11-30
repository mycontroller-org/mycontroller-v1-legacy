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

import org.mycontroller.standalone.db.TypeUtils.METRIC_TYPE;

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
        V_TEMP,         // S_TEMP. Temperature S_TEMP, S_HEATER, S_HVAC
        V_HUM,          // S_HUM. Humidity
        V_STATUS,       //  S_LIGHT, S_DIMMER, S_SPRINKLER, S_HVAC, S_HEATER. Used for setting/reporting binary (on/off) status. 1=on, 0=off  
        V_PERCENTAGE,   // S_DIMMER. Used for sending a percentage value 0-100 (%). 
        V_PRESSURE,     // S_BARO. Atmospheric Pressure
        V_FORECAST,     // S_BARO. Whether forecast. string of "stable", "sunny", "cloudy", "unstable", "thunderstorm" or "unknown"
        V_RAIN,         // S_RAIN. Amount of rain
        V_RAINRATE,     // S_RAIN. Rate of rain
        V_WIND,         // S_WIND. Wind speed
        V_GUST,         // S_WIND. Gust
        V_DIRECTION,    // S_WIND. Wind direction 0-360 (degrees)
        V_UV,           // S_UV. UV light level
        V_WEIGHT,       // S_WEIGHT. Weight(for scales etc)
        V_DISTANCE,     // S_DISTANCE. Distance
        V_IMPEDANCE,    // S_MULTIMETER, S_WEIGHT. Impedance value
        V_ARMED,        // S_DOOR, S_MOTION, S_SMOKE, S_SPRINKLER. Armed status of a security sensor. 1 = Armed, 0 = Bypassed
        V_TRIPPED,      // S_DOOR, S_MOTION, S_SMOKE, S_SPRINKLER, S_WATER_LEAK, S_SOUND, S_VIBRATION, S_MOISTURE. Tripped status of a security sensor. 1 = Tripped, 0
        V_WATT,         // S_POWER, S_LIGHT, S_DIMMER, S_RGB, S_RGBW. Watt value for power meters
        V_KWH,          // S_POWER. Accumulated number of KWH for a power meter
        V_SCENE_ON,     // S_SCENE_CONTROLLER. Turn on a scene
        V_SCENE_OFF,    // S_SCENE_CONTROLLER. Turn of a scene
        V_HVAC_FLOW_STATE,  // S_HEATER, S_HVAC. HVAC flow state ("Off", "HeatOn", "CoolOn", or "AutoChangeOver") 
        V_HVAC_SPEED,   // S_HVAC, S_HEATER. HVAC/Heater fan speed ("Min", "Normal", "Max", "Auto") 
        V_LIGHT_LEVEL,  // S_LIGHT_LEVEL. Uncalibrated light level. 0-100%. Use V_LEVEL for light level in lux
        V_VAR1, V_VAR2, V_VAR3, V_VAR4, V_VAR5,
        V_UP,           // S_COVER. Window covering. Up
        V_DOWN,         // S_COVER. Window covering. Down
        V_STOP,         // S_COVER. Window covering. Stop
        V_IR_SEND,      // S_IR. Send out an IR-command
        V_IR_RECEIVE,   // S_IR. This message contains a received IR-command
        V_FLOW,         // S_WATER. Flow of water (in meter)
        V_VOLUME,       // S_WATER. Water volume
        V_LOCK_STATUS,  // S_LOCK. Set or get lock status. 1=Locked, 0=Unlocked
        V_LEVEL,        // S_DUST, S_AIR_QUALITY, S_SOUND (dB), S_VIBRATION (hz), S_LIGHT_LEVEL (lux)
        V_VOLTAGE,      // S_MULTIMETER 
        V_CURRENT,      // S_MULTIMETER
        V_RGB,          // S_RGB_LIGHT, S_COLOR_SENSOR. 
        // Used for sending color information for multi color LED lighting or color sensors. 
        // Sent as ASCII hex: RRGGBB (RR=red, GG=green, BB=blue component)
        V_RGBW,         // S_RGBW_LIGHT
        // Used for sending color information to multi color LED lighting. 
        // Sent as ASCII hex: RRGGBBWW (WW=white component)
        V_ID,           // S_TEMP
        // Used for sending in sensors hardware ids (i.e. OneWire DS1820b). 
        V_UNIT_PREFIX,  // S_DUST, S_AIR_QUALITY
                       // Allows sensors to send in a string representing the 
                       // unit prefix to be displayed in GUI, not parsed by controller! E.g. cm, m, km, inch.
                       // Can be used for S_DISTANCE or gas concentration
        V_HVAC_SETPOINT_COOL, // S_HVAC. HVAC cool setpoint (Integer between 0-100)
        V_HVAC_SETPOINT_HEAT, // S_HEATER, S_HVAC. HVAC/Heater setpoint (Integer between 0-100)
        V_HVAC_FLOW_MODE, // S_HVAC. Flow mode for HVAC ("Auto", "ContinuousOn", "PeriodicOn")
        V_TEXT,         // S_INFO. Text message to display on LCD or controller device
        V_CUSTOM,       // Custom messages used for controller/inter node specific commands, preferably using S_CUSTOM device type.
        V_POSITION,     // GPS position and altitude. Payload: latitude;longitude;altitude(m). E.g. "55.722526;13.017972;18"
        V_IR_RECORD;         // Record IR codes S_IR for playback

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
        I_REBOOT, I_GATEWAY_READY, I_REQUEST_SIGNING, I_GET_NONCE, I_GET_NONCE_RESPONSE,
        I_HEARTBEAT, I_PRESENTATION, I_DISCOVER, I_DISCOVER_RESPONSE;
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
        S_DOOR,     // Door sensor, V_TRIPPED, V_ARMED
        S_MOTION,   // Motion sensor, V_TRIPPED, V_ARMED 
        S_SMOKE,    // Smoke sensor, V_TRIPPED, V_ARMED
        S_BINARY,   // Binary light or relay, V_STATUS (or V_LIGHT), V_WATT (same as S_LIGHT)
        S_DIMMER,   // Dimmable light or fan device, V_STATUS (on/off), V_DIMMER (dimmer level 0-100), V_WATT
        S_COVER,    // Blinds or window cover, V_UP, V_DOWN, V_STOP, V_DIMMER (open/close to a percentage)
        S_TEMP,     // Temperature sensor, V_TEMP
        S_HUM,      // Humidity sensor, V_HUM
        S_BARO,     // Barometer sensor, V_PRESSURE, V_FORECAST
        S_WIND,     // Wind sensor, V_WIND, V_GUST
        S_RAIN,     // Rain sensor, V_RAIN, V_RAINRATE
        S_UV,       // Uv sensor, V_UV
        S_WEIGHT,   // Personal scale sensor, V_WEIGHT, V_IMPEDANCE
        S_POWER,    // Power meter, V_WATT, V_KWH
        S_HEATER,   // Header device, V_HVAC_SETPOINT_HEAT, V_HVAC_FLOW_STATE, V_TEMP
        S_DISTANCE, // Distance sensor, V_DISTANCE
        S_LIGHT_LEVEL,  // Light level sensor, V_LIGHT_LEVEL (uncalibrated in percentage),  V_LEVEL (light level in lux)
        S_ARDUINO_NODE, // Used (internally) for presenting a non-repeating Arduino node
        S_ARDUINO_REPEATER_NODE, // Used (internally) for presenting a repeating Arduino node 
        S_LOCK,     // Lock device, V_LOCK_STATUS
        S_IR,       // Ir device, V_IR_SEND, V_IR_RECEIVE
        S_WATER,    // Water meter, V_FLOW, V_VOLUME
        S_AIR_QUALITY, // Air quality sensor, V_LEVEL
        S_CUSTOM,   // Custom sensor 
        S_DUST,     // Dust sensor, V_LEVEL
        S_SCENE_CONTROLLER, // Scene controller device, V_SCENE_ON, V_SCENE_OFF. 
        S_RGB_LIGHT,    // RGB light. Send color component data using V_RGB. Also supports V_WATT 
        S_RGBW_LIGHT,   // RGB light with an additional White component. Send data using V_RGBW. Also supports V_WATT
        S_COLOR_SENSOR, // Color sensor, send color information using V_RGB
        S_HVAC,     // Thermostat/HVAC device. V_HVAC_SETPOINT_HEAT, V_HVAC_SETPOINT_COLD, V_HVAC_FLOW_STATE, V_HVAC_FLOW_MODE, V_TEMP
        S_MULTIMETER, // Multimeter device, V_VOLTAGE, V_CURRENT, V_IMPEDANCE 
        S_SPRINKLER,  // Sprinkler, V_STATUS (turn on/off), V_TRIPPED (if fire detecting device)
        S_WATER_LEAK, // Water leak sensor, V_TRIPPED, V_ARMED
        S_SOUND,    // Sound sensor, V_TRIPPED, V_ARMED, V_LEVEL (sound level in dB)
        S_VIBRATION,        // Vibration sensor, V_TRIPPED, V_ARMED, V_LEVEL (vibration in Hz)
        S_MOISTURE, // Moisture sensor, V_TRIPPED, V_ARMED, V_LEVEL (water content or moisture in percentage?) 
        S_INFO,     // LCD text device / Simple information device on controller, V_TEXT
        S_GAS,      // Gas meter, V_FLOW, V_VOLUME
        S_GPS;      // GPS Sensor, V_POSITION

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

    public static METRIC_TYPE getMetricType(PAYLOAD_TYPE payloadType) {
        switch (payloadType) {
            case PL_BOOLEAN:
                return METRIC_TYPE.BINARY;
            case PL_DOUBLE:
                return METRIC_TYPE.DOUBLE;
            default:
                return METRIC_TYPE.NONE;
        }
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

}
