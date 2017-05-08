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
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.MetricEngineTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.TrustHostTypeSerializer;
import org.mycontroller.standalone.metrics.METRIC_ENGINE;
import org.mycontroller.standalone.metrics.engine.conf.HawkularConf;
import org.mycontroller.standalone.metrics.engine.conf.InfluxDBConf;
import org.mycontroller.standalone.metrics.engine.conf.MetricEngineConf;
import org.mycontroller.standalone.metrics.engine.conf.MyControllerConf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@JsonDeserialize(using = MetricEngineConfDeserializer.class)
@JsonIgnoreProperties(value = { "purgeEveryThing", "testOnly" })
abstract class MetricEngineConfMixin {
    @JsonSerialize(using = MetricEngineTypeSerializer.class)
    public abstract String getType();

    @JsonSerialize(using = TrustHostTypeSerializer.class)
    public abstract String getTrustHostType();
}

class MetricEngineConfDeserializer extends JsonDeserializer<MetricEngineConf> {
    @Override
    public MetricEngineConf deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        if (node.get("type") == null) {
            return null;
        }

        METRIC_ENGINE type = METRIC_ENGINE.fromString(node.get("type").asText());

        MetricEngineConf conf = null;
        switch (type) {
            case MY_CONTROLLER:
                MyControllerConf mcConf = new MyControllerConf();
                conf = mcConf;
                break;
            case INFLUXDB:
                InfluxDBConf infulxConf = InfluxDBConf.builder()
                        .url(node.get("url").asText())
                        .trustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()))
                        .database(node.get("database").asText())
                        .build();
                if (node.get("username") != null) {
                    infulxConf.setUsername(node.get("username").asText());
                    infulxConf.setPassword(node.get("password").asText());
                }
                conf = infulxConf;
                break;
            case HAWKULAR:
                HawkularConf hwkConf = HawkularConf.builder()
                        .url(node.get("url").asText())
                        .trustHostType(TRUST_HOST_TYPE.fromString(node.get("trustHostType").asText()))
                        .tenant(node.get("tenant").asText())
                        .build();
                if (node.get("username") != null) {
                    hwkConf.setUsername(node.get("username").asText());
                    hwkConf.setPassword(node.get("password").asText());
                }
                conf = hwkConf;
                break;
            default:
                break;
        }
        //Update engine type
        conf.setType(type);
        if (node.get("purgeEveryThing") != null) {
            conf.setPurgeEveryThing(node.get("purgeEveryThing").asBoolean());
        }
        if (node.get("testOnly") != null) {
            conf.setTestOnly(node.get("testOnly").asBoolean());
        }
        return conf;
    }
}