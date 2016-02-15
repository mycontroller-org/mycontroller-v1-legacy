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
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.alarm.AlarmUtils.NOTIFICATION_TYPE;
import org.mycontroller.standalone.alarm.NotificationSendPayLoad;
import org.mycontroller.standalone.api.jaxrs.mapper.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/alarms")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class AlarmHandler {
    private static final Logger _logger = LoggerFactory.getLogger(AlarmHandler.class);

    @Context
    SecurityContext securityContext;

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        hasAccess(id);
        return RestUtils.getResponse(Status.OK, DaoUtils.getAlarmDefinitionDao().getById(id));
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
    public Response add(AlarmDefinition alarmDefinition) {
        hasAccess(alarmDefinition);
        AlarmUtils.addAlarmDefinition(alarmDefinition);
        return RestUtils.getResponse(Status.CREATED);
    }

    @PUT
    @Path("/")
    public Response update(AlarmDefinition alarmDefinition) {
        hasAccess(alarmDefinition);
        AlarmUtils.updateAlarmDefinition(alarmDefinition);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<Integer> ids) {
        updateIds(ids);
        AlarmUtils.deleteAlarmDefinitionIds(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/enable")
    public Response enableIds(List<Integer> ids) {
        updateIds(ids);
        AlarmUtils.enableAlarmDefinitions(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/disable")
    public Response disableIds(List<Integer> ids) {
        updateIds(ids);
        AlarmUtils.disableAlarmDefinitions(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    private void updateIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll(ids);
            for (AlarmDefinition alarmDefinition : alarmDefinitions) {
                if (!AuthUtils.hasAccess(securityContext, alarmDefinition.getResourceType(),
                        alarmDefinition.getResourceId())) {
                    ids.remove(alarmDefinition.getId());
                }
            }
        }
    }

    private void hasAccess(AlarmDefinition alarmDefinition) {
        _logger.debug("Alarm definition:{}", alarmDefinition);
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.hasAccess(securityContext, alarmDefinition.getResourceType(),
                    alarmDefinition.getResourceId())) {
                throw new ForbiddenException("You do not have access to add this resource!");
            }
            if (alarmDefinition.getNotificationType() == NOTIFICATION_TYPE.SEND_PAYLOAD) {
                NotificationSendPayLoad sendPayLoad = new NotificationSendPayLoad(alarmDefinition);
                if (!AuthUtils.hasAccess(securityContext, sendPayLoad.getResourceType(), sendPayLoad.getResourceId())) {
                    throw new ForbiddenException("You do not have access to add this resource!");
                }
            }
        }
    }

    private void hasAccess(Integer id) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            AlarmDefinition alarmDefinition = DaoUtils.getAlarmDefinitionDao().getById(id);
            if (!AuthUtils.hasAccess(securityContext, alarmDefinition.getResourceType(),
                    alarmDefinition.getResourceId())) {
                throw new ForbiddenException("You do not have access for this resource!");
            }
        }
    }

}
