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
package org.mycontroller.standalone.provider.phantio;

import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.gateway.config.GatewayConfigPhantIO;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.IMessageParser;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public class MessageParserPhantIO implements IMessageParser<MessagePhantIO> {

    @Override
    public IMessage getMessage(GatewayConfig config, MessagePhantIO rawMessage) throws MessageParserException {
        GatewayConfigPhantIO _config = (GatewayConfigPhantIO) config;
        MESSAGE_TYPE messageType = MESSAGE_TYPE.C_SET;
        String nodeEui = _config.getPublicKey();
        String sensorId = rawMessage.getKey();
        MESSAGE_TYPE_SET_REQ dataSubType = MESSAGE_TYPE_SET_REQ.fromString(rawMessage.getKey());
        if (dataSubType == null) {
            dataSubType = MESSAGE_TYPE_SET_REQ.V_VAR1;
        }

        return IMessage.builder()
                .ack(IMessage.NO_ACK)
                .gatewayId(_config.getId())
                .nodeEui(nodeEui)
                .sensorId(sensorId)
                .type(messageType.getText())
                .subType(dataSubType.getText())
                .timestamp(rawMessage.getTimestamp())
                .isTxMessage(false)
                .payload(rawMessage.getValue())
                .build();
    }

    @Override
    public MessagePhantIO getGatewayData(IMessage message) throws MessageParserException {
        return MessagePhantIO.builder()
                .key(message.getSensorId())
                .value(message.getPayload())
                .timestamp(message.getTimestamp())
                .build();
    }

}
