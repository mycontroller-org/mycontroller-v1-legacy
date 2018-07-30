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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.SystemApi;
import org.mycontroller.standalone.api.jaxrs.model.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_DIRECTION;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/resources/logs")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class ResourcesLogsHandler extends AccessEngine {
    private SystemApi systemApi = new SystemApi();

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(ResourcesLogs.KEY_RESOURCE_TYPE) String resourceType,
            @QueryParam(ResourcesLogs.KEY_RESOURCE_ID) Integer resourceId,
            @QueryParam(ResourcesLogs.KEY_MESSAGE_TYPE) String messageType,
            @QueryParam(ResourcesLogs.KEY_LOG_LEVEL) String logLevel,
            @QueryParam(ResourcesLogs.KEY_MESSAGE) List<String> message,
            @QueryParam(ResourcesLogs.KEY_LOG_DIRECTION) String logDirection,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(ResourcesLogs.KEY_RESOURCE_TYPE, RESOURCE_TYPE.fromString(resourceType));
        filters.put(ResourcesLogs.KEY_RESOURCE_ID, resourceId);
        filters.put(ResourcesLogs.KEY_MESSAGE_TYPE, MESSAGE_TYPE.fromString(messageType));
        filters.put(ResourcesLogs.KEY_LOG_LEVEL, LOG_LEVEL.fromString(logLevel));
        filters.put(ResourcesLogs.KEY_MESSAGE, message);
        filters.put(ResourcesLogs.KEY_LOG_DIRECTION, LOG_DIRECTION.fromString(logDirection));

        //Add allowed resources filter if he is non-admin
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            filters.put(AllowedResources.KEY_ALLOWED_RESOURCES, AuthUtils.getUser(securityContext)
                    .getAllowedResources());
        }

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        return RestUtils.getResponse(Status.OK, systemApi.getResourcesLogsAll(filters));
    }

    @RolesAllowed({ "Admin" })
    @PUT
    @Path("/")
    public Response purge(ResourcesLogs resourcesLogs) {
        systemApi.purgeResourcesLogs(resourcesLogs);
        return RestUtils.getResponse(Status.OK);
    }

    @RolesAllowed({ "Admin" })
    @POST
    @Path("/delete")
    public Response purge(List<Integer> ids) {
        systemApi.purgeResourcesLogs(ids);
        return RestUtils.getResponse(Status.OK);
    }

}
