/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;
import org.mycontroller.standalone.exernalserver.model.ExternalServer;
import org.mycontroller.standalone.externalserver.ExternalServerUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class ExternalServerApi {

    public QueryResponse getAll(HashMap<String, Object> filters) {
        return DaoUtils.getExternalServerTableDao().getAll(Query.get(filters));
    }

    public ExternalServerTable get(HashMap<String, Object> filters) {
        QueryResponse response = getAll(filters);
        @SuppressWarnings("unchecked")
        List<ExternalServerTable> items = (List<ExternalServerTable>) response.getData();
        if (items != null && !items.isEmpty()) {
            return items.get(0);
        }
        return null;
    }

    public ExternalServerTable get(int id) {
        return DaoUtils.getExternalServerTableDao().getById(id);
    }

    public void deleteIds(List<Integer> ids) {
        for (Integer externalServerId : ids) {
            ExternalServerUtils.delete(externalServerId);
        }
    }

    public void enableIds(List<Integer> ids) {
        ExternalServerUtils.updateEnabled(ids, true);
    }

    public void disableIds(List<Integer> ids) {
        ExternalServerUtils.updateEnabled(ids, false);
    }

    public void update(ExternalServer externalServer) throws McDuplicateException, McBadRequestException {
        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put(ExternalServerTable.KEY_NAME, externalServer.getName());
        ExternalServerTable availabilityCheck = get(filters);
        if (availabilityCheck != null && availabilityCheck.getId() != externalServer.getId()) {
            throw new McDuplicateException("An external server available with this name.");
        }
        ExternalServerUtils.update(externalServer);
    }

    public void add(ExternalServer externalServer) throws McDuplicateException {
        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put(ExternalServerTable.KEY_NAME, externalServer.getName());
        ExternalServerTable availabilityCheck = get(filters);
        if (availabilityCheck != null) {
            throw new McDuplicateException("An external server available with this name.");
        }
        ExternalServerUtils.add(externalServer);
    }
}
