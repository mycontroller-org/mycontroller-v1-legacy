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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/sensorlog")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class SensorLogHandler {
    @GET
    @Path("/")
    public Response getAll() {
        return RestUtils.getResponse(Status.OK, DaoUtils.getSensorLogDao().getAll());
    }

    @GET
    @Path("/{sensorRefId}/all")
    public Response getAll(@PathParam("sensorRefId") int sensorRefId) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getSensorLogDao().getAll(sensorRefId));
    }

    @GET
    @Path("/{id}/sensorData")
    public Response getSensorDetails(@PathParam("id") int id) {
        return new AlarmHandler().getSensorDetails(id);
    }
}
