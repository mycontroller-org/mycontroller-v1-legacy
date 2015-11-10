/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.mysensors.structs.FirmwareConfigResponse;
import org.mycontroller.standalone.mysensors.NodeDiscover;
import org.mycontroller.standalone.mysensors.RawMessage;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/nodes")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class NodeHandler {
    @GET
    @Path("/")
    public Response getAllNodes() {
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        return RestUtils.getResponse(Status.OK, nodes);
    }

    @GET
    @Path("/{nodeId}")
    public Response get(@PathParam("nodeId") int nodeId) {
        Node node = DaoUtils.getNodeDao().get(nodeId);
        return RestUtils.getResponse(Status.OK, node);
    }

    @DELETE
    @Path("/{nodeId}")
    public Response delete(@PathParam("nodeId") int nodeId) {
        Node node = DaoUtils.getNodeDao().get(nodeId);
        DeleteResourceUtils.deleteNode(node);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(Node node) {
        DaoUtils.getNodeDao().update(node);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/")
    public Response add(Node node) {
        DaoUtils.getNodeDao().create(node);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/reboot")
    public Response reboot(Node node) {
        if (DaoUtils.getNodeDao().get(node) != null) {
            RawMessage rawMessage = new RawMessage(
                    node.getId(),
                    255,
                    MESSAGE_TYPE.C_INTERNAL.ordinal(),
                    0,
                    MESSAGE_TYPE_INTERNAL.I_REBOOT.ordinal(),
                    "",
                    true);
            ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
            return RestUtils.getResponse(Status.OK);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Selected Node not available! Node:[" + node.toString() + "]"));
        }
    }

    @POST
    @Path("/uploadFirmware")
    public Response uploadFirmware(Node node) {
        Node nodeRef = DaoUtils.getNodeDao().get(node.getId());
        if (nodeRef != null) {
            if (nodeRef.getFirmware() != null) {
                FirmwareConfigResponse firmwareConfigResponse = new FirmwareConfigResponse();
                firmwareConfigResponse.setByteBufferPosition(0);

                firmwareConfigResponse.setType(nodeRef.getFirmware().getType().getId());
                firmwareConfigResponse.setVersion(nodeRef.getFirmware().getVersion().getId());
                firmwareConfigResponse.setBlocks(nodeRef.getFirmware().getBlocks());
                firmwareConfigResponse.setCrc(nodeRef.getFirmware().getCrc());

                RawMessage rawMessage = new RawMessage(
                        node.getId(),
                        255,
                        MESSAGE_TYPE.C_STREAM.ordinal(),
                        0,
                        MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.ordinal(),
                        Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase(),
                        true);
                ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
                return RestUtils.getResponse(Status.OK);
            } else {
                return RestUtils.getResponse(Status.BAD_REQUEST,
                        new ApiError("There is no firmware mapped with this Node:[Id:" + node.getId() + ", Name:"
                                + node.getName() + "]"));
            }
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Selected Node not available! Node:[Id:" + node.getId() + ", Name:"
                            + node.getName() + "]"));
        }
    }

    @POST
    @Path("/nodeDiscover")
    public Response executeNodeDiscover() {
        if (NodeDiscover.isDiscoverNodesRunning()) {
            return RestUtils.getResponse(Status.FORBIDDEN, new ApiError("Node Discover util is already running!"));
        } else {
            try {
                new Thread(new NodeDiscover()).start();
                return RestUtils.getResponse(Status.OK, new ApiError("Node Discover util completed successfully"));
            } catch (Exception ex) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
            }
        }
    }

}
