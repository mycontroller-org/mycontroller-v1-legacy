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

import java.io.Serializable;
import java.util.HashMap;

import org.mycontroller.standalone.message.MessageImpl.MessageImplBuilder;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public interface IMessage extends Serializable {
    String SENSOR_BROADCAST_ID = "SENSOR_BC";
    String NODE_BROADCAST_ID = "NODE_BC";
    String PAYLOAD_EMPTY = "";
    String GATEWAY_NODE_ID = "NODE_GY";
    int NO_ACK = 0;
    int ACK_REQUEST = 1;
    int ACK_RESPONSE = 2;

    static IMessage getInstance() {
        return MessageImpl.builder().build();
    }

    static IMessageBuilder builder() {
        return new IMessageBuilder();
    }

    IMessage clone();

    Boolean isValid();

    Integer getGatewayId();

    String getNodeEui();

    String getSensorId();

    String getType();

    String getSubType();

    Integer getAck();

    String getPayload();

    Boolean getPayloadBoolean();

    Integer getPayloadInt();

    Double getPayloadDouble();

    Boolean isTxMessage();

    Long getTimestamp();

    HashMap<String, Object> getProperties();

    Object getProperty(String key);

    void setGatewayId(Integer gatewayId);

    void setNodeEui(String nodeEui);

    void setSensorId(String sensorId);

    void setType(String messageType);

    void setSubType(String messageSubType);

    void setAck(Integer ack);

    void setPayload(String payload);

    void setPayload(Boolean payload);

    void setPayload(Integer payload);

    void setPayload(Double payload);

    void setTxMessage(Boolean isTxMessage);

    void setTimestamp(Long timestamp);

    void setProperties(HashMap<String, Object> properties);

    void setProperty(String key, Object value);

    String getEventTopic();

    class IMessageBuilder extends MessageImplBuilder {
    }

}
