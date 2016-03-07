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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.api.jaxrs.mapper.AlarmDefinitionJson;
import org.mycontroller.standalone.api.jaxrs.mapper.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/alarms")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class AlarmHandler extends AccessEngine {

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        hasAccessAlarmDefinition(id);
        AlarmDefinition alarmDefinition = DaoUtils.getAlarmDefinitionDao().getById(id);
        return RestUtils.getResponse(Status.OK, new AlarmDefinitionJson(alarmDefinition).mapResources());
    }

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(AlarmDefinition.KEY_NAME) List<String> name,
            @QueryParam(AlarmDefinition.KEY_RESOURCE_TYPE) String resourceType,
            @QueryParam(AlarmDefinition.KEY_RESOURCE_ID) Integer resourceId,
            @QueryParam(AlarmDefinition.KEY_TRIGGERED) Boolean triggered,
            @QueryParam(AlarmDefinition.KEY_ENABLED) Boolean enabled,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(AlarmDefinition.KEY_NAME, name);
        filters.put(AlarmDefinition.KEY_RESOURCE_TYPE, RESOURCE_TYPE.fromString(resourceType));
        filters.put(AlarmDefinition.KEY_RESOURCE_ID, resourceId);
        filters.put(AlarmDefinition.KEY_TRIGGERED, triggered);
        filters.put(AlarmDefinition.KEY_ENABLED, enabled);

        //Add allowed resources filter if he is non-admin
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            filters.put(AllowedResources.KEY_ALLOWED_RESOURCES, AuthUtils.getUser(securityContext)
                    .getAllowedResources());
        }

        QueryResponse queryResponse = DaoUtils.getAlarmDefinitionDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : AlarmDefinition.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @POST
    @Path("/")
    public Response add(AlarmDefinitionJson alarmDefinitionJson) {
        hasAccessAlarmDefinition(alarmDefinitionJson.getAlarmDefinition());
        //TODO: has access for notifications
        alarmDefinitionJson.createOrUpdate();
        return RestUtils.getResponse(Status.CREATED);
    }

    @PUT
    @Path("/")
    public Response update(AlarmDefinitionJson alarmDefinitionJson) {
        hasAccessAlarmDefinition(alarmDefinitionJson.getAlarmDefinition());
        //TODO: has access for notifications
        alarmDefinitionJson.createOrUpdate();
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<Integer> ids) {
        updateAlarmDefinitionIds(ids);
        new AlarmDefinitionJson().delete(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/enable")
    public Response enableIds(List<Integer> ids) {
        updateAlarmDefinitionIds(ids);
        AlarmUtils.enableAlarmDefinitions(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/disable")
    public Response disableIds(List<Integer> ids) {
        updateAlarmDefinitionIds(ids);
        AlarmUtils.disableAlarmDefinitions(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

}
