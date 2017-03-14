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
package org.mycontroller.standalone.api.jaxrs.mixins;

import java.io.IOException;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.standalone.AppProperties.ALPHABETICAL_CASE;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.exernalserver.model.ExternalServer;
import org.mycontroller.standalone.exernalserver.model.ExternalServerEmoncms;
import org.mycontroller.standalone.exernalserver.model.ExternalServerInfluxdb;
import org.mycontroller.standalone.exernalserver.model.ExternalServerMqtt;
import org.mycontroller.standalone.exernalserver.model.ExternalServerPhantIO;
import org.mycontroller.standalone.exernalserver.model.ExternalServerWUnderground;
import org.mycontroller.standalone.externalserver.ExternalServerUtils;
import org.mycontroller.standalone.externalserver.ExternalServerUtils.EXTERNAL_SERVER_TYPE;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@JsonSerialize(using = ExternalServerTableSerializer.class)
@JsonDeserialize(using = ExternalServerDeserializer.class)
abstract class ExternalServerMixin {
}

class ExternalServerTableSerializer extends JsonSerializer<ExternalServerTable> {
    @Override
    public void serialize(ExternalServerTable externalServerTable, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (externalServerTable != null) {
            RestUtils.getObjectMapper().writeValue(jgen, ExternalServerUtils.getExternalServer(externalServerTable));
        } else {
            jgen.writeNull();
        }
    }
}

class ExternalServerDeserializer extends JsonDeserializer<ExternalServer> {

    @Override
    public ExternalServer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        if (node.get("id") == null && node.get("type") == null) {
            return null;
        }

        EXTERNAL_SERVER_TYPE type = EXTERNAL_SERVER_TYPE.fromString(node.get("type").asText());

        ExternalServer externalServer = null;
        switch (type) {
            case PHANT_IO:
                ExternalServerPhantIO extServerPhantIO = new ExternalServerPhantIO();
                extServerPhantIO.setUrl(node.get("url").asText());
                extServerPhantIO.setPublicKey(node.get("publicKey").asText());
                extServerPhantIO.setPrivateKey(node.get("privateKey").asText());
                extServerPhantIO.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()));
                externalServer = extServerPhantIO;
                break;
            case EMONCMS:
                ExternalServerEmoncms extServerEmoncms = new ExternalServerEmoncms();
                extServerEmoncms.setUrl(node.get("url").asText());
                extServerEmoncms.setWriteApiKey(node.get("writeApiKey").asText());
                extServerEmoncms.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()));
                externalServer = extServerEmoncms;
                break;
            case INFLUXDB:
                ExternalServerInfluxdb extServerInfluxdb = new ExternalServerInfluxdb();
                extServerInfluxdb.setUrl(node.get("url").asText());
                extServerInfluxdb.setDatabase(node.get("database").asText());
                if (node.get("username") != null) {
                    extServerInfluxdb.setUsername(node.get("username").asText());
                    extServerInfluxdb.setPassword(node.get("password").asText());
                }
                if (node.get("tags") != null) {
                    extServerInfluxdb.setTags(node.get("tags").asText());
                }
                extServerInfluxdb.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()));
                externalServer = extServerInfluxdb;
                break;
            case MQTT:
                ExternalServerMqtt extServerMqttClient = new ExternalServerMqtt();
                extServerMqttClient.setUrl(node.get("url").asText());
                if (node.get("username") != null) {
                    extServerMqttClient.setUsername(node.get("username").asText());
                    extServerMqttClient.setPassword(node.get("password").asText());
                }
                extServerMqttClient.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()));
                externalServer = extServerMqttClient;
                break;
            case WUNDERGROUND:
                ExternalServerWUnderground extServerWUnderground = new ExternalServerWUnderground();
                extServerWUnderground.setUrl(node.get("url").asText());
                extServerWUnderground.setStationId(node.get("stationId").asText());
                extServerWUnderground.setStationPassword(node.get("stationPassword").asText());
                extServerWUnderground.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()));
                externalServer = extServerWUnderground;
                break;
            default:
                break;
        }
        //Update RuleDefinition details
        if (node.get("id") != null) {
            externalServer.setId(node.get("id").asInt());
        }
        if (node.get("keyCase") != null) {
            externalServer.setKeyCase(ALPHABETICAL_CASE.valueOf(node.get("keyCase").asText().toUpperCase()));
        }
        externalServer.setKeyFormat(node.get("keyFormat").asText());
        externalServer.setEnabled(node.get("enabled").asBoolean());
        externalServer.setName(node.get("name").asText());
        externalServer.setType(type);
        return externalServer;
    }
}