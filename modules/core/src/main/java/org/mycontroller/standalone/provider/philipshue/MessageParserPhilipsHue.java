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
package org.mycontroller.standalone.provider.philipshue;

import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.provider.IMessageParser;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public class MessageParserPhilipsHue implements IMessageParser<MessagePhilipsHue> {

    @Override
    public IMessage getMessage(GatewayConfig _config, MessagePhilipsHue rawMessage) throws MessageParserException {
        return IMessage.builder()
                .ack(IMessage.NO_ACK)
                .gatewayId(_config.getId())
                .nodeEui(PhilipsHue.NODE_EUI)
                .sensorId(rawMessage.getSensorId())
                .type(rawMessage.getType())
                .subType(rawMessage.getSubType())
                .timestamp(rawMessage.getTimestamp())
                .isTxMessage(false)
                .payload(rawMessage.getPayload())
                .build();
    }

    @Override
    public MessagePhilipsHue getGatewayData(IMessage message) throws MessageParserException {
        return MessagePhilipsHue.builder()
                .type(message.getType())
                .subType(message.getSubType())
                .sensorId(message.getSensorId())
                .timestamp(message.getTimestamp())
                .build();
    }

}
