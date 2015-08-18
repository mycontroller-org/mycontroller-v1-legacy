/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.mysensors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class RawMessage {
    private static final Logger _logger = LoggerFactory.getLogger(RawMessage.class.getName());
    public static final int NODE_SENSOR_ID_BROADCAST = 255;

    private int nodeId;
    private int childSensorId;
    private int messageType;
    private int ack;
    private int subType;
    private String payLoad;
    private boolean isTxMessage = false;

    public RawMessage() {

    }

    public RawMessage(int nodeId, int childSensorId, int messageType, int ack, int subType, String payLoad,
            boolean isTxMessage) {
        this.nodeId = nodeId;
        this.childSensorId = childSensorId;
        this.messageType = messageType;
        this.ack = ack;
        this.subType = subType;
        this.payLoad = payLoad;
        this.isTxMessage = isTxMessage;
    }

    public RawMessage(int nodeId, int childSensorId, int messageType, int ack, int subType, String payLoad) {
        this(nodeId, childSensorId, messageType, ack, subType, payLoad, false);
    }

    public RawMessage(String gateWayMessage) throws RawMessageException {
        if (gateWayMessage.endsWith("\n")) {
            gateWayMessage = gateWayMessage.substring(0, gateWayMessage.length() - 1);
        }
        String[] msgArry = gateWayMessage.split(";");
        if (msgArry.length == 6) {
            this.payLoad = msgArry[5];
        }
        if (msgArry.length >= 5) {
            this.nodeId = Integer.valueOf(msgArry[0]);
            this.childSensorId = Integer.valueOf(msgArry[1]);
            this.messageType = Integer.valueOf(msgArry[2]);
            this.ack = Integer.valueOf(msgArry[3]);
            this.subType = Integer.valueOf(msgArry[4]);
            _logger.debug("Message: {}", this.toString());
        } else {
            _logger.debug("Unknown message format: {}", gateWayMessage);
            throw new RawMessageException("Unknown message format:" + gateWayMessage);
        }
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getChildSensorId() {
        return childSensorId;
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

    public String getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(String payLoad) {
        this.payLoad = payLoad;
    }

    public void setPayLoad(Object payLoad) {
        this.payLoad = String.valueOf(payLoad);
    }

    public void setPayLoad(boolean payLoad) {
        if (payLoad) {
            this.payLoad = "1";
        } else {
            this.payLoad = "0";
        }
    }

    public Boolean getPayLoadBoolean() {
        if (this.payLoad.trim().equalsIgnoreCase("0")) {
            return false;
        } else if (Integer.valueOf(this.payLoad.trim()) > 0) {
            return true;
        } else {
            _logger.warn("Unable to convert as boolean, Unknown format:{}", this.payLoad);
            return null;
        }
    }

    public Integer getPayLoadInteger() {
        return Integer.valueOf(this.payLoad);
    }

    public String getPayLoadString() {
        return this.payLoad;
    }

    public Double getPayLoadDouble() {
        return Double.valueOf(this.payLoad);
    }

    public Float getPayLoadFloat() {
        return Float.valueOf(this.payLoad);
    }

    public Byte getPayLoadByte() {
        return Byte.valueOf(this.payLoad);
    }

    public byte[] getPayLoadBytes() {
        return this.payLoad.getBytes();
    }

    public String toString() {
        StringBuffer message = new StringBuffer();
        message.append("NodeId:").append(this.nodeId).append(",");
        message.append("ChildSensorId:").append(this.childSensorId).append(",");
        message.append("MessageType:").append(this.messageType).append(",");
        message.append("Ack:").append(this.ack).append(",");
        message.append("SubType:").append(this.subType).append(",");
        message.append("PayLoad:").append(this.payLoad);
        return message.toString();
    }

    public String getGWString() {
        StringBuffer message = new StringBuffer();
        message.append(this.nodeId).append(";");
        message.append(this.childSensorId).append(";");
        message.append(this.messageType).append(";");
        message.append(this.ack).append(";");
        message.append(this.subType).append(";");
        message.append(this.payLoad).append("\n");
        return message.toString();
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
}
