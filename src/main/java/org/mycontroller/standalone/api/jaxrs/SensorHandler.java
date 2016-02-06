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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.VariableStatusModel;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/sensors")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class SensorHandler {
    @Context
    SecurityContext securityContext;

    @GET
    @Path("/")
    public Response getAllSensors(
            @QueryParam(Sensor.KEY_NODE_ID) Integer nodeId,
            @QueryParam(Sensor.KEY_NODE_NAME) String nodeName,
            @QueryParam(Sensor.KEY_TYPE) String type,
            @QueryParam(Sensor.KEY_SENSOR_ID) Integer sensorId,
            @QueryParam(Sensor.KEY_NAME) List<String> name,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {

        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Sensor.KEY_NODE_ID, nodeId);
        filters.put(Sensor.KEY_TYPE, MESSAGE_TYPE_PRESENTATION.fromString(type));
        filters.put(Sensor.KEY_SENSOR_ID, sensorId);
        filters.put(Sensor.KEY_NAME, name);

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
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id) {
        this.hasAccess(id);
        Sensor sensor = DaoUtils.getSensorDao().getById(id);
        return RestUtils.getResponse(Status.OK, sensor);
    }

    @POST
    @Path("/deleteIds")
    public Response deleteIds(List<Integer> ids) {
        this.updateIds(ids);
        DeleteResourceUtils.deleteSensors(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(Sensor sensor) {
        this.hasAccess(sensor.getId());
        //Add Update sensor will be handled by Network type interface
        Node node = DaoUtils.getNodeDao().getById(sensor.getNode().getId());
        ObjectFactory.getIActionEngine(node.getGateway().getNetworkType()).updateSensor(sensor);
        // Update Variable Types
        SensorUtils.updateSensorVariables(sensor);
        return RestUtils.getResponse(Status.NO_CONTENT);

    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/")
    public Response add(Sensor sensor) {
        List<String> variableTypes = sensor.getVariableTypes();
        //Add Update sensor will be handled by Network type interface
        Node node = DaoUtils.getNodeDao().getById(sensor.getNode().getId());
        ObjectFactory.getIActionEngine(node.getGateway().getNetworkType()).addSensor(sensor);

        sensor = DaoUtils.getSensorDao().get(sensor.getNode().getId(), sensor.getSensorId());
        for (String variableType : variableTypes) {
            DaoUtils.getSensorVariableDao()
                    .create(SensorVariable.builder().sensor(sensor)
                            .variableType(MESSAGE_TYPE_SET_REQ.fromString(variableType)).build()
                            .updateUnitAndMetricType());
        }
        return RestUtils.getResponse(Status.CREATED);

    }

    @GET
    @Path("/getVariables")
    public Response getVariables(@QueryParam("ids") List<Integer> ids) {
        updateVariableIds(ids);
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(ids);
        List<VariableStatusModel> variableStatusModels = new ArrayList<VariableStatusModel>();
        //Convert to VariableStatusModel
        if (sensorVariables != null) {
            for (SensorVariable sensorVariable : sensorVariables) {
                variableStatusModels.add(new VariableStatusModel(sensorVariable));
            }
        }
        return RestUtils.getResponse(Status.OK, variableStatusModels);
    }

    @PUT
    @Path("/updateVariable")
    public Response sendpayload(VariableStatusModel variableStatusModel) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(variableStatusModel.getId());
        if (sensorVariable != null) {
            this.hasAccess(sensorVariable.getSensor().getId());
            sensorVariable.setValue(String.valueOf(variableStatusModel.getValue()));
            ObjectFactory.getIActionEngine(sensorVariable.getSensor().getNode().getGateway().getNetworkType())
                    .sendPayload(sensorVariable);

        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST);
        }
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/updateVariableUnit")
    public Response updateVariableUnit(VariableStatusModel variableStatusModel) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(variableStatusModel.getId());
        if (sensorVariable != null) {
            this.hasAccess(sensorVariable.getSensor().getId());
            sensorVariable.setUnit(variableStatusModel.getUnit());
            //Update sensor unit
            DaoUtils.getSensorVariableDao().update(sensorVariable);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST);
        }
        return RestUtils.getResponse(Status.OK);
    }

    private void updateIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            for (Integer id : ids) {
                if (!AuthUtils.getUser(securityContext).getAllowedResources().getSensorIds().contains(id)) {
                    ids.remove(id);
                }
            }
        }
    }

    private void hasAccess(Integer id) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.getUser(securityContext).getAllowedResources().getSensorIds().contains(id)) {
                throw new ForbiddenException("You do not have access for this resource!");
            }
        }
    }

    private void updateVariableIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            for (Integer id : ids) {
                if (!AuthUtils.getUser(securityContext).getAllowedResources().getSensorVariableIds().contains(id)) {
                    ids.remove(id);
                }
            }
        }
    }
}
