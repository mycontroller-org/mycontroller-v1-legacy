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
package org.mycontroller.standalone.provider.mycontroller;

import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.provider.IMessageParser;
import org.mycontroller.standalone.provider.MessageMQTT;
import org.mycontroller.standalone.utils.McUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public class MessageParserMyController implements IMessageParser<MessageMQTT> {

    @Override
    public IMessage getMessage(GatewayConfig _config, MessageMQTT mqttData) throws MessageParserException {
        IMessage message = IMessage.builder()
                .timestamp(System.currentTimeMillis())
                .gatewayId(_config.getId())
                .isTxMessage(false)
                .payload(mqttData.getPayload())
                .build();
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-EUI/SENSOR_ID/MESSAGE_TYPE/MESSAGE_SUB_TYPE/ACK
        String[] msgArry = mqttData.getTopic().split("/");
        int index = msgArry.length - 5;
        if (msgArry.length >= 6) {
            message.setNodeEui(msgArry[index]);
            message.setSensorId(msgArry[index + 1]);
            message.setType(MESSAGE_TYPE.valueOf(msgArry[index + 2]).getText());
            message.setSubType(getSubType(message.getType(), msgArry[index + 3]));
            message.setAck(McUtils.getInteger(msgArry[index + 4]));
            return message;
        } else {
            throw new MessageParserException("Unknown message format: " + mqttData);
        }
    }

    @Override
    public MessageMQTT getGatewayData(IMessage message) throws MessageParserException {
        return MessageMQTT.builder()
                .gatewayId(message.getGatewayId())
                .payload(message.getPayload() != null ? message.getPayload() : MyController.EMPTY_DATA)
                .topic(getMqttTopic(message))
                .build();
    }

    private String getSubType(String type, String subType) {
        switch (MESSAGE_TYPE.fromString(type)) {
            case C_PRESENTATION:
                return MESSAGE_TYPE_PRESENTATION.valueOf(subType).getText();
            case C_INTERNAL:
                return MESSAGE_TYPE_INTERNAL.valueOf(subType).getText();
            case C_REQ:
            case C_SET:
                return MESSAGE_TYPE_SET_REQ.valueOf(subType).getText();
            case C_STREAM:
                return MESSAGE_TYPE_STREAM.valueOf(subType).getText();
            default:
                return null;
        }
    }

    private String getSubTypeName(String type, String subType) {
        switch (MESSAGE_TYPE.fromString(type)) {
            case C_PRESENTATION:
                return MESSAGE_TYPE_PRESENTATION.fromString(subType).name();
            case C_INTERNAL:
                return MESSAGE_TYPE_INTERNAL.fromString(subType).name();
            case C_REQ:
            case C_SET:
                return MESSAGE_TYPE_SET_REQ.fromString(subType).name();
            case C_STREAM:
                return MESSAGE_TYPE_STREAM.fromString(subType).name();
            default:
                return null;
        }
    }

    private String getMqttTopic(IMessage message) {
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-EUI/SENSOR_ID/MESSAGE_TYPE/MESSAGE_SUB_TYPE/ACK
        StringBuilder builder = new StringBuilder();
        builder.append(message.getNodeEui());
        builder.append("/").append(message.getSensorId());
        builder.append("/").append(MESSAGE_TYPE.fromString(message.getType()).name());
        builder.append("/").append(getSubTypeName(message.getType(), message.getSubType()));
        builder.append("/").append(message.getAck());

        return builder.toString();
    }

}
