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
package org.mycontroller.standalone.provider.mysensors;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.model.GatewayMQTT;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.utils.McUtils;

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
    private Long timestamp;

    public MySensorsRawMessage(RawMessage rawMessage) throws RawMessageException {
        gatewayId = rawMessage.getGatewayId();
        isTxMessage = rawMessage.isTxMessage();
        switch (McObjectManager.getGateway(rawMessage.getGatewayId()).getGateway().getType()) {
            case MQTT:
                updateMQTTMessage(rawMessage.getSubData(), (String) rawMessage.getData());
                break;
            case ETHERNET:
            case SERIAL:
                updateSerialMessage((String) rawMessage.getData());
                break;
            default:
                _logger.warn(
                        "This type not implemented yet, Type:[{}]",
                        McObjectManager.getGateway(rawMessage.getGatewayId()).getGateway().getType());
        }
        timestamp = rawMessage.getTimestamp();
        MySensorsEngine.updateMessage(this);
    }

    public MySensorsRawMessage(McMessage mcMessage) throws RawMessageException {
        gatewayId = mcMessage.getGatewayId();
        if (mcMessage.getNodeEui().equalsIgnoreCase(McMessage.NODE_BROADCAST_ID)) {
            nodeId = MySensorsUtils.NODE_ID_BROADCAST;
        } else if (mcMessage.getNodeEui().equalsIgnoreCase(McMessage.GATEWAY_NODE_ID)) {
            nodeId = MySensorsUtils.GATEWAY_ID;
        } else {
            nodeId = McUtils.getInteger(mcMessage.getNodeEui());
        }
        if (mcMessage.getSensorId().equalsIgnoreCase(McMessage.SENSOR_BROADCAST_ID)) {
            childSensorId = MySensorsUtils.SENSOR_ID_BROADCAST;
        } else {
            childSensorId = McUtils.getInteger(mcMessage.getSensorId());
        }
        messageType = MYS_MESSAGE_TYPE.fromString(mcMessage.getType().getText()).ordinal();
        ack = mcMessage.getAck();
        subType = getMysensorsSubType(mcMessage.getType(), mcMessage.getSubType());
        payload = mcMessage.getPayload();
        isTxMessage = mcMessage.isTxMessage();
        timestamp = mcMessage.getTimestamp();
        MySensorsEngine.updateMessage(this);
    }

    private void updateMQTTMessage(String topic, String message) throws RawMessageException {
        if (message != null) {
            payload = message;
        }
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-OPERATION_TYPE/ACK-FLAG/SUB-OPERATION_TYPE
        String[] msgArry = topic.split("/");
        if (msgArry.length == 6) {
            nodeId = Integer.valueOf(msgArry[1]);
            childSensorId = Integer.valueOf(msgArry[2]);
            messageType = Integer.valueOf(msgArry[3]);
            ack = Integer.valueOf(msgArry[4]);
            subType = Integer.valueOf(msgArry[5]);
            _logger.debug("Message: {}", toString());
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
            payload = msgArry[5];
        }
        if (msgArry.length >= 5) {
            nodeId = Integer.valueOf(msgArry[0]);
            childSensorId = Integer.valueOf(msgArry[1]);
            messageType = Integer.valueOf(msgArry[2]);
            ack = Integer.valueOf(msgArry[3]);
            subType = Integer.valueOf(msgArry[4]);
            _logger.debug("Message: {}", toString());
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
        if (payload.trim().equalsIgnoreCase("0")) {
            return false;
        } else if (Integer.valueOf(payload.trim()) > 0) {
            return true;
        } else {
            _logger.warn("Unable to convert as boolean, Unknown format:{}", payload);
            return null;
        }
    }

    public String getGWString() {
        StringBuffer message = new StringBuffer();
        message.append(nodeId).append(";");
        message.append(childSensorId).append(";");
        message.append(messageType).append(";");
        message.append(ack).append(";");
        message.append(subType).append(";");
        message.append(payload).append("\n");
        return message.toString();
    }

    public String getMqttTopic() {
        // Topic structure:
        // MY_MQTT_TOPIC_PREFIX/NODE-KEY_ID/SENSOR_VARIABLE-KEY_ID/CMD-OPERATION_TYPE/ACK-FLAG/SUB-OPERATION_TYPE
        StringBuilder builder = new StringBuilder();
        String[] topicsPublish = ((GatewayMQTT) McObjectManager.getGateway(gatewayId).getGateway())
                .getTopicsPublish().split(GatewayMQTT.TOPICS_SPLITER);
        for (String topic : topicsPublish) {
            if (builder.length() > 0) {
                builder.append(GatewayMQTT.TOPICS_SPLITER);
            }
            builder.append(topic.trim());
            builder.append("/").append(getNodeId());
            builder.append("/").append(getChildSensorId());
            builder.append("/").append(getMessageType());
            builder.append("/").append(getAck());
            builder.append("/").append(getSubType());
        }
        return builder.toString();
    }

    public RawMessage getRawMessage() {
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        switch (gatewayTable.getType()) {
            case MQTT:
                return RawMessage.builder()
                        .gatewayId(gatewayId)
                        .data(getPayload())
                        .subData(getMqttTopic())
                        .isTxMessage(isTxMessage())
                        .timestamp(timestamp)
                        .build();
            case ETHERNET:
            case SERIAL:
                return RawMessage.builder()
                        .gatewayId(gatewayId)
                        .data(getGWString())
                        .isTxMessage(isTxMessage())
                        .timestamp(timestamp)
                        .build();
            default:
                _logger.warn("This type not implemented yet, Type:[{}]", gatewayTable.getType().name());
        }
        return null;
    }

    public McMessage getMcMessage() {
        String sensorId = getChildSensorId() == 255 ? McMessage.SENSOR_BROADCAST_ID : getChildSensorIdString();
        String nodeId = getNodeId() == 255 ? McMessage.NODE_BROADCAST_ID : getNodeEui();
        McMessage mcMessage = McMessage.builder()
                .ack(getAck() == 1 ? 2 : 0)//MySensors gateway never request ack
                .gatewayId(getGatewayId())
                .nodeEui(nodeId)
                .sensorId(sensorId)
                .networkType(NETWORK_TYPE.MY_SENSORS)
                .isTxMessage(isTxMessage())
                .payload(getPayload())
                .timestamp(getTimestamp())
                .build();
        updateMcMessageTypeAndSubType(mcMessage);
        return mcMessage;
    }

    public void updateMcMessageTypeAndSubType(McMessage mcMessage) {
        mcMessage.setType(MESSAGE_TYPE.fromString(MYS_MESSAGE_TYPE.get(messageType).getText()));
        switch (MYS_MESSAGE_TYPE.get(messageType)) {
            case C_PRESENTATION:
                mcMessage.setSubType(MYS_MESSAGE_TYPE_PRESENTATION.get(subType).getText());
                break;
            case C_INTERNAL:
                mcMessage.setSubType(MYS_MESSAGE_TYPE_INTERNAL.get(subType).getText());
                break;
            case C_REQ:
                mcMessage.setSubType(MYS_MESSAGE_TYPE_SET_REQ.get(subType).getText());
                break;
            case C_SET:
                if (!isTxMessage()
                        && MYS_MESSAGE_TYPE_SET_REQ.get(subType) == MYS_MESSAGE_TYPE_SET_REQ.V_VAR5
                        && getPayload() != null
                        && getPayload().startsWith("rssi:")) {
                    mcMessage.setType(MESSAGE_TYPE.C_INTERNAL);
                    mcMessage.setSubType(MESSAGE_TYPE_INTERNAL.I_RSSI.getText());
                    mcMessage.setPayload(getPayload().replace("rssi:", "").trim());
                    mcMessage.setSensorId(McMessage.SENSOR_BROADCAST_ID);
                } else {
                    mcMessage.setSubType(MYS_MESSAGE_TYPE_SET_REQ.get(subType).getText());
                }
                break;
            case C_STREAM:
                mcMessage.setSubType(MYS_MESSAGE_TYPE_STREAM.get(subType).getText());
                break;
            default:
                break;
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
