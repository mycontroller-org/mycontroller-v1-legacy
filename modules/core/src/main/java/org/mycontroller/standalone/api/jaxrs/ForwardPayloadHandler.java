/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.api.ForwardPayloadApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.exceptions.McBadRequestException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/forwardpayload")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "Admin" })
public class ForwardPayloadHandler {

    ForwardPayloadApi forwardPayloadApi = new ForwardPayloadApi();

    @POST
    @Path("/")
    public Response add(ForwardPayload forwardPayload) {
        try {
            forwardPayloadApi.add(forwardPayload);
            return RestUtils.getResponse(Status.CREATED);
        } catch (McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @PUT
    @Path("/")
    public Response update(ForwardPayload forwardPayload) {
        try {
            forwardPayloadApi.update(forwardPayload);
            return RestUtils.getResponse(Status.OK);
        } catch (McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, forwardPayloadApi.get(id));
    }

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(ForwardPayload.KEY_SOURCE_ID) Integer sourceSensorId,
            @QueryParam(ForwardPayload.KEY_DESTINATION_ID) Integer destinationSensorId,
            @QueryParam(ForwardPayload.KEY_ENABLED) Boolean enabled,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(ForwardPayload.KEY_SOURCE_ID, sourceSensorId);
        filters.put(ForwardPayload.KEY_DESTINATION_ID, destinationSensorId);
        filters.put(ForwardPayload.KEY_ENABLED, enabled);

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        return RestUtils.getResponse(Status.OK, forwardPayloadApi.getAll(filters));

    }

    @POST
    @Path("/delete")
    public Response delete(List<Integer> ids) {
        forwardPayloadApi.delete(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/enable")
    public Response enable(List<Integer> ids) {
        forwardPayloadApi.enable(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/disable")
    public Response disable(List<Integer> ids) {
        forwardPayloadApi.disable(ids);
        return RestUtils.getResponse(Status.OK);
    }

}
