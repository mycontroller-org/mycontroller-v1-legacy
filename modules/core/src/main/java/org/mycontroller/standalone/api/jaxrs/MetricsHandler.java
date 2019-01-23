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

import java.io.IOException;
import java.text.MessageFormat;
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
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.MC_LOCALE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.MetricApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.DataPointBinary;
import org.mycontroller.standalone.api.jaxrs.model.DataPointCounter;
import org.mycontroller.standalone.api.jaxrs.model.DataPointDouble;
import org.mycontroller.standalone.api.jaxrs.model.LocaleString;
import org.mycontroller.standalone.api.jaxrs.model.MetricsBulletChartNVD3;
import org.mycontroller.standalone.api.jaxrs.model.MetricsChartDataGroupNVD3;
import org.mycontroller.standalone.api.jaxrs.model.MetricsChartDataNVD3;
import org.mycontroller.standalone.api.jaxrs.model.MetricsChartDataXY;
import org.mycontroller.standalone.api.jaxrs.model.TopologyItem;
import org.mycontroller.standalone.api.jaxrs.model.TopologyKinds;
import org.mycontroller.standalone.api.jaxrs.model.TopologyRelation;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.DATA_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.metrics.export.CsvExportEngine;
import org.mycontroller.standalone.metrics.model.MetricDouble;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.provider.mysensors.MySensors;
import org.mycontroller.standalone.scripts.McScriptException;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsGraph.CHART_TYPE;
import org.mycontroller.standalone.units.UnitUtils;
import org.mycontroller.standalone.units.UnitUtils.UNIT_TYPE;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Slf4j
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
    @Path("/nvd3data")
    public Response getMetricsNvd3Data(
            @QueryParam("sensorId") Integer sensorId,
            @QueryParam("variableId") List<Integer> variableIds,
            @QueryParam("start") Long start,
            @QueryParam("end") Long end,
            @QueryParam("duration") String duration,
            @QueryParam("withMinMax") Boolean withMinMax,
            @QueryParam("chartType") String chartType,
            @QueryParam("bucketDuration") String bucketDuration,
            @QueryParam("enableDetailedKey") Boolean enableDetailedKey) {
        if (!variableIds.isEmpty()) {
            updateSensorVariableIds(variableIds);
        } else if (sensorId != null) {
            hasAccessSensor(sensorId);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Either sensor id or sensor variable id(s) is required!"));
        }
        try {
            return RestUtils.getResponse(
                    Status.OK,
                    getMetricsDataJsonNVD3(variableIds, sensorId, start, end, duration,
                            withMinMax != null ? withMinMax : false, chartType, bucketDuration, enableDetailedKey));
        } catch (McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/stats")
    public Response getMetricsData(
            @QueryParam("resourceId") Integer resourceId,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("start") Long start,
            @QueryParam("end") Long end,
            @QueryParam("duration") String duration,
            @QueryParam("bucketDuration") String bucketDuration,
            @QueryParam("uid") String uid,
            @QueryParam("dataType") String dataType) {
        try {
            DATA_TYPE dType = null;
            if (dataType != null) {
                DATA_TYPE.valueOf(dataType.toUpperCase());
            }
            return RestUtils.getResponse(Status.OK,
                    getMetricsDataInternal(getResourceModel(resourceId, resourceType, uid),
                            start, end, duration, bucketDuration, dType));
        } catch (McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/bulletChart")
    public Response getBulletChart(
            @QueryParam("variableId") List<Integer> variableIds,
            @QueryParam("start") Long start,
            @QueryParam("end") Long end) {
        if (!variableIds.isEmpty()) {
            updateSensorVariableIds(variableIds);
        } else {
            return RestUtils.getResponse(Status.BAD_REQUEST,
                    new ApiError("Sensor variable id(s) is required!"));
        }
        return RestUtils.getResponse(Status.OK, getMetricsBulletChart(variableIds, start, end));
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
    @Path("/export")
    public Response getResourceData(@QueryParam("resourceId") Integer resourceId,
            @QueryParam("resourceType") String resourceType,
            @QueryParam("start") Long start,
            @QueryParam("end") Long end,
            @QueryParam("duration") String duration,
            @QueryParam("bucketDuration") String bucketDuration,
            @QueryParam("uid") String uid) {
        try {
            ResourceModel resourceModel = getResourceModel(resourceId, resourceType, uid);
            return RestUtils.getResponse(Status.OK,
                    new CsvExportEngine().getMetric(resourceModel.getResourceId(), resourceModel
                            .getResourceType().getText(), start, end, duration, bucketDuration));
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/statsBattery")
    public Response getMetricsBattery(
            @QueryParam("nodeId") Integer nodeId,
            @QueryParam("start") Long start,
            @QueryParam("end") Long end,
            @QueryParam("withMinMax") Boolean withMinMax,
            @QueryParam("bucketDuration") String bucketDuration) {
        //Access check
        hasAccessNode(nodeId);
        try {
            return RestUtils.getResponse(Status.OK,
                    getMetricsBatteryJsonNVD3(nodeId, start, end,
                            withMinMax != null ? withMinMax : false, bucketDuration));
        } catch (McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/topology")
    public Response getTopology(
            @QueryParam("resourceType") RESOURCE_TYPE resourceType,
            @QueryParam("resourceId") Integer resourceId,
            @QueryParam("realtime") Boolean realtime) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        HashMap<String, TopologyItem> items = new HashMap<String, TopologyItem>();
        List<TopologyRelation> relations = new ArrayList<TopologyRelation>();
        TopologyKinds kinds = TopologyKinds.builder().build();

        if (realtime == null) {
            realtime = false;
        }

        data.put("items", items);
        data.put("relations", relations);
        data.put("kinds", kinds);

        if (resourceType != null && resourceId != null) {
            kinds.update(resourceType);
            switch (resourceType) {
                case GATEWAY:
                    updateGatewayTopology(items, relations, resourceId, realtime);
                    break;
                case NODE:
                    updateNodeTopology(items, relations, null, resourceId, realtime);
                    break;
                case SENSOR:
                    updateSensorTopology(items, relations, null, resourceId);
                    break;
                default:
                    break;
            }
        } else {
            kinds.update(RESOURCE_TYPE.GATEWAY);
            updateGatewayTopology(items, relations, null, realtime);
        }

        return RestUtils.getResponse(Status.OK, data);
    }

    private ResourceModel getResourceModel(Integer resourceId, String resourceType, String uid)
            throws McBadRequestException {
        ResourceModel resourceModel = metricApi.getResourceModel(resourceId, resourceType, uid);
        switch (resourceModel.getResourceType()) {
            case NODE:
                hasAccessNode(resourceId);
                break;
            case SENSOR_VARIABLE:
                hasAccessSensorVariable(resourceId);
                break;
            default:
                break;
        }
        return resourceModel;
    }

    private List<?> getMetricsDataInternal(ResourceModel resourceModel, Long start, Long end, String duration,
            String bucketDuration, DATA_TYPE dataType) throws McBadRequestException {
        if (bucketDuration == null) {
            throw new McBadRequestException(MessageFormat.format(
                    "Required fields is missing! bucketDuration:[{0}]", bucketDuration));
        }
        try {
            return metricApi.getMetricData(resourceModel, start, end, duration, bucketDuration, dataType);
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            throw new McBadRequestException(ex);
        }
    }

    private void updateGatewayTopology(HashMap<String, TopologyItem> items,
            List<TopologyRelation> relations,
            Integer gatewayId, boolean realtime) {
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
            updateNodeTopology(items, relations, gateway.getId(), null, realtime);
        }
    }

    private void updateNodeTopology(HashMap<String, TopologyItem> items,
            List<TopologyRelation> relations, Integer gatewayId, Integer nodeId, boolean realtime) {
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
                    .name(nodeName)
                    .id(node.getId())
                    .localId(node.getEui())
                    .type(RESOURCE_TYPE.NODE)
                    .kind(TOPOLOGY_KIND_NODE)
                    .status(node.getState().getText())
                    .build());
            if (realtime) {
                if (node.getParentNodeEui() != null) {
                    Node parentNode = DaoUtils.getNodeDao().get(node.getGatewayTable().getId(),
                            node.getParentNodeEui());
                    if (parentNode != null) {
                        relations.add(TopologyRelation.builder()
                                .source(source)
                                .target(TOPOLOGY_PREFIX_NODE + parentNode.getId())
                                .build());
                    }
                } else if (node.getGatewayTable().getNetworkType() != NETWORK_TYPE.MY_SENSORS
                        || (node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_SENSORS
                        && node.getEui().equals(String.valueOf(MySensors.GATEWAY_ID)))) {
                    relations.add(TopologyRelation.builder()
                            .source(source)
                            .target(TOPOLOGY_PREFIX_GATEWAY + node.getGatewayTable().getId())
                            .build());
                }
            } else {
                relations.add(TopologyRelation.builder()
                        .source(source)
                        .target(TOPOLOGY_PREFIX_GATEWAY + node.getGatewayTable().getId())
                        .build());
            }
            updateSensorTopology(items, relations, node.getId(), null);
        }
    }

    private void updateSensorTopology(HashMap<String, TopologyItem> items, List<TopologyRelation> relations,
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
                subType = LocaleString.builder().en("Undefined")
                        .locale(McObjectManager.getMcLocale().getString(MC_LOCALE.UNDEFINED)).build();
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
                    .localId(sensor.getSensorId())
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

    private void updateSensorVariableTopology(HashMap<String, TopologyItem> items, List<TopologyRelation> relations,
            int sensorId) {
        List<SensorVariable> sVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId);
        for (SensorVariable sVariable : sVariables) {
            String source = TOPOLOGY_PREFIX_SENSOR_VARIABLE + sVariable.getId();
            items.put(source, TopologyItem.builder()
                    .name(sVariable.getName() != null ? sVariable.getName() : sVariable.getVariableType().getText())
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
            Long start, Long end) {
        if (end == null) {
            end = System.currentTimeMillis();
        }
        ArrayList<MetricsBulletChartNVD3> bulletCharts = new ArrayList<MetricsBulletChartNVD3>();
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(variableIds);
        for (SensorVariable sensorVariable : sensorVariables) {
            MetricDouble metric = metricApi.getSensorVariableMetricDouble(sensorVariable, start, end);

            String unit = sensorVariable.getUnitType() != UNIT_TYPE.U_NONE ? " ("
                    + UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit() + ")" : "";
            String sensorName = sensorVariable.getSensor().getSensorId();
            if (sensorVariable.getSensor().getName() != null && sensorVariable.getSensor().getName().length() > 0) {
                sensorName = sensorName + ":" + sensorVariable.getSensor().getName();
            } else if (sensorVariable.getSensor().getType() != null) {
                sensorName = sensorName + ":"
                        + McObjectManager.getMcLocale().getString(sensorVariable.getSensor().getType().name());
            }

            //If current value not available, do not allow any value
            if (metric.getCurrent() == null || metric.getMinimum() == null) {
                bulletCharts.add(MetricsBulletChartNVD3
                        .builder()
                        .id(sensorVariable.getId())
                        .internalId(sensorVariable.getSensor().getId())
                        .resourceName(new ResourceModel(
                                RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails() + unit)
                        .displayName(sensorName + " >> " + sensorVariable.getVariableType().getText() + unit)
                        .build());
            } else {
                bulletCharts.add(MetricsBulletChartNVD3
                        .builder()
                        .id(sensorVariable.getId())
                        .internalId(sensorVariable.getSensor().getId())
                        //.title(sensorVariable.getVariableType().getText())
                        //.subtitle(sensorVariable.getUnit())
                        .ranges(new Object[] { metric.getMinimum(), metric.getAverage(), metric.getMaximum() })
                        .measures(new Object[] { metric.getCurrent() })
                        .markers(new Object[] { metric.getPrevious() })
                        .resourceName(new ResourceModel(
                                RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails() + unit)
                        .displayName(sensorName + " >> " + sensorVariable.getVariableType().getText() + unit)
                        .build());
            }

        }
        return bulletCharts;
    }

    private MetricsChartDataGroupNVD3 getMetricsBatteryJsonNVD3(Integer nodeId, Long start,
            Long end, Boolean withMinMax, String bucketDuration) throws McBadRequestException {
        if (bucketDuration == null) {
            bucketDuration = MetricApi.getBucketDuration(start, end, METRIC_TYPE.DOUBLE);
        }
        ArrayList<MetricsChartDataNVD3> preDoubleData = new ArrayList<MetricsChartDataNVD3>();
        //Get metrics
        @SuppressWarnings("unchecked")
        List<DataPointDouble> batteryMetrics = (List<DataPointDouble>) metricApi.getMetricData(nodeId,
                RESOURCE_TYPE.NODE.getText(), start, end, null, bucketDuration, DATA_TYPE.NODE_BATTERY_USAGE);
        ArrayList<Object> avgMetricValues = new ArrayList<Object>();
        ArrayList<Object> minMetricValues = new ArrayList<Object>();
        ArrayList<Object> maxMetricValues = new ArrayList<Object>();
        long timestamp = 0L;
        for (DataPointDouble metric : batteryMetrics) {
            if (!metric.isEmpty()) {
                if (metric.getStart() != null) {
                    timestamp = metric.getStart();
                } else {
                    timestamp = metric.getTimestamp();
                }
                avgMetricValues.add(new Object[] { timestamp, metric.getAvg() });
                if (withMinMax) {
                    minMetricValues.add(new Object[] { timestamp, metric.getMin() });
                    maxMetricValues.add(new Object[] { timestamp, metric.getMax() });
                }
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
                .internalId(nodeId)
                .unit("%")
                .timeFormat(getTimeFormat(start, METRIC_TYPE.DOUBLE))
                .id(nodeId)
                .resourceName(new ResourceModel(RESOURCE_TYPE.NODE, nodeId).getResourceLessDetails())
                .chartType(metricBattery.getType())
                .chartInterpolate(metricBattery.getInterpolate())
                .build();
    }

    private ArrayList<MetricsChartDataGroupNVD3> getMetricsDataJsonNVD3WithChartType(
            List<Integer> variableIds,
            Long start,
            Long end,
            String duration,
            String chartType,
            String bucketDuration,
            Boolean enableDetailedKey) throws McBadRequestException {
        if (enableDetailedKey == null) {
            enableDetailedKey = Boolean.FALSE;
        }

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

        //Update bucket duration
        String bucketDurationDouble = bucketDuration;
        String bucketDurationCounter = bucketDuration;
        String bucketDurationBinary = bucketDuration;
        if (bucketDuration == null) {
            bucketDurationDouble = MetricApi.getBucketDuration(start, end, METRIC_TYPE.DOUBLE);
            bucketDurationCounter = MetricApi.getBucketDuration(start, end, METRIC_TYPE.COUNTER);
            bucketDurationBinary = MetricApi.getBucketDuration(start, end, METRIC_TYPE.BINARY);
        }

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

            StringBuilder seriesName = new StringBuilder();
            StringBuilder postText = new StringBuilder();
            if (enableDetailedKey) {
                postText.append(" [N]:").append(sensorVariable.getSensor().getNode().getEui()).append(" >> [S]:")
                        .append(sensorVariable.getSensor().getSensorId());
            } else {
                postText.append("");
            }

            if (isMultiChart) {
                seriesName.append(sensorVariable.getVariableType().getText());
                if (sensorVariable.getName() != null) {
                    seriesName.append(" (").append(sensorVariable.getName()).append(")");
                }
                seriesName.append(postText);
            } else {
                seriesName.append(sensorVariable.getSensor().getName());
                if (seriesName.length() == 0) {
                    seriesName.append(sensorVariable.getVariableType().getText());
                }
                if (sensorVariable.getName() != null) {
                    seriesName.append(" (").append(sensorVariable.getName()).append(")");
                }
                if (enableDetailedKey) {
                    seriesName.append(postText);
                }
            }
            switch (sensorVariable.getMetricType()) {
                case DOUBLE:
                    @SuppressWarnings("unchecked")
                    List<DataPointDouble> doubleMetrics = (List<DataPointDouble>) metricApi
                            .getMetricData(sensorVariable.getId(), RESOURCE_TYPE.SENSOR_VARIABLE.getText(),
                                    start, end, duration, bucketDurationDouble, DATA_TYPE.SENSOR_VARIABLE);
                    ArrayList<Object> avgMetricDoubleValues = new ArrayList<Object>();
                    long timestamp = 0L;
                    for (DataPointDouble metric : doubleMetrics) {
                        if (!metric.isEmpty()) {
                            if (metric.getStart() != null) {
                                timestamp = metric.getStart();
                            } else {
                                timestamp = metric.getTimestamp();
                            }
                            if (isMultiChart) {
                                avgMetricDoubleValues.add(MetricsChartDataXY.builder().x(timestamp)
                                        .y(metric.getAvg()).build());
                            } else {
                                avgMetricDoubleValues.add(new Object[] { timestamp, metric.getAvg() });
                            }
                        }
                    }
                    if (!doubleMetrics.isEmpty()) {
                        metricDataValues.add(MetricsChartDataNVD3
                                .builder()
                                .key(seriesName.toString())
                                .values(avgMetricDoubleValues)
                                .type(metrics.getSubType())
                                //.interpolate(metrics.getInterpolate())
                                .yAxis(yAxis)
                                .build().updateSubType(chartTypeInternal));
                    }

                    break;
                case COUNTER:
                    @SuppressWarnings("unchecked")
                    List<DataPointCounter> counterMetrics = (List<DataPointCounter>) metricApi
                            .getMetricData(sensorVariable.getId(), RESOURCE_TYPE.SENSOR_VARIABLE.getText(),
                                    start, end, duration, bucketDurationCounter, DATA_TYPE.SENSOR_VARIABLE);
                    ArrayList<Object> metricCounterValues = new ArrayList<Object>();
                    for (DataPointCounter metric : counterMetrics) {
                        if (!metric.isEmpty()) {
                            if (metric.getStart() != null) {
                                timestamp = metric.getStart();
                            } else {
                                timestamp = metric.getTimestamp();
                            }
                            if (isMultiChart) {
                                metricCounterValues.add(MetricsChartDataXY.builder().x(timestamp)
                                        .y(metric.getValue()).build());
                            } else {
                                metricCounterValues.add(new Object[] { timestamp, metric.getValue() });
                            }
                        }
                    }
                    if (!counterMetrics.isEmpty()) {
                        metricDataValues.add(MetricsChartDataNVD3
                                .builder()
                                .key(seriesName.toString())
                                .values(metricCounterValues)
                                .type(metrics.getSubType())
                                //.interpolate(metrics.getInterpolate())
                                .yAxis(yAxis)
                                .build().updateSubType(chartTypeInternal));
                    }
                    break;
                case BINARY:
                    @SuppressWarnings("unchecked")
                    List<DataPointBinary> binaryMetrics = (List<DataPointBinary>) metricApi
                            .getMetricData(sensorVariable.getId(), RESOURCE_TYPE.SENSOR_VARIABLE.getText(), start,
                                    end, duration, bucketDurationBinary, DATA_TYPE.SENSOR_VARIABLE);
                    ArrayList<Object> metricBinaryValues = new ArrayList<Object>();
                    for (DataPointBinary metric : binaryMetrics) {
                        if (!metric.isEmpty()) {
                            if (isMultiChart) {
                                metricBinaryValues.add(MetricsChartDataXY.builder().x(metric.getTimestamp())
                                        .y(metric.getState() ? 1 : 0).build());
                            } else {
                                metricBinaryValues
                                        .add(new Object[] { metric.getTimestamp(), metric.getState() ? 1 : 0 });
                            }
                        }
                    }
                    if (!binaryMetrics.isEmpty()) {
                        metricDataValues.add(MetricsChartDataNVD3
                                .builder()
                                .key(seriesName.toString())
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
                .timeFormat(getTimeFormat(start, METRIC_TYPE.DOUBLE))
                .chartType(chartType)
                .chartInterpolate(chartInterpolate)
                .build());

        return finalData;
    }

    private ArrayList<MetricsChartDataGroupNVD3> getMetricsDataJsonNVD3(
            List<Integer> variableIds,
            Integer sensorId,
            Long start,
            Long end,
            String duration,
            Boolean withMinMax,
            String chartType,
            String bucketDuration,
            Boolean enableDetailedKey) throws McBadRequestException {

        //if chartType not null, call this
        if (chartType != null) {
            return getMetricsDataJsonNVD3WithChartType(variableIds, start, end, duration, chartType,
                    bucketDuration, enableDetailedKey);
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

        //Update bucket duration
        String bucketDurationDouble = bucketDuration;
        String bucketDurationCounter = bucketDuration;
        String bucketDurationBinary = bucketDuration;
        if (bucketDuration == null) {
            bucketDurationDouble = MetricApi.getBucketDuration(start, end, METRIC_TYPE.DOUBLE);
            bucketDurationCounter = MetricApi.getBucketDuration(start, end, METRIC_TYPE.COUNTER);
            bucketDurationBinary = MetricApi.getBucketDuration(start, end, METRIC_TYPE.BINARY);
        }

        for (SensorVariable sensorVariable : sensorVariables) {
            MetricsGraph metrics = sensorVariable.getMetricsGraph();
            switch (sensorVariable.getMetricType()) {
                case DOUBLE:
                    ArrayList<MetricsChartDataNVD3> preDoubleData = new ArrayList<MetricsChartDataNVD3>();
                    @SuppressWarnings("unchecked")
                    List<DataPointDouble> doubleMetrics = (List<DataPointDouble>) metricApi
                            .getMetricData(sensorVariable.getId(), RESOURCE_TYPE.SENSOR_VARIABLE.getText(),
                                    start, end, duration, bucketDurationDouble, DATA_TYPE.SENSOR_VARIABLE);
                    ArrayList<Object> avgMetricDoubleValues = new ArrayList<Object>();
                    ArrayList<Object> minMetricDoubleValues = new ArrayList<Object>();
                    ArrayList<Object> maxMetricDoubleValues = new ArrayList<Object>();
                    long timestamp = 0L;
                    for (DataPointDouble metric : doubleMetrics) {
                        if (!metric.isEmpty()) {
                            if (metric.getStart() != null) {
                                timestamp = metric.getStart();
                            } else {
                                timestamp = metric.getTimestamp();
                            }
                            avgMetricDoubleValues.add(new Object[] { timestamp, metric.getAvg() });
                            if (withMinMax) {
                                minMetricDoubleValues.add(new Object[] { timestamp, metric.getMin() });
                                maxMetricDoubleValues.add(new Object[] { timestamp, metric.getMax() });
                            }
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
                            .internalId(sensorVariable.getSensor().getId())
                            .unit(UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit())
                            .timeFormat(getTimeFormat(start, METRIC_TYPE.DOUBLE))
                            .variableType(
                                    McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name())
                                            + (sensorVariable.getName() != null
                                                    ? " (" + sensorVariable.getName() + ")" : ""))
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
                            .chartType(metrics.getType())
                            .chartInterpolate(metrics.getInterpolate())
                            .marginLeft(metrics.getMarginLeft())
                            .marginRight(metrics.getMarginRight())
                            .marginTop(metrics.getMarginTop())
                            .marginBottom(metrics.getMarginBottom())
                            .build());

                    break;
                case COUNTER:
                    ArrayList<MetricsChartDataNVD3> preCounterData = new ArrayList<MetricsChartDataNVD3>();
                    @SuppressWarnings("unchecked")
                    List<DataPointCounter> counterMetrics = (List<DataPointCounter>) metricApi
                            .getMetricData(sensorVariable.getId(), RESOURCE_TYPE.SENSOR_VARIABLE.getText(),
                                    start, end, duration, bucketDurationCounter, DATA_TYPE.SENSOR_VARIABLE);
                    ArrayList<Object> metricCounterValues = new ArrayList<Object>();
                    for (DataPointCounter metric : counterMetrics) {
                        if (!metric.isEmpty()) {
                            if (metric.getStart() != null) {
                                timestamp = metric.getStart();
                            } else {
                                timestamp = metric.getTimestamp();
                            }
                            metricCounterValues.add(new Object[] { timestamp, metric.getValue() });
                        }
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
                            .internalId(sensorVariable.getSensor().getId())
                            .unit(UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit())
                            .timeFormat(getTimeFormat(start, METRIC_TYPE.COUNTER))
                            .variableType(
                                    McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()))
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
                            .chartType(metrics.getType())
                            .chartInterpolate(metrics.getInterpolate())
                            .marginLeft(metrics.getMarginLeft())
                            .marginRight(metrics.getMarginRight())
                            .marginTop(metrics.getMarginTop())
                            .marginBottom(metrics.getMarginBottom())
                            .build());

                    break;
                case BINARY:
                    ArrayList<MetricsChartDataNVD3> preBinaryData = new ArrayList<MetricsChartDataNVD3>();
                    @SuppressWarnings("unchecked")
                    List<DataPointBinary> binaryMetrics = (List<DataPointBinary>) metricApi
                            .getMetricData(sensorVariable.getId(), RESOURCE_TYPE.SENSOR_VARIABLE.getText(), start,
                                    end, duration, bucketDurationBinary, DATA_TYPE.SENSOR_VARIABLE);
                    ArrayList<Object> metricBinaryValues = new ArrayList<Object>();
                    for (DataPointBinary metric : binaryMetrics) {
                        if (!metric.isEmpty()) {
                            metricBinaryValues.add(new Object[] { metric.getTimestamp(), metric.getState() ? 1 : 0 });
                        }
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
                            .internalId(sensorVariable.getSensor().getId())
                            .unit(UnitUtils.getUnit(sensorVariable.getUnitType()).getUnit())
                            .timeFormat(getTimeFormat(start, METRIC_TYPE.BINARY))
                            .variableType(
                                    McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()))
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
                            .chartType(metrics.getType())
                            .chartInterpolate(metrics.getInterpolate())
                            .marginLeft(metrics.getMarginLeft())
                            .marginRight(metrics.getMarginRight())
                            .marginTop(metrics.getMarginTop())
                            .marginBottom(metrics.getMarginBottom())
                            .build());
                    break;
                default:
                    //no need to do anything here
                    break;
            }
        }

        return finalData;
    }

    private String getTimeFormat(Long start, METRIC_TYPE metricType) {
        if (start != null) {
            //subtract 5 seconds to get proper timeformat
            Long timeDifferance = System.currentTimeMillis() - start - (McUtils.ONE_SECOND * 5);
            switch (metricType) {
                case COUNTER:
                    if (timeDifferance > (McUtils.ONE_DAY * 30)) {
                        return "MMM, yyyy";
                    } else if (timeDifferance > McUtils.ONE_DAY * 7) {
                        return "MMM, dd";
                    } else if (timeDifferance > McUtils.ONE_DAY * 1) {
                        return "dd, EEE";
                    } else {
                        return AppProperties.getInstance().getTimeFormat();
                    }
                case BINARY:
                case DOUBLE:
                    if (timeDifferance > (McUtils.ONE_DAY * 360)) {
                        return AppProperties.getInstance().getDateFormat();
                    } else if (timeDifferance > McUtils.ONE_DAY * 7) {
                        return "MMM dd, " + AppProperties.getInstance().getTimeFormat();
                    } else if (timeDifferance > McUtils.ONE_DAY * 1) {
                        return "dd, " + AppProperties.getInstance().getTimeFormat();
                    } else {
                        return AppProperties.getInstance().getTimeFormat();
                    }
                default:
                    return AppProperties.getInstance().getTimeFormat();
            }

        } else {
            return AppProperties.getInstance().getDateFormat();
        }
    }
}
