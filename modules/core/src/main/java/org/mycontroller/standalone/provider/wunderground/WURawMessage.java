/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.provider.wunderground;

import java.util.Arrays;
import java.util.List;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.1.0
 */
@Data
@ToString
public class WURawMessage {

    private int gatewayId;
    private String nodeEui;
    private String sensorId;
    private MESSAGE_TYPE messageType;
    private String subType;
    private String payload;
    private Long timestamp;
    private boolean isTxMessage = false;
    private String colormode;

    public WURawMessage(RawMessage rawMessage) throws RawMessageException {
        @SuppressWarnings("unchecked")
        List<Object> data = (List<Object>) rawMessage.getData();
        // Data order: node EUI, messageType, subType, sensorId, payload
        if (data.size() != 5) {
            throw new RawMessageException(
                    "data size should be exactly 5, Current data: " + data);
        }
        isTxMessage = rawMessage.isTxMessage();

        gatewayId = rawMessage.getGatewayId();

        nodeEui = (String) data.get(0);
        messageType = MESSAGE_TYPE.fromString((String) data.get(1));
        subType = (String) data.get(2);
        sensorId = (String) data.get(3);
        payload = (String) data.get(4);
        timestamp = rawMessage.getTimestamp();
        WUEngine.updateMessage(this);
    }

    public WURawMessage(McMessage mcMessage) throws RawMessageException {
        gatewayId = mcMessage.getGatewayId();
        nodeEui = mcMessage.getNodeEui();
        sensorId = mcMessage.getSensorId();
        messageType = mcMessage.getType();
        subType = mcMessage.getSubType();
        payload = mcMessage.getPayload();
        isTxMessage = mcMessage.isTxMessage();
        timestamp = mcMessage.getTimestamp();
        WUEngine.updateMessage(this);
    }

    public void setPayload(Object payload) {
        this.payload = String.valueOf(payload);
    }

    public RawMessage getRawMessage() {
        return RawMessage
                .builder()
                .gatewayId(gatewayId)
                .isTxMessage(isTxMessage())
                .timestamp(getTimestamp())
                // Data order: nodeEui, messageType, subType, sensorId, payload
                .data(Arrays.asList(nodeEui, messageType.getText(), subType, sensorId, payload))
                .build();
    }

    public McMessage getMcMessage() {
        return McMessage.builder()
                .ack(McMessage.NO_ACK)
                .gatewayId(getGatewayId())
                .nodeEui(getNodeEui())
                .sensorId(getSensorId())
                .networkType(NETWORK_TYPE.WUNDERGROUND)
                .type(getMessageType())
                .subType(getSubType())
                .timestamp(getTimestamp())
                .isTxMessage(isTxMessage())
                .payload(getPayload())
                .build();
    }
}
