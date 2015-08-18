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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ForwardPayload;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/forwardpayload")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class ForwardPayloadHandler {

    @GET
    @Path("/{sensorRefId}")
    public Response getAll(@PathParam("sensorRefId") int sensorRefId) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getForwardPayloadDao().getAll(sensorRefId));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        DaoUtils.getForwardPayloadDao().delete(id);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/")
    public Response add(ForwardPayload forwardPayload) {
        DaoUtils.getForwardPayloadDao().create(forwardPayload);
        return RestUtils.getResponse(Status.CREATED);
    }
}
