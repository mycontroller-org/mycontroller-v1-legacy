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

import java.util.HashMap;

import org.mycontroller.standalone.db.tables.GatewayTable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Fraid(https://github.com/Fraid)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GatewayPhilipsHue extends Gateway {
    public static final String KEY_URL = "url";
    public static final String KEY_AUTORIZED_USER = "authorizedUser";
    public static final String KEY_POLL_FREQUENCY = "pollFrequency";

    private String url;
    private String authorizedUser;
    private Integer pollFrequency;

    public GatewayPhilipsHue() {
    }

    public GatewayPhilipsHue(GatewayTable gatewayTable) {
        updateGateway(gatewayTable);
    }

    @Override
    @JsonIgnore
    public GatewayTable getGatewayTable() {
        GatewayTable gatewayTable = super.getGatewayTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_URL, url);
        properties.put(KEY_AUTORIZED_USER, authorizedUser);
        properties.put(KEY_POLL_FREQUENCY, pollFrequency);
        gatewayTable.setProperties(properties);
        return gatewayTable;
    }

    @Override
    @JsonIgnore
    public void updateGateway(GatewayTable gatewayTable) {
        super.updateGateway(gatewayTable);
        url = (String) gatewayTable.getProperties().get(KEY_URL);
        authorizedUser = (String) gatewayTable.getProperties().get(KEY_AUTORIZED_USER);
        pollFrequency = (Integer) gatewayTable.getProperties().get(KEY_POLL_FREQUENCY);
    }

    @Override
    public String getConnectionDetails() {
        StringBuilder builder = new StringBuilder();
        builder.append(getUrl());
        return builder.toString();
    }
}
