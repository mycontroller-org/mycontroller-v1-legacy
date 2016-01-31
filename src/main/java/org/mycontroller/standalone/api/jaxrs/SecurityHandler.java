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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.spi.HttpRequest;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.mapper.RoleJson;
import org.mycontroller.standalone.api.jaxrs.mapper.TypesIdNameMapper;
import org.mycontroller.standalone.api.jaxrs.mapper.UserJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.api.jaxrs.utils.UserMapper;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.auth.AuthUtils.PERMISSION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/security")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class SecurityHandler {

    @Context
    SecurityContext securityContext;

    @GET
    @Path("/roles")
    public Response getRoles(
            @QueryParam("onlyRolename") Boolean onlyRolename,
            @QueryParam(Role.KEY_NAME) List<String> name,
            @QueryParam(Role.KEY_DESCRIPTION) List<String> description,
            @QueryParam(Role.KEY_PERMISSION) String permission,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        if (onlyRolename != null && onlyRolename) {
            List<Role> roles = DaoUtils.getRoleDao().getAll();
            ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
            for (Role role : roles) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(role.getId()).displayName(role.getName())
                        .build());
            }
            return RestUtils.getResponse(Status.OK, typesIdNameMappers);
        } else {
            HashMap<String, Object> filters = new HashMap<String, Object>();

            filters.put(Role.KEY_NAME, name);
            filters.put(Role.KEY_DESCRIPTION, description);
            filters.put(Role.KEY_PERMISSION, PERMISSION_TYPE.fromString(permission));

            QueryResponse queryResponse = DaoUtils.getRoleDao().getAll(
                    Query.builder()
                            .order(order != null ? order : Query.ORDER_ASC)
                            .orderBy(orderBy != null ? orderBy : Node.KEY_ID)
                            .filters(filters)
                            .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                            .page(page != null ? page : 1l)
                            .build());
            return RestUtils.getResponse(Status.OK, queryResponse);
        }
    }

    @GET
    @Path("/roles/{id}")
    public Response getRole(@PathParam("id") Integer id) {
        Role role = DaoUtils.getRoleDao().getById(id);
        RoleJson roleJson = new RoleJson();
        roleJson.mapResources(role);
        return RestUtils.getResponse(Status.OK, roleJson);
    }

    @POST
    @Path("/roles")
    public Response addRole(RoleJson roleJson) {
        roleJson.createOrUpdateRole();
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/roles")
    public Response updateRole(RoleJson roleJson) {
        roleJson.createOrUpdateRole();
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/roles/delete")
    public Response deleteRoles(List<Integer> roleIds) {
        new RoleJson().deleteRoles(roleIds);
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/users")
    public Response getUsers(
            @QueryParam("onlyUsername") Boolean onlyUsername,
            @QueryParam(User.KEY_USER_NAME) List<String> username,
            @QueryParam(User.KEY_FULL_NAME) List<String> fullName,
            @QueryParam(User.KEY_ENABLED) Boolean enabled,
            @QueryParam(User.KEY_EMAIL) List<String> email,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {

        //TODO: do not show logged in user

        if (onlyUsername != null && onlyUsername) {
            List<User> users = DaoUtils.getUserDao().getAll();
            ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
            for (User user : users) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(user.getId()).displayName(user.getUsername())
                        .build());
            }
            return RestUtils.getResponse(Status.OK, typesIdNameMappers);
        } else {
            HashMap<String, Object> filters = new HashMap<String, Object>();

            filters.put(User.KEY_USER_NAME, username);
            filters.put(User.KEY_FULL_NAME, fullName);
            filters.put(User.KEY_ENABLED, enabled);
            filters.put(User.KEY_EMAIL, email);

            QueryResponse queryResponse = DaoUtils.getUserDao().getAll(
                    Query.builder()
                            .order(order != null ? order : Query.ORDER_ASC)
                            .orderBy(orderBy != null ? orderBy : Node.KEY_ID)
                            .filters(filters)
                            .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                            .page(page != null ? page : 1l)
                            .build());
            return RestUtils.getResponse(Status.OK, queryResponse);
        }

    }

    @GET
    @Path("/users/{id}")
    public Response getUser(@PathParam("id") Integer id) {
        User user = DaoUtils.getUserDao().getById(id);
        UserJson userJson = new UserJson();
        userJson.mapResources(user);
        return RestUtils.getResponse(Status.OK, userJson);
    }

    @POST
    @Path("/users")
    public Response addUser(UserJson userJson) {
        userJson.createOrUpdateUser();
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/users")
    public Response updateUser(UserJson userJson) {
        userJson.createOrUpdateUser();
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/users/delete")
    public Response deleteUsers(List<Integer> userIds) {
        //Remove logged in user from delete list
        boolean removed = userIds.remove(((User) securityContext.getUserPrincipal()).getId());
        new UserJson().deleteUsers(userIds);
        if (removed) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Self deletion not allowed!"));
        }
        return RestUtils.getResponse(Status.OK);
    }

    @RolesAllowed({ "User", "MQTT user" })
    @PUT
    @Path("/profile")
    public Response updateProfile(UserJson userJson) {
        try {
            //Via profile do not allow to change username
            userJson.getUser().setUsername(AuthUtils.getUser(securityContext).getUsername());
            //He/she can update only his/her profile, not others
            userJson.getUser().setId(AuthUtils.getUser(securityContext).getId());
            userJson.updateProfile();
            return RestUtils.getResponse(Status.OK);
        } catch (IllegalAccessError ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @RolesAllowed({ "User", "MQTT user" })
    @GET
    @Path("/profile")
    public Response getProfile() {
        User user = DaoUtils.getUserDao().getById(AuthUtils.getUser(securityContext).getId());
        UserJson userJson = new UserJson();
        userJson.mapResources(user);
        return RestUtils.getResponse(Status.OK, userJson);
    }

    //review required

    @Context
    HttpRequest request;

    @GET
    @Path("/{userId}")
    public Response getUser(@PathParam("userId") int userId) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getUserDao().getById(userId));
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
        DaoUtils.getUserDao().deleteById(userId);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(User user) {
        User userOrg = RestUtils.getUser(request);
        if (userOrg.getId() == user.getId()) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("You cannot change your role"));
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
