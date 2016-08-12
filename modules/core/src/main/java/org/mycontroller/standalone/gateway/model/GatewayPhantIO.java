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
package org.mycontroller.standalone.gateway.model;

import java.util.HashMap;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.restclient.RestFactory.TRUST_HOST_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { "privateKey" })
public class GatewayPhantIO extends Gateway {
    public static final String KEY_URL = "url";
    public static final String KEY_TRUST_HOST_TYPE = "trustHostType";
    public static final String KEY_PUBLIC_KEY = "publicKey";
    public static final String KEY_PRIVATE_KEY = "privateKey";
    public static final String KEY_POLL_FREQUENCY = "pollFrequency";
    public static final String KEY_RECORDS_LIMIT = "recordsLimit";
    public static final String KEY_LAST_UPDATE = "lastUpdate";

    private String url;
    private TRUST_HOST_TYPE trustHostType;
    private String publicKey;
    private String privateKey;
    private Integer pollFrequency;
    private Long recordsLimit;
    private Long lastUpdate;

    public GatewayPhantIO() {

    }

    public GatewayPhantIO(GatewayTable gatewayTable) {
        updateGateway(gatewayTable);
    }

    @Override
    @JsonIgnore
    public GatewayTable getGatewayTable() {
        GatewayTable gatewayTable = super.getGatewayTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_URL, url);
        properties.put(KEY_TRUST_HOST_TYPE, trustHostType.getText());
        properties.put(KEY_PUBLIC_KEY, publicKey);
        properties.put(KEY_PRIVATE_KEY, privateKey);
        properties.put(KEY_POLL_FREQUENCY, pollFrequency);
        properties.put(KEY_RECORDS_LIMIT, recordsLimit);
        properties.put(KEY_LAST_UPDATE, lastUpdate);
        gatewayTable.setProperties(properties);
        return gatewayTable;
    }

    @Override
    @JsonIgnore
    public void updateGateway(GatewayTable gatewayTable) {
        super.updateGateway(gatewayTable);
        url = (String) gatewayTable.getProperty(KEY_URL);
        trustHostType = TRUST_HOST_TYPE.fromString((String) gatewayTable.getProperty(KEY_TRUST_HOST_TYPE,
                TRUST_HOST_TYPE.DEFAULT.getText()));
        publicKey = (String) gatewayTable.getProperty(KEY_PUBLIC_KEY);
        privateKey = (String) gatewayTable.getProperty(KEY_PRIVATE_KEY);
        pollFrequency = (Integer) gatewayTable.getProperty(KEY_POLL_FREQUENCY, 120);
        recordsLimit = (Long) gatewayTable.getProperty(KEY_RECORDS_LIMIT, 10L);
        lastUpdate = (Long) gatewayTable.getProperty(KEY_LAST_UPDATE);
    }

    @Override
    public String getConnectionDetails() {
        StringBuilder builder = new StringBuilder();
        builder.append("URL:").append(getUrl());
        builder.append(", PublicKey:").append(getPublicKey());
        builder.append(", PollFrequency:").append(getPollFrequency());
        builder.append(", RecordLimit:").append(getRecordsLimit());
        return builder.toString();
    }

    public void updateLastPollTime(long timestamp) {
        lastUpdate = timestamp;
        DaoUtils.getGatewayDao().update(getGatewayTable());
    }

    @JsonGetter("trustHostType")
    private String getTrustHost() {
        return getTrustHostType().getText();
    }
}
