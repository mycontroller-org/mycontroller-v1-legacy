/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.api.jaxrs.mixins;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.NodeTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.StateDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.NodeTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.StateSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@JsonTypeName("node")
abstract class NodeMixin {

    @JsonSerialize(using = NodeTypeSerializer.class)
    abstract public String getType();

    @JsonDeserialize(using = NodeTypeDeserializer.class)
    abstract public void setType(MESSAGE_TYPE_PRESENTATION type);

    @JsonSerialize(using = StateSerializer.class)
    abstract public String getState();

    @JsonDeserialize(using = StateDeserializer.class)
    abstract public void setState(STATE state);

    @JsonIgnoreProperties(value = { "connectionDetails", "statusMessage", "statusSince", "variable1", "variable2",
            "variable3", "variable4", "variable5", "state" })
    abstract public GatewayMixin getGateway();

    @JsonSetter(value = "gateway")
    abstract public void setGateway(GatewayMixin gateway);

    @JsonIgnore
    abstract public Integer getEuiInt();

}
