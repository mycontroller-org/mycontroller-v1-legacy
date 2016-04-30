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
package org.mycontroller.standalone.metrics;

import java.util.Date;
import java.util.List;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.mycontroller.standalone.utils.McUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor
public class MetricsAggregationBase {

    private static boolean isAggregationRunning = false;

    private void aggregateForBucketSensorVariable(AGGREGATION_TYPE aggregationType,
            AGGREGATION_TYPE sourceAggregationType,
            Long fromTimestamp, Long toTimestamp) {
        if (aggregationType == null) {
            _logger.warn("Should create object with valid aggregation type!");
            return;
        }
        _logger.debug("Running aggregation on this time range[from:{}, to:{}, type:{}]", fromTimestamp,
                toTimestamp, aggregationType);
        List<MetricsDoubleTypeDevice> variableIds = DaoUtils.getMetricsDoubleTypeDeviceDao()
                .getAggregationRequiredVariableIds(sourceAggregationType, fromTimestamp, toTimestamp);

        if (variableIds == null || variableIds.isEmpty()) {
            _logger.debug("No values available for aggregation on this time range");
            //Nothing to do just return from here.
            return;
        }
        _logger.debug("Number of sensor variables:{}", variableIds.size());

        //calculate metrics one by one (variable)
        for (MetricsDoubleTypeDevice sensorVariable : variableIds) {
            //Collect past data for last X seconds,minutes, etc., based on 'AGGREGATON_TYPE'
            List<MetricsDoubleTypeDevice> metrics = DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(
                    MetricsDoubleTypeDevice.builder()
                            .aggregationType(sourceAggregationType)
                            .sensorVariable(sensorVariable.getSensorVariable())
                            .timestampFrom(fromTimestamp)
                            .timestampTo(toTimestamp).build());
            _logger.debug("Metrics:{}", metrics);
            //Calculate Metrics
            if (metrics.size() > 0) {
                int samples = 0;
                //If it's one minute aggregation type,
                //it's from RAW data, so size of metrics is a total number of samples
                //In raw metrics data 'average' data called real data
                if (aggregationType == AGGREGATION_TYPE.ONE_MINUTE) {
                    samples = metrics.size();
                }
                _logger.debug("Number of records:{}", metrics.size());
                Double min = Double.MAX_VALUE;  //Possible positive highest double value
                Double max = Double.NEGATIVE_INFINITY;//Take lowest double number, MIN_VALUE, doesn't work.
                Double sum = 0D;
                for (MetricsDoubleTypeDevice metric : metrics) {
                    //for one minute data, taking from raw data.
                    //final result: Min, Max, Avg and samples
                    if (aggregationType == AGGREGATION_TYPE.ONE_MINUTE) {
                        if (metric.getAvg() > max) {
                            max = metric.getAvg();
                        }

                        if (metric.getAvg() < min) {
                            min = metric.getAvg();
                        }
                        sum = sum + metric.getAvg();
                    } else {
                        //for other than one minute data, calculate with max, min, avg and previous samples
                        if (metric.getMax() > max) {
                            max = metric.getMax();
                        }

                        if (metric.getMin() < min) {
                            min = metric.getMin();
                        }
                        sum = sum + (metric.getAvg() * metric.getSamples());
                        samples = samples + metric.getSamples();
                    }
                }
                Double avg = sum / samples;
                MetricsDoubleTypeDevice metric = MetricsDoubleTypeDevice.builder()
                        .aggregationType(aggregationType)
                        .sensorVariable(sensorVariable.getSensorVariable())
                        .min(McUtils.round(min, McUtils.DOUBLE_ROUND))
                        .max(McUtils.round(max, McUtils.DOUBLE_ROUND))
                        .avg(McUtils.round(avg, McUtils.DOUBLE_ROUND))
                        .samples(samples)
                        .timestamp(toTimestamp)
                        .build();
                DaoUtils.getMetricsDoubleTypeDeviceDao().create(metric);
            }
        }
    }

