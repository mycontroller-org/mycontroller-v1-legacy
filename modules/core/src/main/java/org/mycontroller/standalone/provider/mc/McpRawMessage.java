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
package org.mycontroller.standalone.provider.mc;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.gateway.model.GatewayMQTT;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
@Slf4j
public class McpRawMessage {

    private Integer gatewayId;
    private String nodeEui;
    private String sensorId;
    private MESSAGE_TYPE messageType;
    private String subType;
    private String payload;
    private Long timestamp;
    private boolean isTxMessage = false;
    private String topicsPublish = null;

    public McpRawMessage(String topicsPublish) {
        this.topicsPublish = topicsPublish;
    }

    public McpRawMessage(RawMessage rawMessage) throws RawMessageException {
        gatewayId = rawMessage.getGatewayId();
        isTxMessage = rawMessage.isTxMessage();
        updateMQTTMessage(rawMessage.getSubData(), (String) rawMessage.getData());
        McpEngine.updateMessage(this);
    }

    public McpRawMessage(McMessage mcMessage) throws RawMessageException {
        gatewayId = mcMessage.getGatewayId();
        nodeEui = mcMessage.getNodeEui();
        sensorId = mcMessage.getSensorId();
        messageType = mcMessage.getType();
        subType = getMessageSubType(mcMessage.getType(), mcMessage.getSubType());
        payload = mcMessage.getPayload();
        isTxMessage = mcMessage.isTxMessage();
        McpEngine.updateMessage(this);
    }

    private void updateMQTTMessage(String topic, String message) throws RawMessageException {
        if (message != null) {
            payload = message;
        }
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-EUI/SENSOR_ID/MESSAGE_TYPE/MESSAGE_SUB_TYPE
        String[] msgArry = topic.split("/");
        if (msgArry.length == 5) {
            nodeEui = msgArry[1];
            sensorId = msgArry[2];
            messageType = MESSAGE_TYPE.valueOf(msgArry[3]);
            subType = msgArry[4];
            _logger.debug("Message: {}", toString());
        } else {
            _logger.debug("Unknown message format, Topic:[{}], Payload:[{}]", topic, message);
            throw new RawMessageException("Unknown message format, Topic:" + topic + ", Payload:" + message);
        }
    }

    public String getMqttTopic() {
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-EUI/SENSOR_ID/MESSAGE_TYPE/MESSAGE_SUB_TYPE
        String[] topicsPublishList = null;
        if (topicsPublish != null) {
            topicsPublishList = topicsPublish.split(GatewayMQTT.TOPICS_SPLITER);
        } else {
            topicsPublishList = ((GatewayMQTT) McObjectManager.getGateway(gatewayId).getGateway())
                    .getTopicsPublish().split(GatewayMQTT.TOPICS_SPLITER);
        }
        StringBuilder builder = new StringBuilder();
        for (String topic : topicsPublishList) {
            if (builder.length() > 0) {
                builder.append(GatewayMQTT.TOPICS_SPLITER);
            }
            builder.append(topic.trim());
            builder.append("/").append(getNodeEui());
            builder.append("/").append(getSensorId());
            builder.append("/").append(getMessageType());
            builder.append("/").append(getSubType());
        }
        return builder.toString();
    }

    public RawMessage getRawMessage() {
        return RawMessage.builder()
                .gatewayId(gatewayId)
                .networkType(NETWORK_TYPE.MY_CONTROLLER)
                .data(getPayload())
                .subData(getMqttTopic())
                .isTxMessage(isTxMessage())
                .build();
    }

    public McMessage getMcMessage() {
        return McMessage.builder()
                .acknowledge(false)
                .gatewayId(gatewayId)
                .nodeEui(nodeEui)
                .sensorId(sensorId)
                .networkType(NETWORK_TYPE.MY_CONTROLLER)
                .type(messageType)
                .subType(getMcMessageSubType())
                .isTxMessage(isTxMessage())
                .payload(getPayload()).build();
    }

    private String getMcMessageSubType() {
        switch (messageType) {
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

    public String getMessageSubType(MESSAGE_TYPE messageType, String mcSubType) {
        switch (messageType) {
            case C_PRESENTATION:
                return MESSAGE_TYPE_PRESENTATION.fromString(mcSubType).name();
            case C_INTERNAL:
                return MESSAGE_TYPE_INTERNAL.fromString(mcSubType).name();
            case C_REQ:
            case C_SET:
                return MESSAGE_TYPE_SET_REQ.fromString(mcSubType).name();
            case C_STREAM:
                return MESSAGE_TYPE_STREAM.fromString(mcSubType).name();
            default:
                return null;
        }
    }
}
