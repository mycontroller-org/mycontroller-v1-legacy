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
package org.mycontroller.standalone.provider.mysensors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MySensors {

    public static final int NODE_ID_BROADCAST = 255;
    public static final int SENSOR_ID_BROADCAST = 255;
    public static final int GATEWAY_ID = 0;
    public static final int ACK = 1;
    public static final int NO_ACK = 0;
    public static final String EMPTY_DATA = "";
    public static final int NODE_ID_MIN = 1; //Always starts from number 1
    public static final int NODE_ID_MAX = 254; //Last usable address for MySensors is 254
    public static final int SENSOR_ID_MIN = 0;
    public static final int SENSOR_ID_MAX = 255;

    public static final String KEY_RSSI = "rssi:";
    public static final String KEY_PROPERTIES = "p:";

    public static final int MAX_INDEX_MESSAGE_TYPE = MYS_MESSAGE_TYPE.values().length;
    public static final int MAX_INDEX_INTERNAL = MYS_MESSAGE_TYPE_INTERNAL.values().length;
    public static final int MAX_INDEX_PRESENTATION = MYS_MESSAGE_TYPE_PRESENTATION.values().length;
    public static final int MAX_INDEX_SET_REQ = MYS_MESSAGE_TYPE_SET_REQ.values().length;
    public static final int MAX_INDEX_STREAM = MYS_MESSAGE_TYPE_STREAM.values().length;

    // Message types
    public enum MYS_MESSAGE_TYPE {
        C_PRESENTATION("Presentation"),
        C_SET("Set"),
        C_REQ("Request"),
        C_INTERNAL("Internal"),
        C_STREAM("Stream"); // For Firmware and other larger chunks of data that need to be divided into pieces

        public static MYS_MESSAGE_TYPE get(int id) {
            for (MYS_MESSAGE_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MYS_MESSAGE_TYPE(String text) {
            this.text = text;
        }

        public static MYS_MESSAGE_TYPE fromString(String text) {
            if (text != null) {
                for (MYS_MESSAGE_TYPE type : MYS_MESSAGE_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of internal messages (for internal messages)
    public enum MYS_MESSAGE_TYPE_INTERNAL {
        I_BATTERY_LEVEL("Battery level"),
        I_TIME("Time"),
        I_VERSION("Version"),
        I_ID_REQUEST("Id request"),
        I_ID_RESPONSE("Id response"),
        I_INCLUSION_MODE("Inclusion mode"),
        I_CONFIG("Config"),
        I_FIND_PARENT("Find parent"),
        I_FIND_PARENT_RESPONSE("Find parent response"),
        I_LOG_MESSAGE("Log message"),
        I_CHILDREN("Children"),
        I_SKETCH_NAME("Sketch name"),
        I_SKETCH_VERSION("Sketch version"),
        I_REBOOT("Reboot"),
        I_GATEWAY_READY("GatewayTable ready"),
        I_REQUEST_SIGNING("Request signing"),
        I_GET_NONCE("Get nonce"),
        I_GET_NONCE_RESPONSE("Get nonce response"),
        I_HEARTBEAT("Heartbeat"),
        I_PRESENTATION("Presentation"),
        I_DISCOVER("Discover"),
        I_DISCOVER_RESPONSE("Discover response"),
        I_HEARTBEAT_RESPONSE("Heartbeat response"),
        I_LOCKED("Locked"),
        I_PING("Ping"),
        I_PONG("Pong"),
        I_REGISTRATION_REQUEST("Registration request"),
        I_REGISTRATION_RESPONSE("Registration response"),
        I_DEBUG("Debug"),
        I_SIGNAL_REPORT_REQUEST("Signal report request"),       //!< Device signal strength request
        I_SIGNAL_REPORT_REVERSE("Signal report reverse"),       //!< Internal
        I_SIGNAL_REPORT_RESPONSE("Signal report response"),     //!< Device signal strength response (RSSI)
        I_PRE_SLEEP_NOTIFICATION("Pre sleep notification"),     //!< Message sent before node is going to sleep
        I_POST_SLEEP_NOTIFICATION("Post sleep notification");   //!< Message sent after node woke up (if enabled);

        public static MYS_MESSAGE_TYPE_INTERNAL get(int id) {
            for (MYS_MESSAGE_TYPE_INTERNAL type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MYS_MESSAGE_TYPE_INTERNAL(String text) {
            this.text = text;
        }

        public static MYS_MESSAGE_TYPE_INTERNAL fromString(String text) {
            if (text != null) {
                for (MYS_MESSAGE_TYPE_INTERNAL type : MYS_MESSAGE_TYPE_INTERNAL.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of sensor  (for presentation message)
    public enum MYS_MESSAGE_TYPE_PRESENTATION {
        S_DOOR("Door"),     // Door sensor, V_TRIPPED, V_ARMED
        S_MOTION("Motion"),   // Motion sensor, V_TRIPPED, V_ARMED
        S_SMOKE("Smoke"),    // Smoke sensor, V_TRIPPED, V_ARMED
        S_BINARY("Binary"),   // Binary light or relay, V_STATUS (or V_LIGHT), V_WATT (same as S_LIGHT)
        S_DIMMER("Dimmer"),   // Dimmable light or fan device, V_STATUS (on/off), V_DIMMER (dimmer level 0-100), V_WATT
        S_COVER("Cover"),    // Blinds or window cover, V_UP, V_DOWN, V_STOP, V_DIMMER (open/close to a percentage)
        S_TEMP("Temperature"),     // Temperature sensor, V_TEMP
        S_HUM("Humidity"),      // Humidity sensor, V_HUM
        S_BARO("Barometer"),     // Barometer sensor, V_PRESSURE, V_FORECAST
        S_WIND("Wind"),     // Wind sensor, V_WIND, V_GUST
        S_RAIN("Rain"),     // Rain sensor, V_RAIN, V_RAINRATE
        S_UV("UV"),       // Uv sensor, V_UV
        S_WEIGHT("Weight"),   // Personal scale sensor, V_WEIGHT, V_IMPEDANCE
        S_POWER("Power"),    // Power meter, V_WATT, V_KWH
        S_HEATER("Heater"),   // Header device, V_HVAC_SETPOINT_HEAT, V_HVAC_FLOW_STATE, V_TEMP
        S_DISTANCE("Distance"), // Distance sensor, V_DISTANCE
        S_LIGHT_LEVEL("Light level"),  // Light level sensor, V_LIGHT_LEVEL (uncalibrated in percentage),
                                      //V_LEVEL (light level in lux)
        S_ARDUINO_NODE("Node"), // Used (internally) for presenting a non-repeating Arduino node
        S_ARDUINO_REPEATER_NODE("Repeater node"), // Used (internally) for presenting a repeating Arduino node
        S_LOCK("Lock"),     // Lock device, V_LOCK_STATUS
        S_IR("IR"),       // Ir device, V_IR_SEND, V_IR_RECEIVE
        S_WATER("Water"),    // Water meter, V_FLOW, V_VOLUME
        S_AIR_QUALITY("Air quality"), // Air quality sensor, V_LEVEL
        S_CUSTOM("Custom"),   // Custom sensor
        S_DUST("Dust"),     // Dust sensor, V_LEVEL
        S_SCENE_CONTROLLER("Scene controller"), // Scene controller device, V_SCENE_ON, V_SCENE_OFF.
        S_RGB_LIGHT("RGB light"),    // RGB light. Send color component data using V_RGB. Also supports V_WATT
        S_RGBW_LIGHT("RGBW light"),   // RGB light with an additional White component. Send data using V_RGBW.
                                    // Also supports V_WATT
        S_COLOR_SENSOR("Color sensor"), // Color sensor, send color information using V_RGB
        S_HVAC("HVAC"),     // Thermostat/HVAC device. V_HVAC_SETPOINT_HEAT, V_HVAC_SETPOINT_COLD,
        // V_HVAC_FLOW_STATE, V_HVAC_FLOW_MODE, V_TEMP
        S_MULTIMETER("Multimeter"), // Multimeter device, V_VOLTAGE, V_CURRENT, V_IMPEDANCE
        S_SPRINKLER("Sprinkler"),  // Sprinkler, V_STATUS (turn on/off), V_TRIPPED (if fire detecting device)
        S_WATER_LEAK("Water leak"), // Water leak sensor, V_TRIPPED, V_ARMED
        S_SOUND("Sound"),    // Sound sensor, V_TRIPPED, V_ARMED, V_LEVEL (sound level in dB)
        S_VIBRATION("Vibration"),        // Vibration sensor, V_TRIPPED, V_ARMED, V_LEVEL (vibration in Hz)
        S_MOISTURE("Moisture"), // Moisture sensor, V_TRIPPED, V_ARMED,
                                // V_LEVEL (water content or moisture in percentage?)
        S_INFO("Information"),     // LCD text device / Simple information device on controller, V_TEXT
        S_GAS("Gas"),      // Gas meter, V_FLOW, V_VOLUME
        S_GPS("GPS"),      // GPS Sensor, V_POSITION
        S_WATER_QUALITY("Water quality"); //!< V_TEMP, V_PH, V_ORP, V_EC, V_STATUS

        public static MYS_MESSAGE_TYPE_PRESENTATION get(int id) {
            for (MYS_MESSAGE_TYPE_PRESENTATION type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MYS_MESSAGE_TYPE_PRESENTATION(String text) {
            this.text = text;
        }

        public static MYS_MESSAGE_TYPE_PRESENTATION fromString(String text) {
            if (text != null) {
                for (MYS_MESSAGE_TYPE_PRESENTATION type : MYS_MESSAGE_TYPE_PRESENTATION.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of sensor data (for set/req/ack messages)
    public enum MYS_MESSAGE_TYPE_SET_REQ {
        V_TEMP("Temperature"),         // S_TEMP. Temperature S_TEMP, S_HEATER, S_HVAC
        V_HUM("Humidity"),          // S_HUM. Humidity
        V_STATUS("Status"),       //  S_LIGHT, S_DIMMER, S_SPRINKLER, S_HVAC, S_HEATER.
        // Used for setting/reporting binary (on/off) status. 1=on, 0=off
        V_PERCENTAGE("Percentage"),   // S_DIMMER. Used for sending a percentage value 0-100 (%).
        V_PRESSURE("Pressure"),     // S_BARO. Atmospheric Pressure
        V_FORECAST("Forecast"),     // S_BARO. Whether forecast. string of "stable",
        // "sunny", "cloudy", "unstable", "thunderstorm" or "unknown"
        V_RAIN("Rain"),         // S_RAIN. Amount of rain
        V_RAINRATE("Rain rate"),     // S_RAIN. Rate of rain
        V_WIND("Wind"),         // S_WIND. Wind speed
        V_GUST("Gust"),         // S_WIND. Gust
        V_DIRECTION("Direction"),    // S_WIND. Wind direction 0-360 (degrees)
        V_UV("UV"),           // S_UV. UV light level
        V_WEIGHT("Weight"),       // S_WEIGHT. Weight(for scales etc)
        V_DISTANCE("Distance"),     // S_DISTANCE. Distance
        V_IMPEDANCE("Impedance"),    // S_MULTIMETER, S_WEIGHT. Impedance value
        V_ARMED("Armed"),        // S_DOOR, S_MOTION, S_SMOKE, S_SPRINKLER.
        // Armed status of a security sensor. 1 = Armed, 0 = Bypassed
        V_TRIPPED("Tripped"),      // S_DOOR, S_MOTION, S_SMOKE, S_SPRINKLER,
        // S_WATER_LEAK, S_SOUND, S_VIBRATION, S_MOISTURE.
        // Tripped status of a security sensor. 1 = Tripped, 0
        V_WATT("Watt"),         // S_POWER, S_LIGHT, S_DIMMER, S_RGB, S_RGBW. Watt value for power meters
        V_KWH("KWh"),          // S_POWER. Accumulated number of KWH for a power meter
        V_SCENE_ON("Scene ON"),     // S_SCENE_CONTROLLER. Turn on a scene
        V_SCENE_OFF("Scene OFF"),    // S_SCENE_CONTROLLER. Turn of a scene
        V_HVAC_FLOW_STATE("HVAC flow state"),  // S_HEATER, S_HVAC.
                                              // HVAC flow state ("Off", "HeatOn", "CoolOn", or "AutoChangeOver")
        V_HVAC_SPEED("HVAC speed"),   // S_HVAC, S_HEATER. HVAC/Heater fan speed ("Min", "Normal", "Max", "Auto")
        V_LIGHT_LEVEL("Light level"),  // S_LIGHT_LEVEL. Uncalibrated light level.
                                      // 0-100%. Use V_LEVEL for light level in lux
        V_VAR1("Variable 1"),
        V_VAR2("Variable 2"),
        V_VAR3("Variable 3"),
        V_VAR4("Variable 4"),
        V_VAR5("Variable 5"),
        V_UP("Up"),           // S_COVER. Window covering. Up
        V_DOWN("Down"),         // S_COVER. Window covering. Down
        V_STOP("Stop"),         // S_COVER. Window covering. Stop
        V_IR_SEND("IR send"),      // S_IR. Send out an IR-command
        V_IR_RECEIVE("IR receive"),   // S_IR. This message contains a received IR-command
        V_FLOW("Flow"),         // S_WATER. Flow of water (in meter)
        V_VOLUME("Volume"),       // S_WATER. Water volume
        V_LOCK_STATUS("Lock status"),  // S_LOCK. Set or get lock status. 1=Locked, 0=Unlocked
        V_LEVEL("Level"),        // S_DUST, S_AIR_QUALITY, S_SOUND (dB), S_VIBRATION (hz), S_LIGHT_LEVEL (lux)
        V_VOLTAGE("Voltage"),      // S_MULTIMETER
        V_CURRENT("Current"),      // S_MULTIMETER
        V_RGB("RGB"),          // S_RGB_LIGHT, S_COLOR_SENSOR.
        // Used for sending color information for multi color LED lighting or color sensors.
        // Sent as ASCII hex: RRGGBB (RR=red, GG=green, BB=blue component)
        V_RGBW("RGBW"),         // S_RGBW_LIGHT
        // Used for sending color information to multi color LED lighting.
        // Sent as ASCII hex: RRGGBBWW (WW=white component)
        V_ID("KEY_ID"),           // S_TEMP
        // Used for sending in sensors hardware ids (i.e. OneWire DS1820b).
        V_UNIT_PREFIX("Unit prefix"),  // S_DUST, S_AIR_QUALITY
        // Allows sensors to send in a string representing the
        // unit prefix to be displayed in GUI, not parsed by controller! E.g. cm, m, km, inch.
        // Can be used for S_DISTANCE or gas concentration
        V_HVAC_SETPOINT_COOL("HVAC setpoint cool"), // S_HVAC. HVAC cool setpoint (Integer between 0-100)
        V_HVAC_SETPOINT_HEAT("HVAC setpoint heat"), // S_HEATER, S_HVAC. HVAC/Heater setpoint (Integer between 0-100)
        V_HVAC_FLOW_MODE("HVAC flow mode"), // S_HVAC. Flow mode for HVAC ("Auto", "ContinuousOn", "PeriodicOn")
        V_TEXT("Text"),         // S_INFO. Text message to display on LCD or controller device
        V_CUSTOM("Custom"),     // Custom messages used for controller/inter node specific commands,
        // preferably using S_CUSTOM device type.
        V_POSITION("Position"), // GPS position and altitude. Payload: latitude;longitude;altitude(m).
                                // E.g. "55.722526;13.017972;18"
        V_IR_RECORD("IR record"),         // Record IR codes S_IR for playback
        V_PH("PH"), //!< S_WATER_QUALITY, water PH
        V_ORP("ORP"), //!< S_WATER_QUALITY, water ORP : redox potential in mV
        V_EC("EC"), //!< S_WATER_QUALITY, water electric conductivity μS/cm (microSiemens/cm)
        V_VAR("Volt-ampere reactive"),  //!< S_POWER, Reactive power: volt-ampere reactive (var)
        V_VA("Volt-ampere"),    //!< S_POWER, Apparent power: volt-ampere (VA)
        V_POWER_FACTOR("Power factor"); //!< S_POWER, Ratio of real power to apparent power:
                                        //floating point value in the range [-1,..,1]

        public static MYS_MESSAGE_TYPE_SET_REQ get(int id) {
            for (MYS_MESSAGE_TYPE_SET_REQ type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MYS_MESSAGE_TYPE_SET_REQ(String text) {
            this.text = text;
        }

        public static MYS_MESSAGE_TYPE_SET_REQ fromString(String text) {
            if (text != null) {
                for (MYS_MESSAGE_TYPE_SET_REQ type : MYS_MESSAGE_TYPE_SET_REQ.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of data stream  (for streamed message)
    public enum MYS_MESSAGE_TYPE_STREAM {
        ST_FIRMWARE_CONFIG_REQUEST("Firmware config request"),
        ST_FIRMWARE_CONFIG_RESPONSE("Firmware config response"),
        ST_FIRMWARE_REQUEST("Firmware request"),
        ST_FIRMWARE_RESPONSE("Firmware response"),
        ST_SOUND("Sound"),
        ST_IMAGE("Image");
        public static MYS_MESSAGE_TYPE_STREAM get(int id) {
            for (MYS_MESSAGE_TYPE_STREAM type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MYS_MESSAGE_TYPE_STREAM(String text) {
            this.text = text;
        }

        public static MYS_MESSAGE_TYPE_STREAM fromString(String text) {
            if (text != null) {
                for (MYS_MESSAGE_TYPE_STREAM type : MYS_MESSAGE_TYPE_STREAM.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

}
