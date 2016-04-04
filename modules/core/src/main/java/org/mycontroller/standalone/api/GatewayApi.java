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
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class Gateways {

    public void updateGateway(Gateway gateway) {
        GatewayUtils.updateGateway(gateway.getGatewayTable());
    }

    public void addGateway(Gateway gateway) {
        GatewayUtils.addGateway(gateway.getGatewayTable());
    }

    public Gateway getGateway(Integer gatewayId) {
        return GatewayUtils.getGateway(DaoUtils.getGatewayDao().getById(gatewayId));
    }

    public QueryResponse getAllGateways(Query query) {
        return DaoUtils.getGatewayDao().getAll(query);
    }

    public void deleteGateways(List<Integer> ids) {
        DeleteResourceUtils.deleteGateways(ids);
    }

    public void enableGateway(List<Integer> ids) {
        GatewayUtils.enableGateways(ids);
    }

    public void enableGateways(List<Integer> ids) {
        GatewayUtils.disableGateways(ids);
    }

    public void reloadGateways(List<Integer> ids) {
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

    public Gateway getGateway(GatewayTable gatewayTable) {
        return GatewayUtils.getGateway(gatewayTable);
    }

}
