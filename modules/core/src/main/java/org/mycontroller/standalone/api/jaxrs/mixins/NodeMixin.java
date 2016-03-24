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

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.NodeTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.StateDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.NodeTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.StateSerializer;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
abstract class NodeMixin {

    @JsonSerialize(using = NodeTypeSerializer.class)
    public abstract String getType();

    @JsonDeserialize(using = NodeTypeDeserializer.class)
    public abstract void setType(MESSAGE_TYPE_PRESENTATION type);

    @JsonSerialize(using = StateSerializer.class)
    public abstract String getState();

    @JsonDeserialize(using = StateDeserializer.class)
    public abstract void setState(STATE state);

    @JsonIgnoreProperties(value = { "connectionDetails", "statusMessage", "statusSince", "state" })
    public abstract GatewayMixin getGateway();

    @JsonSetter(value = "gateway")
    @JsonDeserialize(using = GatewayTableDeserializer.class)
    public abstract void setGatewayTable(GatewayTable gatewayTable);

}
