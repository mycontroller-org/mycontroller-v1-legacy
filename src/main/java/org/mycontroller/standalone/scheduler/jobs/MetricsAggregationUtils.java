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
package org.mycontroller.standalone.scheduler.jobs;

import org.mycontroller.standalone.db.AGGREGATION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.TIME_REF;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsAggregationUtils {

    private MetricsAggregationUtils() {
    }

    public static void removePreviousData(AGGREGATION_TYPE type, long timestamp) {
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(
                new MetricsDoubleTypeDevice(type.ordinal(), timestamp));
    }

    public static void purgeRawData() {
        purgeRawData(System.currentTimeMillis() - TIME_REF.RAW_DATA_MAX_RETAIN_TIME);
    }

    public static void purgeOneMinuteData() {
        purgeOneMinuteData(System.currentTimeMillis() - TIME_REF.ONE_MINUTE_MAX_RETAIN_TIME);
    }

    public static void purgeFiveMinutesData() {
        purgeFiveMinutesData(System.currentTimeMillis() - TIME_REF.FIVE_MINUTES_MAX_RETAIN_TIME);
    }

    public static void purgeOneHourData() {
        purgeOneHourData(System.currentTimeMillis() - TIME_REF.ONE_HOUR_MAX_RETAIN_TIME);
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
