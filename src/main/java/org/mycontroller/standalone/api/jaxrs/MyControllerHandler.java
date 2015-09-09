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

import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.IdJson;
import org.mycontroller.standalone.api.jaxrs.mapper.SerialPortJson;
import org.mycontroller.standalone.api.jaxrs.mapper.StringValueJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.api.jaxrs.utils.StatusJVM;
import org.mycontroller.standalone.api.jaxrs.utils.StatusOS;
import org.mycontroller.standalone.gateway.MySensorsGatewayException;
import org.mycontroller.standalone.mysensors.ProcessRawMessageUtils;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "user" })
public class MyControllerHandler {
    private static final Logger _logger = LoggerFactory.getLogger(MyControllerHandler.class);

    //https://gist.github.com/tganzarolli/8520728
    //http://stackoverflow.com/questions/21221688/angularjs-resource-makes-http-options-request-instead-of-http-post-for-save-me
    @OPTIONS
    @Path("/{path:.*}")
    public Response sendPreFlightResponse() {
        _logger.trace("Called sendPreFlightResponse...");
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/ping")
    public Response ping() {
        return RestUtils.getResponse(Status.OK, new StringValueJson("Hello Controller"));
    }

    @GET
    @Path("/timestamp")
    public Response timestamp() {
        return RestUtils.getResponse(Status.OK, new StringValueJson(String.valueOf(System.currentTimeMillis())));
    }

    @GET
    @Path("/time")
    public Response time() {
        return RestUtils.getResponse(Status.OK, new StringValueJson(new Date().toString()));
    }

    @GET
    @Path("/nodeid")
    public Response nodeId() {
        return RestUtils.getResponse(Status.OK,
                new IdJson(String.valueOf(ObjectFactory.getAppProperties().getNodeId())));
    }

    @GET
    @Path("/serialport/info")
    public Response serialport() {
        return RestUtils.getResponse(Status.OK, new SerialPortJson(ObjectFactory.getAppProperties()
                .getSerialPortName(),
                ObjectFactory.getAppProperties().getSerialPortDriver(),
                ObjectFactory.getAppProperties().getSerialPortBaudRate()));
    }

    @POST
    @Path("/serialport/send/raw")
    public Response postInSerialportRaw(StringValueJson mySensorsRawMessage) {
        try {
            ProcessRawMessageUtils.sendMessage(mySensorsRawMessage.getValue());
            return RestUtils.getResponse(Status.OK);
        } catch (MySensorsGatewayException ex) {
            return RestUtils.getResponse(Status.INTERNAL_SERVER_ERROR, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/serialport/send/")
    public Response postInSerialport(RawMessage rawMessage) {
        try {
            ProcessRawMessageUtils.sendMessage(rawMessage);
            return RestUtils.getResponse(Status.OK);
        } catch (MySensorsGatewayException ex) {
            return RestUtils.getResponse(Status.INTERNAL_SERVER_ERROR, new ApiError(ex.getMessage()));
        }

    }

    @GET
    @Path("/osStatus")
    public Response getOsStatus() {
        return RestUtils.getResponse(Status.OK, new StatusOS());
    }

    @GET
    @Path("/jvmStatus")
    public Response getJvmStatus() {
        return RestUtils.getResponse(Status.OK, new StatusJVM());
    }
}
