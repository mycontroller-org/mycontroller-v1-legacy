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
package org.mycontroller.standalone.api;

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;
import org.mycontroller.standalone.group.ResourcesGroupUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class ResourcesGroupApi {

    public void updateResourcesGroup(ResourcesGroup resourcesGroup) {
        ResourcesGroup resourcesGroupOld = DaoUtils.getResourcesGroupDao().get(resourcesGroup.getId());
        resourcesGroupOld.setDescription(resourcesGroup.getDescription());
        resourcesGroupOld.setName(resourcesGroup.getName());
        DaoUtils.getResourcesGroupDao().update(resourcesGroupOld);
    }

    public void addResourcesGroup(ResourcesGroup resourcesGroup) {
        DaoUtils.getResourcesGroupDao().create(resourcesGroup);
    }

    public ResourcesGroup getResourcesGroup(int groupId) {
        return DaoUtils.getResourcesGroupDao().get(groupId);
    }

    public QueryResponse getAllResourcesGroups(HashMap<String, Object> filters) {
        return DaoUtils.getResourcesGroupDao().getAll(Query.get(filters));
    }

    public void deleteResourcesGroup(List<Integer> ids) {
        DeleteResourceUtils.deleteResourcesGroup(ids);
    }

    public void turnOn(List<Integer> ids) {
        ResourcesGroupUtils.turnONresourcesGroup(ids);
    }

    public void turnOff(List<Integer> ids) {
        ResourcesGroupUtils.turnOFFresourcesGroup(ids);
    }

    //Mapping
    public void updateResourcesGroupMap(ResourcesGroupMap resourcesGroupMap) {
        DaoUtils.getResourcesGroupMapDao().update(resourcesGroupMap);
    }

    public void addResourcesGroupMap(ResourcesGroupMap resourcesGroupMap) {
        DaoUtils.getResourcesGroupMapDao().createOrUpdate(resourcesGroupMap);
    }

    public ResourcesGroupMap getResourcesGroupMap(int id) {
        return DaoUtils.getResourcesGroupMapDao().get(id);
    }

    public QueryResponse getAllResourcesGroupsMap(HashMap<String, Object> filters) {
        return DaoUtils.getResourcesGroupMapDao().getAll(Query.get(filters));
    }

    public void deleteResourcesGroupMap(List<Integer> ids) {
        DaoUtils.getResourcesGroupMapDao().delete(ids);
    }

}
