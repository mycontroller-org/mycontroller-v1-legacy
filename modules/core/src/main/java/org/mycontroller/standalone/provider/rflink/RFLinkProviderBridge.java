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
package org.mycontroller.standalone.provider.rflink;

import java.util.ArrayList;
import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.message.IProviderBridge;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class RFLinkProviderBridge implements IProviderBridge {

    @Override
    public void executeMcMessage(McMessage mcMessage) {
        if (mcMessage.getNetworkType() != NETWORK_TYPE.RF_LINK) {
            _logger.error("This is not '{}' message! McMessage:{}", NETWORK_TYPE.RF_LINK.getText(), mcMessage);
        }
        try {
            _logger.debug("McMessage about to send to gateway: [{}]", mcMessage);
            McMessageUtils.sendToGateway(new RFLinkRawMessage(mcMessage).getRawMessage());
        } catch (RawMessageException ex) {
            _logger.error("Unable to process this McMessage:{}", mcMessage, ex);
        }
    }

    @Override
    public void executeRawMessage(RawMessage rawMessage) {
        if (rawMessage.getNetworkType() != NETWORK_TYPE.RF_LINK) {
            _logger.error("This is not '{}' message! RawMessage:{}", NETWORK_TYPE.RF_LINK.getText(), rawMessage);
        }
        try {
            _logger.debug("Received a {}", rawMessage);
            String rawData = (String) rawMessage.getData();
            rawData = rawData.replaceAll("(\\r|\\n)", ""); //Replace \n and \r
            if (!rawData.endsWith(";")) {
                throw new RawMessageException("Cannot take this message. This is invalid or incomplete: ["
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
                throw new RawMessageException("data size should be greater than 2, Current data: " + rawData);
            }

            if (!dataList.get(0).equals("20") && !rawMessage.isTxMessage()) {
                throw new RawMessageException("RFLink Rx message should start with '20', RawMessage:["
                        + rawMessage.getData() + "]");
            }

            //Format: 20;2D;UPM/Esic;ID=0001;TEMP=00cf;HUM=16;BAT=OK;
            //Refer: http://www.nemcon.nl/blog2/protref
            dataList.remove(0);//Remove 20
            dataList.remove(0);//Remove RFLink serial number
            //Update protocol
            String protocol = dataList.remove(0);
            if (protocol.equalsIgnoreCase("ok")) {
                //This is ack message from RFLink. Just ignore
                return;
            }

            properties.clear();
            for (String data : dataList) {
                if (data.contains("=")) {
                    String[] prop = data.split("=", 2);
                    properties.put(prop[0].toLowerCase(), prop[1]);
                } else if (data.trim().length() > 0) {
                    _logger.warn("Unknown property:[{}] from {}", data, rawMessage);
                }
            }

            //Update nodeEui
            String nodeEui = properties.remove(RFLinkRawMessage.KEY_ID.toLowerCase());
            if (nodeEui == null) {
                throw new RawMessageException("NodeEui can not be NULL. Message:[" + rawMessage.getData() + "]");
            }
            String switchName = properties.remove("switch");
            //Send protocol message
            RFLinkRawMessage rfLinkRawMessage = new RFLinkRawMessage(rawMessage, nodeEui, protocol);
            McMessageUtils.sendToMcMessageEngine(rfLinkRawMessage.getMcMessage());
            //BAT message, if we have
            String bat = properties.remove("bat");
            if (bat != null) {
                rfLinkRawMessage.setSubType(MESSAGE_TYPE_INTERNAL.I_BATTERY_LEVEL.getText());
                rfLinkRawMessage.setPayload(bat.equalsIgnoreCase("OK") ? "100" : "0");
                //Send battery message
                McMessageUtils.sendToMcMessageEngine(rfLinkRawMessage.getMcMessage());
            }
            for (String key : properties.keySet()) {
                rfLinkRawMessage = new RFLinkRawMessage(rawMessage, nodeEui, key, properties.get(key));
                if (switchName != null) {
                    rfLinkRawMessage.setSensorId(switchName);
                }
                //Send normal set messages
                McMessageUtils.sendToMcMessageEngine(rfLinkRawMessage.getMcMessage());
            }

        } catch (RawMessageException ex) {
            _logger.error("Unable to process this rawMessage:{}", rawMessage, ex);
        }
    }

    @Override
    public boolean validateSensorId(Sensor sensor) {
        if (sensor.getSensorId().contains(" ")) {
            throw new RuntimeException("Sensor Id should not contain any space");
        }
        return true;
    }

    @Override
    public boolean validateNodeId(Node node) {
        if (node.getEui().contains(" ")) {
            throw new RuntimeException("Node EUI should not contain any space");
        }
        return true;
    }
}
