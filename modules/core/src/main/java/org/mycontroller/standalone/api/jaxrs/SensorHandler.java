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
package org.mycontroller.standalone.api.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
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

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.api.jaxrs.json.ApiError;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.json.SensorVariableJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/sensors")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class SensorHandler extends AccessEngine {
    private static final Logger _logger = LoggerFactory.getLogger(SensorHandler.class);

    @GET
    @Path("/")
    public Response getAllSensors(
            @QueryParam(Sensor.KEY_NODE_ID) List<Integer> nodeIds,
            @QueryParam(Sensor.KEY_NODE_NAME) List<String> nodeName,
            @QueryParam(Sensor.KEY_NODE_EUI) List<String> nodeEui,
            @QueryParam(Sensor.KEY_TYPE) String type,
            @QueryParam(Sensor.KEY_SENSOR_ID) List<Integer> sensorId,
            @QueryParam(Sensor.KEY_NAME) List<String> name,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {

        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Sensor.KEY_TYPE, MESSAGE_TYPE_PRESENTATION.fromString(type));
        filters.put(Sensor.KEY_SENSOR_ID, sensorId);
        filters.put(Sensor.KEY_NAME, name);

        //If nodeName or nodeEui is not null, fetch nodeIds
        if (nodeName.size() > 0 || nodeEui.size() > 0) {
            HashMap<String, Object> nodeFilters = new HashMap<String, Object>();
            nodeFilters.put(Node.KEY_NAME, nodeName);
            nodeFilters.put(Node.KEY_EUI, nodeEui);
            nodeFilters.put(Node.KEY_ID, nodeIds);
            nodeIds = DaoUtils.getNodeDao().getAllIds(
                    Query.builder()
                            .order(Query.ORDER_ASC)
                            .orderBy(Node.KEY_ID)
                            .filters(nodeFilters)
                            .pageLimit(Query.MAX_ITEMS_UNLIMITED)
                            .page(1L)
                            .build());
            if (nodeIds.size() == 0) {
                nodeIds.add(-1);//If there is no node available, return empty
            }
        }

        //Add nodeIds
        filters.put(Sensor.KEY_NODE_ID, nodeIds);

        //Add id filter if he is non-admin
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            filters.put(Sensor.KEY_ID, AuthUtils.getUser(securityContext).getAllowedResources().getSensorIds());
        }

        QueryResponse queryResponse = DaoUtils.getSensorDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : Sensor.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1L)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id) {
        this.hasAccessSensor(id);
        Sensor sensor = DaoUtils.getSensorDao().getById(id);
        return RestUtils.getResponse(Status.OK, sensor);
    }

    @POST
    @Path("/deleteIds")
    public Response deleteIds(List<Integer> ids) {
        this.updateSensorIds(ids);
        DeleteResourceUtils.deleteSensors(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(Sensor sensor) {
        this.hasAccessSensor(sensor.getId());
        Sensor availabilityCheck = DaoUtils.getSensorDao().get(sensor.getNode().getId(), sensor.getSensorId());
        if (availabilityCheck != null && sensor.getId() != availabilityCheck.getId()) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("A sensor available with this sensor id!"));
        }
        try {
            if (McMessageUtils.validateSensorIdByProvider(sensor)) {
                DaoUtils.getSensorDao().update(sensor);
                // Update Variable Types
                SensorUtils.updateSensorVariables(sensor);
                return RestUtils.getResponse(Status.NO_CONTENT);
            }
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Refer server logs"));
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }

    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/")
    public Response add(Sensor sensor) {
        Sensor availabilityCheck = DaoUtils.getSensorDao().get(sensor.getNode().getId(), sensor.getSensorId());
        if (availabilityCheck != null) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("A sensor available with this sensor id!"));
        }
        try {
            //Take variable types reference
            List<String> variableTypes = sensor.getVariableTypes();
            if (McMessageUtils.validateSensorIdByProvider(sensor)) {
                DaoUtils.getSensorDao().create(sensor);
                sensor = DaoUtils.getSensorDao().get(sensor.getNode().getId(), sensor.getSensorId());
                // Update Variable Types
                sensor.setVariableTypes(variableTypes);
                //Update into database
                SensorUtils.updateSensorVariables(sensor);
                return RestUtils.getResponse(Status.CREATED);
            }
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Refer server logs"));
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Exception: " + ex.getMessage()));
        }

    }

    @GET
    @Path("/getVariables")
    public Response getVariables(@QueryParam("ids") List<Integer> ids) {
        updateSensorVariableIds(ids);
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(ids);
        List<SensorVariableJson> sensorVariableJson = new ArrayList<SensorVariableJson>();
        //Convert to SensorVariableJson
        if (sensorVariables != null) {
            for (SensorVariable sensorVariable : sensorVariables) {
                sensorVariableJson.add(new SensorVariableJson(sensorVariable));
            }
        }
        return RestUtils.getResponse(Status.OK, sensorVariableJson);
    }

    @PUT
    @Path("/updateVariable")
    public Response sendpayload(SensorVariableJson sensorVariableJson) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariableJson.getId());
        if (sensorVariable != null) {
            this.hasAccessSensor(sensorVariable.getSensor().getId());
            try {
                switch (sensorVariable.getMetricType()) {
                    case BINARY:
                        if (McUtils.getBoolean(sensorVariableJson.getValue() == null)) {
                            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Invalid value: "
                                    + sensorVariableJson.getValue()));
                        }
                        break;
                    case DOUBLE:
                        if (McUtils.getDouble(sensorVariableJson.getValue()) == null) {
                            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Invalid value: "
                                    + sensorVariableJson.getValue()));
                        }
                        break;

                    default:
                        break;
                }
            } catch (NumberFormatException nfex) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("NumberFormatException: "
                        + nfex.getMessage()));
            }

            sensorVariable.setValue(String.valueOf(sensorVariableJson.getValue()));
            McObjectManager.getMcActionEngine().sendPayload(sensorVariable);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST);
        }
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/updateVariableUnit")
    public Response updateVariableUnit(SensorVariableJson sensorVariableJson) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariableJson.getId());
        if (sensorVariable != null) {
            hasAccessSensor(sensorVariable.getSensor().getId());
            sensorVariable.setUnit(sensorVariableJson.getUnit());
            //Update sensor unit
            DaoUtils.getSensorVariableDao().update(sensorVariable);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST);
        }
        return RestUtils.getResponse(Status.OK);
    }

}
