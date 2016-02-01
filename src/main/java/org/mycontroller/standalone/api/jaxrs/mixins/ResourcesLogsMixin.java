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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.LogDirectionDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.LogLevelDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.MessageTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.ResourceTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.LogDirectionSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.LogLevelSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.MessageTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.ResourceTypeSerializer;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_DIRECTION;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
abstract class ResourcesLogsMixin {

    @JsonSerialize(using = LogLevelSerializer.class)
    abstract public String getLogLevel();

    @JsonSerialize(using = ResourceTypeSerializer.class)
    abstract public String getResourceType();

    @JsonSerialize(using = LogDirectionSerializer.class)
    abstract public String getLogDirection();

    @JsonSerialize(using = MessageTypeSerializer.class)
    abstract public String getMessageType();

    @JsonDeserialize(using = LogLevelDeserializer.class)
    abstract public void setLogLevel(LOG_LEVEL logLevel);

    @JsonDeserialize(using = ResourceTypeDeserializer.class)
    abstract public void setResourceType(RESOURCE_TYPE resourceType);

    @JsonDeserialize(using = LogDirectionDeserializer.class)
    abstract public void setLogDirection(LOG_DIRECTION logDirection);

    @JsonDeserialize(using = MessageTypeDeserializer.class)
    abstract public void setMessageType(MESSAGE_TYPE messageType);

}