/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.imperihome;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;

import lombok.experimental.UtilityClass;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@UtilityClass
public class ImperiHomeSSIUtils {
    //private static final Logger _logger = LoggerFactory.getLogger(ImperiHomeSSIUtils.class);

    // Type of devices
    public enum IMPERI_HOME_DEVICE_TYPE {
        S_DOOR("DevDoor"),
        S_MOTION("DevMotion"),
        S_SMOKE("DevSmoke"),
        S_BINARY("DevSwitch"),
        S_DIMMER("DevDimmer"),
        S_COVER("DevShutter"),
        S_TEMP("DevTemperature"),
        S_HUM("DevTempHygro"),
        S_BARO("Barometer"),
        S_WIND("DevWind"),
        S_RAIN("DevRain"),
        S_UV("DevUV"),
        S_WEIGHT("DevGenericSensor"),
        S_POWER("DevElectricity"),
        S_HEATER("DevGenericSensor"),
        S_DISTANCE("DevGenericSensor"),
        S_LIGHT_LEVEL("DevGenericSensor"),
        S_ARDUINO_NODE("Node"),
        S_ARDUINO_REPEATER_NODE("Repeater node"),
        S_LOCK("DevLock"),
        S_IR("DevGenericSensor"),
        S_WATER("DevGenericSensor"),
        S_AIR_QUALITY("DevCO2"),
        S_CUSTOM("DevGenericSensor"),
        S_DUST("DevGenericSensor"),
        S_SCENE_CONTROLLER("DevScene"),
        S_RGB_LIGHT("DevRGBLight"),
        S_RGBW_LIGHT("DevRGBLight"),
        S_COLOR_SENSOR("DevGenericSensor"),
        S_HVAC("DevThermostat"),
        S_MULTIMETER("DevGenericSensor"),
        S_SPRINKLER("DevGenericSensor"),
        S_WATER_LEAK("DevGenericSensor"),
        S_SOUND("DevNoise"),
        S_VIBRATION("DevGenericSensor"),
        S_MOISTURE("DevGenericSensor"),
        S_INFO("DevGenericSensor"),
        S_GAS("DevGenericSensor"),
        S_GPS("DevGenericSensor");

        public static IMPERI_HOME_DEVICE_TYPE get(int id) {
            for (IMPERI_HOME_DEVICE_TYPE type : values()) {
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

        private IMPERI_HOME_DEVICE_TYPE(String text) {
            this.text = text;
        }

        public static IMPERI_HOME_DEVICE_TYPE fromString(String text) {
            if (text != null) {
                for (IMPERI_HOME_DEVICE_TYPE type : IMPERI_HOME_DEVICE_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum DEVICE_PARM_KEY {
        V_TEMP("Value"),
        V_HUM("Value"),
        V_STATUS("Status"),
        V_PERCENTAGE("Level"),
        V_PRESSURE("Value"),
        V_FORECAST("Value"),
        V_RAIN("Value"),
        V_RAINRATE("Value"),
        V_WIND("Speed"),
        V_GUST("Value"),
        V_DIRECTION("Direction"),
        V_UV("Value"),
        V_WEIGHT("Value"),
        V_DISTANCE("Value"),
        V_IMPEDANCE("Value"),
        V_ARMED("Armed"),
        V_TRIPPED("Tripped"),
        V_WATT("Watts"),
        V_KWH("Value"),
        V_SCENE_ON("Value"),
        V_SCENE_OFF("Value"),
        V_HVAC_FLOW_STATE("Value"),
        V_HVAC_SPEED("Value"),
        V_LIGHT_LEVEL("Value"),
        V_VAR1("Value"),
        V_VAR2("Value"),
        V_VAR3("Value"),
        V_VAR4("Value"),
        V_VAR5("Value"),
        V_UP("Value"),
        V_DOWN("Value"),
        V_STOP("Value"),
        V_IR_SEND("Value"),
        V_IR_RECEIVE("Value"),
        V_FLOW("Value"),
        V_VOLUME("Value"),
        V_LOCK_STATUS("Value"),
        V_LEVEL("Level"),
        V_VOLTAGE("Value"),
        V_CURRENT("Value"),
        V_RGB("Value"),

        V_RGBW("color"),

        V_ID("Value"),

        V_UNIT_PREFIX("Value"),

        V_HVAC_SETPOINT_COOL("Value"),
        V_HVAC_SETPOINT_HEAT("Value"),
        V_HVAC_FLOW_MODE("Value"),
        V_TEXT("Value"),
        V_CUSTOM("Value"),
        V_POSITION("Value"),
        V_IR_RECORD("Value");

        public static DEVICE_PARM_KEY get(int id) {
            for (DEVICE_PARM_KEY type : values()) {
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

        private DEVICE_PARM_KEY(String text) {
            this.text = text;
        }

        public static DEVICE_PARM_KEY fromString(String text) {
            if (text != null) {
                for (DEVICE_PARM_KEY type : DEVICE_PARM_KEY.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static Devices getAllDevices() {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAll();
        List<Device> devices = new ArrayList<Device>();
        for (Sensor sensor : sensors) {
            Device device = Device.builder().id(String.valueOf(sensor.getId()))
                    .name(sensor.getName())
                    .room(sensor.getRoom() != null ? String.valueOf(sensor.getRoom().getId()) : null)
                    .type(IMPERI_HOME_DEVICE_TYPE.valueOf(sensor.getType().name()).getText()).build();
            for (SensorVariable sensorVariable : DaoUtils.getSensorVariableDao().getAllBySensorId(sensor.getId())) {
                device.updateDeviceParm(sensorVariable);
            }
            devices.add(device);
        }
        return Devices.builder().devices(devices).build();
    }
}
