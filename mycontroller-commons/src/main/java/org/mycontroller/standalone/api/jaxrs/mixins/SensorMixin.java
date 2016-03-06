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

import java.util.List;

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.api.jaxrs.mapper.VariableStatusModel;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.SensorTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.LastSeenSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.SensorTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.SensorVariableSerializer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
abstract class SensorMixin {

    @JsonSerialize(using = SensorTypeSerializer.class)
    abstract public String getType();

    @JsonProperty("type")
    @JsonDeserialize(using = SensorTypeDeserializer.class)
    abstract public void setType(MESSAGE_TYPE_PRESENTATION type);

    @JsonIgnoreProperties(value = { "batteryLevel", "state" })
    abstract public NodeMixin getNode();

    @JsonGetter(value = "variables")
    @JsonSerialize(using = SensorVariableSerializer.class)
    abstract public List<VariableStatusModel> getIdforVariables();

    @JsonGetter(value = "lastSeen")
    @JsonSerialize(using = LastSeenSerializer.class)
    abstract public String getLastSeen();

}