    private void aggregateForBucketBattery(AGGREGATION_TYPE aggregationType, AGGREGATION_TYPE sourceAggregationType,
            Long fromTimestamp, Long toTimestamp) {
        if (aggregationType == null) {
            _logger.warn("Should create object with valid aggregation type!");
            return;
        }
        _logger.debug("Running aggregation on this time range[from:{}, to:{}, type:{}]", fromTimestamp,
                toTimestamp, aggregationType);

        List<MetricsBatteryUsage> nodeIds = DaoUtils.getMetricsBatteryUsageDao()
                .getAggregationRequiredNodeIds(sourceAggregationType, fromTimestamp, toTimestamp);

        if (nodeIds == null || nodeIds.isEmpty()) {
            _logger.debug("No values available for aggregation on this time range");
            //Nothing to do just return from here.
            return;
        }
        _logger.debug("Number of nodes:{}", nodeIds.size());

        //calculate metrics one by one (variable)
        for (MetricsBatteryUsage node : nodeIds) {
            //Collect past data for last X seconds,minutes, etc., based on 'AGGREGATON_TYPE'
            List<MetricsBatteryUsage> metrics = DaoUtils.getMetricsBatteryUsageDao().getAll(
                    MetricsBatteryUsage.builder()
                            .aggregationType(sourceAggregationType)
                            .node(node.getNode())
                            .timestampFrom(fromTimestamp)
                            .timestampTo(toTimestamp).build());
            _logger.debug("Metrics:{}", metrics);
            //Calculate Metrics
            if (metrics.size() > 0) {
                int samples = 0;
                //If it's one minute aggregation type,
                //it's from RAW data, so size of metrics is a total number of samples
                //In raw metrics data 'average' data called real data
                if (aggregationType == AGGREGATION_TYPE.ONE_MINUTE) {
                    samples = metrics.size();
                }
                _logger.debug("Number of records:{}", metrics.size());
                Double min = Double.MAX_VALUE;  //Possible positive highest double value
                Double max = Double.NEGATIVE_INFINITY;//Take lowest double number, MIN_VALUE, doesn't work.
                Double sum = 0D;
                for (MetricsBatteryUsage metric : metrics) {
                    //for one minute data, taking from raw data.
                    //final result: Min, Max, Avg and samples
                    if (aggregationType == AGGREGATION_TYPE.ONE_MINUTE) {
                        if (metric.getAvg() > max) {
                            max = metric.getAvg();
                        }

                        if (metric.getAvg() < min) {
                            min = metric.getAvg();
                        }
                        sum = sum + metric.getAvg();
                    } else {
                        //for other than one minute data, calculate with max, min, avg and previous samples
                        if (metric.getMax() > max) {
                            max = metric.getMax();
                        }

                        if (metric.getMin() < min) {
                            min = metric.getMin();
                        }
                        sum = sum + (metric.getAvg() * metric.getSamples());
                        samples = samples + metric.getSamples();
                    }
                }
                Double avg = sum / samples;
                MetricsBatteryUsage metric = MetricsBatteryUsage.builder()
                        .aggregationType(aggregationType)
                        .node(node.getNode())
                        .min(McUtils.round(min, McUtils.DOUBLE_ROUND))
                        .max(McUtils.round(max, McUtils.DOUBLE_ROUND))
                        .avg(McUtils.round(avg, McUtils.DOUBLE_ROUND))
                        .samples(samples)
                        .timestamp(toTimestamp)
                        .build();
                DaoUtils.getMetricsBatteryUsageDao().create(metric);
            }
        }
    }

