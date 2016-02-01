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

import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
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
    private static final long TRUNCATE_TRACE = TIME_REF.ONE_MINUTE * 15;
    private static final long TRUNCATE_NOTICE = TIME_REF.ONE_HOUR * 2;
    private static final long TRUNCATE_INFO = TIME_REF.ONE_HOUR * 6;
    private static final long TRUNCATE_WARNING = TIME_REF.ONE_HOUR * 12;
    private static final long TRUNCATE_ERROR = TIME_REF.ONE_DAY * 2;
    private static final long TRUNCATE_ALL = TIME_REF.ONE_DAY * 3;

    private void truncateSensorLogs() {

        ResourcesLogs resourcesLogs = ResourcesLogs.builder().build();

        //Truncate Trace
        resourcesLogs.setLogLevel(LOG_LEVEL.TRACE);
        resourcesLogs.setTimestamp(System.currentTimeMillis() - TRUNCATE_TRACE);
        DaoUtils.getResourcesLogsDao().deleteAll(resourcesLogs);

        //Truncate notice logs
        resourcesLogs.setLogLevel(LOG_LEVEL.NOTICE);
        resourcesLogs.setTimestamp(System.currentTimeMillis() - TRUNCATE_NOTICE);
        DaoUtils.getResourcesLogsDao().deleteAll(resourcesLogs);

        //Truncate info logs
        resourcesLogs.setLogLevel(LOG_LEVEL.INFO);
        resourcesLogs.setTimestamp(System.currentTimeMillis() - TRUNCATE_INFO);
        DaoUtils.getResourcesLogsDao().deleteAll(resourcesLogs);

        //Truncate warning logs
        resourcesLogs.setLogLevel(LOG_LEVEL.WARNING);
        resourcesLogs.setTimestamp(System.currentTimeMillis() - TRUNCATE_WARNING);
        DaoUtils.getResourcesLogsDao().deleteAll(resourcesLogs);

        //Truncate warning logs
        resourcesLogs.setLogLevel(LOG_LEVEL.ERROR);
        resourcesLogs.setTimestamp(System.currentTimeMillis() - TRUNCATE_ERROR);
        DaoUtils.getResourcesLogsDao().deleteAll(resourcesLogs);

        //truncate all data
        resourcesLogs.setLogLevel(null);
        resourcesLogs.setTimestamp(System.currentTimeMillis() - TRUNCATE_ALL);
        DaoUtils.getResourcesLogsDao().deleteAll(resourcesLogs);
    }

    @Override
    public void doRun() throws JobInterruptException {
        _logger.debug("Truncate ResourcesLogs job triggered...");
        truncateSensorLogs();
        _logger.debug("Truncate ResourcesLogs job completed...");
    }

}
