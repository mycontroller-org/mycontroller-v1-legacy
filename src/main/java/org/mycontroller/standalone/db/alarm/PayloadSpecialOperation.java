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
package org.mycontroller.standalone.db.alarm;

import org.mycontroller.standalone.db.AlarmUtils;
import org.mycontroller.standalone.db.AlarmUtils.SEND_PAYLOAD_OPERATIONS;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class PayloadSpecialOperation {

    public PayloadSpecialOperation(String payload) {
        this.operationType = AlarmUtils.SEND_PAYLOAD_OPERATIONS.findByValue(payload.toLowerCase());
        if (this.operationType == null) {
            this.operationType = AlarmUtils.SEND_PAYLOAD_OPERATIONS.findByValue(payload.substring(0, 1).toLowerCase());
            if (this.operationType != null) {
                this.value = Double.valueOf(payload.substring(1));
            }
        }
    }

    private SEND_PAYLOAD_OPERATIONS operationType;
    private Double value;

    public Double getValue() {
        return this.value;
    }

    public SEND_PAYLOAD_OPERATIONS getOperationType() {
        return this.operationType;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Operation:").append(this.operationType.value());
        builder.append(", Value:").append(this.value);
        return builder.toString();
    }
}
