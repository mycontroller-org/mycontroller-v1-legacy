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

import org.mycontroller.standalone.metrics.METRIC_ENGINE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class MetricEngineTypeDeserializer extends JsonDeserializer<METRIC_ENGINE> {

    @Override
    public METRIC_ENGINE deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        final String nodeType = parser.getText();
        if (nodeType != null) {
            return METRIC_ENGINE.fromString(nodeType);
        } else {
            return null;
        }
    }

}
