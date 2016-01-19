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

import org.mycontroller.standalone.api.jaxrs.mixins.serializers.LogDirectionSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.LogLevelSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.MessageTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.ResourceTypeSerializer;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@JsonTypeName("resourcesLogs")
abstract class ResourcesLogsMixin {

    @JsonSerialize(using = LogLevelSerializer.class)
    abstract public String getLogLevel();

    @JsonSerialize(using = ResourceTypeSerializer.class)
    abstract public String getResourceType();

    @JsonSerialize(using = LogDirectionSerializer.class)
    abstract public String getLogDirection();

    @JsonSerialize(using = MessageTypeSerializer.class)
    abstract public String getMessageType();

}