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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.THRESHOLD_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.TRIGGER_TYPE;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.DampeningTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.ResourceTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.ThresholdTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.TriggerTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.DampeningTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.LastSeenSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.ResourceTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.ThresholdTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.TriggerTypeSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@JsonIgnoreProperties({ "dampeningInternal1", "dampeningInternal2" })
abstract class AlarmDefinitionMixin {

    @JsonIgnore
    private Long lastTrigger;

    @JsonSerialize(using = ResourceTypeSerializer.class)
    abstract public String getResourceType();

    @JsonSerialize(using = TriggerTypeSerializer.class)
    abstract public String getTriggerType();

    @JsonSerialize(using = ThresholdTypeSerializer.class)
    abstract public String getThresholdType();

    @JsonSerialize(using = DampeningTypeSerializer.class)
    abstract public String getDampeningType();

    @JsonDeserialize(using = ResourceTypeDeserializer.class)
    abstract public void setResourceType(RESOURCE_TYPE resourceType);

    @JsonDeserialize(using = TriggerTypeDeserializer.class)
    abstract public void setTriggerType(TRIGGER_TYPE triggerType);

    @JsonDeserialize(using = ThresholdTypeDeserializer.class)
    abstract public void setThresholdType(THRESHOLD_TYPE thresholdType);

    @JsonDeserialize(using = DampeningTypeDeserializer.class)
    abstract public void setDampeningType(DAMPENING_TYPE dampningType);

    @JsonProperty("lastTrigger")
    @JsonSerialize(using = LastSeenSerializer.class)
    abstract public String getLastTrigger();

    @JsonIgnore
    abstract public void setLastTrigger(Long lastTrigger);

}
