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
package org.mycontroller.standalone.message;

import java.util.HashMap;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Data
@ToString
@Builder
class MessageImpl implements IMessage, Cloneable {
    /**  */
    private static final long serialVersionUID = -5319269006518045474L;

    private Integer gatewayId;
    private String nodeEui;
    private String sensorId;
    private String type;
    private String subType;
    private Integer ack;
    private String payload;
    private Boolean isTxMessage;
    private Long timestamp;
    private HashMap<String, Object> properties;

    @Override
    public Long getTimestamp() {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    @Override
    public Boolean getPayloadBoolean() {
        return Boolean.valueOf(payload);
    }

    @Override
    public Integer getPayloadInt() {
        return Integer.valueOf(payload);
    }

    @Override
    public Double getPayloadDouble() {
        return Double.valueOf(payload);
    }

    @Override
    public Boolean isTxMessage() {
        return isTxMessage;
    }

    @Override
    public Object getProperty(String key) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties.get(key);
    }

    @Override
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public void setPayload(Boolean payload) {
        this.payload = String.valueOf(payload);
    }

    @Override
    public void setPayload(Integer payload) {
        this.payload = String.valueOf(payload);
    }

    @Override
    public void setPayload(Double payload) {
        this.payload = String.valueOf(payload);
    }

    @Override
    public void setTxMessage(Boolean isTxMessage) {
        this.isTxMessage = isTxMessage;
    }

    @Override
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(key, value);
    }

    @Override
    public Boolean isValid() {
        if (gatewayId == null
                || nodeEui == null
                || sensorId == null
                || type == null
                || subType == null) {
            return false;
        }
        return true;
    }

    @Override
    public String getEventTopic() {
        StringBuilder topic = new StringBuilder("MSG_");

        topic.append(gatewayId).append("_")
                .append(nodeEui).append("_")
                .append(sensorId).append("_")
                .append(type).append("_")
                .append(subType);
        return topic.toString();
    }

    @SuppressWarnings("unchecked")
    public MessageImpl clone() {
        return MessageImpl.builder()
                .ack(ack)
                .gatewayId(gatewayId)
                .isTxMessage(isTxMessage)
                .nodeEui(nodeEui)
                .payload(payload)
                .sensorId(sensorId)
                .subType(subType)
                .timestamp(timestamp)
                .type(type)
                .properties(properties != null ? (HashMap<String, Object>) properties.clone() : null)
                .build();
    }

}
