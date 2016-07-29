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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsAggregationUtils {

    public static void removePreviousData(AGGREGATION_TYPE type, long timestamp) {
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(
                MetricsDoubleTypeDevice.builder().aggregationType(type).timestamp(timestamp).build());
    }

    public static void purgeRawData() {
        purgeRawData(System.currentTimeMillis() - MetricsUtils.RAW_DATA_MAX_RETAIN_TIME);
    }

    public static void purgeOneMinuteData() {
        purgeOneMinuteData(System.currentTimeMillis() - MetricsUtils.ONE_MINUTE_MAX_RETAIN_TIME);
    }

    public static void purgeFiveMinutesData() {
        purgeFiveMinutesData(System.currentTimeMillis() - MetricsUtils.FIVE_MINUTES_MAX_RETAIN_TIME);
    }

    public static void purgeOneHourData() {
        purgeOneHourData(System.currentTimeMillis() - MetricsUtils.ONE_HOUR_MAX_RETAIN_TIME);
    }

    public static void purgeRawData(long timestamp) {
        removePreviousData(AGGREGATION_TYPE.RAW, timestamp);
    }

    public static void purgeOneMinuteData(long timestamp) {
        removePreviousData(AGGREGATION_TYPE.ONE_MINUTE, timestamp);
    }

    public static void purgeFiveMinutesData(long timestamp) {
        removePreviousData(AGGREGATION_TYPE.FIVE_MINUTES, timestamp);
    }

    public static void purgeOneHourData(long timestamp) {
        removePreviousData(AGGREGATION_TYPE.ONE_HOUR, timestamp);
    }
}
