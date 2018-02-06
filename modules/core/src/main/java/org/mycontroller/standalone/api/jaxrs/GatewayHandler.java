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
package org.mycontroller.standalone.api.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.api.GatewayApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.ApiMessage;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;
import org.mycontroller.standalone.gateway.config.GatewayConfig;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@Path("/rest/gateways")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class GatewayHandler extends AccessEngine {
    private GatewayApi gatewayApi = new GatewayApi();

    @PUT
    @Path("/")
    public Response updateGateway(GatewayConfig gatewayConfig) {
        this.hasAccessGateway(gatewayConfig.getId());
        gatewayApi.update(gatewayConfig);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/")
    public Response addGateway(GatewayConfig gatewayConfig) {
        gatewayApi.add(gatewayConfig);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @GET
    @Path("/{id}")
    public Response getGateway(@PathParam("id") Integer gatewayId) {
        this.hasAccessGateway(gatewayId);
        return RestUtils.getResponse(Status.OK, gatewayApi.getRaw(gatewayId));
    }

    @GET
    @Path("/statistics/{id}")
    public Response getStatistics(@PathParam("id") Integer gatewayId) {
        this.hasAccessGateway(gatewayId);
        return RestUtils.getResponse(Status.OK, gatewayApi.getStatistics(gatewayId));
    }

    @GET
    @Path("/")
    public Response getAllGateways(
            @QueryParam(GatewayTable.KEY_NAME) List<String> name,
            @QueryParam(GatewayTable.KEY_NETWORK_TYPE) String networkType,
            @QueryParam(GatewayTable.KEY_TYPE) String type,
            @QueryParam(GatewayTable.KEY_STATE) String state,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(GatewayTable.KEY_NAME, name);
        filters.put(GatewayTable.KEY_NETWORK_TYPE, NETWORK_TYPE.fromString(networkType));
        filters.put(GatewayTable.KEY_TYPE, GATEWAY_TYPE.fromString(type));
        filters.put(GatewayTable.KEY_STATE, STATE.fromString(state));

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        //Update query filter if he is non-admin
        AuthUtils.updateQueryFilter(securityContext, filters, RESOURCE_TYPE.GATEWAY);

        return RestUtils.getResponse(Status.OK, gatewayApi.getAllRaw(filters));
    }

    @POST
    @Path("/delete")
    public Response deleteGateways(List<Integer> ids) {
        updateGatewayIds(ids);
        gatewayApi.delete(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/enable")
    public Response enableGateway(List<Integer> ids) {
        updateGatewayIds(ids);
        gatewayApi.enable(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/disable")
    public Response enableGateways(List<Integer> ids) {
        updateGatewayIds(ids);
        gatewayApi.disable(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/reload")
    public Response reloadGateways(List<Integer> ids) {
        updateGatewayIds(ids);
        gatewayApi.reload(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/discover")
    public Response executeNodeDiscover(List<Integer> ids) {
        updateGatewayIds(ids);
        try {
            gatewayApi.executeNodeDiscover(ids);
            return RestUtils.getResponse(Status.OK,
                    ApiMessage.builder().message("Node Discover util started successfully").build());

        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/executeNodeInfoUpdate")
    public Response executeNodeInfoUpdate(List<Integer> ids) {
        updateGatewayIds(ids);
        try {
            gatewayApi.executeNodeInfoUpdate(ids);
            return RestUtils.getResponse(Status.OK,
                    ApiMessage.builder().message("'Node info refresh' util started successfully").build());
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

}
