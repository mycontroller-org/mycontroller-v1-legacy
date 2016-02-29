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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.imperihome.ImperiHomeSSIUtils;
import org.mycontroller.standalone.imperihome.Rooms;
import org.mycontroller.standalone.imperihome.SystemInfo;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/issapi")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class ImperiHomeISSHandler extends AccessEngine {

    @GET
    @Path("/devices")
    public Response getDevices() {
        return RestUtils.getResponse(Status.OK, ImperiHomeSSIUtils.getAllDevices());
    }

    @GET
    @Path("/system")
    public Response getSystem() {
        return RestUtils.getResponse(Status.OK, SystemInfo.builder().id("52:54:00:01:02:03").apiversion(1).build());
    }

    @GET
    @Path("/rooms")
    public Response getRooms() {
        return RestUtils.getResponse(Status.OK, Rooms.getRoomsObject());
    }
}
