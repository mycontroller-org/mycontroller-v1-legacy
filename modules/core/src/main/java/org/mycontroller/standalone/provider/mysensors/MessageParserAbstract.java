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
package org.mycontroller.standalone.provider.mysensors;

import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.provider.MessageMQTT;
import org.mycontroller.standalone.provider.mysensors.MySensors.MYS_MESSAGE_TYPE;
import org.mycontroller.standalone.provider.mysensors.MySensors.MYS_MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.provider.mysensors.MySensors.MYS_MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.provider.mysensors.MySensors.MYS_MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mysensors.MySensors.MYS_MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.utils.McUtils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
@ToString
public class MessageParserAbstract {
    private int gatewayId;
    private int nodeId;
    private int childSensorId;
    private int messageType;
    private int ack;
    private int subType;
    private String payload;
    private boolean isTxMessage = false;
    private Long timestamp;

    void update(MessageMQTT messageMQTT) throws MessageParserException {
        _logger.debug("RawData: {}", messageMQTT);
        gatewayId = messageMQTT.getGatewayId();
        timestamp = System.currentTimeMillis();
        isTxMessage = false;
        payload = messageMQTT.getPayload();

        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-OPERATION_TYPE/ACK-FLAG/SUB-OPERATION_TYPE
        String[] msgArry = messageMQTT.getTopic().split("/");
        int index = msgArry.length - 5;
        if (msgArry.length >= 6) {
            nodeId = McUtils.getInteger(msgArry[index]);
            childSensorId = McUtils.getInteger(msgArry[index + 1]);
            messageType = McUtils.getInteger(msgArry[index + 2]);
            ack = McUtils.getInteger(msgArry[index + 3]);
            subType = McUtils.getInteger(msgArry[index + 4]);
            _logger.debug("Message: {}", toString());
        } else {
            _logger.debug("Unknown message format, [{}]", messageMQTT);
            throw new MessageParserException("Unknown message format, " + messageMQTT.toString());
        }
        update();
        validate();

    }

    void update(IMessage message) throws MessageParserException {
        gatewayId = message.getGatewayId();
        if (message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
            nodeId = MySensors.NODE_ID_BROADCAST;
        } else if (message.getNodeEui().equalsIgnoreCase(IMessage.GATEWAY_NODE_ID)) {
            nodeId = MySensors.GATEWAY_ID;
        } else {
            nodeId = McUtils.getInteger(message.getNodeEui());
        }
        if (message.getSensorId().equalsIgnoreCase(IMessage.SENSOR_BROADCAST_ID)) {
            childSensorId = MySensors.SENSOR_ID_BROADCAST;
        } else {
            childSensorId = McUtils.getInteger(message.getSensorId());
        }
        messageType = MYS_MESSAGE_TYPE.fromString(MESSAGE_TYPE.fromString(message.getType()).getText()).ordinal();
        ack = message.getAck();
        subType = getMysensorsSubType(MESSAGE_TYPE.fromString(message.getType()), message.getSubType());
        payload = message.getPayload();
        isTxMessage = message.isTxMessage();
        timestamp = message.getTimestamp();
        update();
        validate();
    }

    void update(Integer gatewayId, String rawData) throws MessageParserException {
        _logger.debug("GatewayId:{}, rawData:[{}]", gatewayId, rawData);
        this.gatewayId = gatewayId;
        timestamp = System.currentTimeMillis();
        isTxMessage = false;
        String[] msgArry = rawData.split(";");
        if (msgArry.length == 6) {
            payload = msgArry[5];
        }
        if (msgArry.length >= 5) {
            nodeId = McUtils.getInteger(msgArry[0]);
            childSensorId = McUtils.getInteger(msgArry[1]);
            messageType = McUtils.getInteger(msgArry[2]);
            ack = McUtils.getInteger(msgArry[3]);
            subType = McUtils.getInteger(msgArry[4]);
            _logger.debug("Message: {}", toString());
        } else {
            _logger.debug("Unknown message format: [gatewayId: {}, payload:{}]", gatewayId, rawData);
            throw new MessageParserException("Unknown message format:[" + rawData + "], gatewayId:" + gatewayId);
        }
        update();
        validate();
    }

    private void update() {
        switch (MYS_MESSAGE_TYPE.get(messageType)) {
            case C_SET:
            case C_REQ:
                updateSetReq();
                break;
            default:
                break;
        }
    }

    private void updateSetReq() {
        switch (MYS_MESSAGE_TYPE_SET_REQ.get(subType)) {
            case V_RGB:
            case V_RGBW:
                //Change RGB and RGBW values
                if (isTxMessage) {
                    if (payload.startsWith("#")) {
                        setPayload(payload.replaceAll("#", ""));
                    }
                } else if (!payload.startsWith("#")) {
                    setPayload("#" + payload);
                }
                break;
            default:
                break;
        }
    }

