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
import org.mycontroller.standalone.api.RuleApi;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.rule.model.RuleDefinition;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/rules")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "Admin" })
public class RuleHandler extends AccessEngine {

    private static RuleApi ruleApi = new RuleApi();

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, ruleApi.get(id));
    }

    @GET
    @Path("/")
    public Response getAll(
            @QueryParam(RuleDefinitionTable.KEY_NAME) List<String> name,
            @QueryParam(RuleDefinitionTable.KEY_RESOURCE_TYPE) String resourceType,
            @QueryParam(RuleDefinitionTable.KEY_CONDITION_TYPE) String conditionType,
            @QueryParam(RuleDefinitionTable.KEY_DAMPENING_TYPE) String dampeningType,
            @QueryParam(RuleDefinitionTable.KEY_TRIGGERED) Boolean triggered,
            @QueryParam(RuleDefinitionTable.KEY_ENABLED) Boolean enabled,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(RuleDefinitionTable.KEY_NAME, name);
        filters.put(RuleDefinitionTable.KEY_TRIGGERED, triggered);
        filters.put(RuleDefinitionTable.KEY_ENABLED, enabled);
        filters.put(RuleDefinitionTable.KEY_RESOURCE_TYPE, RESOURCE_TYPE.fromString(resourceType));
        filters.put(RuleDefinitionTable.KEY_CONDITION_TYPE, CONDITION_TYPE.fromString(conditionType));
        filters.put(RuleDefinitionTable.KEY_DAMPENING_TYPE, DAMPENING_TYPE.fromString(dampeningType));

        QueryResponse queryResponse = ruleApi.getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : RuleDefinitionTable.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1L)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @POST
    @Path("/")
    public Response add(RuleDefinition ruleDefinition) {
        ruleApi.add(ruleDefinition);
        return RestUtils.getResponse(Status.CREATED);
    }

    @PUT
    @Path("/")
    public Response update(RuleDefinition ruleDefinition) {
        ruleApi.update(ruleDefinition);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<Integer> ids) {
        ruleApi.deleteIds(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/enable")
    public Response enableIds(List<Integer> ids) {
        ruleApi.enableIds(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/disable")
    public Response disableIds(List<Integer> ids) {
        ruleApi.disableIds(ids);
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

}
