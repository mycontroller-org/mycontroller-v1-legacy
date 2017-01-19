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
package org.mycontroller.standalone.gateway.model;

import org.mycontroller.standalone.db.tables.GatewayTable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GatewayMQTT extends Gateway {
    public static final int DEFAULT_MQTT_QOS = 0;

    public static final String TOPICS_SPLITER = ",";

    public static final String KEY_BROKER_HOST = "bh";
    public static final String KEY_CLIENT_ID = "cid";
    public static final String KEY_TOPICS_PUBLISH = "tp";
    public static final String KEY_TOPICS_SUBSCRIBE = "ts";
    public static final String KEY_USERNAME = "u";
    public static final String KEY_PASSWORD = "p";
    public static final String KEY_QOS = "qos";

    private String brokerHost;
    private String clientId;
    private String topicsPublish;
    private String topicsSubscribe;
    private String username;
    private String password;
    private Integer qos;

    public GatewayMQTT() {

    }

    public GatewayMQTT(GatewayTable gatewayTable) {
        updateGateway(gatewayTable);
    }

    @Override
    @JsonIgnore
    public GatewayTable getGatewayTable() {
        GatewayTable gatewayTable = super.getGatewayTable();
        gatewayTable.getProperties().put(KEY_BROKER_HOST, brokerHost);
        gatewayTable.getProperties().put(KEY_CLIENT_ID, clientId);
        gatewayTable.getProperties().put(KEY_TOPICS_PUBLISH, topicsPublish);
        gatewayTable.getProperties().put(KEY_TOPICS_SUBSCRIBE, topicsSubscribe);
        gatewayTable.getProperties().put(KEY_USERNAME, username);
        gatewayTable.getProperties().put(KEY_PASSWORD, password);
        gatewayTable.getProperties().put(KEY_QOS, qos);
        return gatewayTable;
    }

    @Override
    @JsonIgnore
    public void updateGateway(GatewayTable gatewayTable) {
        super.updateGateway(gatewayTable);
        brokerHost = (String) gatewayTable.getProperties().get(KEY_BROKER_HOST);
        clientId = (String) gatewayTable.getProperties().get(KEY_CLIENT_ID);
        topicsPublish = (String) gatewayTable.getProperties().get(KEY_TOPICS_PUBLISH);
        topicsSubscribe = (String) gatewayTable.getProperties().get(KEY_TOPICS_SUBSCRIBE);
        username = (String) gatewayTable.getProperties().get(KEY_USERNAME);
        password = (String) gatewayTable.getProperties().get(KEY_PASSWORD);
        qos = (Integer) gatewayTable.getProperties().get(KEY_QOS);
        if (qos == null) {
            qos = DEFAULT_MQTT_QOS;
        }
    }

    public Integer getQos() {
        if (qos == null) {
            return DEFAULT_MQTT_QOS;
        }
        return qos;
    }

    @Override
    public String getConnectionDetails() {
        StringBuilder builder = new StringBuilder();
        builder.append("BrokerHost:").append(getBrokerHost());
        builder.append(", ClientId:").append(getClientId());
        return builder.toString();
    }
}
