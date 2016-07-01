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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.MC_LOCALE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.MetricApi;
import org.mycontroller.standalone.api.jaxrs.json.ApiError;
import org.mycontroller.standalone.api.jaxrs.json.LocaleString;
import org.mycontroller.standalone.api.jaxrs.json.MetricsBulletChartNVD3;
import org.mycontroller.standalone.api.jaxrs.json.MetricsChartDataGroupNVD3;
import org.mycontroller.standalone.api.jaxrs.json.MetricsChartDataNVD3;
import org.mycontroller.standalone.api.jaxrs.json.MetricsChartDataXY;
import org.mycontroller.standalone.api.jaxrs.json.TopologyItem;
import org.mycontroller.standalone.api.jaxrs.json.TopologyKinds;
import org.mycontroller.standalone.api.jaxrs.json.TopologyRelation;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsCounterTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.MetricDouble;
import org.mycontroller.standalone.metrics.MetricsCsvEngine;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.scripts.McScriptException;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsGraph.CHART_TYPE;
import org.mycontroller.standalone.units.UnitUtils;
import org.mycontroller.standalone.units.UnitUtils.UNIT_TYPE;
import org.mycontroller.standalone.utils.McUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/metrics")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class MetricsHandler extends AccessEngine {
    public static final String COLOR_MINIMUM = "#2ca02c";
    public static final String COLOR_MAXIMUM = "#802b00";

    public static final String TOPOLOGY_PREFIX_GATEWAY = "G-";
    public static final String TOPOLOGY_PREFIX_NODE = "N-";
    public static final String TOPOLOGY_PREFIX_SENSOR = "S-";
    public static final String TOPOLOGY_PREFIX_SENSOR_VARIABLE = "SV-";
    public static final String TOPOLOGY_KIND_GATEWAY = "Gateway";
    public static final String TOPOLOGY_KIND_NODE = "Node";
    public static final String TOPOLOGY_KIND_SENSOR = "Sensor";
    public static final String TOPOLOGY_KIND_SENSOR_VARIABLE = "SensorVariable";

    private MetricApi metricApi = new MetricApi();

    //Get count of resources
    @GET
    @Path("/resourceCount")
    public Response getResourceCount(@QueryParam("resourceType") RESOURCE_TYPE resourceType,
            @QueryParam("resourceId") Integer resourceId) {
        return RestUtils.getResponse(Status.OK, metricApi.getResourceCount(resourceType, resourceId));
    }

    @GET
    @Path("/metricsData")
    public Response getMetricsData(
            @QueryParam("sensorId") Integer sensorId,
            @QueryParam("variableId") List<Integer> variableIds,
            @QueryParam("timestampFrom") Long timestampFrom,
            @QueryParam("timestampTo") Long timestampTo,
            @QueryParam("withMinMax") Boolean withMinMax,
            @QueryParam("chartType") String chartType) {
        if (!variableIds.isEmpty()) {
            updateSensorVariableIds(variableIds);
        } else if (sensorId != null) {
            hasAccessSensor(sensorId);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Either sensor id or sensor variable id(s) is required!"));
        }
        return RestUtils.getResponse(
                Status.OK,
                getMetricsDataJsonNVD3(variableIds, sensorId, timestampFrom, timestampTo,
                        withMinMax != null ? withMinMax : false, chartType));
    }

    @GET
    @Path("/bulletChart")
    public Response getBulletChart(
            @QueryParam("variableId") List<Integer> variableIds,
            @QueryParam("timestampFrom") Long timestampFrom,
            @QueryParam("timestampTo") Long timestampTo) {
        if (!variableIds.isEmpty()) {
            updateSensorVariableIds(variableIds);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Sensor variable id(s) is required!"));
        }
        return RestUtils.getResponse(Status.OK, getMetricsBulletChart(variableIds, timestampFrom, timestampTo));
    }

    @GET
    @Path("/heatMapBatteryLevel")
    public Response getHeatMapBatteryLevel(@QueryParam("nodeId") List<Integer> nodeIds) {
        if (!nodeIds.isEmpty()) {
            return RestUtils.getResponse(Status.OK, metricApi.getHeatMapNodeBatteryLevel(nodeIds));
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Node id(s) is required!"));
        }
    }

    @GET
    @Path("/heatMapNodeStatus")
    public Response getHeatMapNodeStatus(@QueryParam("nodeId") List<Integer> nodeIds) {
        if (!nodeIds.isEmpty()) {
            return RestUtils.getResponse(Status.OK, metricApi.getHeatMapNodeState(nodeIds));
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Node id(s) is required!"));
        }
    }

    @GET
    @Path("/heatMapSensorVariable")
    public Response getHeatMapSensorVariable(@QueryParam("variableId") List<Integer> sVariableIds,
            @QueryParam("upperLimit") Double upperLimit) {
        if (!sVariableIds.isEmpty()) {
            return RestUtils
                    .getResponse(Status.OK, metricApi.getHeatMapSensorVariableDouble(sVariableIds, upperLimit));
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Sensor variable id(s) is required!"));
        }
    }

    @GET
    @Path("/heatMapScript")
    public Response getHeatMapScript(@QueryParam("scriptName") String scriptName) {
        if (scriptName != null) {
            try {
                return RestUtils.getResponse(Status.OK, metricApi.getHeatMapScript(scriptName));
            } catch (McBadRequestException | IllegalAccessException | IOException | McScriptException
                    | ScriptException ex) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
            }
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Script file name is required!"));
        }
    }

    @GET
    @Path("/csvFile")
    public Response getCsvFile(@QueryParam("variableTypeId") int variableTypeId,
            @QueryParam("aggregationType") int aggregationType) {
        try {
            return RestUtils.getResponse(Status.OK,
                    new MetricsCsvEngine().getMetricsCSV(variableTypeId, aggregationType));
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/metricsBattery")
    public Response getMetricsBattery(
            @QueryParam("nodeId") Integer nodeId,
            @QueryParam("timestampFrom") Long timestampFrom,
            @QueryParam("timestampTo") Long timestampTo,
            @QueryParam("withMinMax") Boolean withMinMax) {
        //Access check
        hasAccessNode(nodeId);
        return RestUtils.getResponse(Status.OK,
                getMetricsBatteryJsonNVD3(nodeId, timestampFrom, timestampTo,
                        withMinMax != null ? withMinMax : false));
    }

    @GET
    @Path("/topology")
    public Response getTopology(
            @QueryParam("resourceType") RESOURCE_TYPE resourceType,
            @QueryParam("resourceId") Integer resourceId) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        HashMap<String, TopologyItem> items = new HashMap<String, TopologyItem>();
        List<TopologyRelation> relations = new ArrayList<TopologyRelation>();
        TopologyKinds kinds = TopologyKinds.builder().build();

        data.put("items", items);
        data.put("relations", relations);
        data.put("kinds", kinds);

        if (resourceType != null && resourceId != null) {
            kinds.update(resourceType);
            switch (resourceType) {
                case GATEWAY:
                    updateGatewayTopology(items, relations, resourceId);
                    break;
                case NODE:
                    updateNodeTopology(items, relations, null, resourceId);
                    break;
                case SENSOR:
                    updateSensorTopology(items, relations, null, resourceId);
                    break;
                default:
                    break;
            }
        } else {
            kinds.update(RESOURCE_TYPE.GATEWAY);
            updateGatewayTopology(items, relations, null);
        }

        return RestUtils.getResponse(Status.OK, data);
    }

    private void updateGatewayTopology(HashMap<String, TopologyItem> items,
            List<TopologyRelation> relations,
            Integer gatewayId) {
        List<GatewayTable> gateways = null;
        if (gatewayId != null) {
            gateways = DaoUtils.getGatewayDao().getAll(Collections.singletonList(gatewayId));
        } else {
            gateways = DaoUtils.getGatewayDao().getAll();
        }

        //Update Gateways
        for (GatewayTable gateway : gateways) {
            String source = TOPOLOGY_PREFIX_GATEWAY + gateway.getId();
            items.put(source, TopologyItem.builder()
                    .name(gateway.getName())
                    .id(gateway.getId())
                    .type(RESOURCE_TYPE.GATEWAY)
                    .kind(TOPOLOGY_KIND_GATEWAY)
                    .status(gateway.getState().getText())
                    .build());
            //Update node topology
            updateNodeTopology(items, relations, gateway.getId(), null);
        }
    }

    private void updateNodeTopology(HashMap<String, TopologyItem> items,
            List<TopologyRelation> relations,
            Integer gatewayId, Integer nodeId) {
        List<Node> nodes = null;
        if (gatewayId != null) {
            nodes = DaoUtils.getNodeDao().getAllByGatewayId(gatewayId);
        } else if (nodeId != null) {
            nodes = DaoUtils.getNodeDao().getAll(Collections.singletonList(nodeId));
        } else {
            return;
        }
        for (Node node : nodes) {
            String source = TOPOLOGY_PREFIX_NODE + node.getId();
            String nodeName = node.getName();
            //If node name is null, update node eui as node name
            if (nodeName == null) {
                nodeName = node.getEui();
            }
            items.put(source, TopologyItem.builder()
                    .name(node.getName())
                    .id(node.getId())
                    .type(RESOURCE_TYPE.NODE)
                    .kind(TOPOLOGY_KIND_NODE)
                    .status(node.getState().getText())
                    .build());
            relations.add(TopologyRelation.builder()
                    .source(source)
                    .target(TOPOLOGY_PREFIX_GATEWAY + node.getGatewayTable().getId())
                    .build());

            updateSensorTopology(items, relations, node.getId(), null);
        }
    }

    private void updateSensorTopology(HashMap<String, TopologyItem> items,
            List<TopologyRelation> relations,
            Integer nodeId, Integer sensorId) {
        List<Sensor> sensors = null;
        if (nodeId != null) {
            sensors = DaoUtils.getSensorDao().getAllByNodeId(nodeId);
        } else if (sensorId != null) {
            sensors = DaoUtils.getSensorDao().getAll(Collections.singletonList(sensorId));
        } else {
            return;
        }
        for (Sensor sensor : sensors) {
            String source = TOPOLOGY_PREFIX_SENSOR + sensor.getId();
            LocaleString subType = null;
            if (sensor.getType() != null) {
                subType = LocaleString.builder().en(sensor.getType().getText())
                        .locale(McObjectManager.getMcLocale().getString(sensor.getType().name())).build();
            } else {
                subType = LocaleString.builder().en("Undefined").locale("Undefined").build();
            }
            String sensorName = sensor.getName();
            //If sensor name is null, update with sensor type and sensorId
            if (sensorName == null) {
                if (sensor.getType() != null) {
                    sensorName = McObjectManager.getMcLocale().getString(sensor.getType().name())
                            + "-" + sensor.getSensorId();
                } else {
                    sensorName = sensor.getSensorId();
                }
            }
            items.put(source, TopologyItem.builder()
                    .name(sensorName)
                    .id(sensor.getId())
                    .type(RESOURCE_TYPE.SENSOR)
                    .subType(subType)
                    .kind(TOPOLOGY_KIND_SENSOR)
                    .build());
            relations.add(TopologyRelation.builder()
                    .source(source)
                    .target(TOPOLOGY_PREFIX_NODE + sensor.getNode().getId())
                    .build());
            updateSensorVariableTopology(items, relations, sensor.getId());
        }
    }

    private void updateSensorVariableTopology(HashMap<String, TopologyItem> items,
            List<TopologyRelation> relations,
            int sensorId) {
        List<SensorVariable> sVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId);
        for (SensorVariable sVariable : sVariables) {
            String source = TOPOLOGY_PREFIX_SENSOR_VARIABLE + sVariable.getId();
            items.put(source, TopologyItem.builder()
                    .name(sVariable.getVariableType().getText())
                    .id(sVariable.getId())
                    .type(RESOURCE_TYPE.SENSOR_VARIABLE)
                    .subType(LocaleString.builder()
                            .en(sVariable.getVariableType().getText())
                            .locale(McObjectManager.getMcLocale().getString(
                                    sVariable.getVariableType().name())).build())
                    .displayKind(RESOURCE_TYPE.SENSOR_VARIABLE.getText())
                    .kind(TOPOLOGY_KIND_SENSOR_VARIABLE)
                    .status(SensorUtils.getValue(sVariable))
                    .lastSeen(sVariable.getTimestamp())
                    .build());
            relations.add(TopologyRelation.builder()
                    .source(source)
                    .target(TOPOLOGY_PREFIX_SENSOR + sVariable.getSensor().getId())
                    .build());
        }
    }

    private List<MetricsBulletChartNVD3> getMetricsBulletChart(List<Integer> variableIds,
            Long timestampFrom, Long timestampTo) {
        ArrayList<MetricsBulletChartNVD3> bulletCharts = new ArrayList<MetricsBulletChartNVD3>();
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(variableIds);
        //Update from/to time
        MetricsDoubleTypeDevice queryInput = MetricsDoubleTypeDevice.builder().timestampFrom(timestampFrom)
                .timestampTo(timestampTo).build();
        for (SensorVariable sensorVariable : sensorVariables) {
            //Update sensor variable
            queryInput.setSensorVariable(sensorVariable);

            MetricDouble metric = metricApi.getSensorVariableMetricDouble(sensorVariable, timestampFrom, timestampTo);

            String unit = sensorVariable.getUnitType() != UNIT_TYPE.U_NONE ? " ("
                    + UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit() + ")" : "";

            //If current value not available, do not allow any value
            if (metric.getCurrent() == null || metric.getMinimum() == null) {
                bulletCharts.add(MetricsBulletChartNVD3
                        .builder()
                        .id(sensorVariable.getId())
                        .resourceName(new ResourceModel(
                                RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails() + unit)
                        .displayName(sensorVariable.getSensor().getName() + " >> "
                                + sensorVariable.getVariableType().getText() + unit)
                        .build());
            } else {
                bulletCharts.add(MetricsBulletChartNVD3
                        .builder()
                        .id(sensorVariable.getId())
                        //.title(sensorVariable.getVariableType().getText())
                        //.subtitle(sensorVariable.getUnit())
                        .ranges(new Object[] { metric.getMinimum(), metric.getAverage(), metric.getMaximum() })
                        .measures(new Object[] { metric.getCurrent() })
                        .markers(new Object[] { metric.getPrevious() })
                        .resourceName(new ResourceModel(
                                RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails() + unit)
                        .displayName(sensorVariable.getSensor().getName() + " >> "
                                + sensorVariable.getVariableType().getText() + unit)
                        .build());
            }

        }
        return bulletCharts;
    }

    private MetricsChartDataGroupNVD3 getMetricsBatteryJsonNVD3(Integer nodeId, Long timestampFrom,
            Long timestampTo, Boolean withMinMax) {
        ArrayList<MetricsChartDataNVD3> preDoubleData = new ArrayList<MetricsChartDataNVD3>();
        //Get metrics
        List<MetricsBatteryUsage> batteryMetrics = metricApi.getMetricsBattery(nodeId, timestampFrom, timestampTo,
                withMinMax);
        ArrayList<Object> avgMetricValues = new ArrayList<Object>();
        ArrayList<Object> minMetricValues = new ArrayList<Object>();
        ArrayList<Object> maxMetricValues = new ArrayList<Object>();
        for (MetricsBatteryUsage metric : batteryMetrics) {
            avgMetricValues.add(new Object[] { metric.getTimestamp(), metric.getAvg() });
            if (withMinMax) {
                minMetricValues.add(new Object[] { metric.getTimestamp(), metric.getMin() });
                maxMetricValues.add(new Object[] { metric.getTimestamp(), metric.getMax() });
            }

        }
        MetricsGraph metricBattery = AppProperties.getInstance().getMetricsGraphSettings().getBattery();
        preDoubleData.add(MetricsChartDataNVD3.builder()
                .key(McObjectManager.getMcLocale().getString(MC_LOCALE.AVERAGE))
                .values(avgMetricValues)
                .color(metricBattery.getColor())
                .type(metricBattery.getSubType())
                .build().updateSubType(metricBattery.getType()));
        if (withMinMax) {
            preDoubleData.add(MetricsChartDataNVD3.builder()
                    .key(McObjectManager.getMcLocale().getString(MC_LOCALE.MINUMUM))
                    .values(minMetricValues)
                    .color(COLOR_MINIMUM)
                    .type(metricBattery.getSubType())
                    .build().updateSubType(metricBattery.getType()));
            preDoubleData.add(MetricsChartDataNVD3.builder()
                    .key(McObjectManager.getMcLocale().getString(MC_LOCALE.MAXIMUM))
                    .values(maxMetricValues)
                    .color(COLOR_MAXIMUM)
                    .type(metricBattery.getSubType())
                    .build().updateSubType(metricBattery.getType()));
        }

        return MetricsChartDataGroupNVD3.builder()
                .metricsChartDataNVD3(preDoubleData)
                .unit("%")
                .timeFormat(getTimeFormat(timestampFrom))
                .id(nodeId)
                .resourceName(new ResourceModel(RESOURCE_TYPE.NODE, nodeId).getResourceLessDetails())
                .chartType(metricBattery.getType())
                .chartInterpolate(metricBattery.getInterpolate())
                .build();
    }

    private ArrayList<MetricsChartDataGroupNVD3> getMetricsDataJsonNVD3WithChartType(
            List<Integer> variableIds,
            Long timestampFrom,
            Long timestampTo,
            String chartType) {

        //Get sensor variables
        List<SensorVariable> sensorVariables = null;
        if (!variableIds.isEmpty()) {
            sensorVariables = DaoUtils.getSensorVariableDao().getAll(variableIds);
        }

        //Return if no data available
        if (sensorVariables == null) {
            return new ArrayList<MetricsChartDataGroupNVD3>();
        }

        ArrayList<MetricsChartDataGroupNVD3> finalData = new ArrayList<MetricsChartDataGroupNVD3>();

        SensorVariable yaxis1Variable = null;
        boolean initialLoadDone = false;
        String chartTypeInternal = null;

        ArrayList<MetricsChartDataNVD3> metricDataValues = new ArrayList<MetricsChartDataNVD3>();

        String unit = null;
        String unit2 = null;
        String chartInterpolate = null;
        boolean isMultiChart = false;

        for (SensorVariable sensorVariable : sensorVariables) {
            MetricsGraph metrics = sensorVariable.getMetricsGraph();
            //Load initial settings
            if (!initialLoadDone) {
                if (CHART_TYPE.MULTI_CHART == CHART_TYPE.fromString(chartType)) {
                    isMultiChart = true;
                    yaxis1Variable = sensorVariable;
                    chartTypeInternal = chartType;
                } else if (CHART_TYPE.LINE_CHART == CHART_TYPE.fromString(chartType)) {
                    chartTypeInternal = chartType;
                    chartInterpolate = metrics.getInterpolate();
                }
                initialLoadDone = true;
            }

            Integer yAxis = null;
            if (yaxis1Variable != null) {
                if (yaxis1Variable.getVariableType() == sensorVariable.getVariableType()) {
                    yAxis = 1;
                    unit = UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit();
                } else {
                    yAxis = 2;
                    unit2 = UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit();
                }
            } else {
                unit = UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit();
            }

            if (chartTypeInternal == null) {
                chartTypeInternal = metrics.getType();
            }

            String seriesName = null;
            if (isMultiChart) {
                seriesName = sensorVariable.getSensor().getName() + "-" + sensorVariable.getVariableType().getText();
            } else {
                seriesName = sensorVariable.getSensor().getName();
                if (seriesName == null) {
                    seriesName = sensorVariable.getSensor().getNode().getEui() + "-"
                            + sensorVariable.getSensor().getSensorId() + "-"
                            + sensorVariable.getVariableType().getText();
                }
            }
            switch (sensorVariable.getMetricType()) {
                case DOUBLE:
                    List<MetricsDoubleTypeDevice> doubleMetrics = metricApi.getSensorVariableMetricsDouble(
                            sensorVariable.getId(), timestampFrom, timestampTo);
                    ArrayList<Object> avgMetricDoubleValues = new ArrayList<Object>();
                    for (MetricsDoubleTypeDevice metric : doubleMetrics) {
                        if (isMultiChart) {
                            avgMetricDoubleValues.add(MetricsChartDataXY.builder().x(metric.getTimestamp())
                                    .y(metric.getAvg()).build());
                        } else {
                            avgMetricDoubleValues.add(new Object[] { metric.getTimestamp(), metric.getAvg() });
                        }
                    }
                    if (!doubleMetrics.isEmpty()) {
                        metricDataValues.add(MetricsChartDataNVD3
                                .builder()
                                .key(seriesName)
                                .values(avgMetricDoubleValues)
                                .type(metrics.getSubType())
                                //.interpolate(metrics.getInterpolate())
                                .yAxis(yAxis)
                                .build().updateSubType(chartTypeInternal));
                    }

                    break;
                case COUNTER:
                    List<MetricsCounterTypeDevice> counterMetrics = metricApi.getSensorVariableMetricsCounter(
                            sensorVariable.getId(), timestampFrom, timestampTo);
                    ArrayList<Object> metricCounterValues = new ArrayList<Object>();
                    for (MetricsCounterTypeDevice metric : counterMetrics) {
                        if (isMultiChart) {
                            metricCounterValues.add(MetricsChartDataXY.builder().x(metric.getTimestamp())
                                    .y(metric.getValue()).build());
                        } else {
                            metricCounterValues.add(new Object[] { metric.getTimestamp(), metric.getValue() });
                        }
                    }
                    if (!counterMetrics.isEmpty()) {
                        metricDataValues.add(MetricsChartDataNVD3
                                .builder()
                                .key(seriesName)
                                .values(metricCounterValues)
                                .type(metrics.getSubType())
                                //.interpolate(metrics.getInterpolate())
                                .yAxis(yAxis)
                                .build().updateSubType(chartTypeInternal));
                    }
                    break;
                case BINARY:
                    List<MetricsBinaryTypeDevice> binaryMetrics = metricApi.getSensorVariableMetricsBinary(
                            sensorVariable.getId(), timestampFrom, timestampTo);
                    ArrayList<Object> metricBinaryValues = new ArrayList<Object>();
                    for (MetricsBinaryTypeDevice metric : binaryMetrics) {
                        if (isMultiChart) {
                            metricBinaryValues.add(MetricsChartDataXY.builder().x(metric.getTimestamp())
                                    .y(metric.getState() ? 1 : 0).build());
                        } else {
                            metricBinaryValues.add(new Object[] { metric.getTimestamp(), metric.getState() ? 1 : 0 });
                        }
                    }
                    if (!binaryMetrics.isEmpty()) {
                        metricDataValues.add(MetricsChartDataNVD3
                                .builder()
                                .key(seriesName)
                                .values(metricBinaryValues)
                                .type(metrics.getSubType())
                                //.interpolate(metrics.getInterpolate())
                                .yAxis(yAxis)
                                .id(sensorVariable.getId())
                                .resourceName(
                                        new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable)
                                                .getResourceLessDetails())
                                .build().updateSubType(chartTypeInternal));
                    }

                    break;
                default:
                    //no need to do anything here
                    break;
            }
        }

        finalData.add(MetricsChartDataGroupNVD3
                .builder()
                .metricsChartDataNVD3(metricDataValues)
                .unit(unit)
                .unit2(unit2)
                .timeFormat(getTimeFormat(timestampFrom))
                .chartType(chartType)
                .chartInterpolate(chartInterpolate)
                .build());

        return finalData;
    }

    private ArrayList<MetricsChartDataGroupNVD3> getMetricsDataJsonNVD3(
            List<Integer> variableIds,
            Integer sensorId,
            Long timestampFrom,
            Long timestampTo,
            Boolean withMinMax,
            String chartType) {

        //if chartType not null, call this
        if (chartType != null) {
            return getMetricsDataJsonNVD3WithChartType(variableIds, timestampFrom, timestampTo, chartType);
        }

        //Get sensor variables
        List<SensorVariable> sensorVariables = null;
        if (!variableIds.isEmpty()) {
            sensorVariables = DaoUtils.getSensorVariableDao().getAll(variableIds);
        } else {
            sensorVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId);
        }
        //Return if no data available
        if (sensorVariables == null) {
            return new ArrayList<MetricsChartDataGroupNVD3>();
        }

        ArrayList<MetricsChartDataGroupNVD3> finalData = new ArrayList<MetricsChartDataGroupNVD3>();

        for (SensorVariable sensorVariable : sensorVariables) {
            MetricsGraph metrics = sensorVariable.getMetricsGraph();
            switch (sensorVariable.getMetricType()) {
                case DOUBLE:
                    ArrayList<MetricsChartDataNVD3> preDoubleData = new ArrayList<MetricsChartDataNVD3>();
                    List<MetricsDoubleTypeDevice> doubleMetrics = metricApi.getSensorVariableMetricsDouble(
                            sensorVariable.getId(), timestampFrom, timestampTo);
                    ArrayList<Object> avgMetricDoubleValues = new ArrayList<Object>();
                    ArrayList<Object> minMetricDoubleValues = new ArrayList<Object>();
                    ArrayList<Object> maxMetricDoubleValues = new ArrayList<Object>();
                    for (MetricsDoubleTypeDevice metric : doubleMetrics) {
                        avgMetricDoubleValues.add(new Object[] { metric.getTimestamp(), metric.getAvg() });
                        if (withMinMax) {
                            minMetricDoubleValues.add(new Object[] { metric.getTimestamp(), metric.getMin() });
                            maxMetricDoubleValues.add(new Object[] { metric.getTimestamp(), metric.getMax() });
                        }
                    }
                    preDoubleData.add(MetricsChartDataNVD3.builder()
                            .key(McObjectManager.getMcLocale().getString(MC_LOCALE.AVERAGE))
                            .values(avgMetricDoubleValues)
                            .color(metrics.getColor())
                            .type(metrics.getSubType())
                            .build().updateSubType(metrics.getType()));
                    if (withMinMax) {
                        preDoubleData.add(MetricsChartDataNVD3.builder()
                                .key(McObjectManager.getMcLocale().getString(MC_LOCALE.MINUMUM))
                                .values(minMetricDoubleValues)
                                .color(COLOR_MINIMUM)
                                .type(metrics.getSubType())
                                .build().updateSubType(metrics.getType()));
                        preDoubleData.add(MetricsChartDataNVD3.builder()
                                .key(McObjectManager.getMcLocale().getString(MC_LOCALE.MAXIMUM))
                                .values(maxMetricDoubleValues)
                                .color(COLOR_MAXIMUM)
                                .type(metrics.getSubType())
                                .build().updateSubType(metrics.getType()));
                    }
                    finalData.add(MetricsChartDataGroupNVD3
                            .builder()
                            .metricsChartDataNVD3(preDoubleData)
                            .id(sensorVariable.getId())
                            .unit(UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit())
                            .timeFormat(getTimeFormat(timestampFrom))
                            .variableType(
                                    McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()))
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
                            .chartType(metrics.getType())
                            .chartInterpolate(metrics.getInterpolate())
                            .build());

                    break;
                case COUNTER:
                    ArrayList<MetricsChartDataNVD3> preCounterData = new ArrayList<MetricsChartDataNVD3>();
                    List<MetricsCounterTypeDevice> counterMetrics = metricApi.getSensorVariableMetricsCounter(
                            sensorVariable.getId(), timestampFrom, timestampTo);
                    ArrayList<Object> metricCounterValues = new ArrayList<Object>();
                    for (MetricsCounterTypeDevice metric : counterMetrics) {
                        metricCounterValues.add(new Object[] { metric.getTimestamp(), metric.getValue() });
                    }
                    preCounterData.add(MetricsChartDataNVD3.builder()
                            .key(sensorVariable.getVariableType().getText())
                            .values(metricCounterValues)
                            .color(metrics.getColor())
                            .type(metrics.getSubType())
                            .build().updateSubType(metrics.getType()));
                    finalData.add(MetricsChartDataGroupNVD3
                            .builder()
                            .metricsChartDataNVD3(preCounterData)
                            .id(sensorVariable.getId())
                            .unit(UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit())
                            .timeFormat(getTimeFormat(timestampFrom))
                            .variableType(
                                    McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()))
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
                            .chartType(metrics.getType())
                            .chartInterpolate(metrics.getInterpolate())
                            .build());

                    break;
                case BINARY:
                    ArrayList<MetricsChartDataNVD3> preBinaryData = new ArrayList<MetricsChartDataNVD3>();
                    List<MetricsBinaryTypeDevice> binaryMetrics = metricApi.getSensorVariableMetricsBinary(
                            sensorVariable.getId(), timestampFrom, timestampTo);
                    ArrayList<Object> metricBinaryValues = new ArrayList<Object>();
                    for (MetricsBinaryTypeDevice metric : binaryMetrics) {
                        metricBinaryValues.add(new Object[] { metric.getTimestamp(), metric.getState() ? 1 : 0 });
                    }
                    preBinaryData.add(MetricsChartDataNVD3.builder()
                            .key(sensorVariable.getVariableType().getText())
                            .values(metricBinaryValues)
                            .color(metrics.getColor())
                            .type(metrics.getSubType())
                            .build().updateSubType(metrics.getType()));
                    finalData.add(MetricsChartDataGroupNVD3
                            .builder()
                            .metricsChartDataNVD3(preBinaryData)
                            .id(sensorVariable.getId())
                            .unit(UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit())
                            .timeFormat(getTimeFormat(timestampFrom))
                            .variableType(
                                    McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()))
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
                            .chartType(metrics.getType())
                            .chartInterpolate(metrics.getInterpolate())
                            .build());
                    break;
                default:
                    //no need to do anything here
                    break;
            }
        }

        return finalData;
    }

    private String getTimeFormat(Long timestampFrom) {
        if (timestampFrom != null) {
            //subtract 5 seconds to get proper timeformat
            Long timeDifferance = System.currentTimeMillis() - timestampFrom - (McUtils.ONE_SECOND * 5);
            if (timeDifferance > (McUtils.ONE_DAY * 365)) {
                return AppProperties.getInstance().getDateFormat();
            } else if (timeDifferance > McUtils.ONE_DAY * 7) {
                return "MMM dd, " + AppProperties.getInstance().getTimeFormat();
            } else if (timeDifferance > McUtils.ONE_DAY * 1) {
                return "dd, " + AppProperties.getInstance().getTimeFormat();
            } else {
                return AppProperties.getInstance().getTimeFormat();
            }
        } else {
            return AppProperties.getInstance().getDateFormat();
        }
    }
}
