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
import org.mycontroller.standalone.api.SensorApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.ApiMessage;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf;
import org.mycontroller.standalone.api.jaxrs.model.SensorVariableJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.eventbus.McEventBus;
import org.mycontroller.standalone.eventbus.MessageStatus;
import org.mycontroller.standalone.eventbus.MessageStatusHandler;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McInvalidException;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_STATUS;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;

import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/sensors")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
@Slf4j
public class SensorHandler extends AccessEngine {
    private SensorApi sensorApi = new SensorApi();

    @GET
    @Path("/")
    public Response getAllSensors(
            @QueryParam(Sensor.KEY_NODE_ID) List<Integer> nodeIds,
            @QueryParam(Sensor.KEY_NODE_NAME) List<String> nodeName,
            @QueryParam(Sensor.KEY_NODE_EUI) List<String> nodeEui,
            @QueryParam(Sensor.KEY_TYPE) String type,
            @QueryParam(Sensor.KEY_SENSOR_ID) List<Integer> sensorId,
            @QueryParam(Sensor.KEY_NAME) List<String> name,
            @QueryParam(Sensor.KEY_ROOM_ID) List<Integer> roomId,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {

        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Sensor.KEY_TYPE, MESSAGE_TYPE_PRESENTATION.fromString(type));
        filters.put(Sensor.KEY_SENSOR_ID, sensorId);
        filters.put(Sensor.KEY_ROOM_ID, roomId);
        filters.put(Sensor.KEY_NAME, name);

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        //If nodeName or nodeEui is not null, fetch nodeIds
        if (nodeName.size() > 0 || nodeEui.size() > 0) {
            HashMap<String, Object> nodeFilters = new HashMap<String, Object>();
            nodeFilters.put(Node.KEY_NAME, nodeName);
            nodeFilters.put(Node.KEY_EUI, nodeEui);
            nodeFilters.put(Node.KEY_ID, nodeIds);

            //Query primary filters
            nodeFilters.put(Query.PAGE_LIMIT, Query.MAX_ITEMS_UNLIMITED);

            nodeIds = DaoUtils.getNodeDao().getAllIds(Query.get(nodeFilters));
            if (nodeIds.size() == 0) {
                nodeIds.add(-1);//If there is no node available, return empty
            }
        }

        //Add nodeIds
        filters.put(Sensor.KEY_NODE_ID, nodeIds);

        //Update query filter if he is non-admin
        AuthUtils.updateQueryFilter(securityContext, filters, RESOURCE_TYPE.SENSOR);

        return RestUtils.getResponse(Status.OK, sensorApi.getAll(filters));
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id) {
        this.hasAccessSensor(id);
        return RestUtils.getResponse(Status.OK, sensorApi.get(id));
    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/deleteIds")
    public Response deleteIds(List<Integer> ids) {
        this.updateSensorIds(ids);
        sensorApi.deleteIds(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @RolesAllowed({ "admin" })
    @PUT
    @Path("/")
    public Response update(Sensor sensor) {
        this.hasAccessSensor(sensor.getId());
        try {
            sensorApi.update(sensor);
            return RestUtils.getResponse(Status.NO_CONTENT);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }

    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/")
    public Response add(Sensor sensor) {
        try {
            sensorApi.add(sensor);
            return RestUtils.getResponse(Status.CREATED);
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Exception: " + ex.getMessage()));
        }
    }

    @GET
    @Path("/getVariables")
    public Response getVariables(@QueryParam("ids") List<Integer> ids) {
        updateSensorVariableIds(ids);
        return RestUtils.getResponse(Status.OK, sensorApi.getVariables(ids));
    }

    @GET
    @Path("/getVariable/{id}")
    public Response getVariable(@PathParam("id") Integer id) {
        hasAccessSensorVariable(id);
        return RestUtils.getResponse(Status.OK, sensorApi.getVariable(id));
    }

    @PUT
    @Path("/updateVariable")
    public Response sendPayload(SensorVariableJson sensorVariableJson) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariableJson.getId());
        if (sensorVariable != null) {
            this.hasAccessSensor(sensorVariable.getSensor().getId());
            try {
                String topic = sensorApi.sendPayload(sensorVariableJson);
                MessageConsumer<MessageStatus> _consumer = null;
                try {
                    MessageStatusHandler _handler = new MessageStatusHandler();
                    _consumer = McEventBus.getInstance().registerConsumer(topic, _handler);
                    long sleep = 1000 * 3;
                    while (sleep > 0) {
                        if (_handler.getStatusMessage() != null) {
                            if (_handler.getStatusMessage().getStatus() == MESSAGE_STATUS.SUCCESS) {
                                return RestUtils.getResponse(Status.OK,
                                        new ApiMessage(_handler.getStatusMessage().toString()));
                            }
                        }
                        Thread.sleep(10);
                        sleep -= 10;
                    }
                    if (_handler.getStatusMessage() != null
                            && _handler.getStatusMessage().getStatus() != MESSAGE_STATUS.SUCCESS) {
                        return RestUtils.getResponse(Status.BAD_REQUEST,
                                new ApiError(_handler.getStatusMessage().toString()));
                    } else {
                        return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(
                                "Request timeout! No idea about the status. "
                                        + "If it is a sleeping node data will be sent "
                                        + "when receive a request from the node."));
                    }
                } catch (InterruptedException ex) {
                    _logger.error("Exception, ", ex);
                } finally {
                    if (_consumer != null) {
                        _consumer.unregister();
                    }
                }
                return RestUtils.getResponse(Status.OK);
            } catch (NumberFormatException | McInvalidException | McBadRequestException ex) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
            }
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST);
        }
    }

    @PUT
    @Path("/updateVariableConfig")
    public Response updateVariableConfig(SensorVariableJson sensorVariableJson) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariableJson.getId());
        if (sensorVariable != null) {
            hasAccessSensor(sensorVariable.getSensor().getId());
            try {
                sensorApi.updateVariable(sensorVariableJson);
                return RestUtils.getResponse(Status.OK);
            } catch (McBadRequestException ex) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
            }
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST);
        }
    }

    @RolesAllowed({ "Admin" })
    @POST
    @Path("/sendRawMessage")
    public Response sendRawMessage(IMessage message) {
        try {
            sensorApi.sendRawMessage(message);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @RolesAllowed({ "Admin" })
    @PUT
    @Path("/purgeVariable")
    public Response purgeVariable(ResourcePurgeConf purge) {
        try {
            sensorApi.purgeSensorVariable(purge);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

}
