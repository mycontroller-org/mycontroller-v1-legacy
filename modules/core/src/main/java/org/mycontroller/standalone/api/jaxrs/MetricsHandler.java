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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.MC_LOCALE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.api.jaxrs.json.ApiError;
import org.mycontroller.standalone.api.jaxrs.json.MetricsBulletChartNVD3;
import org.mycontroller.standalone.api.jaxrs.json.MetricsChartDataGroupNVD3;
import org.mycontroller.standalone.api.jaxrs.json.MetricsChartDataNVD3;
import org.mycontroller.standalone.api.jaxrs.json.MetricsChartDataXY;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricsCsvEngine;
import org.mycontroller.standalone.model.ResourceCountModel;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsGraph.CHART_TYPE;
import org.mycontroller.standalone.settings.MetricsGraphSettings;

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

    //Get count of resources
    @GET
    @Path("/resourceCount")
    public Response getResourceCount(@QueryParam("resourceType") RESOURCE_TYPE resourceType,
            @QueryParam("resourceId") Integer resourceId) {
        return RestUtils.getResponse(Status.OK, new ResourceCountModel(resourceType, resourceId));
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
            MetricsDoubleTypeDevice metric = DaoUtils.getMetricsDoubleTypeDeviceDao().getMinMaxAvg(queryInput);

            String unit = sensorVariable.getUnit() != "" ? " (" + sensorVariable.getUnit() + ")" : "";

            //If current value not available, do not allow any value
            if (sensorVariable.getValue() == null || metric.getMin() == null) {
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
                        .ranges(new Object[] { metric.getMin(), metric.getAvg(), metric.getMax() })
                        .measures(new Object[] { sensorVariable.getValue() })
                        .markers(new Object[] { sensorVariable.getPreviousValue() })
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

        MetricsBatteryUsage metricQueryBattery = MetricsBatteryUsage.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .node(Node.builder().id(nodeId).build())
                .build();
        List<MetricsBatteryUsage> batteryMetrics = DaoUtils.getMetricsBatteryUsageDao().getAll(metricQueryBattery);
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
        MetricsGraph metricBattery = McObjectManager.getAppProperties().getMetricsGraphSettings().getBattery();
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

        MetricsGraphSettings metricsGraphSettings = McObjectManager.getAppProperties().getMetricsGraphSettings();
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
            MetricsGraph metrics = metricsGraphSettings.getMetric(sensorVariable.getVariableType().getText());
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
                    unit = sensorVariable.getUnit();
                } else {
                    yAxis = 2;
                    unit2 = sensorVariable.getUnit();
                }
            } else {
                unit = sensorVariable.getUnit();
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

                    MetricsDoubleTypeDevice metricQueryDouble = MetricsDoubleTypeDevice.builder()
                            .timestampFrom(timestampFrom)
                            .timestampTo(timestampTo)
                            .sensorVariable(sensorVariable)
                            .build();
                    List<MetricsDoubleTypeDevice> doubleMetrics = DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(
                            metricQueryDouble);
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
                case BINARY:
                    MetricsBinaryTypeDevice metricQueryBinary = MetricsBinaryTypeDevice.builder()
                            .timestampFrom(timestampFrom)
                            .timestampTo(timestampTo)
                            .sensorVariable(sensorVariable)
                            .build();
                    List<MetricsBinaryTypeDevice> binaryMetrics = DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(
                            metricQueryBinary);
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

        MetricsGraphSettings metricsGraphSettings = McObjectManager.getAppProperties().getMetricsGraphSettings();
        ArrayList<MetricsChartDataGroupNVD3> finalData = new ArrayList<MetricsChartDataGroupNVD3>();

        for (SensorVariable sensorVariable : sensorVariables) {
            MetricsGraph metrics = metricsGraphSettings.getMetric(sensorVariable.getVariableType().getText());
            switch (sensorVariable.getMetricType()) {
                case DOUBLE:
                    ArrayList<MetricsChartDataNVD3> preDoubleData = new ArrayList<MetricsChartDataNVD3>();

                    MetricsDoubleTypeDevice metricQueryDouble = MetricsDoubleTypeDevice.builder()
                            .timestampFrom(timestampFrom)
                            .timestampTo(timestampTo)
                            .sensorVariable(sensorVariable)
                            .build();
                    List<MetricsDoubleTypeDevice> doubleMetrics = DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(
                            metricQueryDouble);
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
                            .unit(sensorVariable.getUnit())
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
                    MetricsBinaryTypeDevice metricQueryBinary = MetricsBinaryTypeDevice.builder()
                            .timestampFrom(timestampFrom)
                            .timestampTo(timestampTo)
                            .sensorVariable(sensorVariable)
                            .build();
                    List<MetricsBinaryTypeDevice> binaryMetrics = DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(
                            metricQueryBinary);
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
                            .unit(sensorVariable.getUnit())
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
                return McObjectManager.getAppProperties().getDateFormat();
            } else if (timeDifferance > McUtils.ONE_DAY * 7) {
                return "MMM dd, " + McObjectManager.getAppProperties().getTimeFormat();
            } else if (timeDifferance > McUtils.ONE_DAY * 1) {
                return "dd, " + McObjectManager.getAppProperties().getTimeFormat();
            } else {
                return McObjectManager.getAppProperties().getTimeFormat();
            }
        } else {
            return McObjectManager.getAppProperties().getDateFormat();
        }
    }
}
