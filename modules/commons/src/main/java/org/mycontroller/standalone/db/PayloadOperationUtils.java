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
package org.mycontroller.standalone.db;

import org.mycontroller.standalone.MycUtils;

public class PayloadOperationUtils {

    private PayloadOperationUtils() {

    }

    //http://www.tutorialspoint.com/cprogramming/c_operators.htm
    public enum SEND_PAYLOAD_OPERATIONS {
        TOGGLE("Toggle"),
        INCREMENT("++"),
        DECREMENT("--"),
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLIE("*"),
        DIVIDE("/"),
        MODULUS("%"),
        START("Start"),
        STOP("Stop"),
        REBOOT("Reboot"),
        RELOAD("Reload"),
        ENABLE("Enable"),
        DISABLE("Disable"),
        ON("On"),
        OFF("Off");

        public static SEND_PAYLOAD_OPERATIONS get(int id) {
            for (SEND_PAYLOAD_OPERATIONS type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private SEND_PAYLOAD_OPERATIONS(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static SEND_PAYLOAD_OPERATIONS fromString(String text) {
            if (text != null) {
                for (SEND_PAYLOAD_OPERATIONS type : SEND_PAYLOAD_OPERATIONS.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static synchronized String getPayload(PayloadOperation specialOperation, String orginalValue) {
        String revisedValue;
        if (orginalValue == null) {
            orginalValue = "0";//Note if there is no value on variable, assumes as zero as reference
        }
        switch (specialOperation.getOperationType()) {
            case TOGGLE:
                revisedValue = MycUtils.getDoubleAsString(Double.valueOf(orginalValue) > 0 ? 0.0 : 1.0);
                break;
            case INCREMENT:
                revisedValue = MycUtils.getDoubleAsString(Double.valueOf(orginalValue) + 1);
                break;
            case DECREMENT:
                revisedValue = MycUtils.getDoubleAsString(Double.valueOf(orginalValue) - 1);
                break;
            case ADD:
                revisedValue =
                        MycUtils.getDoubleAsString(Double.valueOf(orginalValue) + specialOperation.getValue());
                break;
            case SUBTRACT:
                revisedValue =
                        MycUtils.getDoubleAsString(Double.valueOf(orginalValue) - specialOperation.getValue());
                break;
            case MULTIPLIE:
                revisedValue =
                        MycUtils.getDoubleAsString(Double.valueOf(orginalValue) * specialOperation.getValue());
                break;
            case DIVIDE:
                revisedValue =
                        MycUtils.getDoubleAsString(Double.valueOf(orginalValue) / specialOperation.getValue());
                break;
            case MODULUS:
                revisedValue =
                        MycUtils.getDoubleAsString(Double.valueOf(orginalValue) % specialOperation.getValue());
                break;
            case REBOOT:
                revisedValue = specialOperation.getOperationType().getText();
                break;
            default:
                revisedValue = null;
                break;
        }
        return revisedValue;
    }
}
