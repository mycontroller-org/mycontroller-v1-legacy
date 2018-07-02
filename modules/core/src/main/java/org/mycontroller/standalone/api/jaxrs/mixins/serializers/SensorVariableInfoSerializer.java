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
package org.mycontroller.standalone.api.jaxrs.mixins.serializers;

import java.io.IOException;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.model.ResourceModel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class SensorVariableInfoSerializer extends JsonSerializer<SensorVariable> {

    @Override
    public void serialize(SensorVariable sensorVariable, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        if (sensorVariable != null) {
            ResourceModel resourceModel = new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId());
            jgen.writeStartObject();
            provider.defaultSerializeField("id", sensorVariable.getId(), jgen);
            provider.defaultSerializeField("name", resourceModel.getResourceLessDetails(), jgen);
            provider.defaultSerializeField("type", sensorVariable.getVariableType().getText(), jgen);
            jgen.writeEndObject();
        } else {
            jgen.writeNull();
        }

    }

}
