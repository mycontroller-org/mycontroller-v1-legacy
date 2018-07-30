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
package org.mycontroller.standalone.api.jaxrs.mixins;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.DateTimeDeserializer;
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
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
abstract class ResourcesLogsMixin {

    @JsonSerialize(using = LogLevelSerializer.class)
    public abstract String getLogLevel();

    @JsonSerialize(using = ResourceTypeSerializer.class)
    public abstract String getResourceType();

    @JsonSerialize(using = LogDirectionSerializer.class)
    public abstract String getLogDirection();

    @JsonSerialize(using = MessageTypeSerializer.class)
    public abstract String getMessageType();

    @JsonDeserialize(using = LogLevelDeserializer.class)
    public abstract void setLogLevel(LOG_LEVEL logLevel);

    @JsonDeserialize(using = ResourceTypeDeserializer.class)
    public abstract void setResourceType(RESOURCE_TYPE resourceType);

    @JsonDeserialize(using = LogDirectionDeserializer.class)
    public abstract void setLogDirection(LOG_DIRECTION logDirection);

    @JsonDeserialize(using = MessageTypeDeserializer.class)
    public abstract void setMessageType(MESSAGE_TYPE messageType);

    @JsonDeserialize(using = DateTimeDeserializer.class)
    public abstract void setTimestamp(Long timestamp);

}