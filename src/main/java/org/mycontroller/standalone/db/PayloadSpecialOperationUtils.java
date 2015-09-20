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
package org.mycontroller.standalone.db;

import org.mycontroller.standalone.NumericUtils;

public class PayloadSpecialOperationUtils {

    private PayloadSpecialOperationUtils() {

    }

    //http://www.tutorialspoint.com/cprogramming/c_operators.htm
    public enum SEND_PAYLOAD_OPERATIONS {
        INVERT("!"),
        INCREMENT("++"),
        DECREMENT("--"),
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLIE("*"),
        DIVIDE("/"),
        MODULUS("%"),
        REBOOT("reboot");
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

        public String value() {
            return this.value;
        }

        public static SEND_PAYLOAD_OPERATIONS findByValue(String value) {
            for (SEND_PAYLOAD_OPERATIONS type : values()) {
                if (type.value().equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    public static synchronized String getPayload(PayloadSpecialOperation specialOperation, String orginalValue) {
        String revisedValue;
        switch (specialOperation.getOperationType()) {
            case INVERT:
                revisedValue = NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) > 0 ? 0.0 : 1.0);
                break;
            case INCREMENT:
                revisedValue = NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) + 1);
                break;
            case DECREMENT:
                revisedValue = NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) - 1);
                break;
            case ADD:
                revisedValue =
                        NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) + specialOperation.getValue());
                break;
            case SUBTRACT:
                revisedValue =
                        NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) - specialOperation.getValue());
                break;
            case MULTIPLIE:
                revisedValue =
                        NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) * specialOperation.getValue());
                break;
            case DIVIDE:
                revisedValue =
                        NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) / specialOperation.getValue());
                break;
            case MODULUS:
                revisedValue =
                        NumericUtils.getDoubleAsString(Double.valueOf(orginalValue) % specialOperation.getValue());
                break;
            case REBOOT:
                revisedValue = specialOperation.getOperationType().value();
                break;
            default:
                revisedValue = null;
                break;
        }
        return revisedValue;
    }
}
