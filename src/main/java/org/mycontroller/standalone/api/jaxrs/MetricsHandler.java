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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.MetricsChartDataGroupNVD3;
import org.mycontroller.standalone.api.jaxrs.mapper.MetricsChartDataNVD3;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricsCsvEngine;
import org.mycontroller.standalone.model.ResourceCountModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/metrics")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class MetricsHandler {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsHandler.class.getName());
    public static final String MINUMUM = "Minimum";
    public static final String MAXIMUM = "Maximum";
    public static final String AVERAGE = "Average";

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
            @QueryParam("timestampFrom") Long timestampFrom,
            @QueryParam("timestampTo") Long timestampTo,
            @QueryParam("withMinMax") Boolean withMinMax) {
        return RestUtils.getResponse(Status.OK,
                getMetricsDataJsonNVD3(sensorId, timestampFrom, timestampTo, withMinMax != null ? withMinMax : false));
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
    @Path("/batteryUsage")
    public Response getBatteryUsageDetails(@QueryParam("nodeId") Integer nodeId) {
        return RestUtils.getResponse(Status.OK, this.getBatterUsage(nodeId));
    }

    private ArrayList<MetricsChartDataNVD3> getBatterUsage(int nodeId) {
        ArrayList<MetricsChartDataNVD3> finalData = new ArrayList<MetricsChartDataNVD3>();
        List<MetricsBatteryUsage> metrics = DaoUtils.getMetricsBatteryUsageDao().getAll(nodeId);
        if (metrics == null) {
            _logger.debug("No data");
            return null;
        }
        else {
            ArrayList<Object> batteryMetrics = new ArrayList<Object>();
            for (MetricsBatteryUsage metric : metrics) {
                batteryMetrics.add(new Object[] { metric.getTimestamp(), metric.getValue() });
            }
            finalData.add(MetricsChartDataNVD3.builder().key("Battery Level")
                    .values(batteryMetrics).build());
        }
        return finalData;
    }

    private ArrayList<MetricsChartDataGroupNVD3> getMetricsDataJsonNVD3(Integer sensorId, Long timestampFrom,
            Long timestampTo, Boolean withMinMax) {

        //Get sensor variables
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(sensorId);
        //Return if no data available
        if (sensorVariables == null || sensorVariables.size() == 0) {
            return null;
        }

        ArrayList<MetricsChartDataGroupNVD3> finalData = new ArrayList<MetricsChartDataGroupNVD3>();

        for (SensorVariable sensorVariable : sensorVariables) {
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
                            .key("Average")
                            .values(avgMetricDoubleValues)
                            .type("lineChart")
                            .interpolate("linear")
                            .area(false)
                            .build());
                    if (withMinMax) {
                        preDoubleData.add(MetricsChartDataNVD3.builder()
                                .key("Minimum")
                                .values(minMetricDoubleValues)
                                .type("lineChart")
                                .interpolate("linear")
                                .area(false)
                                .build());
                        preDoubleData.add(MetricsChartDataNVD3.builder()
                                .key("Maximum")
                                .values(maxMetricDoubleValues)
                                .type("lineChart")
                                .interpolate("linear")
                                .area(false)
                                .build());
                    }
                    finalData.add(MetricsChartDataGroupNVD3.builder()
                            .metricsChartDataNVD3(preDoubleData)
                            .unit(sensorVariable.getUnit())
                            .variableType(sensorVariable.getVariableType().getText())
                            .dataType(sensorVariable.getMetricType().getText()).build());

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
                            .type("lineChart")
                            .interpolate("step-after")
                            .area(false)
                            .build());
                    finalData.add(MetricsChartDataGroupNVD3.builder()
                            .metricsChartDataNVD3(preBinaryData)
                            .unit(sensorVariable.getUnit())
                            .variableType(sensorVariable.getVariableType().getText())
                            .dataType(sensorVariable.getMetricType().getText()).build());
                    break;
                default:
                    //no need to do anything here
                    break;
            }
        }

        return finalData;
    }
}
