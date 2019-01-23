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
package org.mycontroller.standalone.api.jaxrs.mixins;

import java.io.IOException;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;
import org.mycontroller.standalone.gateway.GatewayUtils.SERIAL_PORT_DRIVER;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.gateway.config.GatewayConfigEthernet;
import org.mycontroller.standalone.gateway.config.GatewayConfigMQTT;
import org.mycontroller.standalone.gateway.config.GatewayConfigPhantIO;
import org.mycontroller.standalone.gateway.config.GatewayConfigPhilipsHue;
import org.mycontroller.standalone.gateway.config.GatewayConfigSerial;
import org.mycontroller.standalone.gateway.config.GatewayConfigWunderground;
import org.mycontroller.standalone.utils.McUtils;

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
 * @since 0.0.2
 */
@JsonSerialize(using = GatewayTableSerializer.class)
@JsonDeserialize(using = GatewayDeserializer.class)
abstract class GatewayMixin {

}

class GatewayTableSerializer extends JsonSerializer<GatewayTable> {
    @Override
    public void serialize(GatewayTable gatewayTable, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (gatewayTable != null) {
            RestUtils.getObjectMapper().writeValue(jgen, GatewayUtils.getGateway(gatewayTable));
        } else {
            jgen.writeNull();
        }
    }
}

class GatewayDeserializer extends JsonDeserializer<GatewayConfig> {

    @Override
    public GatewayConfig deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        if (node.get("id") == null && node.get("type") == null) {
            return null;
        }

        GATEWAY_TYPE gatewayType = GATEWAY_TYPE.fromString(node.get("type").asText());

        GatewayConfig gatewayConfig = null;
        switch (gatewayType) {
            case SERIAL:
                GatewayConfigSerial gatewayConfigSerial = new GatewayConfigSerial();
                gatewayConfigSerial.setDriver(SERIAL_PORT_DRIVER.fromString(node.get("driver").asText()));
                gatewayConfigSerial.setPortName(node.get("portName").asText());
                gatewayConfigSerial.setBaudRate(node.get("baudRate").asInt());
                gatewayConfig = gatewayConfigSerial;
                break;
            case ETHERNET:
                GatewayConfigEthernet gatewayConfigEthernet = new GatewayConfigEthernet();
                gatewayConfigEthernet.setHost(node.get("host").asText());
                gatewayConfigEthernet.setPort(node.get("port").asInt());
                gatewayConfigEthernet.setAliveFrequency(node.get("aliveFrequency").asLong());
                gatewayConfig = gatewayConfigEthernet;
                break;
            case MQTT:
                GatewayConfigMQTT gatewayConfigMQTT = new GatewayConfigMQTT();
                gatewayConfigMQTT.setBrokerHost(node.get("brokerHost").asText());
                gatewayConfigMQTT.setClientId(node.get("clientId").asText());
                gatewayConfigMQTT.setTopicsPublish(node.get("topicsPublish").asText());
                gatewayConfigMQTT.setTopicsSubscribe(node.get("topicsSubscribe").asText());
                gatewayConfigMQTT.setUsername(node.get("username").asText());
                gatewayConfigMQTT.setPassword(node.get("password").asText());
                gatewayConfigMQTT.setQos(node.get("qos").asInt());
                gatewayConfig = gatewayConfigMQTT;
                break;
            case PHANT_IO:
                GatewayConfigPhantIO gatewayConfigPhantIO = new GatewayConfigPhantIO();
                gatewayConfigPhantIO.setUrl(node.get("url").asText());
                gatewayConfigPhantIO.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()));
                gatewayConfigPhantIO.setPublicKey(node.get("publicKey").asText());
                if (node.get("privateKey") != null) {
                    gatewayConfigPhantIO.setPrivateKey(node.get("privateKey").asText());
                }
                gatewayConfigPhantIO.setPollFrequency(node.get("pollFrequency").asInt());
                gatewayConfigPhantIO.setRecordsLimit(node.get("recordsLimit").asLong());
                gatewayConfigPhantIO.setLastUpdate(System.currentTimeMillis() - (McUtils.SECOND * 10));
                gatewayConfig = gatewayConfigPhantIO;
                break;
            case PHILIPS_HUE:
                GatewayConfigPhilipsHue gatewayConfigPhilipsHue = new GatewayConfigPhilipsHue();
                gatewayConfigPhilipsHue.setAuthorizedUser(node.get(GatewayConfigPhilipsHue.KEY_AUTORIZED_USER)
                        .asText());
                gatewayConfigPhilipsHue.setPollFrequency(node.get(GatewayConfigPhilipsHue.KEY_POLL_FREQUENCY).asInt());
                gatewayConfigPhilipsHue.setUrl(node.get(GatewayConfigPhilipsHue.KEY_URL).asText());
                gatewayConfig = gatewayConfigPhilipsHue;
                break;
            case WUNDERGROUND:
                GatewayConfigWunderground gatewayConfigWunderground = new GatewayConfigWunderground();
                gatewayConfigWunderground.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType")
                        .asText()));
                gatewayConfigWunderground.setApiKey(node.get("apiKey").asText());
                gatewayConfigWunderground.setLocation(node.get("location").asText());
                gatewayConfigWunderground.setMergeAllStations(
                        node.get("mergeAllStations") != null ? node.get("mergeAllStations").asBoolean() : false);
                if (node.get("geoIp") != null) {
                    gatewayConfigWunderground.setGeoIp(node.get("geoIp").asText());
                }
                gatewayConfigWunderground.setPollFrequency(node.get("pollFrequency").asInt());
                gatewayConfigWunderground.setLastUpdate(System.currentTimeMillis() - (McUtils.SECOND * 10));
                gatewayConfig = gatewayConfigWunderground;
                break;
            default:
                break;
        }
        //Update RuleDefinition details
        if (node.get("id") != null) {
            gatewayConfig.setId(node.get("id").asInt());
        }
        if (node.get("ackEnabled") != null) {
            gatewayConfig.setAckEnabled(node.get("ackEnabled").asBoolean());
            if (gatewayConfig.getAckEnabled()) {
                gatewayConfig.setStreamAckEnabled(node.get("streamAckEnabled").asBoolean());
                gatewayConfig.setFailedRetryCount(node.get("failedRetryCount").asInt());
                gatewayConfig.setAckWaitTime(node.get("ackWaitTime").asLong());
            }
        } else {
            gatewayConfig.setAckEnabled(false);
        }
        gatewayConfig.setTxDelay(node.get("txDelay").asLong());
        gatewayConfig.setReconnectDelay(node.get("reconnectDelay").asInt());
        gatewayConfig.setEnabled(node.get("enabled").asBoolean());
        gatewayConfig.setName(node.get("name").asText());
        gatewayConfig.setType(gatewayType);
        gatewayConfig.setNetworkType(NETWORK_TYPE.fromString(node.get("networkType").asText()));
        return gatewayConfig;
    }
}

class GatewayTableDeserializer extends JsonDeserializer<GatewayTable> {
    @Override
    public GatewayTable deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        if (node.get("id") == null) {
            return null;
        }
        return GatewayTable.builder().id(node.get("id").asInt()).build();
    }
}