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
package org.mycontroller.standalone.mysensors;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewayMQTT;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MySensorsRawMessage {
    private static final Logger _logger = LoggerFactory.getLogger(MySensorsRawMessage.class.getName());

    private int gatewayId;
    private int nodeId;
    private int childSensorId;
    private int messageType;
    private int ack;
    private int subType;
    private String payload;
    private boolean isTxMessage = false;

    public MySensorsRawMessage(int gatewayId) {
        this.gatewayId = gatewayId;
    }

    public MySensorsRawMessage(RawMessage rawMessage) throws RawMessageException {
        this.gatewayId = rawMessage.getGatewayId();
        this.isTxMessage = rawMessage.isTxMessage();
        switch (ObjectFactory.getGateway(rawMessage.getGatewayId()).getGateway().getType()) {
            case MQTT:
                this.updateMQTTMessage(rawMessage.getSubData(), rawMessage.getData());
                break;
            case ETHERNET:
            case SERIAL:
                updateSerialMessage(rawMessage.getData());
                break;
            default:
                _logger.warn(
                        "This type not implemented yet, Type:[{}]",
                        ObjectFactory.getGateway(rawMessage.getGatewayId()).getGateway().getType());
        }
    }

    private void updateMQTTMessage(String topic, String message) throws RawMessageException {
        if (message != null) {
            this.payload = message;
        }
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-NOTIFICATION_TYPE/ACK-FLAG/SUB-NOTIFICATION_TYPE
        String[] msgArry = topic.split("/");
        if (msgArry.length == 6) {
            this.nodeId = Integer.valueOf(msgArry[1]);
            this.childSensorId = Integer.valueOf(msgArry[2]);
            this.messageType = Integer.valueOf(msgArry[3]);
            this.ack = Integer.valueOf(msgArry[4]);
            this.subType = Integer.valueOf(msgArry[5]);
            _logger.debug("Message: {}", this.toString());
        } else {
            _logger.debug("Unknown message format, Topic:[{}], PayLoad:[{}]", topic, message);
            throw new RawMessageException("Unknown message format, Topic:" + topic + ", PayLoad:" + message);
        }
    }

    private void updateSerialMessage(String gateWayMessage) throws RawMessageException {
        if (gateWayMessage.endsWith("\n")) {
            gateWayMessage = gateWayMessage.substring(0, gateWayMessage.length() - 1);
        }
        String[] msgArry = gateWayMessage.split(";");
        if (msgArry.length == 6) {
            this.payload = msgArry[5];
        }
        if (msgArry.length >= 5) {
            this.nodeId = Integer.valueOf(msgArry[0]);
            this.childSensorId = Integer.valueOf(msgArry[1]);
            this.messageType = Integer.valueOf(msgArry[2]);
            this.ack = Integer.valueOf(msgArry[3]);
            this.subType = Integer.valueOf(msgArry[4]);
            _logger.debug("Message: {}", this.toString());
        } else {
            _logger.debug("Unknown message format: [{}]", gateWayMessage);
            throw new RawMessageException("Unknown message format:[" + gateWayMessage + "]");
        }
    }

    public MySensorsRawMessage(int gatewayId, int nodeId, int childSensorId,
            int messageType, int ack, int subType, String payload, boolean isTxMessage) {
        this.gatewayId = gatewayId;
        this.nodeId = nodeId;
        this.childSensorId = childSensorId;
        this.messageType = messageType;
        this.ack = ack;
        this.subType = subType;
        this.payload = payload;
        this.isTxMessage = isTxMessage;
    }

    public MySensorsRawMessage(int gatewayId, int nodeId, int childSensorId,
            int messageType, int ack, int subType, String payload) {
        this(gatewayId, nodeId, childSensorId, messageType, ack, subType, payload, false);
    }

    public int getNodeId() {
        return nodeId;
    }

    public String getNodeEui() {
        return String.valueOf(nodeId);
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getChildSensorId() {
        return childSensorId;
    }

    public String getChildSensorIdString() {
        return String.valueOf(childSensorId);
    }

    public void setChildSensorId(int childSensorId) {
        this.childSensorId = childSensorId;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public String getPayload() {
        return payload;
    }

    @JsonSetter
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setPayload(Object payload) {
        this.payload = String.valueOf(payload);
    }

    public void setPayload(boolean payload) {
        if (payload) {
            this.payload = "1";
        } else {
            this.payload = "0";
        }
    }

    public Boolean getPayloadBoolean() {
        if (this.payload.trim().equalsIgnoreCase("0")) {
            return false;
        } else if (Integer.valueOf(this.payload.trim()) > 0) {
            return true;
        } else {
            _logger.warn("Unable to convert as boolean, Unknown format:{}", this.payload);
            return null;
        }
    }

    public Integer getPayloadInteger() {
        return Integer.valueOf(this.payload);
    }

    public String getPayloadString() {
        return this.payload;
    }

    public Double getPayloadDouble() {
        return Double.valueOf(this.payload);
    }

    public Float getPayloadFloat() {
        return Float.valueOf(this.payload);
    }

    public Byte getPayloadByte() {
        return Byte.valueOf(this.payload);
    }

    public byte[] getPayloadBytes() {
        return this.payload.getBytes();
    }

    public String toString() {
        StringBuffer message = new StringBuffer();
        message.append("NodeId:").append(this.nodeId).append(",");
        message.append("ChildSensorId:").append(this.childSensorId).append(",");
        message.append("MessageType:").append(this.messageType).append(",");
        message.append("Ack:").append(this.ack).append(",");
        message.append("SubType:").append(this.subType).append(",");
        message.append("Payload:").append(this.payload);
        return message.toString();
    }

    public String getGWString() {
        StringBuffer message = new StringBuffer();
        message.append(this.nodeId).append(";");
        message.append(this.childSensorId).append(";");
        message.append(this.messageType).append(";");
        message.append(this.ack).append(";");
        message.append(this.subType).append(";");
        message.append(this.payload).append("\n");
        return message.toString();
    }

    public String getMqttTopic() {
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-NOTIFICATION_TYPE/ACK-FLAG/SUB-NOTIFICATION_TYPE
        StringBuilder builder = new StringBuilder();
        builder.append(((GatewayMQTT) ObjectFactory.getGateway(this.gatewayId).getGateway()).getTopicPublish());
        builder.append("/").append(this.getNodeId());
        builder.append("/").append(this.getChildSensorId());
        builder.append("/").append(this.getMessageType());
        builder.append("/").append(this.getAck());
        builder.append("/").append(this.getSubType());

        return builder.toString();
    }

    public byte[] getGWBytes() {
        return this.getGWString().getBytes();
    }

    public boolean isTxMessage() {
        return isTxMessage;
    }

    public void setTxMessage(boolean isTxMessage) {
        this.isTxMessage = isTxMessage;
    }

    public int getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(int gatewayId) {
        this.gatewayId = gatewayId;
    }

    public RawMessage getRawMessage() {
        Gateway gateway = DaoUtils.getGatewayDao().getById(this.gatewayId);
        switch (gateway.getType()) {
            case MQTT:
                return new RawMessage(this.gatewayId, this.getPayload(), this.getMqttTopic(), this.isTxMessage());
            case ETHERNET:
            case SERIAL:
                return new RawMessage(this.gatewayId, this.getGWString(), null, this.isTxMessage());
            default:
                _logger.warn("This type not implemented yet, Type:[{}]", gateway.getType().name());
        }
        return null;

    }
}
