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
package org.mycontroller.standalone.metrics.jobs;

import java.sql.SQLException;
import java.text.MessageFormat;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DB_QUERY;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.metrics.METRIC_ENGINE;
import org.mycontroller.standalone.metrics.MetricsUtils;
import org.mycontroller.standalone.metrics.engines.McMetricsAggregationBase;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class MetricsAggregationJob extends Job {

    private void executeSqlQuery(String sqlQuery) {
        try {
            long startTime = System.currentTimeMillis();
            int deleteCount = DaoUtils.getMetricsDoubleTypeDeviceDao().getDao().executeRaw(sqlQuery);
            _logger.debug("Sql Query[{}], Deletion count:{}, Time taken:{} ms", sqlQuery, deleteCount,
                    System.currentTimeMillis() - startTime);
        } catch (SQLException ex) {
            _logger.error("Exception when executing query[{}] ", sqlQuery, ex);
        }
    }

    @Override
    public void doRun() throws JobInterruptException {
        if (MetricsUtils.type() != METRIC_ENGINE.MY_CONTROLLER) {
            //If some of external metric engine configured. no need to run internal metric aggregation
            return;
        }
        _logger.debug("Metrics aggregation job triggered");
        new McMetricsAggregationBase().runAggregation();

        _logger.debug("Executing purge of binary and GPS data.");
        //Execute purge of binary and gps data
        MetricsDataRetentionSettings retentionSettings = AppProperties.getInstance().getMetricsDataRetentionSettings();
        //Binary data
        String sqlDeleteQueryBinary = MessageFormat.format(DB_QUERY.getQuery(DB_QUERY.DELETE_METRICS_BINARY),
                String.valueOf(System.currentTimeMillis() - retentionSettings.getRetentionBinary()));
        executeSqlQuery(sqlDeleteQueryBinary);
        //GPS data
        String sqlDeleteQueryGPS = MessageFormat.format(DB_QUERY.getQuery(DB_QUERY.DELETE_METRICS_GPS),
                String.valueOf(System.currentTimeMillis() - retentionSettings.getRetentionGPS()));
        executeSqlQuery(sqlDeleteQueryGPS);
        _logger.debug("Metrics aggregation job completed");
    }
}