    private void executeBucketByBucket(AGGREGATION_TYPE aggregationType, AGGREGATION_TYPE sourceAggregationType,
            Long fromTimestamp, Long toTimestamp, Long bucketDuration) {
        _logger.debug("Aggregation Type:{}, FromTime:{}, ToTime:{}, bucketDuration:{}", aggregationType, new Date(
                fromTimestamp), new Date(toTimestamp), McUtils.getFriendlyTime(bucketDuration, true));

        //Complete for all missed and current time
        while ((fromTimestamp + bucketDuration) <= toTimestamp) {

            //Call aggregation double data (sensor variables)
            //-----------------------------------------------
            aggregateForBucketSensorVariable(aggregationType, sourceAggregationType,
                    fromTimestamp, (fromTimestamp + bucketDuration));

            //Call aggregation for battery usage
            //----------------------------------
            aggregateForBucketBattery(aggregationType, sourceAggregationType,
                    fromTimestamp, (fromTimestamp + bucketDuration));

            //Update last aggregation status
            //-----------------------------------
            MetricsDataRetentionSettings dataRetentionSettings = null;
            switch (aggregationType) {
            //One minute should handle raw data also
                case ONE_MINUTE:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationOneMinute((fromTimestamp + bucketDuration))
                            .lastAggregationRawData((fromTimestamp + bucketDuration))
                            .build();
                    break;
                case FIVE_MINUTES:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationFiveMinutes((fromTimestamp + bucketDuration)).build();
                    break;
                case ONE_HOUR:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationOneHour((fromTimestamp + bucketDuration)).build();
                    break;
                case SIX_HOURS:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationSixHours((fromTimestamp + bucketDuration)).build();
                    break;
                case TWELVE_HOURS:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationTwelveHours((fromTimestamp + bucketDuration)).build();
                    break;
                case ONE_DAY:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationOneDay((fromTimestamp + bucketDuration)).build();
                    break;
                default:
                    break;
            }
            if (dataRetentionSettings != null) {
                dataRetentionSettings.updateInternal();
                dataRetentionSettings = MetricsDataRetentionSettings.get();
                AppProperties.getInstance().setMetricsDataRetentionSettings(dataRetentionSettings);
                _logger.debug(
                        "Metrics settings update successfully! New referances, Last aggregation:[Raw:{}, "
                                + "OneMinute:{}, FiveMinute:{}, "
                                + "OneHour:{}, SixHours:{}, TwelveHours:{}, OneDay:{}]",
                        dataRetentionSettings.getLastAggregationRawData(),
                        dataRetentionSettings.getLastAggregationOneMinute(),
                        dataRetentionSettings.getLastAggregationFiveMinutes(),
                        dataRetentionSettings.getLastAggregationOneHour(),
                        dataRetentionSettings.getLastAggregationSixHours(),
                        dataRetentionSettings.getLastAggregationTwelveHours(),
                        dataRetentionSettings.getLastAggregationOneDay());
            } else {
                _logger.warn("metricsSettings is null cannot update");
            }

            //Add bucket duration
            //-----------------------------
            fromTimestamp += bucketDuration;
        }
    }

