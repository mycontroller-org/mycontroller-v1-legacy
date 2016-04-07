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
package org.mycontroller.standalone.provider.mysensors;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.model.GatewayMQTT;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_STREAM;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Data
@ToString
@Slf4j
public class MySensorsRawMessage {

    private int gatewayId;
    private int nodeId;
    private int childSensorId;
    private int messageType;
    private int ack;
    private int subType;
    private String payload;
    private boolean isTxMessage = false;

    public MySensorsRawMessage(RawMessage rawMessage) throws RawMessageException {
        this.gatewayId = rawMessage.getGatewayId();
        this.isTxMessage = rawMessage.isTxMessage();
        switch (McObjectManager.getGateway(rawMessage.getGatewayId()).getGateway().getType()) {
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
                        McObjectManager.getGateway(rawMessage.getGatewayId()).getGateway().getType());
        }
        MySensorsEngine.updateMessage(this);
    }

    public MySensorsRawMessage(McMessage mcMessage) throws RawMessageException {
        gatewayId = mcMessage.getGatewayId();
        if (mcMessage.getNodeEui().equalsIgnoreCase(McMessage.NODE_BROADCAST_ID)) {
            nodeId = MySensorsUtils.NODE_ID_BROADCAST;
        } else {
            nodeId = McUtils.getInteger(mcMessage.getNodeEui());
        }
        if (mcMessage.getSensorId().equalsIgnoreCase(McMessage.SENSOR_BROADCAST_ID)) {
            childSensorId = MySensorsUtils.SENSOR_ID_BROADCAST;
        } else {
            childSensorId = McUtils.getInteger(mcMessage.getSensorId());
        }
        messageType = MYS_MESSAGE_TYPE.fromString(mcMessage.getType().getText()).ordinal();
        ack = mcMessage.isAcknowledge() ? 1 : 0;
        subType = getMysensorsSubType(mcMessage.getType(), mcMessage.getSubType());
        payload = mcMessage.getPayload();
        isTxMessage = mcMessage.isTxMessage();
        MySensorsEngine.updateMessage(this);
    }

    private void updateMQTTMessage(String topic, String message) throws RawMessageException {
        if (message != null) {
            this.payload = message;
        }
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-OPERATION_TYPE/ACK-FLAG/SUB-OPERATION_TYPE
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

    public String getNodeEui() {
        return String.valueOf(nodeId);
    }

    public String getChildSensorIdString() {
        return String.valueOf(childSensorId);
    }

    public void setPayload(Object payload) {
        this.payload = String.valueOf(payload);
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
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-OPERATION_TYPE/ACK-FLAG/SUB-OPERATION_TYPE
        StringBuilder builder = new StringBuilder();
        String[] topicsPublish = ((GatewayMQTT) McObjectManager.getGateway(this.gatewayId).getGateway())
                .getTopicsPublish().split(GatewayMQTT.TOPICS_SPLITER);
        for (String topic : topicsPublish) {
            if (builder.length() > 0) {
                builder.append(GatewayMQTT.TOPICS_SPLITER);
            }
            builder.append(topic.trim());
            builder.append("/").append(this.getNodeId());
            builder.append("/").append(this.getChildSensorId());
            builder.append("/").append(this.getMessageType());
            builder.append("/").append(this.getAck());
            builder.append("/").append(this.getSubType());
        }
        return builder.toString();
    }

    public RawMessage getRawMessage() {
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(this.gatewayId);
        switch (gatewayTable.getType()) {
            case MQTT:
                return RawMessage.builder()
                        .gatewayId(gatewayId)
                        .data(getPayload())
                        .subData(getMqttTopic()).isTxMessage(isTxMessage())
                        .build();
            case ETHERNET:
            case SERIAL:
                return RawMessage.builder()
                        .gatewayId(gatewayId)
                        .data(getGWString())
                        .build();
            default:
                _logger.warn("This type not implemented yet, Type:[{}]", gatewayTable.getType().name());
        }
        return null;
    }

    public McMessage getMcMessage() {
        String sensorId = getChildSensorId() == 255 ? McMessage.SENSOR_BROADCAST_ID : getChildSensorIdString();
        String nodeId = getNodeId() == 255 ? McMessage.NODE_BROADCAST_ID : getNodeEui();
        return McMessage.builder()
                .acknowledge(ack == 0)
                .gatewayId(gatewayId)
                .nodeEui(nodeId)
                .SensorId(sensorId)
                .networkType(NETWORK_TYPE.MY_SENSORS)
                .type(MESSAGE_TYPE.fromString(MYS_MESSAGE_TYPE.get(messageType).getText()))
                .subType(getMcMessageSubType())
                .isTxMessage(isTxMessage())
                .payload(getPayload()).build();
    }

    public String getMcMessageSubType() {
        switch (MYS_MESSAGE_TYPE.get(messageType)) {
            case C_PRESENTATION:
                return MYS_MESSAGE_TYPE_PRESENTATION.get(subType).getText();
            case C_INTERNAL:
                return MYS_MESSAGE_TYPE_INTERNAL.get(subType).getText();
            case C_REQ:
            case C_SET:
                return MYS_MESSAGE_TYPE_SET_REQ.get(subType).getText();
            case C_STREAM:
                return MYS_MESSAGE_TYPE_STREAM.get(subType).getText();
            default:
                return null;
        }
    }

    public Integer getMysensorsSubType(MESSAGE_TYPE messageType, String mcSubType) {
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
