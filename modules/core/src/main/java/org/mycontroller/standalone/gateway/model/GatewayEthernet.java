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
public class GatewayEthernet extends Gateway {
    public static final String KEY_HOST = "h";
    public static final String KEY_PORT = "p";
    public static final String KEY_ALIVE_FREQUENCY = "af";

    private String host;
    private Integer port;
    private Long aliveFrequency;

    public GatewayEthernet() {

    }

    public GatewayEthernet(GatewayTable gatewayTable) {
        updateGateway(gatewayTable);
    }

    @Override
    @JsonIgnore
    public GatewayTable getGatewayTable() {
        GatewayTable gatewayTable = super.getGatewayTable();
        gatewayTable.getProperties().put(KEY_HOST, host);
        gatewayTable.getProperties().put(KEY_PORT, port);
        gatewayTable.getProperties().put(KEY_ALIVE_FREQUENCY, aliveFrequency);
        return gatewayTable;
    }

    @Override
    @JsonIgnore
    public void updateGateway(GatewayTable gatewayTable) {
        super.updateGateway(gatewayTable);
        host = (String) gatewayTable.getProperties().get(KEY_HOST);
        port = (Integer) gatewayTable.getProperties().get(KEY_PORT);
        aliveFrequency = (Long) gatewayTable.getProperties().get(KEY_ALIVE_FREQUENCY);
    }

    @Override
    public String getConnectionDetails() {
        StringBuilder builder = new StringBuilder();
        builder.append("Host:").append(getHost());
        builder.append(", Port:").append(getPort());
        return builder.toString();
    }
}
