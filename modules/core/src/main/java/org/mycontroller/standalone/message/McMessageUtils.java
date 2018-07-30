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
package org.mycontroller.standalone.message;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/* All the messages based on MYSENSORS.ORG, Do not add new */
/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class McMessageUtils {

    private static HashMap<Integer, Boolean> nodeInfoUpdateRunning = new HashMap<Integer, Boolean>();

    public static synchronized boolean isNodeInfoUpdateRunning(int gatewayId) {
        if (nodeInfoUpdateRunning.get(gatewayId) == null) {
            nodeInfoUpdateRunning.put(gatewayId, false);
        }
        return nodeInfoUpdateRunning.get(gatewayId);
    }

    public static synchronized void updateNodeInfoRunningState(int gatewayId, boolean status) {
        nodeInfoUpdateRunning.put(gatewayId, status);
    }

    // Message types
    public enum MESSAGE_STATUS {
        SUCCESS,
        FAILED,
        ACK_RECEIVED,
        NO_ACK_RECEIVED,
        UNKNOWN_ERROR,
        GATEWAY_NOT_AVAILABLE,
        ADDED_TO_SLEEP_QUEUE;
    }

    // Message types
    public enum MESSAGE_TYPE {
        C_PRESENTATION("Presentation"),
        C_SET("Set"),
        C_REQ("Request"),
        C_INTERNAL("Internal"),
        C_STREAM("Stream"); // For Firmware and other larger chunks of data that need to be divided into pieces

        public static MESSAGE_TYPE get(int id) {
            for (MESSAGE_TYPE type : values()) {
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

        private MESSAGE_TYPE(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE type : MESSAGE_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of internal messages (for internal messages)
    public enum MESSAGE_TYPE_INTERNAL {
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
        I_RSSI("RSSI"),
        I_PROPERTIES("Properties"),
        I_FACTORY_RESET("Factory reset"),
        I_SIGNAL_REPORT_REQUEST("Signal report request"),
        I_SIGNAL_REPORT_REVERSE("Signal report reverse"),
        I_SIGNAL_REPORT_RESPONSE("Signal report response"),
        I_PRE_SLEEP_NOTIFICATION("Pre sleep notification"),
        I_POST_SLEEP_NOTIFICATION("Post sleep notification");

        public static MESSAGE_TYPE_INTERNAL get(int id) {
            for (MESSAGE_TYPE_INTERNAL type : values()) {
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

        private MESSAGE_TYPE_INTERNAL(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_INTERNAL fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_INTERNAL type : MESSAGE_TYPE_INTERNAL.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of sensor  (for presentation message)
    public enum MESSAGE_TYPE_PRESENTATION {
        S_DOOR("Door"),
        S_MOTION("Motion"),
        S_SMOKE("Smoke"),
        S_BINARY("Binary"),
        S_DIMMER("Dimmer"),
        S_COVER("Cover"),
        S_TEMP("Temperature"),
        S_HUM("Humidity"),
        S_BARO("Barometer"),
        S_WIND("Wind"),
        S_RAIN("Rain"),
        S_UV("UV"),
        S_WEIGHT("Weight"),
        S_POWER("Power"),
        S_HEATER("Heater"),
        S_DISTANCE("Distance"),
        S_LIGHT_LEVEL("Light level"),
        S_ARDUINO_NODE("Node"),
        S_ARDUINO_REPEATER_NODE("Repeater node"),
        S_LOCK("Lock"),
        S_IR("IR"),
        S_WATER("Water"),
        S_AIR_QUALITY("Air quality"),
        S_CUSTOM("Custom"),
        S_DUST("Dust"),
        S_SCENE_CONTROLLER("Scene controller"),
        S_RGB_LIGHT("RGB light"),
        S_RGBW_LIGHT("RGBW light"),
        S_COLOR_SENSOR("Color sensor"),
        S_HVAC("HVAC"),
        S_MULTIMETER("Multimeter"),
        S_SPRINKLER("Sprinkler"),
        S_WATER_LEAK("Water leak"),
        S_SOUND("Sound"),
        S_VIBRATION("Vibration"),
        S_MOISTURE("Moisture"),
        S_INFO("Information"),
        S_GAS("Gas"),
        S_GPS("GPS"),
        S_WATER_QUALITY("Water quality"),
        S_CPU("CPU"),
        S_MEMORY("Memory"),
        S_DISK("Disk"),
        S_PWM("PWM");

        public static MESSAGE_TYPE_PRESENTATION get(int id) {
            for (MESSAGE_TYPE_PRESENTATION type : values()) {
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

        private MESSAGE_TYPE_PRESENTATION(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_PRESENTATION fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_PRESENTATION type : MESSAGE_TYPE_PRESENTATION.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of sensor data (for set/req/ack messages)
    public enum MESSAGE_TYPE_SET_REQ {
        V_TEMP("Temperature"),
        V_HUM("Humidity"),
        V_STATUS("Status"),
        V_PERCENTAGE("Percentage"),
        V_PRESSURE("Pressure"),
        V_FORECAST("Forecast"),
        V_RAIN("Rain"),
        V_RAINRATE("Rain rate"),
        V_WIND("Wind"),
        V_GUST("Gust"),
        V_DIRECTION("Direction"),
        V_UV("UV"),
        V_WEIGHT("Weight"),
        V_DISTANCE("Distance"),
        V_IMPEDANCE("Impedance"),
        V_ARMED("Armed"),
        V_TRIPPED("Tripped"),
        V_WATT("Watt"),
        V_KWH("KWh"),
        V_SCENE_ON("Scene ON"),
        V_SCENE_OFF("Scene OFF"),
        V_HVAC_FLOW_STATE("HVAC flow state"),
        V_HVAC_SPEED("HVAC speed"),
        V_LIGHT_LEVEL("Light level"),
        V_VAR1("Variable 1"),
        V_VAR2("Variable 2"),
        V_VAR3("Variable 3"),
        V_VAR4("Variable 4"),
        V_VAR5("Variable 5"),
        V_UP("Up"),
        V_DOWN("Down"),
        V_STOP("Stop"),
        V_IR_SEND("IR send"),
        V_IR_RECEIVE("IR receive"),
        V_FLOW("Flow"),
        V_VOLUME("Volume"),
        V_LOCK_STATUS("Lock status"),
        V_LEVEL("Level"),
        V_VOLTAGE("Voltage"),
        V_CURRENT("Current"),
        V_RGB("RGB"),
        V_RGBW("RGBW"),
        V_ID("Id"),
        V_UNIT_PREFIX("Unit prefix"),
        V_HVAC_SETPOINT_COOL("HVAC setpoint cool"),
        V_HVAC_SETPOINT_HEAT("HVAC setpoint heat"),
        V_HVAC_FLOW_MODE("HVAC flow mode"),
        V_TEXT("Text"),
        V_CUSTOM("Custom"),
        V_POSITION("Position"),
        V_IR_RECORD("IR record"),
        V_PH("PH"),
        V_ORP("ORP"),
        V_EC("EC"),
        V_VAR("Volt-ampere reactive"),
        V_VA("Volt-ampere"),
        V_POWER_FACTOR("Power factor"),
        V_USED("Used"),
        V_FREE("Free"),
        V_TOTAL("Total"),
        V_COUNT("Count"),
        V_RATE("Rate");

        public static MESSAGE_TYPE_SET_REQ get(int id) {
            for (MESSAGE_TYPE_SET_REQ type : values()) {
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

        private MESSAGE_TYPE_SET_REQ(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_SET_REQ fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_SET_REQ type : MESSAGE_TYPE_SET_REQ.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of data stream  (for streamed message)
    public enum MESSAGE_TYPE_STREAM {
        ST_FIRMWARE_CONFIG_REQUEST("Firmware config request"),
        ST_FIRMWARE_CONFIG_RESPONSE("Firmware config response"),
        ST_FIRMWARE_REQUEST("Firmware request"),
        ST_FIRMWARE_RESPONSE("Firmware response"),
        ST_SOUND("Sound"),
        ST_IMAGE("Image");
        public static MESSAGE_TYPE_STREAM get(int id) {
            for (MESSAGE_TYPE_STREAM type : values()) {
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

        private MESSAGE_TYPE_STREAM(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_STREAM fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_STREAM type : MESSAGE_TYPE_STREAM.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum PAYLOAD_TYPE {
        PL_DOUBLE, PL_BOOLEAN, PL_HEX, PL_STRING, PL_LONG, PL_GPS;
    }

    public static METRIC_TYPE getMetricType(PAYLOAD_TYPE payloadType) {
        switch (payloadType) {
            case PL_BOOLEAN:
                return METRIC_TYPE.BINARY;
            case PL_DOUBLE:
                return METRIC_TYPE.DOUBLE;
            case PL_LONG:
                return METRIC_TYPE.COUNTER;
            case PL_GPS:
                return METRIC_TYPE.GPS;
            default:
                return METRIC_TYPE.NONE;
        }
    }

    public static METRIC_TYPE getMetricType(MESSAGE_TYPE_SET_REQ type_set_req) {
        return getMetricType(getPayLoadType(type_set_req));
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
                return PAYLOAD_TYPE.PL_STRING;
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
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_SETPOINT_HEAT:
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_FLOW_MODE:
                return PAYLOAD_TYPE.PL_STRING;
            case V_FREE:
            case V_USED:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_COUNT:
                return PAYLOAD_TYPE.PL_STRING;
            case V_RATE:
                return PAYLOAD_TYPE.PL_STRING;
            case V_POSITION:
                return PAYLOAD_TYPE.PL_GPS;
            case V_ORP:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_PH:
                return PAYLOAD_TYPE.PL_DOUBLE;
            default:
                //Make default to string
                return PAYLOAD_TYPE.PL_STRING;
        }
    }

    //HVAC Options flow state
    public static final HashMap<String, String> HVAC_OPTIONS_FLOW_STATE;
    static {
        HVAC_OPTIONS_FLOW_STATE = new HashMap<String, String>();
        HVAC_OPTIONS_FLOW_STATE.put("AutoChangeOver", "Auto Change Over");
        HVAC_OPTIONS_FLOW_STATE.put("HeatOn", "Heat On");
        HVAC_OPTIONS_FLOW_STATE.put("CoolOn", "Cool On");
        HVAC_OPTIONS_FLOW_STATE.put("Off", "Off");
    }

    //HVAC Options flow state
    public static final HashMap<String, String> HVAC_OPTIONS_FLOW_MODE;
    static {
        HVAC_OPTIONS_FLOW_MODE = new HashMap<String, String>();
        HVAC_OPTIONS_FLOW_MODE.put("Auto", "Auto");
        HVAC_OPTIONS_FLOW_MODE.put("ContinuousOn", "Continuous On");
        HVAC_OPTIONS_FLOW_MODE.put("PeriodicOn", "Periodic On");
    }

    //HVAC heater options - HVAC fan speed
    public static final HashMap<String, String> HVAC_OPTIONS_FAN_SPEED;
    static {
        HVAC_OPTIONS_FAN_SPEED = new HashMap<String, String>();
        HVAC_OPTIONS_FAN_SPEED.put("Min", "Minimum");
        HVAC_OPTIONS_FAN_SPEED.put("Normal", "Normal");
        HVAC_OPTIONS_FAN_SPEED.put("Max", "Maximum");
        HVAC_OPTIONS_FAN_SPEED.put("Auto", "Auto");
    }

    public static String getMetricType() {
        if (AppProperties.getInstance().getControllerSettings().getUnitConfig() != null) {
            return AppProperties.getInstance().getControllerSettings().getUnitConfig();
        }
        return UNIT_CONFIG.METRIC.getText();
    }
}
