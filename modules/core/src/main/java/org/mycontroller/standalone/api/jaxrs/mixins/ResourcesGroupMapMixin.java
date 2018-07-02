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
import org.mycontroller.standalone.api.jaxrs.mixins.deserializers.ResourceTypeDeserializer;
import org.mycontroller.standalone.api.jaxrs.mixins.serializers.ResourceTypeSerializer;
import org.mycontroller.standalone.db.tables.ResourcesGroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
abstract class ResourcesGroupMapMixin {

    @JsonSerialize(using = ResourceTypeSerializer.class)
    public abstract String getResourceType();

    @JsonDeserialize(using = ResourceTypeDeserializer.class)
    public abstract void setResourceType(RESOURCE_TYPE resourceType);

    @JsonIgnoreProperties({ "name", "description", "state", "stateSince" })
    public abstract ResourcesGroup getResourcesGroup();

}
