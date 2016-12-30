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
package org.mycontroller.standalone.api.jaxrs.mixins;

import java.io.IOException;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;
import org.mycontroller.standalone.gateway.GatewayUtils.SERIAL_PORT_DRIVER;
import org.mycontroller.standalone.gateway.model.Gateway;
import org.mycontroller.standalone.gateway.model.GatewayEthernet;
import org.mycontroller.standalone.gateway.model.GatewayMQTT;
import org.mycontroller.standalone.gateway.model.GatewayPhantIO;
import org.mycontroller.standalone.gateway.model.GatewayPhilipsHue;
import org.mycontroller.standalone.gateway.model.GatewaySerial;
import org.mycontroller.standalone.restclient.RestFactory.TRUST_HOST_TYPE;
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

class GatewayDeserializer extends JsonDeserializer<Gateway> {

    @Override
    public Gateway deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        if (node.get("id") == null && node.get("type") == null) {
            return null;
        }

        GATEWAY_TYPE gatewayType = GATEWAY_TYPE.fromString(node.get("type").asText());

        Gateway gateway = null;
        switch (gatewayType) {
            case SERIAL:
                GatewaySerial gatewaySerial = new GatewaySerial();
                gatewaySerial.setDriver(SERIAL_PORT_DRIVER.fromString(node.get("driver").asText()));
                gatewaySerial.setPortName(node.get("portName").asText());
                gatewaySerial.setBaudRate(node.get("baudRate").asInt());
                gatewaySerial.setRetryFrequency(node.get("retryFrequency").asLong());
                gateway = gatewaySerial;
                break;
            case ETHERNET:
                GatewayEthernet gatewayEthernet = new GatewayEthernet();
                gatewayEthernet.setHost(node.get("host").asText());
                gatewayEthernet.setPort(node.get("port").asInt());
                gatewayEthernet.setAliveFrequency(node.get("aliveFrequency").asLong());
                gateway = gatewayEthernet;
                break;
            case MQTT:
                GatewayMQTT gatewayMQTT = new GatewayMQTT();
                gatewayMQTT.setBrokerHost(node.get("brokerHost").asText());
                gatewayMQTT.setClientId(node.get("clientId").asText());
                gatewayMQTT.setTopicsPublish(node.get("topicsPublish").asText());
                gatewayMQTT.setTopicsSubscribe(node.get("topicsSubscribe").asText());
                gatewayMQTT.setUsername(node.get("username").asText());
                gatewayMQTT.setPassword(node.get("password").asText());
                gatewayMQTT.setQos(node.get("qos").asInt());
                gateway = gatewayMQTT;
                break;
            case PHANT_IO:
                GatewayPhantIO gatewayPhantIO = new GatewayPhantIO();
                gatewayPhantIO.setUrl(node.get("url").asText());
                gatewayPhantIO.setTrustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()));
                gatewayPhantIO.setPublicKey(node.get("publicKey").asText());
                if (node.get("privateKey") != null) {
                    gatewayPhantIO.setPrivateKey(node.get("privateKey").asText());
                }
                gatewayPhantIO.setPollFrequency(node.get("pollFrequency").asInt());
                gatewayPhantIO.setRecordsLimit(node.get("recordsLimit").asLong());
                gatewayPhantIO.setLastUpdate(System.currentTimeMillis() - (McUtils.SECOND * 10));
                gateway = gatewayPhantIO;
                break;
            case PHILIPS_HUE:
                GatewayPhilipsHue gatewayPhilipsHue = new GatewayPhilipsHue();
                gatewayPhilipsHue.setAuthorizedUser(node.get(GatewayPhilipsHue.KEY_AUTORIZED_USER).asText());
                gatewayPhilipsHue.setPollFrequency(node.get(GatewayPhilipsHue.KEY_POLL_FREQUENCY).asInt());
                gatewayPhilipsHue.setUrl(node.get(GatewayPhilipsHue.KEY_URL).asText());
                gateway = gatewayPhilipsHue;
                break;
            default:
                break;
        }
        //Update RuleDefinition details
        if (node.get("id") != null) {
            gateway.setId(node.get("id").asInt());
        }
        gateway.setEnabled(node.get("enabled").asBoolean());
        gateway.setName(node.get("name").asText());
        gateway.setType(gatewayType);
        gateway.setNetworkType(NETWORK_TYPE.fromString(node.get("networkType").asText()));
        return gateway;
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