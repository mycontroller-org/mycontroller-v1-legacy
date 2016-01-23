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
package org.mycontroller.standalone.jobs;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xeiam.sundial.Job;
import com.xeiam.sundial.exceptions.JobInterruptException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ResourcesLogsAggregationJob extends Job {
    private static final Logger _logger = LoggerFactory.getLogger(ResourcesLogsAggregationJob.class.getName());
    private static final long TRUNCATE_SENSOR_LOG_BEFORE = NumericUtils.MINUTE * 30;
    private static final long TRUNCATE_SENSOR_LOG_OTHERS_BEFORE = NumericUtils.MINUTE * 30;
    private static final long TRUNCATE_ALARM_LOG_BEFORE = NumericUtils.HOUR * 12;
    private static final long TRUNCATE_TIMER_LOG_BEFORE = NumericUtils.HOUR * 12;
    private static final long TRUNCATE_ALL_OTHERS = NumericUtils.DAY * 3;

    private void truncateSensorLogs() {

        //Truncate Sensor related logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.SENSOR,
                System.currentTimeMillis() - TRUNCATE_SENSOR_LOG_BEFORE);

        //Truncate Sensor related logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.SENSOR_VARIABLE,
                System.currentTimeMillis() - TRUNCATE_SENSOR_LOG_OTHERS_BEFORE);

        //Truncate AlarmDefinition related logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.ALARM_DEFINITION,
                System.currentTimeMillis() - TRUNCATE_ALARM_LOG_BEFORE);

        //Truncate Scheduler related logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.TIMER,
                System.currentTimeMillis() - TRUNCATE_TIMER_LOG_BEFORE);

        //Remove other logs which is not listed here
        DaoUtils.getResourcesLogsDao().deleteAll(null, System.currentTimeMillis() - TRUNCATE_ALL_OTHERS);
    }

    @Override
    public void doRun() throws JobInterruptException {
        _logger.debug("Truncate ResourcesLogs job triggered...");
        truncateSensorLogs();
        _logger.debug("Truncate ResourcesLogs job completed...");
    }

}
