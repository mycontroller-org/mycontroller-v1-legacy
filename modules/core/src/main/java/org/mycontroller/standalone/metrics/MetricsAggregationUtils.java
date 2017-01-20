/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsCounterTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsGPSTypeDevice;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsAggregationUtils {

    //We have purge methods here, however we are using direct SQL query to purge data.
    //Have a look on DB_QUERY.java

    public static void removePreviousData(METRIC_TYPE metricType, AGGREGATION_TYPE type, long timestamp) {
        if (metricType == METRIC_TYPE.DOUBLE) {
            DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(
                    MetricsDoubleTypeDevice.builder().aggregationType(type).timestamp(timestamp).build());
        } else if (metricType == METRIC_TYPE.COUNTER) {
            DaoUtils.getMetricsCounterTypeDeviceDao().deletePrevious(
                    MetricsCounterTypeDevice.builder().aggregationType(type).timestamp(timestamp).build());
        }
    }

    public static void purgeRawData(METRIC_TYPE metricType) {
        purgeRawData(metricType, System.currentTimeMillis() - MetricsUtils.RAW_DATA_MAX_RETAIN_TIME);
    }

    public static void purgeOneMinuteData(METRIC_TYPE metricType) {
        purgeOneMinuteData(metricType, System.currentTimeMillis() - MetricsUtils.ONE_MINUTE_MAX_RETAIN_TIME);
    }

    public static void purgeFiveMinutesData(METRIC_TYPE metricType) {
        purgeFiveMinutesData(metricType, System.currentTimeMillis() - MetricsUtils.FIVE_MINUTES_MAX_RETAIN_TIME);
    }

    public static void purgeOneHourData(METRIC_TYPE metricType) {
        purgeOneHourData(metricType, System.currentTimeMillis() - MetricsUtils.ONE_HOUR_MAX_RETAIN_TIME);
    }

    public static void purgeRawData(METRIC_TYPE metricType, long timestamp) {
        removePreviousData(metricType, AGGREGATION_TYPE.RAW, timestamp);
    }

    public static void purgeOneMinuteData(METRIC_TYPE metricType, long timestamp) {
        removePreviousData(metricType, AGGREGATION_TYPE.ONE_MINUTE, timestamp);
    }

    public static void purgeFiveMinutesData(METRIC_TYPE metricType, long timestamp) {
        removePreviousData(metricType, AGGREGATION_TYPE.FIVE_MINUTES, timestamp);
    }

    public static void purgeOneHourData(METRIC_TYPE metricType, long timestamp) {
        removePreviousData(metricType, AGGREGATION_TYPE.ONE_HOUR, timestamp);
    }

    public static void purgeBinaryData(long timestamp) {
        DaoUtils.getMetricsBinaryTypeDeviceDao().deletePrevious(
                MetricsBinaryTypeDevice.builder().timestamp(timestamp).build());
    }

    public static void purgeGpsData(long timestamp) {
        DaoUtils.getMetricsGPSTypeDeviceDao().deletePrevious(
                MetricsGPSTypeDevice.builder().timestamp(timestamp).build());
    }
}
