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
package org.mycontroller.standalone.provider.rflink;

import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RFLink {
    public static final String EMPTY_DATA = "";
    public static final int NEGATIVE_SIGN = 32768;
    public static final String KEY_PROTOCOL = "protocol";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ID = "id";
    public static final double DIMMER_REF = 0.15;

    // Type of sensor data (for set/req/ack messages)
    public enum RFLINK_MESSAGE_TYPE {
        TEMP(MESSAGE_TYPE_SET_REQ.V_TEMP.getText()),
        SET_LEVEL(MESSAGE_TYPE_SET_REQ.V_PERCENTAGE.getText()),
        HUM(MESSAGE_TYPE_SET_REQ.V_HUM.getText()),
        BFORECAST(MESSAGE_TYPE_SET_REQ.V_FORECAST.getText()),
        CMD(MESSAGE_TYPE_SET_REQ.V_STATUS.getText()),
        BARO(MESSAGE_TYPE_SET_REQ.V_PRESSURE.getText()),
        HSTATUS(MESSAGE_TYPE_SET_REQ.V_VAR1.getText()),
        UV(MESSAGE_TYPE_SET_REQ.V_UV.getText()),
        LUX(MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL.getText()),
        RAIN(MESSAGE_TYPE_SET_REQ.V_RAIN.getText()),
        RAINRATE(MESSAGE_TYPE_SET_REQ.V_RAINRATE.getText()),
        WINSP(MESSAGE_TYPE_SET_REQ.V_WIND.getText()),
        AWINSP(MESSAGE_TYPE_SET_REQ.V_VAR1.getText()),
        WINGS(MESSAGE_TYPE_SET_REQ.V_GUST.getText()),
        WINDIR(MESSAGE_TYPE_SET_REQ.V_DIRECTION.getText()),
        WINCHL(MESSAGE_TYPE_SET_REQ.V_VAR1.getText()),
        WINTMP(MESSAGE_TYPE_SET_REQ.V_VAR1.getText()),
        CHIME(MESSAGE_TYPE_SET_REQ.V_VAR1.getText()),
        SMOKEALERT(MESSAGE_TYPE_SET_REQ.V_ARMED.getText()),
        PIR(MESSAGE_TYPE_SET_REQ.V_ARMED.getText()),
        CO2(MESSAGE_TYPE_SET_REQ.V_LEVEL.getText()),
        SOUND(MESSAGE_TYPE_SET_REQ.V_LEVEL.getText()),
        KWATT(MESSAGE_TYPE_SET_REQ.V_WATT.getText()),
        WATT(MESSAGE_TYPE_SET_REQ.V_WATT.getText()),
        CURRENT(MESSAGE_TYPE_SET_REQ.V_CURRENT.getText()),
        CURRENT2(MESSAGE_TYPE_SET_REQ.V_VAR2.getText()),
        CURRENT3(MESSAGE_TYPE_SET_REQ.V_VAR3.getText()),
        DIST(MESSAGE_TYPE_SET_REQ.V_DISTANCE.getText()),
        METER(MESSAGE_TYPE_SET_REQ.V_VAR1.getText()),
        VOLT(MESSAGE_TYPE_SET_REQ.V_VOLTAGE.getText()),
        RGBW(MESSAGE_TYPE_SET_REQ.V_RGBW.getText()),
        UP(MESSAGE_TYPE_SET_REQ.V_UP.getText()),
        DOWN(MESSAGE_TYPE_SET_REQ.V_DOWN.getText()),
        STOP(MESSAGE_TYPE_SET_REQ.V_STOP.getText());

        public static RFLINK_MESSAGE_TYPE get(int id) {
            for (RFLINK_MESSAGE_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return text;
        }

        private RFLINK_MESSAGE_TYPE(String text) {
            this.text = text;
        }

        public static RFLINK_MESSAGE_TYPE fromString(String text) {
            if (text != null) {
                for (RFLINK_MESSAGE_TYPE type : RFLINK_MESSAGE_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static String getPayload(String hexValue, double divideBy, boolean checkNegativeSign) {
        //32768
        int value = Integer.parseInt(hexValue.trim(), 16);
        if (checkNegativeSign && value >= NEGATIVE_SIGN) {
            value = -1 * (value - NEGATIVE_SIGN);
        }
        return McUtils.getDoubleAsString(value / divideBy);
    }

    public static String getPayload(String hexValue, boolean checkNegativeSign) {
        return getPayload(hexValue, 0.0, checkNegativeSign);
    }
}
