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

import org.mycontroller.standalone.api.RoomApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.model.RoomJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/rooms")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "Admin" })
public class RoomHandler extends AccessEngine {

    RoomApi roomApi = new RoomApi();

    @RolesAllowed({ "User" })
    @GET
    @Path("/")
    public Response getAllRooms(
            @QueryParam(Room.KEY_NAME) List<String> name,
            @QueryParam(Room.KEY_DESCRIPTION) List<String> description,
            @QueryParam(Room.KEY_PARENT_ID) Integer parentId,
            @QueryParam("isSimpleQuery") Boolean isSimpleQuery,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {

        if (isSimpleQuery != null && isSimpleQuery) {
            return RestUtils.getResponse(Status.OK,
                    QueryResponse.builder().data(DaoUtils.getRoomDao().getByParentId(parentId)).build());
        } else {
            HashMap<String, Object> filters = new HashMap<String, Object>();

            filters.put(Room.KEY_NAME, name);
            filters.put(Room.KEY_DESCRIPTION, description);
            filters.put(Room.KEY_PARENT_ID, parentId);

            //Query primary filters
            filters.put(Query.ORDER, order);
            filters.put(Query.ORDER_BY, orderBy);
            filters.put(Query.PAGE_LIMIT, pageLimit);
            filters.put(Query.PAGE, page);

            return RestUtils.getResponse(Status.OK, roomApi.getAll(filters));
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id) {
        return RestUtils.getResponse(Status.OK, new RoomJson(roomApi.get(id)).mapResources());
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<Integer> ids) {
        roomApi.delete(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @PUT
    @Path("/")
    public Response update(RoomJson roomJson) {
        return add(roomJson);
    }

    @POST
    @Path("/")
    public Response add(RoomJson roomJson) {
        try {
            roomApi.createOrUpdate(roomJson.getRoom(), roomJson.getSensorIds());
        } catch (McDuplicateException | McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
        return RestUtils.getResponse(Status.OK);
    }

}
