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

import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.notification.NotificationUtils;
import org.mycontroller.standalone.notification.NotificationUtils.NOTIFICATION_TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/notifications")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class NotificationHandler extends AccessEngine {

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        //TODO: hasAccessNotification(id);
        return RestUtils.getResponse(Status.OK, DaoUtils.getNotificationDao().getById(id));
    }

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(Notification.KEY_NAME) List<String> name,
            @QueryParam(Notification.KEY_PUBLIC_ACCESS) Boolean publicAccess,
            @QueryParam(Notification.KEY_TYPE) String type,
            @QueryParam(Notification.KEY_ENABLED) Boolean enabled,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Notification.KEY_NAME, name);
        filters.put(Notification.KEY_TYPE, NOTIFICATION_TYPE.fromString(type));
        filters.put(Notification.KEY_PUBLIC_ACCESS, publicAccess);
        filters.put(Notification.KEY_ENABLED, enabled);

        //Add allowed resources filter if he is non-admin
        /*//TODO: verify notifications access
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            filters.put(AllowedResources.KEY_ALLOWED_RESOURCES, AuthUtils.getUser(securityContext)
                    .getAllowedResources());
        }
        */
        QueryResponse queryResponse = DaoUtils.getNotificationDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : Notification.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @POST
    @Path("/")
    public Response add(Notification notification) {
        //TODO: has access for notifications
        notification.setUser(getUser());
        DaoUtils.getNotificationDao().create(notification);
        return RestUtils.getResponse(Status.CREATED);
    }

    @PUT
    @Path("/")
    public Response update(Notification notification) {
        //TODO: has access for notifications
        notification.setUser(getUser());
        if (!notification.getEnabled()) {
            NotificationUtils.unloadNotificationTimerJobs(notification);
        }
        DaoUtils.getNotificationDao().update(notification);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<Integer> ids) {
        //TODO: has access for notifications
        NotificationUtils.unloadNotificationTimerJobs(ids);
        DaoUtils.getNotificationDao().deleteByIds(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/enable")
    public Response enableIds(List<Integer> ids) {
        //TODO: has access for notifications
        for (Notification notification : DaoUtils.getNotificationDao().getAll(ids)) {
            notification.setEnabled(true);
            DaoUtils.getNotificationDao().update(notification);
        }
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/disable")
    public Response disableIds(List<Integer> ids) {
        //TODO: has access for notifications
        NotificationUtils.unloadNotificationTimerJobs(ids);
        for (Notification notification : DaoUtils.getNotificationDao().getAll(ids)) {
            notification.setEnabled(false);
            DaoUtils.getNotificationDao().update(notification);
        }
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

}
