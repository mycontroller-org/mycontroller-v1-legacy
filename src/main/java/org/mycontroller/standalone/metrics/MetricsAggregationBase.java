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
package org.mycontroller.standalone.metrics;

import java.util.List;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.settings.MetricsSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsAggregationBase {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsAggregationBase.class.getName());

    private static boolean isAggregationRunning = false;

    public MetricsAggregationBase() {
    }

    private void aggregateForBucket(AGGREGATION_TYPE aggregationType, AGGREGATION_TYPE sourceAggregationType,
            Long fromTimestamp, Long toTimestamp) {
        if (aggregationType == null) {
            _logger.warn("Should create object with valid aggregation type!");
            return;
        }

        _logger.debug("Running aggregation on this time range[from:{}, to:{}, type:{}]", fromTimestamp, toTimestamp,
                aggregationType);

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
                        .min(NumericUtils.round(min, NumericUtils.DOUBLE_ROUND))
                        .max(NumericUtils.round(max, NumericUtils.DOUBLE_ROUND))
                        .avg(NumericUtils.round(avg, NumericUtils.DOUBLE_ROUND))
                        .samples(samples)
                        .timestamp(toTimestamp)
                        .build();
                DaoUtils.getMetricsDoubleTypeDeviceDao().create(metric);
            }
        }
        //Update last aggregation reference
        MetricsSettings metricsSettings = null;
        switch (aggregationType) {
        //One minute should handle raw data also
            case ONE_MINUTE:
                metricsSettings = MetricsSettings.builder()
                        .lastAggregationOneMinute(toTimestamp)
                        .lastAggregationRawData(toTimestamp)
                        .build();
                break;
            case FIVE_MINUTES:
                metricsSettings = MetricsSettings.builder().lastAggregationFiveMinutes(toTimestamp).build();
                break;
            case ONE_HOUR:
                metricsSettings = MetricsSettings.builder().lastAggregationOneHour(toTimestamp).build();
                break;
            case SIX_HOURS:
                metricsSettings = MetricsSettings.builder().lastAggregationSixHours(toTimestamp).build();
                break;
            case TWELVE_HOURS:
                metricsSettings = MetricsSettings.builder().lastAggregationTwelveHours(toTimestamp).build();
                break;
            case ONE_DAY:
                metricsSettings = MetricsSettings.builder().lastAggregationOneDay(toTimestamp).build();
                break;
            default:
                break;
        }
        if (metricsSettings != null) {
            metricsSettings.updateInternal();
            metricsSettings = MetricsSettings.get();
            ObjectFactory.getAppProperties().setMetricsSettings(metricsSettings);
            _logger.debug("Metrics settings update successfully! new settings:{}", metricsSettings);
        } else {
            _logger.warn("metricsSettings is null cannot update");
        }
    }

    private void executeBucketByBucket(AGGREGATION_TYPE aggregationType, AGGREGATION_TYPE sourceAggregationType,
            Long fromTimestamp, Long toTimestamp,
            Long bucketDuration) {
        //Complete for all missed and current time
        while ((fromTimestamp + bucketDuration) <= toTimestamp) {
            //Call aggregation
            aggregateForBucket(aggregationType, sourceAggregationType, fromTimestamp, (fromTimestamp + bucketDuration));
            //Add bucket duration
            fromTimestamp += bucketDuration;
        }
    }

    public void runAggregation() {
        //Check is there any previous run going on
        if (isAggregationRunning) {
            _logger.warn("Already a aggregation is running. Cannot run now!");
            return;
        }
        //set aggregation started
        setAggregationRunning(true);
        //Run aggregation one bye in order..
        //run aggregation for one minute
        executeBucketByBucket(AGGREGATION_TYPE.ONE_MINUTE, AGGREGATION_TYPE.RAW, ObjectFactory.getAppProperties()
                .getMetricsSettings().getLastAggregationOneMinute(), getToTime(AGGREGATION_TYPE.ONE_MINUTE),
                TIME_REF.ONE_MINUTE);
        //run aggregation for five minutes
        executeBucketByBucket(AGGREGATION_TYPE.FIVE_MINUTES, AGGREGATION_TYPE.ONE_MINUTE, ObjectFactory
                .getAppProperties().getMetricsSettings().getLastAggregationFiveMinutes(),
                getToTime(AGGREGATION_TYPE.FIVE_MINUTES), TIME_REF.FIVE_MINUTES);
        //run aggregation for one hour
        executeBucketByBucket(AGGREGATION_TYPE.ONE_HOUR, AGGREGATION_TYPE.FIVE_MINUTES, ObjectFactory
                .getAppProperties().getMetricsSettings().getLastAggregationOneHour(),
                getToTime(AGGREGATION_TYPE.ONE_HOUR), TIME_REF.ONE_HOUR);
        //run aggregation for six hours
        executeBucketByBucket(AGGREGATION_TYPE.SIX_HOURS, AGGREGATION_TYPE.ONE_HOUR, ObjectFactory.getAppProperties()
                .getMetricsSettings().getLastAggregationSixHours(), getToTime(AGGREGATION_TYPE.SIX_HOURS),
                (TIME_REF.ONE_HOUR * 6));
        //run aggregation for twelve hours
        executeBucketByBucket(AGGREGATION_TYPE.TWELVE_HOURS, AGGREGATION_TYPE.SIX_HOURS, ObjectFactory
                .getAppProperties().getMetricsSettings().getLastAggregationTwelveHours(),
                getToTime(AGGREGATION_TYPE.TWELVE_HOURS), (TIME_REF.ONE_HOUR * 12));
        //run aggregation for one day
        executeBucketByBucket(AGGREGATION_TYPE.ONE_DAY, AGGREGATION_TYPE.TWELVE_HOURS, ObjectFactory
                .getAppProperties().getMetricsSettings().getLastAggregationOneDay(),
                getToTime(AGGREGATION_TYPE.ONE_DAY), TIME_REF.ONE_DAY);

        //Start purge job
        purgeMetricTables();

        //set aggregation completed
        setAggregationRunning(false);
    }

    public static Long getToTime(AGGREGATION_TYPE aggregationType) {
        switch (aggregationType) {
            case ONE_MINUTE:
                return System.currentTimeMillis() - MetricsUtils.RAW_DATA_MAX_RETAIN_TIME;
            case FIVE_MINUTES:
                return System.currentTimeMillis() - MetricsUtils.ONE_MINUTE_MAX_RETAIN_TIME;
            case ONE_HOUR:
                return System.currentTimeMillis() - MetricsUtils.FIVE_MINUTES_MAX_RETAIN_TIME;
            case SIX_HOURS:
                return System.currentTimeMillis() - MetricsUtils.ONE_HOUR_MAX_RETAIN_TIME;
            case TWELVE_HOURS:
                return System.currentTimeMillis() - MetricsUtils.SIX_HOURS_MAX_RETAIN_TIME;
            case ONE_DAY:
                return System.currentTimeMillis() - MetricsUtils.TWELVE_HOURS_MAX_RETAIN_TIME;
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

    /** Get metric data for boolean type */

    public static List<MetricsBinaryTypeDevice> getMetricsBinaryData(SensorVariable sensorVariable, Long fromTimestamp) {
        MetricsBinaryTypeDevice binaryTypeDevice = MetricsBinaryTypeDevice.builder()
                .sensorVariable(sensorVariable).build();
        if (fromTimestamp != null) {
            binaryTypeDevice.setTimestampFrom(fromTimestamp);
        }
        return DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(binaryTypeDevice);
    }

    private void purgeMetricTables() {
        //remove data one by one
        MetricsSettings metricsSettings = ObjectFactory.getAppProperties().getMetricsSettings();
        MetricsDoubleTypeDevice metric = MetricsDoubleTypeDevice.builder()
                .aggregationType(AGGREGATION_TYPE.RAW)
                .timestamp(metricsSettings.getLastAggregationOneMinute())
                .build();
        //Delete raw data
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metric);
        //Delete One minute data
        metric.setAggregationType(AGGREGATION_TYPE.ONE_MINUTE);
        metric.setTimestamp(metricsSettings.getLastAggregationFiveMinutes());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metric);

        //Delete five minutes data
        metric.setAggregationType(AGGREGATION_TYPE.FIVE_MINUTES);
        metric.setTimestamp(metricsSettings.getLastAggregationOneHour());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metric);

        //Delete One hour data
        metric.setAggregationType(AGGREGATION_TYPE.ONE_HOUR);
        metric.setTimestamp(metricsSettings.getLastAggregationSixHours());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metric);

        //Delete six hours data
        metric.setAggregationType(AGGREGATION_TYPE.SIX_HOURS);
        metric.setTimestamp(metricsSettings.getLastAggregationTwelveHours());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metric);

        //Delete twelve hours data
        metric.setAggregationType(AGGREGATION_TYPE.TWELVE_HOURS);
        metric.setTimestamp(metricsSettings.getLastAggregationOneDay());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metric);

        //never delete one day data

    }

    public static synchronized boolean isAggregationRunning() {
        return isAggregationRunning;
    }

    public static synchronized void setAggregationRunning(boolean isAggregationRunning) {
        MetricsAggregationBase.isAggregationRunning = isAggregationRunning;
    }
}
