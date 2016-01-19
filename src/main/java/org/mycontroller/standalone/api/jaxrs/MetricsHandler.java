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
import org.mycontroller.standalone.MYCMessages.PAYLOAD_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.MetricsChartDataKeyValuesJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.AGGREGATION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricsAggregationBase;
import org.mycontroller.standalone.metrics.MetricsCsvEngine;
import org.mycontroller.standalone.metrics.TypeUtils;
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

    //Get count of resources
    @GET
    @Path("/resourceCount")
    public Response getResourceCount(@QueryParam("resourceType") RESOURCE_TYPE resourceType,
            @QueryParam("resourceId") Integer resourceId) {
        return RestUtils.getResponse(Status.OK, new ResourceCountModel(resourceType, resourceId));
    }

    @GET
    @Path("/rawData")
    public Response getLastMinute(@QueryParam("variableTypeId") int variableTypeId,
            @QueryParam("lastNmilliSeconds") Long lastNmilliSeconds) {
        return RestUtils.getResponse(Status.OK,
                this.getMetrics(AGGREGATION_TYPE.RAW, variableTypeId, lastNmilliSeconds));
    }

    @GET
    @Path("/oneMinuteData")
    public Response getLast5Minutes(@QueryParam("variableTypeId") int variableTypeId,
            @QueryParam("lastNmilliSeconds") Long lastNmilliSeconds) {
        return RestUtils.getResponse(Status.OK,
                this.getMetrics(AGGREGATION_TYPE.ONE_MINUTE, variableTypeId, lastNmilliSeconds));
    }

    @GET
    @Path("/fiveMinutesData")
    public Response getLastOneHour(@QueryParam("variableTypeId") int variableTypeId,
            @QueryParam("lastNmilliSeconds") Long lastNmilliSeconds) {
        return RestUtils.getResponse(Status.OK,
                this.getMetrics(AGGREGATION_TYPE.FIVE_MINUTES, variableTypeId, lastNmilliSeconds));
    }

    @GET
    @Path("/oneHourData")
    public Response getLast24Hours(@QueryParam("variableTypeId") int variableTypeId,
            @QueryParam("lastNmilliSeconds") Long lastNmilliSeconds) {
        return RestUtils.getResponse(Status.OK,
                this.getMetrics(AGGREGATION_TYPE.ONE_HOUR, variableTypeId, lastNmilliSeconds));
    }

    @GET
    @Path("/oneDayData")
    public Response getLast30Days(@QueryParam("variableTypeId") int variableTypeId,
            @QueryParam("lastNmilliSeconds") Long lastNmilliSeconds) {
        return RestUtils.getResponse(Status.OK,
                this.getMetrics(AGGREGATION_TYPE.ONE_DAY, variableTypeId, lastNmilliSeconds));
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

    private ArrayList<MetricsChartDataKeyValuesJson> getMetrics(AGGREGATION_TYPE aggregationType, int variableTypeId,
            Long lastNmilliSeconds) {
        MetricsAggregationBase metricsAggregationBase = new MetricsAggregationBase();

        ArrayList<MetricsChartDataKeyValuesJson> finalData = new ArrayList<MetricsChartDataKeyValuesJson>();

        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(variableTypeId);

        if (sensorVariable.getMetricType() == null) {
            //Sensor pay load type not up to date
            _logger.debug("Payload type not updated in sensor.");
            return null;
        } else if (sensorVariable.getMetricType() == TypeUtils.METRIC_TYPE.DOUBLE) {
            _logger.debug("Payload type: {}", PAYLOAD_TYPE.PL_DOUBLE.toString());

            List<MetricsDoubleTypeDevice> metrics = metricsAggregationBase.getMetricsDoubleData(
                    sensorVariable,
                    aggregationType,
                    lastNmilliSeconds != null ? System.currentTimeMillis() - lastNmilliSeconds : null);

            if (metrics == null) {
                //throw new ApiError("No data available");
                return null;
            }

            if (aggregationType == AGGREGATION_TYPE.RAW) {
                MetricsChartDataKeyValuesJson rawChartData =
                        new MetricsChartDataKeyValuesJson(sensorVariable.getVariableType().toString());
                for (MetricsDoubleTypeDevice metric : metrics) {
                    rawChartData.add(new Object[] { metric.getTimestamp(), metric.getAvg() });
                }
                finalData.add(rawChartData);
            } else {
                MetricsChartDataKeyValuesJson minChartData = new MetricsChartDataKeyValuesJson("Minimum");
                MetricsChartDataKeyValuesJson avgChartData = new MetricsChartDataKeyValuesJson("Average");
                MetricsChartDataKeyValuesJson maxChartData = new MetricsChartDataKeyValuesJson("Maximum");

                for (MetricsDoubleTypeDevice metric : metrics) {
                    minChartData.add(new Object[] { metric.getTimestamp(), metric.getMin() });
                    avgChartData.add(new Object[] { metric.getTimestamp(), metric.getAvg() });
                    maxChartData.add(new Object[] { metric.getTimestamp(), metric.getMax() });
                }

                finalData.add(minChartData);
                finalData.add(avgChartData);
                finalData.add(maxChartData);
            }
        } else if (sensorVariable.getMetricType() == TypeUtils.METRIC_TYPE.BINARY) {
            _logger.debug("Payload type: {}", PAYLOAD_TYPE.PL_BOOLEAN.toString());
            List<MetricsBinaryTypeDevice> metrics = metricsAggregationBase.getMetricsBinaryData(
                    sensorVariable,
                    lastNmilliSeconds != null ? System.currentTimeMillis() - lastNmilliSeconds : null);
            if (metrics == null) {
                //throw new ApiError("No data available");
                return null;
            }

            Sensor sensor = DaoUtils.getSensorDao().getById(sensorVariable.getSensor().getId());

            String name = sensor.getName() != null ? sensor.getName() : "State";
            MetricsChartDataKeyValuesJson minChartData = new MetricsChartDataKeyValuesJson(name);

            for (MetricsBinaryTypeDevice metric : metrics) {
                minChartData.add(new Object[] { metric.getTimestamp(), metric.getState() ? 1 : 0 });
            }

            finalData.add(minChartData);
        }

        return finalData;
    }

    private ArrayList<MetricsChartDataKeyValuesJson> getBatterUsage(int nodeId) {
        ArrayList<MetricsChartDataKeyValuesJson> finalData = new ArrayList<MetricsChartDataKeyValuesJson>();
        List<MetricsBatteryUsage> metrics = DaoUtils.getMetricsBatteryUsageDao().getAll(nodeId);
        if (metrics == null) {
            _logger.debug("No data");
            return null;
        }
        else {
            MetricsChartDataKeyValuesJson chartData = new MetricsChartDataKeyValuesJson("Battery Level");
            for (MetricsBatteryUsage metric : metrics) {
                chartData.add(new Object[] { metric.getTimestamp(), metric.getValue() });
            }

            finalData.add(chartData);
        }
        return finalData;
    }
}
