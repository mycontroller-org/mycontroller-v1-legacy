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
package org.mycontroller.standalone.message;

import java.io.Serializable;
import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class McMessage implements Serializable {
    /**  */
    private static final long serialVersionUID = -677512912537380066L;

    public static final String SENSOR_BROADCAST_ID = "SENSOR_BC";
    public static final String NODE_BROADCAST_ID = "NODE_BC";
    public static final String PAYLOAD_EMPTY = "";
    public static final String GATEWAY_NODE_ID = "NODE_GY";
    public static final int NO_ACK = 0;
    public static final int ACK_REQUEST = 1;
    public static final int ACK_RESPONSE = 2;

    @NonNull
    private Integer gatewayId;
    private String nodeEui;
    private String sensorId;
    private MESSAGE_TYPE type;
    private String subType;
    private int ack = NO_ACK;
    private String payload;
    private boolean isTxMessage;
    private NETWORK_TYPE networkType;
    private boolean isScreeningDone = false;
    private Long timestamp;
    private HashMap<String, Object> properties = null;

    public boolean validate() {
        if (gatewayId == null
                || nodeEui == null
                || sensorId == null
                || type == null
                || subType == null) {
            return false;
        }
        return true;
    }

    public long getTimestamp() {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public HashMap<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties;
    }

    public boolean isAckResponse() {
        return ack == ACK_RESPONSE;
    }

    public boolean isAckRequest() {
        return ack == ACK_REQUEST;
    }
}
