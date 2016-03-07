/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
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
@RolesAllowed({ "Admin" })
public class ForwardPayloadHandler {

    @POST
    @Path("/")
    public Response add(ForwardPayload forwardPayload) {
        if (forwardPayload.getSource().getId() == forwardPayload.getDestination().getId()) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(
                    "Source and destination should be different!"));
        }
        DaoUtils.getForwardPayloadDao().create(forwardPayload);
        return RestUtils.getResponse(Status.CREATED);
    }

    @PUT
    @Path("/")
    public Response update(ForwardPayload forwardPayload) {
        if (forwardPayload.getSource().getId() == forwardPayload.getDestination().getId()) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(
                    "Source and destination should be different!"));
        }
        DaoUtils.getForwardPayloadDao().update(forwardPayload);
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getForwardPayloadDao().getById(id));
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

        QueryResponse queryResponse = DaoUtils.getForwardPayloadDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : ForwardPayload.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);

    }

    @POST
    @Path("/delete")
    public Response delete(List<Integer> ids) {
        DaoUtils.getForwardPayloadDao().deleteByIds(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/enable")
    public Response enable(List<Integer> ids) {
        DaoUtils.getForwardPayloadDao().enable(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/disable")
    public Response disable(List<Integer> ids) {
        DaoUtils.getForwardPayloadDao().disable(ids);
        return RestUtils.getResponse(Status.OK);
    }

}
