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

import java.util.ArrayList;
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

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.KeyValueJson;
import org.mycontroller.standalone.api.jaxrs.mapper.PayloadJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.RawMessage;
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
    public Response getAllSensors(@QueryParam("nodeName") String nodeName, @QueryParam("typeString") String typeString,
	    @QueryParam("sensorRefId") String sensorRefId) {
	List<Sensor> sensors = null;
	if (typeString != null) {
	    sensors = DaoUtils.getSensorDao().getByType(typeString);
	} else if (sensorRefId != null) {
	    return RestUtils.getResponse(Status.OK, DaoUtils.getSensorDao().get(Integer.valueOf(sensorRefId)));
	} else {

	    sensors = DaoUtils.getSensorDao().getAll();
	}
	if (nodeName != null) {
	    sensors = new ArrayList<>();
	    List<Node> nodes = DaoUtils.getNodeDao().getByName(nodeName);

	    for (Node node : nodes) {
		sensors.addAll(DaoUtils.getSensorDao().getAll(node.getId()));
	    }
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
	// Update Variable Types
	SensorUtils.updateSensorValues(sensor);
	return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/{nodeId}")
    public Response add(@PathParam("nodeId") Integer nodeId, Sensor sensor) {
	_logger.debug("NodeId:{}, Sensor:{}", nodeId, sensor);
	String variableTypes = sensor.getVariableTypes();
	boolean status = DaoUtils.getSensorDao().create(nodeId, sensor);
	if (status) {
	    sensor = DaoUtils.getSensorDao().get(nodeId, sensor.getSensorId());
	    for (String variableType : variableTypes.split(",")) {
		DaoUtils.getSensorValueDao()
			.create(new SensorValue(sensor, MESSAGE_TYPE_SET_REQ.valueOf(variableType.trim()).ordinal()));
	    }
	    return RestUtils.getResponse(Status.CREATED);
	} else {
	    return RestUtils.getResponse(Status.BAD_REQUEST);
	}
    }

    @GET
    @Path("/sensorByRefId")
    public Response getSensorByRefId(@QueryParam("sensorRefId") Integer sensorRefId) {
	if (sensorRefId != null) {
	    return RestUtils.getResponse(Status.OK, DaoUtils.getSensorDao().get(sensorRefId));
	} else {
	    return RestUtils.getResponse(Status.OK, new ApiError("sensorRefId should not be null"));
	}

    }

    @PUT
    @Path("/updateOthers/{sensorRefId}")
    public Response update(@PathParam("sensorRefId") int sensorRefId, List<KeyValueJson> keyValues) {
	SensorUtils.updateOthers(sensorRefId, keyValues);
	return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @GET
    @Path("/getOthers/{sensorRefId}")
    public Response get(@PathParam("sensorRefId") int sensorRefId) {
	return RestUtils.getResponse(Status.OK, SensorUtils.getOthers(sensorRefId));
    }

    @POST
    @Path("/sendPayload")
    public Response sendPayload(PayloadJson payload) {
	_logger.debug("PayloadJson:{}", payload);
	Sensor sensor = null;

	if (payload.getButtonType() != null) {
	    sensor = DaoUtils.getSensorDao().get(payload.getSensorRefId());
	    switch (PayloadJson.BUTTON_TYPE.valueOf(payload.getButtonType().toUpperCase())) {
	    case ON_OFF:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_STATUS.ordinal());
		break;
	    case LOCK_UNLOCK:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_LOCK_STATUS.ordinal());
		break;
	    case ARMED:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_ARMED.ordinal());
		break;
	    case TRIPPED:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_TRIPPED.ordinal());
		break;
	    case INCREASE:
		SensorValue sensorValue = DaoUtils.getSensorValueDao().get(sensor.getId(), payload.getVariableType());
		if (sensorValue != null && sensorValue.getLastValue() != null) {
		    payload.setPayload(String.valueOf(NumericUtils.getDouble(sensorValue.getLastValue()) + 1));
		} else {
		    payload.setPayload("0");
		}
		break;
	    case DECREASE:
		sensorValue = DaoUtils.getSensorValueDao().get(sensor.getId(), payload.getVariableType());
		if (sensorValue != null && sensorValue.getLastValue() != null) {
		    payload.setPayload(String.valueOf(NumericUtils.getDouble(sensorValue.getLastValue()) - 1));
		} else {
		    payload.setPayload("0");
		}
		break;
	    case UP:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_UP.ordinal());
		break;
	    case DOWN:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_DOWN.ordinal());
		break;
	    case STOP:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_STOP.ordinal());
		break;
	    case RGB:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_RGB.ordinal());
		payload.setPayload(payload.getPayload().replace("#", ""));
		break;
	    case RGBW:
		payload.setVariableType(MESSAGE_TYPE_SET_REQ.V_RGBW.ordinal());
		payload.setPayload(SensorUtils.getHexFromRgba(payload.getPayload()));
		break;
	    default:
		break;
	    }
	} else {
	    sensor = DaoUtils.getSensorDao().get(payload.getNodeId(), payload.getSensorId());
	}
	RawMessage rawMessage = new RawMessage(sensor.getNode().getId(), sensor.getSensorId(),
		MESSAGE_TYPE.C_SET.ordinal(), // messageType
		0, // ack
		payload.getVariableType(), // subType
		payload.getPayload(), true);// isTxMessage
	ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
	return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("sensorValue/{sensorValueId}")
    public Response getSensorValue(@PathParam("sensorValueId") int sensorValueId) {
	return RestUtils.getResponse(Status.OK, DaoUtils.getSensorValueDao().get(sensorValueId));
    }
}
