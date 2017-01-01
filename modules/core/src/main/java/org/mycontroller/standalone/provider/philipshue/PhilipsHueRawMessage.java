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
package org.mycontroller.standalone.provider.philipshue;

import java.util.Arrays;
import java.util.List;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.model.GatewayPhilipsHue;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;

import lombok.Data;
import lombok.ToString;

/**
 * @author Fraid(https://github.com/Fraid)
 */
@Data
@ToString
public class PhilipsHueRawMessage {

    private int gatewayId;
    private String url;
    private String sensorId;
    private MESSAGE_TYPE messageType;
    private String subType;
    private String payload;
    private Long timestamp;
    private boolean isTxMessage = false;
    private String name;
    private String colormode;

    public PhilipsHueRawMessage(RawMessage rawMessage) throws RawMessageException {
        IGateway gateway = McObjectManager.getGateway(rawMessage.getGatewayId());
        GatewayPhilipsHue gatewayPhilipsHue = (GatewayPhilipsHue) gateway
                .getGateway();
        @SuppressWarnings("unchecked")
        List<Object> data = (List<Object>) rawMessage.getData();
        //Data order: key, value,type,subType
        if (data.size() != 4) {
            throw new RawMessageException("data size should be exactly 4, Current data: " + data);
        }
        isTxMessage = rawMessage.isTxMessage();
        messageType = MESSAGE_TYPE.C_SET;

        gatewayId = rawMessage.getGatewayId();
        url = gatewayPhilipsHue.getUrl();
        sensorId = (String) data.get(0);
        subType = (String) data.get(3);
        payload = (String) data.get(1);
        name = (String) data.get(2);
        PhilipsHueEngine.updateMessage(this);
    }

    public PhilipsHueRawMessage(McMessage mcMessage) throws RawMessageException {
        gatewayId = mcMessage.getGatewayId();
        url = mcMessage.getNodeEui();
        sensorId = mcMessage.getSensorId();
        messageType = mcMessage.getType();
        subType = mcMessage.getSubType();
        payload = mcMessage.getPayload();
        isTxMessage = mcMessage.isTxMessage();
        timestamp = mcMessage.getTimestamp();
        PhilipsHueEngine.updateMessage(this);
    }

    public void setPayload(Object payload) {
        this.payload = String.valueOf(payload);
    }

    public RawMessage getRawMessage() {
        return RawMessage.builder()
                .gatewayId(gatewayId)
                .data(Arrays.asList(sensorId, payload, messageType.getText(), subType))
                .build();
    }

    public McMessage getMcMessage() {
        return McMessage.builder()
                .ack(McMessage.NO_ACK)
                .gatewayId(getGatewayId())
                .nodeEui(getUrl())
                .sensorId(getSensorId())
                .networkType(NETWORK_TYPE.PHILIPS_HUE)
                .type(getMessageType())
                .subType(getSubType())
                .timestamp(getTimestamp())
                .isTxMessage(isTxMessage())
                .payload(getPayload()).build();
    }
}
