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
package org.mycontroller.standalone.exernalserver.model;

import java.util.HashMap;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.ExternalServerUtils;
import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.emoncms.EmoncmsClient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = { "writeApiKey" })
@NoArgsConstructor
@Slf4j
public class ExternalServerEmoncms extends ExternalServer {

    public static final String KEY_URL = "url";
    public static final String KEY_TRUST_HOST_TYPE = "trustHostType";
    public static final String KEY_WRITE_API_KEY = "writeApiKey";

    private String url;
    private TRUST_HOST_TYPE trustHostType;
    private String writeApiKey;

    public ExternalServerEmoncms(ExternalServerTable externalServerTable) {
        this.update(externalServerTable);
    }

    @Override
    public void update(ExternalServerTable externalServerTable) {
        super.update(externalServerTable);
        url = (String) externalServerTable.getProperties().get(KEY_URL);
        trustHostType = TRUST_HOST_TYPE.fromString((String) externalServerTable.getProperties().get(
                KEY_TRUST_HOST_TYPE));
        writeApiKey = (String) externalServerTable.getProperties().get(KEY_WRITE_API_KEY);
    }

    @Override
    @JsonIgnore
    public ExternalServerTable getExternalServerTable() {
        ExternalServerTable externalServerTable = super.getExternalServerTable();
        HashMap<String, Object> properties = getProperties();
        properties.put(KEY_URL, url);
        properties.put(KEY_TRUST_HOST_TYPE, trustHostType.getText());
        properties.put(KEY_WRITE_API_KEY, writeApiKey);
        externalServerTable.setProperties(properties);
        return externalServerTable;
    }

    @Override
    public String getServerDetail() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("URL: ").append(url)
                .append(", TrustHost: ").append(getTrustHostType().getText());
        return stringBuilder.toString();
    }

    @Override
    public synchronized void send(SensorVariable sensorVariable) {
        if (getEnabled()) {
            ClientResponse<String> clientResponse = ((EmoncmsClient) ExternalServerUtils.getClient(getId()))
                    .send(getVariableKey(sensorVariable, getKeyFormat()), sensorVariable.getValue());
            if (!clientResponse.isSuccess()) {
                _logger.error("Failed to send data to remote server! {}, Remote server:{}, {}", clientResponse,
                        toString(), getUrl());
            } else {
                _logger.debug("Remote server update status: {}, Remote server:{}, {}", clientResponse,
                        toString(), getUrl());
            }
        }
    }

    @JsonGetter("trustHostType")
    private String getTrustHost() {
        return getTrustHostType().getText();
    }

}
