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
package org.mycontroller.standalone.group;

import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.model.ResourceModel;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class ResourcesGroupUtils {
    private ResourcesGroupUtils() {

    }

    public static void turnONresourcesGroup(List<Integer> ids) {
        for (Integer id : ids) {
            changeStateResourcesGroup(id, STATE.ON);
        }
    }

    public static void turnOFFresourcesGroup(List<Integer> ids) {
        for (Integer id : ids) {
            changeStateResourcesGroup(id, STATE.OFF);
        }
    }

    public static void turnONresourcesGroup(Integer id) {
        changeStateResourcesGroup(id, STATE.ON);
    }

    public static void turnOFFresourcesGroup(Integer id) {
        changeStateResourcesGroup(id, STATE.OFF);
    }

    private static void changeStateResourcesGroup(Integer id, STATE state) {
        ResourcesGroup resourcesGroup = DaoUtils.getResourcesGroupDao().get(id);
        if (resourcesGroup.getState() == state) {
            //nothing to do just return from here
            return;
        }

        List<ResourcesGroupMap> resourcesGroupMaps = DaoUtils.getResourcesGroupMapDao().getAll(id);
        for (ResourcesGroupMap resourcesGroupMap : resourcesGroupMaps) {
            ResourceModel resourceModel = new ResourceModel(resourcesGroupMap.getResourceType(),
                    resourcesGroupMap.getResourceId());
            PayloadOperation operation = null;
            if (STATE.ON == state) {
                operation = new PayloadOperation(resourcesGroupMap.getPayloadOn());
            } else if (STATE.OFF == state) {
                operation = new PayloadOperation(resourcesGroupMap.getPayloadOff());
            } else {
                //return
                return;
            }

            if (resourceModel.getResourceType() == RESOURCE_TYPE.GATEWAY) {
                GatewayUtils.executeGatewayOperation(resourceModel, operation);
            } else {
                ObjectFactory.getIActionEngine(resourceModel.getNetworkType())
                        .executeSendPayload(resourceModel, operation);
            }
        }

        //Update status in group table
        resourcesGroup.setState(state);
        resourcesGroup.setStateSince(System.currentTimeMillis());
        DaoUtils.getResourcesGroupDao().update(resourcesGroup);

        //TODO: add it in to log message
    }

}
