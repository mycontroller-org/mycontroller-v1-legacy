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

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.GatewayTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.NetworkTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.StateDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.GatewayTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.NetworkTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.StateSerializer;
import org.mycontroller.standalone.gateway.GatewayUtils.TYPE;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
abstract class GatewayMixin {

    @JsonSerialize(using = GatewayTypeSerializer.class)
    abstract public String getType();

    @JsonDeserialize(using = GatewayTypeDeserializer.class)
    abstract public void setType(TYPE type);

    @JsonSerialize(using = StateSerializer.class)
    abstract public String getState();

    @JsonDeserialize(using = StateDeserializer.class)
    abstract public void setState(STATE state);

    @JsonSerialize(using = NetworkTypeSerializer.class)
    abstract public String getNetworkType();

    @JsonDeserialize(using = NetworkTypeDeserializer.class)
    abstract public void setNetworkType(NETWORK_TYPE type);

}
