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
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/timers")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class TimerHandler {
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getTimerDao().get(id));
    }

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(Timer.KEY_NAME) List<String> name,
            @QueryParam(Timer.KEY_RESOURCE_TYPE) String resourceType,
            @QueryParam(Timer.KEY_RESOURCE_ID) Integer resourceId,
            @QueryParam(Timer.KEY_TIMER_TYPE) String timerType,
            @QueryParam(Timer.KEY_FREQUENCY) String frequency,
            @QueryParam(Timer.KEY_ENABLED) Boolean enabled,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Timer.KEY_NAME, name);
        filters.put(Timer.KEY_RESOURCE_TYPE, RESOURCE_TYPE.fromString(resourceType));
        filters.put(Timer.KEY_RESOURCE_ID, resourceId);
        filters.put(Timer.KEY_TIMER_TYPE, TIMER_TYPE.fromString(timerType));
        filters.put(Timer.KEY_FREQUENCY, FREQUENCY_TYPE.fromString(frequency));
        filters.put(Timer.KEY_ENABLED, enabled);

        QueryResponse queryResponse = DaoUtils.getTimerDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : Timer.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @PUT
    @Path("/")
    public Response update(Timer timer) {
        TimerUtils.updateTimer(timer);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/")
    public Response add(Timer timer) {
        TimerUtils.addTimer(timer);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/delete")
    public Response delete(List<Integer> ids) {
        TimerUtils.deleteTimers(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/enable")
    public Response enable(List<Integer> ids) {
        TimerUtils.enableTimers(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/disable")
    public Response disable(List<Integer> ids) {
        TimerUtils.disableTimers(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }
}
