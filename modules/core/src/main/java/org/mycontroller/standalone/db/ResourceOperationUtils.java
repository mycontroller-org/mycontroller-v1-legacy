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

import org.mycontroller.standalone.utils.McUtils;

public class ResourceOperationUtils {

    private ResourceOperationUtils() {

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

    public static synchronized String getPayload(ResourceOperation specialOperation, String orginalValue) {
        String revisedValue;
        if (orginalValue == null) {
            orginalValue = "0";//Note if there is no value on variable, assumes as zero as reference
        }
        switch (specialOperation.getOperationType()) {
            case TOGGLE:
                revisedValue = McUtils.getDoubleAsString(Double.valueOf(orginalValue) > 0 ? 0.0 : 1.0, 0);
                break;
            case INCREMENT:
                revisedValue = McUtils.getDoubleAsString(Double.valueOf(orginalValue) + 1);
                break;
            case DECREMENT:
                revisedValue = McUtils.getDoubleAsString(Double.valueOf(orginalValue) - 1);
                break;
            case ADD:
                revisedValue =
                        McUtils.getDoubleAsString(Double.valueOf(orginalValue) + specialOperation.getValue());
                break;
            case SUBTRACT:
                revisedValue =
                        McUtils.getDoubleAsString(Double.valueOf(orginalValue) - specialOperation.getValue());
                break;
            case MULTIPLIE:
                revisedValue =
                        McUtils.getDoubleAsString(Double.valueOf(orginalValue) * specialOperation.getValue());
                break;
            case DIVIDE:
                revisedValue =
                        McUtils.getDoubleAsString(Double.valueOf(orginalValue) / specialOperation.getValue());
                break;
            case MODULUS:
                revisedValue =
                        McUtils.getDoubleAsString(Double.valueOf(orginalValue) % specialOperation.getValue());
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
