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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.KeyValueJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.api.jaxrs.utils.TypesUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.USER_ROLE;
import org.mycontroller.standalone.db.tables.Sensor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/types")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class TypesHandler {
    @GET
    @Path("/sensorTypes")
    public Response getSensorTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getSensorTypes());
    }

    @GET
    @Path("/sensorValueTypes")
    public Response getSensorValueTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getSensorValueTypes());
    }

    @GET
    @Path("/nodeTypes")
    public Response getNodeTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getNodeTypes());
    }

    @GET
    @Path("/roles")
    public Response getUserRoles() {
        return RestUtils.getResponse(Status.OK, USER_ROLE.values());
    }

    @GET
    @Path("/alarmtriggers")
    public Response getAlarmTriggers() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getAlarmTriggerTypes());
    }

    @GET
    @Path("/alarmtypes")
    public Response getAlarmTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getAlarmTypes());
    }

    @GET
    @Path("/alarmDampeningTypes")
    public Response getAlarmDampeningTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getAlarmDampeningTypes());
    }

    @GET
    @Path("/timerTypes")
    public Response getTimerTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimerTypes());
    }

    @GET
    @Path("/timerFrequencies")
    public Response getTimerFrequencies() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimerFrequencies());
    }

    @GET
    @Path("/timerDays")
    public Response getTimerDays(@QueryParam("allDays") Boolean allDays) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimerDays(allDays != null ? true : false));
    }

    @GET
    @Path("/nodes")
    public Response getNodes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getNodes());
    }

    @GET
    @Path("/sensors/{nodeId}")
    public Response getSensors(@PathParam("nodeId") int nodeId) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getSensors(nodeId));
    }

    @GET
    @Path("/graphInterpolate")
    public Response getGraphInterpolateTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getGraphInterpolateTypes());
    }

    @GET
    @Path("/sensorVariableTypesAll/{sensorType}")
    public Response getSensorVariableTypesAll(@PathParam("sensorType") int sensorType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getSensorVariableTypesAll(sensorType));
    }

    @GET
    @Path("/sensorVariableTypes/{sensorType}")
    public Response getSensorVariableTypes(@PathParam("sensorType") int sensorType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getSensorVariableTypes(sensorType, null));
    }

    @GET
    @Path("/sensorVariableTypesBySenRef/{sensorRefId}")
    public Response getSensorVariableTypesBySensorRefId(@PathParam("sensorRefId") int sensorRefId) {
        Sensor sensor = DaoUtils.getSensorDao().get(sensorRefId);
        if (sensor != null && sensor.getType() != null) {
            return RestUtils.getResponse(Status.OK,
                    TypesUtils.getSensorVariableTypes(sensor.getType(), sensor.getVariableTypes()));
        }
        return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(
                "Requested Sensor[" + sensor.getName() + "] type not available or Sensor not available"));
    }

    @GET
    @Path("/graphSensorVariableTypes/{sensorRefId}")
    public Response getGraphSensorVariableTypes(@PathParam("sensorRefId") int sensorRefId) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getGraphSensorVariableTypes(sensorRefId));
    }

    @GET
    @Path("/messageTypes")
    public Response getMessageTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getMessageTypes());
    }

    @GET
    @Path("/messageSubTypes/{messageType}")
    public Response getMessageSubTypes(@PathParam("messageType") int messageType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getMessageSubTypes(messageType));
    }

    @GET
    @Path("/sensorVariableMapper")
    public Response getSensorVariableMapper() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getVariableMapperList());
    }

    @PUT
    @Path("/sensorVariableMapper")
    public Response updateSensorVariableMapper(KeyValueJson keyValue) {
        TypesUtils.updateVariableMap(keyValue);
        return RestUtils.getResponse(Status.OK);
    }

}
