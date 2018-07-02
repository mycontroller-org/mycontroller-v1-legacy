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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.api.NodeApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/nodes")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
@Slf4j
public class NodeHandler extends AccessEngine {
    private NodeApi nodeApi = new NodeApi();

    @GET
    @Path("/")
    public Response getAllNodes(
            @QueryParam(Node.KEY_GATEWAY_ID) Integer gatewayId,
            @QueryParam(Node.KEY_GATEWAY_NAME) List<String> gatewayName,
            @QueryParam(Node.KEY_TYPE) String type,
            @QueryParam(Node.KEY_STATE) String state,
            @QueryParam(Node.KEY_EUI) List<String> eui,
            @QueryParam(Node.KEY_NAME) List<String> name,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Node.KEY_GATEWAY_ID, gatewayId);
        filters.put(Node.KEY_GATEWAY_NAME, gatewayName);
        filters.put(Node.KEY_TYPE, MESSAGE_TYPE_PRESENTATION.fromString(type));
        filters.put(Node.KEY_STATE, STATE.fromString(state));
        filters.put(Node.KEY_EUI, eui);
        filters.put(Node.KEY_NAME, name);

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        //Update query filter if he is non-admin
        AuthUtils.updateQueryFilter(securityContext, filters, RESOURCE_TYPE.NODE);

        return RestUtils.getResponse(Status.OK, nodeApi.getAll(filters));
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id) {
        hasAccessNode(id);
        return RestUtils.getResponse(Status.OK, nodeApi.get(id));
    }

    @POST
    @Path("/deleteIds")
    public Response deleteIds(List<Integer> ids) {
        updateNodeIds(ids);
        nodeApi.deleteIds(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(Node node) {
        hasAccessNode(node.getId());
        try {
            nodeApi.update(node);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/")
    public Response add(Node node) {
        try {
            nodeApi.add(node);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/reboot")
    public Response reboot(List<Integer> ids) {
        updateNodeIds(ids);
        try {
            nodeApi.reboot(ids);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/uploadFirmware")
    public Response uploadFirmware(List<Integer> ids) {
        updateNodeIds(ids);
        try {
            nodeApi.uploadFirmware(ids);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/eraseConfiguration")
    public Response eraseConfig(List<Integer> ids) {
        updateNodeIds(ids);
        try {
            nodeApi.eraseConfig(ids);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/executeNodeInfoUpdate")
    public Response executeNodeInfoUpdate(List<Integer> ids) {
        updateNodeIds(ids);
        try {
            nodeApi.executeNodeInfoUpdate(ids);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }
}
