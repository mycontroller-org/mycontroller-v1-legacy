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
package org.mycontroller.standalone.provider.phantio;

import java.util.Arrays;
import java.util.List;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.gateway.model.GatewayPhantIO;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
public class PhantIORawMessage {

    private int gatewayId;
    private String nodeEui;
    private String sensorId;
    private MESSAGE_TYPE messageType;
    private String subType;
    private String payload;
    private Long timestamp;
    private boolean isTxMessage = false;

    public PhantIORawMessage(RawMessage rawMessage) throws RawMessageException {
        GatewayPhantIO gatewayPhantIO = (GatewayPhantIO) McObjectManager.getGateway(rawMessage.getGatewayId())
                .getGateway();
        @SuppressWarnings("unchecked")
        List<Object> data = (List<Object>) rawMessage.getData();
        //Data order: key, value, timestamp
        if (data.size() != 3) {
            throw new RawMessageException("data size should be exactly 3, Current data: " + data);
        }
        isTxMessage = rawMessage.isTxMessage();
        messageType = MESSAGE_TYPE.C_SET;

        gatewayId = rawMessage.getGatewayId();
        nodeEui = gatewayPhantIO.getPublicKey();
        sensorId = (String) data.get(0);
        MESSAGE_TYPE_SET_REQ dataSubType = MESSAGE_TYPE_SET_REQ.fromString((String) data.get(0));
        if (dataSubType == null) {
            dataSubType = MESSAGE_TYPE_SET_REQ.V_VAR1;
        }
        subType = dataSubType.getText();
        payload = (String) data.get(1);
        timestamp = (Long) data.get(2);
        PhantIOEngine.updateMessage(this);
    }

    public PhantIORawMessage(McMessage mcMessage) throws RawMessageException {
        gatewayId = mcMessage.getGatewayId();
        nodeEui = mcMessage.getNodeEui();
        sensorId = mcMessage.getSensorId();
        messageType = mcMessage.getType();
        subType = mcMessage.getSubType();
        payload = mcMessage.getPayload();
        isTxMessage = mcMessage.isTxMessage();
        timestamp = mcMessage.getTimestamp();
        PhantIOEngine.updateMessage(this);
    }

    public void setPayload(Object payload) {
        this.payload = String.valueOf(payload);
    }

    public RawMessage getRawMessage() {
        return RawMessage.builder()
                .gatewayId(gatewayId)
                .data(Arrays.asList(sensorId, payload))
                .build();
    }

    public McMessage getMcMessage() {
        return McMessage.builder()
                .acknowledge(false)
                .gatewayId(gatewayId)
                .nodeEui(nodeEui)
                .sensorId(sensorId)
                .networkType(NETWORK_TYPE.PHANT_IO)
                .type(messageType)
                .subType(subType)
                .timestamp(timestamp)
                .isTxMessage(isTxMessage())
                .payload(getPayload()).build();
    }
}
