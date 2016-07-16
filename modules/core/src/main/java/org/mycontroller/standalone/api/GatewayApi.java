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
package org.mycontroller.standalone.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.model.Gateway;

/**
 * Using this API's can do activities on gateway
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class GatewayApi {

    /**
     * Gives list of gateways with filter
     * <p><b>Filter(s):</b>
     * <p>name - {@link List} of gateway names
     * <p>networkType - MySensors
     * <p>type - Serial, Ethernet, MQTT
     * <p>state - Up, Down, Unavailable
     * <p><b>Page filter(s):</b>
     * <p>pageLimit - Set number of items per page
     * <p>page - Request page number
     * <p>order - Set order. <b>Option:</b> asc, desc
     * <p>orderBy - column name for order. <b>Options:</b> name, networkType, type, state, enabled, statusMessage
     * @param filters refer Filter(s)
     * @return QueryResponse Contains input filter and response
     */
    public QueryResponse getAll(HashMap<String, Object> filters) {
        QueryResponse queryResponse = getAllRaw(filters);
        ArrayList<Gateway> gateways = new ArrayList<Gateway>();
        @SuppressWarnings("unchecked")
        List<GatewayTable> gatewayTables = (List<GatewayTable>) queryResponse.getData();
        for (GatewayTable gatewayTable : gatewayTables) {
            gateways.add(GatewayUtils.getGateway(gatewayTable));
        }
        queryResponse.setData(gateways);
        return queryResponse;
    }

    public Gateway getGateway(GatewayTable gatewayTable) {
        return GatewayUtils.getGateway(gatewayTable);
    }

    public Gateway get(HashMap<String, Object> filters) {
        QueryResponse response = getAllRaw(filters);
        @SuppressWarnings("unchecked")
        List<GatewayTable> items = (List<GatewayTable>) response.getData();
        if (items != null && !items.isEmpty()) {
            return GatewayUtils.getGateway(items.get(0));
        }
        return null;
    }

    public GatewayTable getRaw(Integer gatewayId) {
        return DaoUtils.getGatewayDao().getById(gatewayId);
    }

    public QueryResponse getAllRaw(HashMap<String, Object> filters) {
        return DaoUtils.getGatewayDao().getAll(Query.get(filters));
    }

    public Gateway get(Integer gatewayId) {
        return GatewayUtils.getGateway(getRaw(gatewayId));
    }

    public void add(Gateway gateway) {
        GatewayUtils.addGateway(gateway.getGatewayTable());
    }

    public void update(Gateway gateway) {
        GatewayUtils.updateGateway(gateway.getGatewayTable());
    }

    public void delete(List<Integer> ids) {
        DeleteResourceUtils.deleteGateways(ids);
    }

    public void enable(List<Integer> ids) {
        GatewayUtils.enableGateways(ids);
    }

    public void disable(List<Integer> ids) {
        GatewayUtils.disableGateways(ids);
    }

    public void reload(List<Integer> ids) {
        GatewayUtils.reloadGateways(ids);
    }

    public void executeNodeDiscover(List<Integer> ids) throws McBadRequestException {
        try {
            for (Integer id : ids) {
                GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(id);
                if (gatewayTable.getEnabled()) {
                    McObjectManager.getMcActionEngine().discover(id);
                }
            }

        } catch (Exception ex) {
            throw new McBadRequestException(ex.getMessage());
        }
    }

    public void executeNodeInfoUpdate(List<Integer> ids) throws McBadRequestException {
        try {
            for (Integer id : ids) {
                GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(id);
                if (gatewayTable.getEnabled()) {
                    McObjectManager.getMcActionEngine().updateNodeInformations(id, null);
                }
            }

        } catch (Exception ex) {
            throw new McBadRequestException(ex.getMessage());
        }
    }

}