    public void runAggregation() {
        //Check is there any previous run going on
        if (isAggregationRunning) {
            _logger.warn("Already a aggregation is running. Cannot run now!");
            return;
        }
        _logger.debug("Data retention settings:{}", AppProperties.getInstance()
                .getMetricsDataRetentionSettings());
        try {
            //set aggregation started
            setAggregationRunning(true);
            //Run aggregation one bye in order..
            //run aggregation for one minute
            executeBucketByBucket(AGGREGATION_TYPE.ONE_MINUTE, AGGREGATION_TYPE.RAW, AppProperties.getInstance()
                    .getMetricsDataRetentionSettings().getLastAggregationOneMinute(),
                    getToTime(AGGREGATION_TYPE.ONE_MINUTE), McUtils.ONE_MINUTE);
            //run aggregation for five minutes
            executeBucketByBucket(AGGREGATION_TYPE.FIVE_MINUTES, AGGREGATION_TYPE.ONE_MINUTE,
                    AppProperties.getInstance().getMetricsDataRetentionSettings().getLastAggregationFiveMinutes(),
                    getToTime(AGGREGATION_TYPE.FIVE_MINUTES), McUtils.FIVE_MINUTES);
            //run aggregation for one hour
            executeBucketByBucket(AGGREGATION_TYPE.ONE_HOUR, AGGREGATION_TYPE.FIVE_MINUTES,
                    AppProperties.getInstance().getMetricsDataRetentionSettings().getLastAggregationOneHour(),
                    getToTime(AGGREGATION_TYPE.ONE_HOUR), McUtils.ONE_HOUR);
            //run aggregation for six hours
            executeBucketByBucket(AGGREGATION_TYPE.SIX_HOURS, AGGREGATION_TYPE.ONE_HOUR,
                    AppProperties.getInstance()
                            .getMetricsDataRetentionSettings().getLastAggregationSixHours(),
                    getToTime(AGGREGATION_TYPE.SIX_HOURS),
                    (McUtils.ONE_HOUR * 6));
            //run aggregation for twelve hours
            executeBucketByBucket(AGGREGATION_TYPE.TWELVE_HOURS, AGGREGATION_TYPE.SIX_HOURS,
                    AppProperties.getInstance().getMetricsDataRetentionSettings().getLastAggregationTwelveHours(),
                    getToTime(AGGREGATION_TYPE.TWELVE_HOURS), (McUtils.ONE_HOUR * 12));
            //run aggregation for one day
            executeBucketByBucket(AGGREGATION_TYPE.ONE_DAY, AGGREGATION_TYPE.TWELVE_HOURS,
                    AppProperties.getInstance().getMetricsDataRetentionSettings().getLastAggregationOneDay(),
                    getToTime(AGGREGATION_TYPE.ONE_DAY), McUtils.ONE_DAY);

            //Start purge job
            purgeMetricTables();

        } finally {
            //set aggregation completed
            setAggregationRunning(false);
        }
    }

    public static Long getToTime(AGGREGATION_TYPE aggregationType) {
        switch (aggregationType) {
            case ONE_MINUTE:
                return System.currentTimeMillis()
                        - AppProperties.getInstance().getMetricsDataRetentionSettings().getRetentionRawData();
            case FIVE_MINUTES:
                return System.currentTimeMillis()
                        - AppProperties.getInstance().getMetricsDataRetentionSettings().getRetentionOneMinute();
            case ONE_HOUR:
                return System.currentTimeMillis() - AppProperties.getInstance().
                        getMetricsDataRetentionSettings().getRetentionFiveMinutes();
            case SIX_HOURS:
                return System.currentTimeMillis()
                        - AppProperties.getInstance().getMetricsDataRetentionSettings().getRetentionOneHour();
            case TWELVE_HOURS:
                return System.currentTimeMillis()
                        - AppProperties.getInstance().getMetricsDataRetentionSettings().getRetentionSixHours();
            case ONE_DAY:
                return System.currentTimeMillis() - AppProperties.getInstance().
                        getMetricsDataRetentionSettings().getRetentionTwelveHours();
            default:
                return null;
        }
    }

    public static List<MetricsDoubleTypeDevice> getMetricsDoubleData(SensorVariable sensorVariable,
            Long fromTimestamp,
            Long toTimestamp) {
        return DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(
                MetricsDoubleTypeDevice.builder()
                        .sensorVariable(sensorVariable)
                        .timestampFrom(fromTimestamp)
                        .timestampTo(toTimestamp).build());
    }

    public static List<MetricsBatteryUsage> getMetricsBatteryUsage(Node node,
            Long fromTimestamp,
            Long toTimestamp) {
        return DaoUtils.getMetricsBatteryUsageDao().getAll(
                MetricsBatteryUsage.builder()
                        .node(node)
                        .timestampFrom(fromTimestamp)
                        .timestampTo(toTimestamp).build());
    }

    //Get metric data for boolean type
    public static List<MetricsBinaryTypeDevice> getMetricsBinaryData(
            SensorVariable sensorVariable,
            Long fromTimestamp) {
        MetricsBinaryTypeDevice binaryTypeDevice = MetricsBinaryTypeDevice.builder()
                .sensorVariable(sensorVariable).build();
        if (fromTimestamp != null) {
            binaryTypeDevice.setTimestampFrom(fromTimestamp);
        }
        return DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(binaryTypeDevice);
    }

