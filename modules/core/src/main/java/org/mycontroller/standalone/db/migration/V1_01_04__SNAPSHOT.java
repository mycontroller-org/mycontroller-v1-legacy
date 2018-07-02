/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.db.migration;

import java.sql.Connection;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.metrics.MetricsUtils;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.mycontroller.standalone.settings.SettingsUtils;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_01_04__SNAPSHOT extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description: add option to control metrics data retention
         *  1. copy old aggregations reference to new location
         *  2. remove old history from settings table
         *  (last aggregation status moved under MetricsDataRetentionSettings)
         *  3. Metrics data retention limit available for user - add default options
         **/

        //Migration #1
        //copy old aggregations reference to new location
        boolean doDelete = false;
        if (SettingsUtils.getValue("metrics", "lastAggregationRawData") != null) {
            MetricsDataRetentionSettings
                    .builder()
                    .lastAggregationRawData(
                            McUtils.getLong(SettingsUtils.getValue("metrics", "lastAggregationRawData")))
                    .lastAggregationOneMinute(
                            McUtils.getLong(SettingsUtils.getValue("metrics", "lastAggregationOneMinute")))
                    .lastAggregationFiveMinutes(
                            McUtils.getLong(SettingsUtils.getValue("metrics", "lastAggregationFiveMinutes")))
                    .lastAggregationOneHour(
                            McUtils.getLong(SettingsUtils.getValue("metrics", "lastAggregationOneHour")))
                    .lastAggregationSixHours(
                            McUtils.getLong(SettingsUtils.getValue("metrics", "lastAggregationSixHours")))
                    .lastAggregationTwelveHours(
                            McUtils.getLong(SettingsUtils.getValue("metrics", "lastAggregationTwelveHours")))
                    .lastAggregationOneDay(
                            McUtils.getLong(SettingsUtils.getValue("metrics", "lastAggregationOneDay")))
                    .build().updateInternal();
            doDelete = true;
        }

        //Migration #2
        //Remove old history.
        if (doDelete) {
            DaoUtils.getSettingsDao().deleteById(
                    SettingsUtils.getSettings("metrics", "lastAggregationRawData").getId());
            DaoUtils.getSettingsDao().deleteById(
                    SettingsUtils.getSettings("metrics", "lastAggregationOneMinute").getId());
            DaoUtils.getSettingsDao().deleteById(
                    SettingsUtils.getSettings("metrics", "lastAggregationFiveMinutes").getId());
            DaoUtils.getSettingsDao().deleteById(
                    SettingsUtils.getSettings("metrics", "lastAggregationOneHour").getId());
            DaoUtils.getSettingsDao().deleteById(
                    SettingsUtils.getSettings("metrics", "lastAggregationSixHours").getId());
            DaoUtils.getSettingsDao().deleteById(
                    SettingsUtils.getSettings("metrics", "lastAggregationTwelveHours").getId());
            DaoUtils.getSettingsDao()
                    .deleteById(SettingsUtils.getSettings("metrics", "lastAggregationOneDay").getId());
        }

        //Migration #2
        // Metrics data retention limit available for user
        //Update Metrics retention data reference
        MetricsDataRetentionSettings.builder()
                .retentionRawData(MetricsUtils.RAW_DATA_MAX_RETAIN_TIME)
                .retentionOneMinute(MetricsUtils.ONE_MINUTE_MAX_RETAIN_TIME)
                .retentionFiveMinutes(MetricsUtils.FIVE_MINUTES_MAX_RETAIN_TIME)
                .retentionOneHour(MetricsUtils.ONE_HOUR_MAX_RETAIN_TIME)
                .retentionSixHours(MetricsUtils.SIX_HOURS_MAX_RETAIN_TIME)
                .retentionTwelveHours(MetricsUtils.TWELVE_HOURS_MAX_RETAIN_TIME)
                .retentionOneDay(MetricsUtils.ONE_DAY_MAX_RETAIN_TIME)
                .build().save();

        _logger.info("Migration completed successfully.");
    }
}
