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
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.MetricsChartDataGroupNVD3;
import org.mycontroller.standalone.api.jaxrs.mapper.MetricsChartDataNVD3;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricsCsvEngine;
import org.mycontroller.standalone.model.ResourceCountModel;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsSettings;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/metrics")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class MetricsHandler {
    public static final String MINUMUM = "Minimum";
    public static final String MAXIMUM = "Maximum";
    public static final String AVERAGE = "Average";
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
            @QueryParam("variableId") Integer variableId,
            @QueryParam("timestampFrom") Long timestampFrom,
            @QueryParam("timestampTo") Long timestampTo,
            @QueryParam("withMinMax") Boolean withMinMax) {
        return RestUtils.getResponse(
                Status.OK,
                getMetricsDataJsonNVD3(sensorId, variableId, timestampFrom, timestampTo,
                        withMinMax != null ? withMinMax : false));
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
        return RestUtils.getResponse(Status.OK,
                getMetricsBatteryJsonNVD3(nodeId, timestampFrom, timestampTo,
                        withMinMax != null ? withMinMax : false));
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
        MetricsGraph metricBattery = ObjectFactory.getAppProperties().getMetricsSettings().getBattery();
        preDoubleData.add(MetricsChartDataNVD3.builder()
                .key(AVERAGE)
                .values(avgMetricValues)
                .type(metricBattery.getType())
                .interpolate(metricBattery.getInterpolate())
                .area(metricBattery.getArea())
                .bar(metricBattery.getBar())
                .color(metricBattery.getColor())
                .build());
        if (withMinMax) {
            preDoubleData.add(MetricsChartDataNVD3.builder()
                    .key(MINUMUM)
                    .values(minMetricValues)
                    .type(metricBattery.getType())
                    .interpolate(metricBattery.getInterpolate())
                    .area(metricBattery.getArea())
                    .bar(metricBattery.getBar())
                    .color(COLOR_MINIMUM)
                    .build());
            preDoubleData.add(MetricsChartDataNVD3.builder()
                    .key(MAXIMUM)
                    .values(maxMetricValues)
                    .type(metricBattery.getType())
                    .interpolate(metricBattery.getInterpolate())
                    .area(metricBattery.getArea())
                    .bar(metricBattery.getBar())
                    .color(COLOR_MAXIMUM)
                    .build());
        }

        return MetricsChartDataGroupNVD3.builder()
                .metricsChartDataNVD3(preDoubleData)
                .unit("%")
                .timeFormat(getTimeFormat(timestampFrom))
                .id(nodeId)
                .resourceName(new ResourceModel(RESOURCE_TYPE.NODE, nodeId).getResourceLessDetails())
                .build();
    }

    private ArrayList<MetricsChartDataGroupNVD3> getMetricsDataJsonNVD3(
            Integer sensorId,
            Integer variableId,
            Long timestampFrom,
            Long timestampTo, Boolean withMinMax) {
        //Get sensor variables
        List<SensorVariable> sensorVariables = null;
        if (variableId != null) {
            SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(variableId);
            if (sensorVariable != null) {
                sensorVariables = new ArrayList<SensorVariable>();
                sensorVariables.add(sensorVariable);
            }
        } else {
            sensorVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId);
        }
        //Return if no data available
        if (sensorVariables == null) {
            return new ArrayList<MetricsChartDataGroupNVD3>();
        }

        MetricsSettings metricsSettings = ObjectFactory.getAppProperties().getMetricsSettings();
        ArrayList<MetricsChartDataGroupNVD3> finalData = new ArrayList<MetricsChartDataGroupNVD3>();

        for (SensorVariable sensorVariable : sensorVariables) {
            MetricsGraph metrics = metricsSettings.getMetric(sensorVariable.getVariableType().getText());
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
                            .key(AVERAGE)
                            .values(avgMetricDoubleValues)
                            .type(metrics.getType())
                            .interpolate(metrics.getInterpolate())
                            .area(metrics.getArea())
                            .bar(metrics.getBar())
                            .color(metrics.getColor())
                            .build());
                    if (withMinMax) {
                        preDoubleData.add(MetricsChartDataNVD3.builder()
                                .key(MINUMUM)
                                .values(minMetricDoubleValues)
                                .type(metrics.getType())
                                .interpolate(metrics.getInterpolate())
                                .area(metrics.getArea())
                                .bar(metrics.getBar())
                                .color(COLOR_MINIMUM)
                                .build());
                        preDoubleData.add(MetricsChartDataNVD3.builder()
                                .key(MAXIMUM)
                                .values(maxMetricDoubleValues)
                                .type(metrics.getType())
                                .interpolate(metrics.getInterpolate())
                                .area(metrics.getArea())
                                .bar(metrics.getBar())
                                .color(COLOR_MAXIMUM)
                                .build());
                    }
                    finalData.add(MetricsChartDataGroupNVD3
                            .builder()
                            .metricsChartDataNVD3(preDoubleData)
                            .id(sensorVariable.getId())
                            .unit(sensorVariable.getUnit())
                            .timeFormat(getTimeFormat(timestampFrom))
                            .variableType(sensorVariable.getVariableType().getText())
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
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
                            .type(metrics.getType())
                            .interpolate(metrics.getInterpolate())
                            .area(metrics.getArea())
                            .bar(metrics.getBar())
                            .color(metrics.getColor())
                            .build());
                    finalData.add(MetricsChartDataGroupNVD3.builder()
                            .metricsChartDataNVD3(preBinaryData)
                            .id(sensorVariable.getId())
                            .unit(sensorVariable.getUnit())
                            .timeFormat(getTimeFormat(timestampFrom))
                            .variableType(sensorVariable.getVariableType().getText())
                            .dataType(sensorVariable.getMetricType().getText())
                            .resourceName(new ResourceModel(
                                    RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
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
            Long timeDifferance = System.currentTimeMillis() - timestampFrom - (TIME_REF.ONE_SECOND * 5);
            if (timeDifferance > (TIME_REF.ONE_DAY * 365)) {
                return ObjectFactory.getAppProperties().getDateFormat();
            } else if (timeDifferance > TIME_REF.ONE_DAY * 7) {
                return "MMM dd, " + ObjectFactory.getAppProperties().getTimeFormat();
            } else if (timeDifferance > TIME_REF.ONE_DAY * 1) {
                return "dd, " + ObjectFactory.getAppProperties().getTimeFormat();
            } else {
                return ObjectFactory.getAppProperties().getTimeFormat();
            }
        } else {
            return ObjectFactory.getAppProperties().getDateFormat();
        }
    }
}
