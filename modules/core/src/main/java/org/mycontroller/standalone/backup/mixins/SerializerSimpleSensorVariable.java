/*
 * Copyright 2015-2019 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.backup.mixins;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.SensorVariable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */

public class SerializerSimpleSensorVariable extends JsonSerializer<SensorVariable> {

    @Override
    public void serialize(SensorVariable item, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonProcessingException {
        if (item != null) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("id", item.getId());
            RestUtils.getObjectMapper().writeValue(jgen, data);
        } else {
            jgen.writeNull();
        }
    }

}
