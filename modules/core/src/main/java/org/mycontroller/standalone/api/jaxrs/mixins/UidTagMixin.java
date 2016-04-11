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

import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.UidTag;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@JsonDeserialize(using = UidTagDeserializer.class)
abstract class UidTagMixin {

}

class UidTagDeserializer extends JsonDeserializer<UidTag> {

    @Override
    public UidTag deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        final JsonNode sVariableNode = node.get("sensorVariable");

        if (node.get("uid") == null || sVariableNode == null || sVariableNode.get("id") == null) {
            return null;
        }

        UidTag uidTag = UidTag.builder()
                .uid(node.get("uid").asInt())
                .sensorVariable(SensorVariable.builder().id(sVariableNode.get("id").asInt()).build())
                .build();
        if (node.get("id") != null) {
            uidTag.setId(node.get("id").asInt());
        }
        return uidTag;
    }
}