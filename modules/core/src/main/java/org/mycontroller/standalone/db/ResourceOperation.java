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

import org.mycontroller.standalone.db.ResourceOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class ResourceOperation {

    private SEND_PAYLOAD_OPERATIONS operationType;
    private Double value;
    private String payload;

    public ResourceOperation(String payloadOrg) {
        if (payloadOrg.toLowerCase().startsWith("sp:")) {
            payload = payloadOrg.substring(3);
            this.operationType = SEND_PAYLOAD_OPERATIONS.fromString(payload.toLowerCase().trim());
            if (this.operationType == null) {
                this.operationType = SEND_PAYLOAD_OPERATIONS.fromString(payload.substring(0, 1).toLowerCase());
                if (this.operationType != null) {
                    this.value = McUtils.getDouble(payload.substring(1));
                    this.payload = McUtils.getDoubleAsString(this.value);
                }
            }
        } else {
            payload = payloadOrg;
        }
        _logger.debug("Received payload:{}, Operation:[{}]", payloadOrg, this.toString());
    }

    public Double getValue() {
        return this.value;
    }

    public SEND_PAYLOAD_OPERATIONS getOperationType() {
        return this.operationType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OperationType:").append(getOperationType() != null ? getOperationType().getText() : null);
        builder.append(", Value:").append(this.value);
        builder.append(", Payload:").append(this.payload);
        return builder.toString();
    }

    public String getPayload() {
        return payload;
    }
}
