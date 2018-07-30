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
import org.mycontroller.standalone.db.tables.ExternalServerResourceMap;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.db.tables.Resource;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class ResourceApi {

    public QueryResponse getAll(HashMap<String, Object> filters) {
        return DaoUtils.getResourceDao().getAll(Query.get(filters));
    }

    public Resource get(HashMap<String, Object> filters) {
        QueryResponse response = getAll(filters);
        @SuppressWarnings("unchecked")
        List<Resource> items = (List<Resource>) response.getData();
        if (items != null && !items.isEmpty()) {
            return items.get(0);
        }
        return null;
    }

    public Resource get(int id) {
        return DaoUtils.getResourceDao().getById(id);
    }

    public void deleteIds(List<Integer> ids) {
        //Remove resource external server map
        for (Integer resourceId : ids) {
            DaoUtils.getExternalServerResourceMapDao().deleteByResourceId(resourceId);
        }
        DaoUtils.getResourceDao().deleteByIds(ids);
    }

    private void updateEnabled(List<Integer> ids, boolean enabled) {
        List<Resource> resources = DaoUtils.getResourceDao().getAll(ids);
        for (Resource resource : resources) {
            resource.setEnabled(enabled);
            DaoUtils.getResourceDao().update(resource);
        }
    }

    public void enableIds(List<Integer> ids) {
        updateEnabled(ids, true);
    }

    public void disableIds(List<Integer> ids) {
        updateEnabled(ids, false);
    }

    public void add(Resource resource) throws McDuplicateException {
        Resource availabilityCheck = DaoUtils.getResourceDao().get(resource.getResourceType(),
                resource.getResourceId());
        if (availabilityCheck != null) {
            throw new McDuplicateException("This resource already configured. Please check existing one.");
        }
        List<Integer> extServersId = resource.getExternalServers();
        DaoUtils.getResourceDao().create(resource);
        resource = DaoUtils.getResourceDao().get(resource.getResourceType(), resource.getResourceId());
        resource.setExternalServers(extServersId);
        updateExternalServerMap(resource);
    }

    public void update(Resource resource) throws McDuplicateException, McBadRequestException {
        DaoUtils.getResourceDao().update(resource);
        updateExternalServerMap(resource);
    }

    private void updateExternalServerMap(Resource resource) {
        DaoUtils.getExternalServerResourceMapDao().deleteByResourceId(resource.getId());
        ExternalServerTable externalServerTable = ExternalServerTable.builder().build();
        ExternalServerResourceMap externalServerResourceMap = ExternalServerResourceMap.builder()
                .externalServerTable(externalServerTable)
                .resource(resource)
                .build();
        for (Integer extServerId : resource.getExternalServers()) {
            externalServerTable.setId(extServerId);
            DaoUtils.getExternalServerResourceMapDao().create(externalServerResourceMap);
        }
    }
}
