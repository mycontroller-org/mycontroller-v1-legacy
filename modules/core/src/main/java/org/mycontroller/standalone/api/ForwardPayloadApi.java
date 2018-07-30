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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.exceptions.McBadRequestException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.1.0
 */

public class ForwardPayloadApi {

    public List<ForwardPayload> getAll() {
        return DaoUtils.getForwardPayloadDao().getAll();
    }

    public QueryResponse getAll(HashMap<String, Object> filters) {
        return DaoUtils.getForwardPayloadDao().getAll(Query.get(filters));
    }

    public void add(ForwardPayload forwardPayload) throws McBadRequestException {
        if (forwardPayload.getSource().getId() == forwardPayload.getDestination().getId()) {
            throw new McBadRequestException("Source and destination should be different!");
        }
        DaoUtils.getForwardPayloadDao().create(forwardPayload);
    }

    public void update(ForwardPayload forwardPayload) throws McBadRequestException {
        if (forwardPayload.getSource().getId() == forwardPayload.getDestination().getId()) {
            throw new McBadRequestException("Source and destination should be different!");
        }
        DaoUtils.getForwardPayloadDao().update(forwardPayload);
    }

    public ForwardPayload get(Integer id) {
        return DaoUtils.getForwardPayloadDao().getById(id);
    }

    public void delete(List<Integer> ids) {
        DaoUtils.getForwardPayloadDao().deleteByIds(ids);
    }

    public void enable(List<Integer> ids) {
        DaoUtils.getForwardPayloadDao().enable(ids);
    }

    public void disable(List<Integer> ids) {
        DaoUtils.getForwardPayloadDao().disable(ids);
    }

    public void enable(Integer id) {
        enable(Arrays.asList(id));
    }

    public void disable(Integer id) {
        disable(Arrays.asList(id));
    }

    public void enableBySource(Integer id) {
        enable(getIds(getList(id, null, null)));
    }

    public void disableBySource(Integer id) {
        disable(getIds(getList(id, null, null)));
    }

    public void enableByDestination(Integer id) {
        enable(getIds(getList(null, id, null)));
    }

    public void disableByDestination(Integer id) {
        disable(getIds(getList(null, id, null)));
    }

    private List<Integer> getIds(List<ForwardPayload> list) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (ForwardPayload forwardPayload : list) {
            ids.add(forwardPayload.getId());
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private List<ForwardPayload> getList(Integer sourceId, Integer destinationId, Boolean enabled) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(ForwardPayload.KEY_SOURCE_ID, sourceId);
        filters.put(ForwardPayload.KEY_DESTINATION_ID, destinationId);
        filters.put(ForwardPayload.KEY_ENABLED, enabled);
        QueryResponse response = getAll(filters);
        if (response.getData() != null) {
            return (List<ForwardPayload>) response.getData();
        } else {
            return new ArrayList<ForwardPayload>();
        }
    }
}
