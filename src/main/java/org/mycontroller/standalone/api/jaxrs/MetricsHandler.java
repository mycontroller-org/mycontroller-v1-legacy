/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.mapper.MetricsChartDataKeyValuesJson;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.AGGREGATION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsOnOffTypeDevice;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.jobs.metrics.MetricsAggregationBase;
import org.mycontroller.standalone.mysensors.MyMessages;
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

    @GET
    @Path("/lastMinute/{sensorId}")
    public Response getLastMinute(@PathParam("sensorId") int sensorId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_MINUTE,
                sensorId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/last5Minutes/{sensorId}")
    public Response getLast5Minutes(@PathParam("sensorId") int sensorId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.FIVE_MINUTES,
                sensorId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/lastOneHour/{sensorId}")
    public Response getLastOneHour(@PathParam("sensorId") int sensorId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_HOUR, sensorId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/last24Hours/{sensorId}")
    public Response getLast24Hours(@PathParam("sensorId") int sensorId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_DAY, sensorId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/last30Days/{sensorId}")
    public Response getLast30Days(@PathParam("sensorId") int sensorId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.THIRTY_DAYS,
                sensorId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/lastYear/{sensorId}")
    public Response getLastYear(@PathParam("sensorId") int sensorId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ONE_YEAR, sensorId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/allYears/{sensorId}")
    public Response getAllYears(@PathParam("sensorId") int sensorId) {
        ArrayList<MetricsChartDataKeyValuesJson> chartDataJson = this.getAllAfter(AGGREGATION_TYPE.ALL_DAYS, sensorId);
        return RestUtils.getResponse(Status.OK, chartDataJson);
    }

    @GET
    @Path("/sensorData/{id}")
    public Response getSensorDetails(@PathParam("id") int id) {
        Sensor sensor = DaoUtils.getSensorDao().get(id);
        return RestUtils.getResponse(Status.OK, sensor);
    }

    @GET
    @Path("/batteryUsage/{nodeId}")
    public Response getBatteryUsageDetails(@PathParam("nodeId") int nodeId) {
        return RestUtils.getResponse(Status.OK, this.getBatterUsage(nodeId));
    }

    private ArrayList<MetricsChartDataKeyValuesJson> getAllAfter(AGGREGATION_TYPE aggregationType, int sensorId) {
        MetricsAggregationBase metricsAggregationBase = new MetricsAggregationBase();
        Sensor sensor = DaoUtils.getSensorDao().get(sensorId); //Here sensorId means 'id'(db reference) not actual sensorId

        ArrayList<MetricsChartDataKeyValuesJson> finalData = new ArrayList<MetricsChartDataKeyValuesJson>();

        if (sensor.getMetricType() == null) {
            //Sensor pay load type not up to date
            _logger.debug("Payload type not updated in sensor.");
            return null;
        } else if (sensor.getMetricType() == MyMessages.PAYLOAD_TYPE.PL_DOUBLE.ordinal()) {
            _logger.debug("Payload type: {}", MyMessages.PAYLOAD_TYPE.PL_DOUBLE.toString());
            List<MetricsDoubleTypeDevice> metrics = metricsAggregationBase.getMetricsDoubleTypeAllAfter(
                    aggregationType, sensor);
            if (metrics == null) {
                //throw new ApiError("No data available");
                return null;
            }

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
        } else if (sensor.getMetricType() == MyMessages.PAYLOAD_TYPE.PL_BOOLEAN.ordinal()) {
            _logger.debug("Payload type: {}", MyMessages.PAYLOAD_TYPE.PL_BOOLEAN.toString());
            List<MetricsOnOffTypeDevice> metrics = metricsAggregationBase.getMetricsBooleanTypeAllAfter(
                    aggregationType, sensor);
            if (metrics == null) {
                //throw new ApiError("No data available");
                return null;
            }

            String name = sensor.getName() != null ? sensor.getName() : "State";
            MetricsChartDataKeyValuesJson minChartData = new MetricsChartDataKeyValuesJson(name);

            for (MetricsOnOffTypeDevice metric : metrics) {
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
