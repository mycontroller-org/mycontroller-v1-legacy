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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.VariableApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.settings.Variable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/variables")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "Admin" })
public class VariablesHandler {

    VariableApi variableApi = new VariableApi();

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(Variable.SKEY_KEY) List<String> keys,
            @QueryParam(Variable.SKEY_VALUE) List<String> values,
            @QueryParam(Variable.SKEY_VALUE2) List<String> values2,
            @QueryParam(Variable.SKEY_VALUE3) List<String> values3,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put(Variable.SKEY_KEY, keys);
        filters.put(Variable.SKEY_VALUE, values);
        filters.put(Variable.SKEY_VALUE2, values2);
        filters.put(Variable.SKEY_VALUE3, values3);

        if (orderBy == null) {
            orderBy = Variable.SKEY_KEY;
        }
        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);
        try {
            return RestUtils.getResponse(Status.OK, variableApi.getAll(filters));
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<Integer> ids) {
        variableApi.delete(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @GET
    @Path("/get")
    public Response get(
            @QueryParam("key") String key,
            @QueryParam("id") Integer id) {
        if (id != null) {
            return RestUtils.getResponse(Status.OK, variableApi.get(id));
        } else if (key != null) {
            return RestUtils.getResponse(Status.OK, variableApi.get(key));
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Required field is missing!"));
        }
    }

    @PUT
    @POST
    @Path("/")
    public Response upload(Variable variable) {
        variableApi.update(variable);
        return RestUtils.getResponse(Status.OK);
    }

}