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
package org.mycontroller.standalone.api.jaxrs.mixins.deserializers;

import java.io.IOException;

import org.mycontroller.standalone.api.jaxrs.model.LocaleString;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class SensorTypeDeserializer extends JsonDeserializer<MESSAGE_TYPE_PRESENTATION> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public MESSAGE_TYPE_PRESENTATION deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        final LocaleString localeString = OBJECT_MAPPER.treeToValue(parser.getCodec().readTree(parser),
                LocaleString.class);
        if (localeString != null && localeString.getEn() != null) {
            return MESSAGE_TYPE_PRESENTATION.fromString(localeString.getEn());
        } else {
            return null;
        }
    }

}
