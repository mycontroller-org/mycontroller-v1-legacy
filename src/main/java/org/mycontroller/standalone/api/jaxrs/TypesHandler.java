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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.api.jaxrs.utils.TypesUtils;
import org.mycontroller.standalone.db.USER_ROLE;

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
}
