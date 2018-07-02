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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.ResourceApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.Resource;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/resourcesdata")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class ResourcesDataHandler {

    private ResourceApi resourceApi = new ResourceApi();

    @PUT
    @Path("/")
    public Response updateResource(Resource resource) {
        try {
            resourceApi.update(resource);
            return RestUtils.getResponse(Status.OK);
        } catch (McDuplicateException | McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/")
    public Response addResource(Resource resource) {
        try {
            resourceApi.add(resource);
            return RestUtils.getResponse(Status.OK);
        } catch (McDuplicateException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/{id}")
    public Response getResource(@PathParam("id") Integer id) {
        return RestUtils.getResponse(Status.OK, resourceApi.get(id));
    }

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(Resource.KEY_ID) List<Integer> ids,
            @QueryParam(Resource.KEY_RESOURCE_TYPE) String resourceType,
            @QueryParam(Resource.KEY_ENABLED) Boolean enabled,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Resource.KEY_ID, ids);
        filters.put(Resource.KEY_RESOURCE_TYPE, RESOURCE_TYPE.fromString(resourceType));
        filters.put(Resource.KEY_ENABLED, enabled);

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        return RestUtils.getResponse(Status.OK, resourceApi.getAll(filters));
    }

    @POST
    @Path("/delete")
    public Response delete(List<Integer> ids) {
        resourceApi.deleteIds(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/enable")
    public Response enable(List<Integer> ids) {
        resourceApi.enableIds(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/disable")
    public Response disable(List<Integer> ids) {
        resourceApi.disableIds(ids);
        return RestUtils.getResponse(Status.OK);
    }
}
