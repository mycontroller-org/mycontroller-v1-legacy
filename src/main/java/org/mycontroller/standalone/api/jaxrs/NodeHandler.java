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

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.Node;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/nodes")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class NodeHandler extends AccessEngine {

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

        //Add id filter if he is non-admin
        if (!isSuperAdmin()) {
            filters.put(Node.KEY_ID, getUser().getAllowedResources().getNodeIds());
        }

        QueryResponse queryResponse = DaoUtils.getNodeDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : Node.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id) {
        hasAccessNode(id);
        Node node = DaoUtils.getNodeDao().getById(id);
        return RestUtils.getResponse(Status.OK, node);
    }

    @POST
    @Path("/deleteIds")
    public Response deleteIds(List<Integer> ids) {
        updateNodeIds(ids);
        DeleteResourceUtils.deleteNodes(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(Node node) {
        hasAccessNode(node.getId());
        Node availabilityCheck = DaoUtils.getNodeDao().get(node.getGateway().getId(), node.getEui());
        if (availabilityCheck != null && availabilityCheck.getId() != node.getId()) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("A node available with this EUI."));
        }
        ObjectManager.getIActionEngine(node.getGateway().getNetworkType()).updateNode(node);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/")
    public Response add(Node node) {
        if (DaoUtils.getNodeDao().get(node.getGateway().getId(), node.getEui()) != null) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("A node available with this EUI."));
        }
        Gateway gateway = DaoUtils.getGatewayDao().getById(node.getGateway().getId());
        node.setGateway(gateway);
        ObjectManager.getIActionEngine(node.getGateway().getNetworkType()).addNode(node);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/reboot")
    public Response reboot(List<Integer> ids) {
        updateNodeIds(ids);
        List<Node> nodes = DaoUtils.getNodeDao().getAll(ids);
        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
                ObjectManager.getIActionEngine(node.getGateway().getNetworkType()).rebootNode(node);
            }
            return RestUtils.getResponse(Status.OK);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Selected Node(s) not available! Number of nodes:[" + nodes.size() + "]"));
        }
    }

    @POST
    @Path("/uploadFirmware")
    public Response uploadFirmware(List<Integer> ids) {
        updateNodeIds(ids);
        List<Node> nodes = DaoUtils.getNodeDao().getAll(ids);
        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
                if (node.getFirmware() != null) {
                    ObjectManager.getIActionEngine(node.getGateway().getNetworkType()).uploadFirmware(node);
                }
            }
            return RestUtils.getResponse(Status.OK);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Selected Node(s) not available! Node Ids:[" + ids + "]"));
        }
    }

    @POST
    @Path("/eraseConfiguration")
    public Response eraseConfig(List<Integer> ids) {
        updateNodeIds(ids);
        List<Node> nodes = DaoUtils.getNodeDao().getAll(ids);
        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
                ObjectManager.getIActionEngine(node.getGateway().getNetworkType()).eraseConfiguration(node);
            }
            return RestUtils.getResponse(Status.OK);

        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Selected Node not available! Node Ids:[" + ids + "]"));
        }
    }

}
