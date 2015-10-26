/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.HttpRequest;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.api.jaxrs.utils.UserMapper;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/users")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class UserHandler {
    //private static final Logger _logger = LoggerFactory.getLogger(UserHandler.class);

    @Context
    HttpRequest request;

    @GET
    @Path("/{userId}")
    public Response getUser(@PathParam("userId") int userId) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getUserDao().get(userId));
    }

    @GET
    @Path("/")
    public Response getAll() {
        return RestUtils.getResponse(Status.OK, DaoUtils.getUserDao().getAll());
    }

    @DELETE
    @Path("/{userId}")
    public Response delete(@PathParam("userId") int userId) {
        User user = RestUtils.getUser(request);
        if (user.getId() == userId) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("You cannot delete yourself"));
        }
        UserMapper.removeUser(user.getName());
        DaoUtils.getUserDao().delete(userId);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(User user) {
        User userOrg = RestUtils.getUser(request);
        if (user.getId() == user.getId()) {
            if (userOrg.getRoleId() != user.getRoleId()) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("You cannot change your role"));
            }
        }
        DaoUtils.getUserDao().update(user);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/")
    public Response add(User user) {
        DaoUtils.getUserDao().create(user);
        return RestUtils.getResponse(Status.CREATED);
    }
}
