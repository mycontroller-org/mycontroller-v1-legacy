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

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.TypesIdNameMapper;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.settings.MetricsGraphSettings;
import org.mycontroller.standalone.utils.TypesUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/types")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class TypesHandler extends AccessEngine {
    @GET
    @Path("/gatewayTypes")
    public Response getGatewayTypes(@QueryParam("networkType") String networkType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getGatewayTypes(networkType));
    }

    @GET
    @Path("/gatewayNetworkTypes")
    public Response getGatewaySubTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getGatewayNetworkTypes());
    }

    @GET
    @Path("/externalServerTypes")
    public Response getExternalServerTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getExternalServerTypes());
    }

    @GET
    @Path("/metricEngineTypes")
    public Response getMetricEngineTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getMetricEngineTypes());
    }

    @GET
    @Path("/gatewaySerialDrivers")
    public Response getGatewaySerialDrivers() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getGatewaySerialDrivers());
    }

    @GET
    @Path("/nodeTypes")
    public Response getNodeTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getNodeTypes());
    }

    @GET
    @Path("/nodeRegistrationStatuses")
    public Response getNodeRegistrationStatuses() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getNodeRegistrationStatuses());
    }

    @GET
    @Path("/resourceTypes")
    public Response getResourceTypes(
            @QueryParam("resourceType") String resourceType,
            @QueryParam("operationType") String operationType,
            @QueryParam("conditionType") String conditionType) {
        return RestUtils.getResponse(Status.OK,
                TypesUtils.getResourceTypes(AuthUtils.getUser(securityContext), resourceType, operationType,
                        conditionType));
    }

    @GET
    @Path("/resources")
    public Response getResources(@QueryParam("resourceType") String resourceType,
            @QueryParam("filter") String filter,
            @QueryParam(Query.PAGE_LIMIT) @DefaultValue(RestUtils.DROP_DOWN_ITEM_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) @DefaultValue("1") Long page) {
        return RestUtils.getResponse(Status.OK,
                TypesUtils.getResources(AuthUtils.getUser(securityContext), resourceType, filter, page, pageLimit));
    }

    @GET
    @Path("/gateways")
    public Response getGateways(@QueryParam("filter") String filter,
            @QueryParam(Query.PAGE_LIMIT) @DefaultValue(RestUtils.DROP_DOWN_ITEM_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) @DefaultValue("1") Long page) {
        return RestUtils.getResponse(Status.OK,
                TypesUtils.getGateways(AuthUtils.getUser(securityContext), filter, page, pageLimit));
    }

    @GET
    @Path("/nodes")
    public Response getNodes(@QueryParam("gatewayId") Integer gatewayId, @QueryParam("filter") String filter,
            @QueryParam(Query.PAGE_LIMIT) @DefaultValue(RestUtils.DROP_DOWN_ITEM_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) @DefaultValue("1") Long page) {
        return RestUtils.getResponse(Status.OK,
                TypesUtils.getNodes(AuthUtils.getUser(securityContext), gatewayId, filter, page, pageLimit));
    }

    @GET
    @Path("/externalServers")
    public Response getExternalServers() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getExternalServers(AuthUtils.getUser(securityContext)));
    }

    @GET
    @Path("/trustHostTypes")
    public Response getTrustHostTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTrustHostTypes());
    }

    @GET
    @Path("/sensors")
    public Response getSensors(
            @QueryParam("nodeId") Integer nodeId,
            @QueryParam("roomId") Integer roomId,
            @QueryParam("enableNoRoomFilter") Boolean enableNoRoomFilter,
            @QueryParam("filter") String filter,
            @QueryParam(Query.PAGE_LIMIT) @DefaultValue(RestUtils.DROP_DOWN_ITEM_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) @DefaultValue("1") Long page) {
        return RestUtils.getResponse(Status.OK,
                TypesUtils.getSensors(getUser(), nodeId, roomId, enableNoRoomFilter, filter, page, pageLimit));
    }

    @GET
    @Path("/sensorVariables")
    public Response getSensorVaribles(
            @QueryParam("sensorId") Integer sensorId,
            @QueryParam("sensorVariableId") Integer sensorVariableId,
            @QueryParam("variableType") List<String> variableTypes,
            @QueryParam("metricType") List<String> metricTypes,
            @QueryParam("filter") String filter,
            @QueryParam(Query.PAGE_LIMIT) @DefaultValue(RestUtils.DROP_DOWN_ITEM_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) @DefaultValue("1") Long page) {
        try {
            return RestUtils.getResponse(Status.OK,
                    TypesUtils.getSensorVariables(AuthUtils.getUser(securityContext), sensorId, sensorVariableId,
                            variableTypes, metricTypes, filter, page, pageLimit));
        } catch (IllegalAccessException ex) {
            return RestUtils.getResponse(Status.FORBIDDEN, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/resourcesGroups")
    public Response getResourcesGroups() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getResourcesGroups());
    }

    @GET
    @Path("/timers")
    public Response getTimers() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimers(AuthUtils.getUser(securityContext)));
    }

    @GET
    @Path("/forwardPayloads")
    public Response getForwardPayloads() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getForwardPayloads(AuthUtils.getUser(securityContext)));
    }

    @GET
    @Path("/ruleDefinitions")
    public Response getRuleDefinitions() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getRuleDefinitions(AuthUtils.getUser(securityContext)));
    }

    @GET
    @Path("/firmwares")
    public Response getFirmwares() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getFirmwares());
    }

    @GET
    @Path("/firmwareTypes")
    public Response getFirmwareTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getFirmwareTypes());
    }

    @GET
    @Path("/firmwareVersions")
    public Response getFirmwareVersions() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getFirmwareVersions());
    }

    @GET
    @Path("/sensorTypes")
    public Response getSensorTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getSensorTypes());
    }

    @GET
    @Path("/metricTypes")
    public Response getMetricTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getMetricTypes());
    }

    @GET
    @Path("/unitTypes")
    public Response getUnitTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getUnitTypes());
    }

    @GET
    @Path("/sensorVariableTypes")
    public Response getSensorVariableTypes(@QueryParam("sensorType") String sensorType,
            @QueryParam("sensorId") Integer sensorId,
            @QueryParam("metricType") List<String> metricTypes) {
        return RestUtils.getResponse(Status.OK,
                TypesUtils.getSensorVariableTypes(MESSAGE_TYPE_PRESENTATION.fromString(sensorType), sensorId,
                        metricTypes));
    }

    @GET
    @Path("/ruleOperatorTypes")
    public Response getRuleOperatorTypes(@QueryParam("conditionType") String conditionType,
            @QueryParam("resourceType") String resourceType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getRuleOperatorTypes(conditionType, resourceType));
    }

    @GET
    @Path("/ruleThresholdDataTypes")
    public Response getAlarmThresholdTypes(@QueryParam("resourceType") String resourceType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getThresholdDataTypes(resourceType));
    }

    @GET
    @Path("/operationTypes")
    public Response getOperationTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getOperationTypes());
    }

    @GET
    @Path("/ruleDampeningTypes")
    public Response getRuleDampeningTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getRuleDampeningTypes());
    }

    @GET
    @Path("/ruleConditionTypes")
    public Response getRuleConditionTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getRuleConditionTypes());
    }

    @GET
    @Path("/payloadOperations")
    public Response getPayloadOperations(@QueryParam("resourceType") String resourceType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getPayloadOperations(resourceType));
    }

    @GET
    @Path("/stateTypes")
    public Response getStateTypes(@QueryParam("resourceType") String resourceType) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getStateTypes(resourceType));
    }

    //Timer stuff

    @GET
    @Path("/timerTypes")
    public Response getTimerTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimerTypes());
    }

    @GET
    @Path("/timerFrequencyTypes")
    public Response getTimerFrequencies() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimerFrequencyTypes());
    }

    @GET
    @Path("/timerWeekDays")
    public Response getTimerDays(@QueryParam("allDays") Boolean allDays) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimerWeekDays(allDays != null ? allDays : false));
    }

    @RolesAllowed({ "admin" })
    @GET
    @Path("/sensorVariableMapper")
    public Response getSensorVariableMapper() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getVariableMapperList());
    }

    @RolesAllowed({ "admin" })
    @GET
    @Path("/sensorVariableMapperByType")
    public Response getSensorVariableTypesAll(@QueryParam("sensorType") String sensorType) {
        return RestUtils.getResponse(Status.OK,
                TypesUtils.getSensorVariableMapperByType(MESSAGE_TYPE_PRESENTATION.fromString(sensorType)));
    }

    @RolesAllowed({ "admin" })
    @PUT
    @Path("/sensorVariableMapper")
    public Response updateSensorVariableMapper(TypesIdNameMapper idNameMapper) {
        TypesUtils.updateVariableMap(idNameMapper);
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/languages")
    public Response getLanguages() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getLanguages());
    }

    @GET
    @Path("/hvacOptionsFlowState")
    public Response getHvacOptionsFlowState() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getHvacOptionsFlowState());
    }

    @GET
    @Path("/hvacOptionsFlowMode")
    public Response getHvacOptionsFlowMode() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getHvacOptionsFlowMode());
    }

    @GET
    @Path("/hvacOptionsFanSpeed")
    public Response getHvacOptionsFanSpeed() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getHvacOptionsFanSpeed());
    }

    //Security
    @GET
    @Path("/rolePermissions")
    public Response getRolePermissions() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getRolePermissions());
    }

    //Resource logs
    @GET
    @Path("/resourceLogsMessageTypes")
    public Response getResourceLogsMessageTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getResourceLogsMessageTypes());
    }

    @GET
    @Path("/resourceLogsLogDirections")
    public Response getResourceLogsLogDirections() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getResourceLogsLogDirections());
    }

    @GET
    @Path("/resourceLogsLogLevels")
    public Response getResourceLogsLogLevels() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getResourceLogsLogLevels());
    }

    @GET
    @Path("/metricsSettings")
    public Response getMetricsSettings() {
        MetricsGraphSettings metricsGraphSettings = MetricsGraphSettings.get();
        metricsGraphSettings.setMetrics(null);
        return RestUtils.getResponse(Status.OK, metricsGraphSettings);
    }

    //Notifications
    @GET
    @Path("/operations")
    public Response getOperations() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getOperations());
    }

    @GET
    @Path("/rooms")
    public Response getRooms(@QueryParam("selfId") Integer selfId) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getRooms(selfId));
    }

    @GET
    @Path("/messageTypes")
    public Response getMessageTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getMessageTypes());
    }

    @GET
    @Path("/messageSubTypes")
    public Response getMessageSubTypes(@QueryParam("messageType") String typeString) {
        MESSAGE_TYPE type = MESSAGE_TYPE.fromString(typeString);
        if (type == null) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Invalid 'type'!"));
        }
        return RestUtils.getResponse(Status.OK, TypesUtils.getMessageSubTypes(type));
    }

    //----------------- review required

    @GET
    @Path("/configUnitTypes")
    public Response getMysConfigTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getConfigUnitTypes());
    }

    //TODO: after this change required

    @GET
    @Path("/graphInterpolate")
    public Response getGraphInterpolateTypes() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getGraphInterpolateTypes());
    }

    @GET
    @Path("/graphSensorVariableTypes/{sensorRefId}")
    public Response getGraphSensorVariableTypes(@PathParam("sensorRefId") int sensorRefId) {
        return RestUtils.getResponse(Status.OK, TypesUtils.getGraphSensorVariableTypes(sensorRefId));
    }

    @GET
    @Path("/timeFormats")
    public Response getTime12h24hformats() {
        return RestUtils.getResponse(Status.OK, TypesUtils.getTimeFormats());
    }

}
