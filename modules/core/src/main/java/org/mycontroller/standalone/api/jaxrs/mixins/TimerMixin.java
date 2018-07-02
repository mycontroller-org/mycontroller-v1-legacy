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
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.FrequencyTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.ResourceTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.TimerTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.TriggerTimeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.DateTimeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.FrequencyTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.ResourceTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.TimerTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.TriggerTimeSerializer;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@JsonIgnoreProperties({ "internalVariable1", "targetClass" })
abstract class TimerMixin {

    @JsonIgnore
    private Long lastFire;

    @JsonSerialize(using = ResourceTypeSerializer.class)
    public abstract String getResourceType();

    @JsonSerialize(using = TimerTypeSerializer.class)
    public abstract String getTimerType();

    @JsonSerialize(using = FrequencyTypeSerializer.class)
    public abstract String getFrequencyType();

    @JsonDeserialize(using = ResourceTypeDeserializer.class)
    public abstract void setResourceType(RESOURCE_TYPE resourceType);

    @JsonDeserialize(using = TimerTypeDeserializer.class)
    public abstract void setTimerType(TIMER_TYPE timerType);

    @JsonDeserialize(using = FrequencyTypeDeserializer.class)
    public abstract void setFrequencyType(FREQUENCY_TYPE frequencyType);

    @JsonDeserialize(using = TriggerTimeDeserializer.class)
    public abstract void setTriggerTime(Long triggerTime);

    @JsonSerialize(using = TriggerTimeSerializer.class)
    public abstract String getTriggerTime();

    @JsonDeserialize(using = DateTimeDeserializer.class)
    public abstract void setValidityFrom(Long validity);

    @JsonSerialize(using = DateTimeSerializer.class)
    public abstract String getValidityFrom();

    @JsonDeserialize(using = DateTimeDeserializer.class)
    public abstract void setValidityTo(Long validity);

    @JsonSerialize(using = DateTimeSerializer.class)
    public abstract String getValidityTo();

    @JsonIgnore
    public abstract void setLastFire(Long lastFire);

    @JsonGetter("lastFire")
    public abstract Long getLastFire();
}
