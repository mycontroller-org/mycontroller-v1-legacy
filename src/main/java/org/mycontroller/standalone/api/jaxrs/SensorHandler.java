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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/sensors")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "user" })
public class SensorHandler {
    private static final Logger _logger = LoggerFactory.getLogger(SensorHandler.class);

    @GET
    @Path("/{nodeId}")
    public Response getAllSensors(@PathParam("nodeId") int nodeId) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAll(nodeId);
        return RestUtils.getResponse(Status.OK, sensors);
    }

    @GET
    @Path("/")
    public Response getAllSensors(@QueryParam("typeString") String typeString,
            @QueryParam("sensorRefId") String sensorRefId) {
        List<Sensor> sensors = null;
        if (typeString != null) {
            sensors = DaoUtils.getSensorDao().getByType(typeString);
        } else if (sensorRefId != null) {
            return RestUtils.getResponse(Status.OK, DaoUtils.getSensorDao().get(Integer.valueOf(sensorRefId)));
        } else {

            sensors = DaoUtils.getSensorDao().getAll();
        }
        return RestUtils.getResponse(Status.OK, sensors);
    }

    @GET
    @Path("/{nodeId}/{sensorId}")
    public Response get(@PathParam("nodeId") int nodeId, @PathParam("sensorId") int sensorId) {
        Sensor sensor = DaoUtils.getSensorDao().get(nodeId, sensorId);
        return RestUtils.getResponse(Status.OK, sensor);
    }

    @DELETE
    @Path("/{nodeId}/{sensorId}")
    public Response delete(@PathParam("nodeId") int nodeId, @PathParam("sensorId") int sensorId) {
        Sensor sensor = DaoUtils.getSensorDao().get(nodeId, sensorId);
        DeleteResourceUtils.deleteSensor(sensor);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/{nodeId}")
    public Response update(@PathParam("nodeId") int nodeId, Sensor sensor) {
        DaoUtils.getSensorDao().update(nodeId, sensor);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/{nodeId}")
    public Response add(@PathParam("nodeId") Integer nodeId, Sensor sensor) {
        _logger.debug("NodeId:{}, Sensor:{}", nodeId, sensor);
        DaoUtils.getSensorDao().create(nodeId, sensor);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/{id}/{payload}")
    public Response sendPayload(@PathParam("id") Integer id, @PathParam("payload") String payload) {
        _logger.debug("Id:{}, Payload:{}", id, payload);
        Sensor sensor = DaoUtils.getSensorDao().get(id);
        if (sensor != null) {
            if (sensor.getMessageType() == null) {
                sensor.setMessageType(MESSAGE_TYPE_SET_REQ.V_VAR1.ordinal());
            }
            RawMessage rawMessage = new RawMessage(
                    sensor.getNode().getId(),
                    sensor.getSensorId(),
                    MESSAGE_TYPE.C_SET.ordinal(), //messageType
                    0, //ack
                    sensor.getMessageType(),//subType
                    payload,
                    true);// isTxMessage
            ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
            return RestUtils.getResponse(Status.OK);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Sensor[" + id + "] not found!"));
        }
    }

}
