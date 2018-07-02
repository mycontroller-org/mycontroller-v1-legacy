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

import org.mycontroller.standalone.api.TimerApi;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/timers")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "Admin" })
public class TimerHandler extends AccessEngine {

    private TimerApi timerApi = new TimerApi();

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        //hasAccessTimer(id);
        return RestUtils.getResponse(Status.OK, timerApi.get(id));
    }

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(Timer.KEY_NAME) List<String> name,
            @QueryParam(Timer.KEY_TIMER_TYPE) String timerType,
            @QueryParam(Timer.KEY_FREQUENCY) String frequency,
            @QueryParam(Timer.KEY_ENABLED) Boolean enabled,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Timer.KEY_NAME, name);
        filters.put(Timer.KEY_TIMER_TYPE, TIMER_TYPE.fromString(timerType));
        filters.put(Timer.KEY_FREQUENCY, FREQUENCY_TYPE.fromString(frequency));
        filters.put(Timer.KEY_ENABLED, enabled);

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        return RestUtils.getResponse(Status.OK, timerApi.getAll(filters));
    }

    @PUT
    @Path("/")
    public Response update(Timer timer) {
        timerApi.update(timer);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/")
    public Response add(Timer timer) {
        timerApi.add(timer);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/delete")
    public Response delete(List<Integer> ids) {
        timerApi.delete(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/enable")
    public Response enable(List<Integer> ids) {
        timerApi.enable(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/disable")
    public Response disable(List<Integer> ids) {
        timerApi.disable(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

}
