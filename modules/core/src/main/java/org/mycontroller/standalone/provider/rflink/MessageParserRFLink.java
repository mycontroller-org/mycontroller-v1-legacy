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
package org.mycontroller.standalone.provider.rflink;

import java.util.ArrayList;
import java.util.HashMap;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;
import org.mycontroller.standalone.provider.rflink.RFLink.RFLINK_MESSAGE_TYPE;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MessageParserRFLink implements IMessageParser<byte[]> {
    private GatewayConfig _config = null;
    private IQueue<IMessage> _queue = null;

    public MessageParserRFLink(GatewayConfig _config, IQueue<IMessage> _queue) {
        this._config = _config;
        this._queue = _queue;
    }

    @Override
    public IMessage getMessage(GatewayConfig _config, byte[] gatewayData) throws MessageParserException {
        try {
            String rawData = new String(gatewayData);
            rawData = rawData.replaceAll("(\\r|\\n)", ""); //Replace \n and \r
            if (!rawData.endsWith(";")) {
                throw new MessageParserException("Cannot take this message. This is invalid or incomplete: ["
                        + rawData + "]");
            }
            HashMap<String, String> properties = new HashMap<String, String>();

            //20;2D;UPM/Esic;ID=0001;TEMP=00cf;HUM=16;BAT=OK;
            //RX;SN;PROTOCOL;
            ArrayList<String> dataList = new ArrayList<String>();
            for (String _rawData : rawData.split(";")) {
                dataList.add(_rawData);
            }
            if (dataList.size() < 2) {
                throw new MessageParserException("data size should be greater than 2, Current data: " + rawData);
            }

            if (!dataList.get(0).equals("20")) {
                throw new MessageParserException("RFLink Rx message should start with '20', RawMessage:["
                        + gatewayData + "]");
            }

            //Format: 20;2D;UPM/Esic;ID=0001;TEMP=00cf;HUM=16;BAT=OK;
            //Refer: http://www.nemcon.nl/blog2/protref
            dataList.remove(0);//Remove 20
            dataList.remove(0);//Remove RFLink serial number
            //Update protocol
            String protocol = dataList.remove(0);
            if (protocol.equalsIgnoreCase("ok")) {
                //This is ack message from RFLink. Just ignore
                return IMessage.builder()
                        .isTxMessage(false)
                        .timestamp(System.currentTimeMillis())
                        .gatewayId(_config.getId())
                        .nodeEui(IMessage.NODE_BROADCAST_ID)
                        .sensorId(IMessage.SENSOR_BROADCAST_ID)
                        .type(MESSAGE_TYPE.C_INTERNAL.getText())
                        .subType(MESSAGE_TYPE_INTERNAL.I_LOG_MESSAGE.getText())
                        .ack(IMessage.ACK_RESPONSE)
                        .build();
            }

            properties.clear();
            for (String data : dataList) {
                if (data.contains("=")) {
                    String[] prop = data.split("=", 2);
                    properties.put(prop[0].toLowerCase(), prop[1]);
                } else if (data.trim().length() > 0) {
                    _logger.warn("Unknown property:[{}] from {}", data, gatewayData);
                }
            }

            //Update nodeEui
            String nodeEui = properties.remove(RFLink.KEY_ID.toLowerCase());
            if (nodeEui == null) {
                throw new MessageParserException("NodeEui can not be NULL. Message:[" + gatewayData + "]");
            }
            String switchName = properties.remove("switch");
            //Send protocol message
            IMessage message = get(rawData, nodeEui, protocol, null, null);
            _queue.add(message);
            //BAT message, if we have
            String bat = properties.remove("bat");
            if (bat != null) {
                _queue.add(IMessage.builder()
                        .gatewayId(_config.getId())
                        .ack(IMessage.NO_ACK)
                        .isTxMessage(false)
                        .timestamp(System.currentTimeMillis())
                        .nodeEui(nodeEui)
                        .sensorId(IMessage.SENSOR_BROADCAST_ID)
                        .type(MESSAGE_TYPE.C_INTERNAL.getText())
                        .subType(MESSAGE_TYPE_INTERNAL.I_BATTERY_LEVEL.getText())
                        .payload(bat.equalsIgnoreCase("OK") ? "100" : "0")
                        .build());
            }
            for (String key : properties.keySet()) {
                IMessage _message = get(rawData, nodeEui, null, key, properties.get(key));
                if (switchName != null) {
                    _message.setSensorId(switchName);
                }
                //Send normal set messages
                _queue.add(_message);
            }

        } catch (MessageParserException ex) {
            _logger.error("Unable to process this gatewayData:{}", gatewayData, ex);
        }
        return null;
    }

    private IMessage get(String gatewayData, String nodeEui, String protocol, String key, String value)
            throws MessageParserException {

        //Node id always should be in 8 digits with ZERO padding (32bit).
        nodeEui = String.format("%08x", Long.parseLong(nodeEui, 16));
        String payload = null;
        String type = null;
        String subType = null;
        String sensorId = IMessage.SENSOR_BROADCAST_ID;
        if (protocol != null) {
            type = MESSAGE_TYPE.C_INTERNAL.getText();
            subType = MESSAGE_TYPE_INTERNAL.I_PROPERTIES.getText();
            payload = RFLink.KEY_PROTOCOL + "=" + protocol;
        } else if (key != null && value != null) {
            type = MESSAGE_TYPE.C_SET.getText();
            if (key.equalsIgnoreCase("cmd") && value.toLowerCase().startsWith("set_level=")) {
                key = "set_level";
                value = value.toLowerCase().replaceFirst("set_level=", "");
            }
            RFLINK_MESSAGE_TYPE mType = RFLINK_MESSAGE_TYPE.valueOf(key.toUpperCase());
            subType = mType.getText();
            sensorId = key;
            switch (mType) {
                case SMOKEALERT:
                case PIR:
                case CMD:
                    payload = value.equalsIgnoreCase("on") ? "1" : "0";
                    break;
                case UP:
                    payload = value.equalsIgnoreCase("up") ? "1" : "0";
                    break;
                case DOWN:
                    payload = value.equalsIgnoreCase("down") ? "1" : "0";
                    break;
                case STOP:
                    payload = value.equalsIgnoreCase("stop") ? "1" : "0";
                    break;
                case AWINSP:
                case RAINRATE:
                case RAIN:
                case WINSP:
                    payload = RFLink.getPayload(value, 10.0, false);
                    break;
                case TEMP:
                case WINCHL:
                case WINTMP:
                    payload = RFLink.getPayload(value, 10.0, true);
                    break;
                case BARO:
                case UV:
                case LUX:
                case WINGS:
                case WATT:
                case KWATT:
                    payload = RFLink.getPayload(value, false);
                    break;
                case SET_LEVEL:
                    payload = String.valueOf(Math.round(Integer.valueOf(value) / RFLink.DIMMER_REF));
                    break;
                default:
                    payload = value;
                    break;
            }
        } else {
            throw new MessageParserException(
                    "Invalid option selected. either 'protocol' [or] 'key', 'value' is mandatory!");
        }
        return IMessage.builder()
                .ack(IMessage.NO_ACK)
                .gatewayId(_config.getId())
                .isTxMessage(false)
                .nodeEui(nodeEui)
                .sensorId(sensorId)
                .type(type)
                .subType(subType)
                .payload(payload)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public byte[] getGatewayData(IMessage message) throws MessageParserException {
        // Copy properties from node and update in message
        message.setProperties(DaoUtils.getNodeDao().get(message.getGatewayId(), message.getNodeEui()).getProperties());
        if (message.getProperties().get(RFLink.KEY_PROTOCOL) == null) {
            throw new MessageParserException("Protocol cannot be null");
        }
        StringBuilder builder = new StringBuilder();
        builder
                .append("10;")
                .append(message.getProperties().get(RFLink.KEY_PROTOCOL)).append(";")
                .append(message.getNodeEui()).append(";")
                .append(message.getSensorId()).append(";");

        if (message.getProperties().get(RFLink.KEY_TYPE) == null) {
            RFLINK_MESSAGE_TYPE mType = RFLINK_MESSAGE_TYPE.fromString(message.getType());
            if (mType == null) {
                throw new MessageParserException("Not supported type: " + this);
            }
            switch (mType) {
                case CMD:
                    builder.append(message.getPayload().equals("1") ? "ON" : "OFF");
                    break;
                case UP:
                    builder.append("UP");
                    break;
                case DOWN:
                    builder.append("DOWN");
                    break;
                case STOP:
                    builder.append("STOP");
                    break;
                case SET_LEVEL:
                    Integer payloadInt = McUtils.getDouble(message.getPayload()).intValue();
                    if (payloadInt == 0) {
                        builder.append("OFF");
                    } else {
                        builder.append(Math.round(payloadInt * RFLink.DIMMER_REF));
                    }
                    break;
                default:
                    throw new MessageParserException("Not supported type: " + mType.name());
            }
        } else if ("doorbell".equalsIgnoreCase((String) message.getProperties().get(RFLink.KEY_TYPE))) {
            if (RFLINK_MESSAGE_TYPE.CHIME.getText().equalsIgnoreCase(message.getSubType())) {
                builder.append(message.getPayload());
            } else if (RFLINK_MESSAGE_TYPE.CMD.getText().equalsIgnoreCase(message.getSubType())) {
                builder.append(message.getPayload().equals("1") ? "ON" : "OFF");
            } else {
                throw new MessageParserException("Not supported type: " + message.getSubType());
            }
        }

        builder.append(";\r\n");
        return builder.toString().getBytes();
    }

}
