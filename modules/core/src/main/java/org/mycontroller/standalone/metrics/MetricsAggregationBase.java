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

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DB_QUERY;
import org.mycontroller.standalone.db.DaoUtils;
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

    public static final AtomicBoolean IS_AGGREGATION_RUNNING = new AtomicBoolean(false);

    private void aggregateAndInsertForBucketDuration(String sourceType, String resultType,
            Long timestampFrom, Long timestampTo, String insertSqlQuery, String deletionSqlQuery) {
        if (sourceType == null || resultType == null || timestampFrom == null || timestampTo == null
                || insertSqlQuery == null) {
            _logger.warn(
                    "Null values are not allowed! sourceType:{}, resultType:{}, timestampFrom:{},"
                            + " timestampTo:{}, insertSqlQuery:{}",
                    sourceType, resultType, timestampFrom, timestampTo, insertSqlQuery);
            return;
        }
        String sqlInsertQuery = MessageFormat.format(insertSqlQuery, sourceType, String.valueOf(timestampFrom),
                String.valueOf(timestampTo), resultType);
        String sqlDeleteQuery = MessageFormat.format(deletionSqlQuery, sourceType, String.valueOf(timestampTo));
        _logger.debug(
                "Running aggregation and data removal for this time range[from:{}, to:{}, sourceType:{},"
                        + " resultType:{}], SQL query: Insert:[{}], Deletion:[{}]", timestampFrom, timestampTo,
                sourceType, resultType, sqlInsertQuery, sqlDeleteQuery);
        try {
            //Aggregate and insert data
            int insertCount = DaoUtils.getMetricsDoubleTypeDeviceDao().getDao().executeRaw(sqlInsertQuery);
            //Remove aggregated data
            int deleteCount = DaoUtils.getMetricsDoubleTypeDeviceDao().getDao().executeRaw(sqlDeleteQuery);
            _logger.debug("Query execution result counts >> [insert:{}, delete:{}]", insertCount, deleteCount);
        } catch (Exception ex) {
            _logger.error("Error,", ex);
        }
    }

    private void executeBucketByBucket(AGGREGATION_TYPE resultType, AGGREGATION_TYPE sourceType,
            Long timestampFrom, Long timestampTo, Long bucketDuration) {
        _logger.debug("sourceType:{}, resultType:{}, timestampFrom:{}, timestampTo:{}, bucketDuration:{} ms",
                sourceType, resultType, timestampFrom, timestampTo, bucketDuration);

        //Complete for all missed and current time
        while ((timestampFrom + bucketDuration) <= timestampTo) {

            //Call aggregation double data (sensor variables)
            //-----------------------------------------------
            aggregateAndInsertForBucketDuration(String.valueOf(sourceType.ordinal()),
                    String.valueOf(resultType.ordinal()), timestampFrom, (timestampFrom + bucketDuration),
                    DB_QUERY.INSERT_METRICS_DOUBLE_AGGREGATION_BY_TYPE, DB_QUERY.DELETE_METRICS_DOUBLE_BY_TYPE);

            //Call aggregation for battery usage
            //----------------------------------
            aggregateAndInsertForBucketDuration(String.valueOf(sourceType.ordinal()),
                    String.valueOf(resultType.ordinal()), timestampFrom, (timestampFrom + bucketDuration),
                    DB_QUERY.INSERT_METRICS_BATTERY_AGGREGATION_BY_TYPE, DB_QUERY.DELETE_METRICS_BATTERY_BY_TYPE);

            //Call aggregation counter data (sensor variables)
            //-----------------------------------------------
            aggregateAndInsertForBucketDuration(sourceType.name(), resultType.name(), timestampFrom,
                    (timestampFrom + bucketDuration), DB_QUERY.INSERT_METRICS_COUNTER_AGGREGATION_BY_TYPE,
                    DB_QUERY.DELETE_METRICS_COUNTER_BY_TYPE);

            //Update last aggregation status
            //-----------------------------------
            MetricsDataRetentionSettings dataRetentionSettings = null;
            switch (resultType) {
            //One minute should handle raw data also
                case ONE_MINUTE:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationOneMinute((timestampFrom + bucketDuration))
                            .lastAggregationRawData((timestampFrom + bucketDuration))
                            .build();
                    break;
                case FIVE_MINUTES:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationFiveMinutes((timestampFrom + bucketDuration)).build();
                    break;
                case ONE_HOUR:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationOneHour((timestampFrom + bucketDuration)).build();
                    break;
                case SIX_HOURS:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationSixHours((timestampFrom + bucketDuration)).build();
                    break;
                case TWELVE_HOURS:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationTwelveHours((timestampFrom + bucketDuration)).build();
                    break;
                case ONE_DAY:
                    dataRetentionSettings = MetricsDataRetentionSettings.builder()
                            .lastAggregationOneDay((timestampFrom + bucketDuration)).build();
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
            timestampFrom += bucketDuration;
        }
    }

    public void runAggregation() {
        //Check is there any previous run going on
        if (IS_AGGREGATION_RUNNING.get()) {
            _logger.warn("Already a aggregation job is running. Cannot run now!");
            return;
        }
        _logger.debug("Data retention settings:{}", AppProperties.getInstance()
                .getMetricsDataRetentionSettings());
        try {
            //set aggregation started
            IS_AGGREGATION_RUNNING.set(true);
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

        } finally {
            //set aggregation completed
            IS_AGGREGATION_RUNNING.set(false);
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
}
