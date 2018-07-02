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
package org.mycontroller.standalone.model;

import java.util.Collections;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Sensor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class ResourceCountModel {
    private Long nodes;
    private Long sensors;
    private Long timers;
    private Long alarmDefinitions;
    private Long resourcesGroups;

    private RESOURCE_TYPE resourceType;
    private Integer resourceId;

    public ResourceCountModel(RESOURCE_TYPE resourceType, Integer resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        switch (resourceType) {
            case GATEWAY:
                nodes = DaoUtils.getNodeDao().countOf(resourceId);
                sensors = DaoUtils.getSensorDao().countOf(Sensor.KEY_NODE_ID,
                        DaoUtils.getNodeDao().getNodeIdsByGatewayIds(Collections.singletonList(resourceId)));
                resourcesGroups = DaoUtils.getResourcesGroupMapDao().countOf(resourceType, resourceId);
                break;
            case NODE:
                sensors = DaoUtils.getSensorDao().countOf(resourceId);
                resourcesGroups = DaoUtils.getResourcesGroupMapDao().countOf(resourceType, resourceId);
                break;
            case SENSOR:
                resourcesGroups = DaoUtils.getResourcesGroupMapDao().countOf(resourceType, resourceId);
                break;
            default:
                throw new RuntimeException("Not supported KEY_RESOURCE_TYPE:" + resourceType.name());
        }

    }

    public Long getNodes() {
        return nodes;
    }

    public Long getSensors() {
        return sensors;
    }

    public Long getTimers() {
        return timers;
    }

    public Long getAlarmDefinitions() {
        return alarmDefinitions;
    }

    public RESOURCE_TYPE getResourceType() {
        return resourceType;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public Long getResourcesGroups() {
        return resourcesGroups;
    }
}