    private void validate() throws MessageParserException {
        if (nodeId < 0 || nodeId > 255) {
            throw new MessageParserException("Invalid range for 'nodeId':" + this);
        }
        if (childSensorId < 0 || childSensorId > 255) {
            throw new MessageParserException("Invalid range for childSensorId:" + this);
        }
        if (messageType < 0 || messageType > MySensors.MAX_INDEX_MESSAGE_TYPE) {
            throw new MessageParserException("Invalid range for 'command':" + this);
        }
        switch (MYS_MESSAGE_TYPE.get(messageType)) {
            case C_INTERNAL:
                if (subType < 0 || subType > MySensors.MAX_INDEX_INTERNAL) {
                    throw new MessageParserException("Invalid range for 'internal' type:" + this);
                }
                break;
            case C_PRESENTATION:
                if (subType < 0 || subType > MySensors.MAX_INDEX_PRESENTATION) {
                    throw new MessageParserException("Invalid range for 'presentation' type:" + this);
                }
                break;
            case C_SET:
            case C_REQ:
                if (subType < 0 || subType > MySensors.MAX_INDEX_SET_REQ) {
                    throw new MessageParserException("Invalid range for 'set/req' type:" + this);
                }
                break;
            case C_STREAM:
                if (subType < 0 || subType > MySensors.MAX_INDEX_STREAM) {
                    throw new MessageParserException("Invalid range for 'stream' type:" + this);
                }
                break;
        }
        if (ack < 0 || ack > 1) {
            throw new MessageParserException("Invalid range for 'ack':" + this);
        }
        if (payload == null) {
            payload = MySensors.EMPTY_DATA;
        }
    }

    private String getNodeEui() {
        return String.valueOf(nodeId);
    }

    private String getChildSensorIdString() {
        return String.valueOf(childSensorId);
    }

    private void setPayload(Object payload) {
        this.payload = String.valueOf(payload);
    }

    public MessageMQTT getMessageMqtt() {
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-OPERATION_TYPE/ACK-FLAG/SUB-OPERATION_TYPE
        StringBuilder builder = new StringBuilder();
        builder.append(nodeId);
        builder.append("/").append(childSensorId);
        builder.append("/").append(messageType);
        builder.append("/").append(ack);
        builder.append("/").append(subType);

        return MessageMQTT.builder()
                .gatewayId(gatewayId)
                .topic(builder.toString())
                .payload(payload)
                .build();
    }

    String getSerialString() {
        StringBuffer message = new StringBuffer();
        message.append(nodeId).append(";");
        message.append(childSensorId).append(";");
        message.append(messageType).append(";");
        message.append(ack).append(";");
        message.append(subType).append(";");
        message.append(payload).append("\n");
        return message.toString();
    }

    public IMessage getMessage() {
        String sensorIdMc = childSensorId == 255 ? IMessage.SENSOR_BROADCAST_ID : getChildSensorIdString();
        String nodeIdMc = nodeId == 255 ? IMessage.NODE_BROADCAST_ID : getNodeEui();
        IMessage message = IMessage.builder()
                .ack(ack == 1 ? 2 : 0)//MySensors gateway never request ack
                .gatewayId(gatewayId)
                .nodeEui(nodeIdMc)
                .sensorId(sensorIdMc)
                .isTxMessage(isTxMessage)
                .payload(payload)
                .timestamp(timestamp != null ? timestamp : System.currentTimeMillis())
                .build();
        updateMcMessageTypeAndSubType(message);
        return message;
    }

    private void updateMcMessageTypeAndSubType(IMessage message) {
        message.setType(MYS_MESSAGE_TYPE.get(messageType).getText());
        switch (MYS_MESSAGE_TYPE.get(messageType)) {
            case C_PRESENTATION:
                message.setSubType(MYS_MESSAGE_TYPE_PRESENTATION.get(subType).getText());
                break;
            case C_INTERNAL:
                message.setSubType(MYS_MESSAGE_TYPE_INTERNAL.get(subType).getText());
                break;
            case C_REQ:
                message.setSubType(MYS_MESSAGE_TYPE_SET_REQ.get(subType).getText());
                break;
            case C_SET:
                if (!isTxMessage
                        && MYS_MESSAGE_TYPE_SET_REQ.get(subType) == MYS_MESSAGE_TYPE_SET_REQ.V_VAR5
                        && payload != null) {
                    if (payload.startsWith(MySensors.KEY_RSSI)) {
                        message.setType(MESSAGE_TYPE.C_INTERNAL.getText());
                        message.setSubType(MESSAGE_TYPE_INTERNAL.I_RSSI.getText());
                        message.setPayload(payload.replace(MySensors.KEY_RSSI, "").trim());
                        message.setSensorId(IMessage.SENSOR_BROADCAST_ID);
                    } else if (payload.startsWith(MySensors.KEY_PROPERTIES)) {
                        message.setType(MESSAGE_TYPE.C_INTERNAL.getText());
                        message.setSubType(MESSAGE_TYPE_INTERNAL.I_PROPERTIES.getText());
                        message.setPayload(payload.replace(MySensors.KEY_PROPERTIES, "").trim());
                        message.setSensorId(IMessage.SENSOR_BROADCAST_ID);
                    } else {
                        message.setSubType(MYS_MESSAGE_TYPE_SET_REQ.get(subType).getText());
                    }
                } else {
                    message.setSubType(MYS_MESSAGE_TYPE_SET_REQ.get(subType).getText());
                }
                break;
            case C_STREAM:
                message.setSubType(MYS_MESSAGE_TYPE_STREAM.get(subType).getText());
                break;
            default:
                break;
        }
    }

    private Integer getMysensorsSubType(MESSAGE_TYPE messageType, String mcSubType) {
        switch (messageType) {
            case C_PRESENTATION:
                return MYS_MESSAGE_TYPE_PRESENTATION.fromString(mcSubType).ordinal();
            case C_INTERNAL:
                return MYS_MESSAGE_TYPE_INTERNAL.fromString(mcSubType).ordinal();
            case C_REQ:
            case C_SET:
                return MYS_MESSAGE_TYPE_SET_REQ.fromString(mcSubType).ordinal();
            case C_STREAM:
                return MYS_MESSAGE_TYPE_STREAM.fromString(mcSubType).ordinal();
            default:
                return null;
        }
    }
}
