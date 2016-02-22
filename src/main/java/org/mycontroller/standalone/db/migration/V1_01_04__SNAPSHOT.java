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
package org.mycontroller.standalone.db.migration;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.metrics.MetricsUtils;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class V1_01_04__SNAPSHOT extends MigrationBase implements JdbcMigration {
    private static final Logger _logger = LoggerFactory.getLogger(V1_01_04__SNAPSHOT.class.getName());

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        updateDao();

        /** Migration comments
         *  Description: add option to control metrics data retention
         *  1. remove old history from settings table 
         *  (last aggregation status moved under MetricsDataRetentionSettings)
         *  2. Metrics data retention limit available for user - add default options
         *  3. add default for last aggregation
         **/

        //Migration #1
        //Remove old history.       
        DaoUtils.getSettingsDao().delete(Settings.KEY_SUB_KEY,
                MetricsDataRetentionSettings.SKEY_LAST_AGGREGATION_RAW_DATA);
        DaoUtils.getSettingsDao().delete(Settings.KEY_SUB_KEY,
                MetricsDataRetentionSettings.SKEY_LAST_AGGREGATION_ONE_MINUTE);
        DaoUtils.getSettingsDao().delete(Settings.KEY_SUB_KEY,
                MetricsDataRetentionSettings.SKEY_LAST_AGGREGATION_FIVE_MINUTES);
        DaoUtils.getSettingsDao().delete(Settings.KEY_SUB_KEY,
                MetricsDataRetentionSettings.SKEY_LAST_AGGREGATION_ONE_HOUR);
        DaoUtils.getSettingsDao().delete(Settings.KEY_SUB_KEY,
                MetricsDataRetentionSettings.SKEY_LAST_AGGREGATION_SIX_HOURS);
        DaoUtils.getSettingsDao().delete(Settings.KEY_SUB_KEY,
                MetricsDataRetentionSettings.SKEY_LAST_AGGREGATION_TWELVE_HOURS);
        DaoUtils.getSettingsDao().delete(Settings.KEY_SUB_KEY,
                MetricsDataRetentionSettings.SKEY_LAST_AGGREGATION_ONE_DAY);

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

        //Migration #3
        //Update Metrics retention data reference
        MetricsDataRetentionSettings.builder()
                .lastAggregationRawData(System.currentTimeMillis())
                .lastAggregationOneMinute(System.currentTimeMillis())
                .lastAggregationFiveMinutes(System.currentTimeMillis())
                .lastAggregationOneHour(System.currentTimeMillis())
                .lastAggregationSixHours(System.currentTimeMillis())
                .lastAggregationTwelveHours(System.currentTimeMillis())
                .lastAggregationOneDay(System.currentTimeMillis())
                .build().updateInternal();

        _logger.info("Migration completed successfully.");
    }
}
