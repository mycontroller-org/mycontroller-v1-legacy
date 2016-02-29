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

import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.NotificationTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.LastSeenSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.NotificationTypeSerializer;
import org.mycontroller.standalone.notification.NotificationUtils.NOTIFICATION_TYPE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
abstract class NotificationMixin {

    @JsonIgnore
    private Long lastExecution;

    @JsonSerialize(using = NotificationTypeSerializer.class)
    abstract public String getType();

    @JsonDeserialize(using = NotificationTypeDeserializer.class)
    abstract public void setType(NOTIFICATION_TYPE notificationType);

    @JsonProperty("lastExecution")
    @JsonSerialize(using = LastSeenSerializer.class)
    abstract public String getLastExecution();

    @JsonIgnore
    abstract public void setLastExecution(Long lastExecution);
}