    private void purgeMetricTables() {
        //remove data one by one
        MetricsDataRetentionSettings dataRetentionSettings = AppProperties.getInstance()
                .getMetricsDataRetentionSettings();

        //For double data
        MetricsDoubleTypeDevice metricDouble = MetricsDoubleTypeDevice.builder()
                .aggregationType(AGGREGATION_TYPE.RAW)
                .timestamp(dataRetentionSettings.getLastAggregationOneMinute())
                .build();

        //Delete raw data
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble);
        //Delete One minute data
        metricDouble.setAggregationType(AGGREGATION_TYPE.ONE_MINUTE);
        metricDouble.setTimestamp(dataRetentionSettings.getLastAggregationFiveMinutes());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble);

        //Delete five minutes data
        metricDouble.setAggregationType(AGGREGATION_TYPE.FIVE_MINUTES);
        metricDouble.setTimestamp(dataRetentionSettings.getLastAggregationOneHour());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble);

        //Delete One hour data
        metricDouble.setAggregationType(AGGREGATION_TYPE.ONE_HOUR);
        metricDouble.setTimestamp(dataRetentionSettings.getLastAggregationSixHours());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble);

        //Delete six hours data
        metricDouble.setAggregationType(AGGREGATION_TYPE.SIX_HOURS);
        metricDouble.setTimestamp(dataRetentionSettings.getLastAggregationTwelveHours());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble);

        //Delete twelve hours data
        metricDouble.setAggregationType(AGGREGATION_TYPE.TWELVE_HOURS);
        metricDouble.setTimestamp(dataRetentionSettings.getLastAggregationOneDay());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble);
        //--------

        //Delete one day day data
        metricDouble.setAggregationType(AGGREGATION_TYPE.ONE_DAY);
        metricDouble.setTimestamp(System.currentTimeMillis() - dataRetentionSettings.getRetentionOneDay());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble);

        //---------------------------------------------------------------
        //For battery usage
        MetricsBatteryUsage metricBattery = MetricsBatteryUsage.builder()
                .aggregationType(AGGREGATION_TYPE.RAW)
                .timestamp(dataRetentionSettings.getLastAggregationOneMinute())
                .build();

        //Delete raw data
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(metricBattery);
        //Delete One minute data
        metricBattery.setAggregationType(AGGREGATION_TYPE.ONE_MINUTE);
        metricBattery.setTimestamp(dataRetentionSettings.getLastAggregationFiveMinutes());
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(metricBattery);

        //Delete five minutes data
        metricBattery.setAggregationType(AGGREGATION_TYPE.FIVE_MINUTES);
        metricBattery.setTimestamp(dataRetentionSettings.getLastAggregationOneHour());
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(metricBattery);

        //Delete One hour data
        metricBattery.setAggregationType(AGGREGATION_TYPE.ONE_HOUR);
        metricBattery.setTimestamp(dataRetentionSettings.getLastAggregationSixHours());
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(metricBattery);

        //Delete six hours data
        metricBattery.setAggregationType(AGGREGATION_TYPE.SIX_HOURS);
        metricBattery.setTimestamp(dataRetentionSettings.getLastAggregationTwelveHours());
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(metricBattery);

        //Delete twelve hours data
        metricBattery.setAggregationType(AGGREGATION_TYPE.TWELVE_HOURS);
        metricBattery.setTimestamp(dataRetentionSettings.getLastAggregationOneDay());
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(metricBattery);
        //---------------------------------------------------------------------

        //Delete one day day data
        metricBattery.setAggregationType(AGGREGATION_TYPE.ONE_DAY);
        metricBattery.setTimestamp(System.currentTimeMillis() - dataRetentionSettings.getRetentionOneDay());
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(metricBattery);

    }

    public static synchronized boolean isAggregationRunning() {
        return isAggregationRunning;
    }

    public static synchronized void setAggregationRunning(boolean isAggregationRunning) {
        MetricsAggregationBase.isAggregationRunning = isAggregationRunning;
    }
}
