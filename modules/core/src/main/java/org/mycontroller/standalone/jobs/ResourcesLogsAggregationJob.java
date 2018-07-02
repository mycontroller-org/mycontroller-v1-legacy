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
package org.mycontroller.standalone.jobs;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class ResourcesLogsAggregationJob extends Job {
    public static final long DEFAULT_RETENTION_DURATION = McUtils.HOUR;

    private void truncateSensorLogs() {
        ResourcesLogs resourcesLogs = ResourcesLogs.builder().build();
        resourcesLogs.setTimestamp(System.currentTimeMillis()
                - AppProperties.getInstance().getControllerSettings().getResourcesLogsRetentionDuration());
        DaoUtils.getResourcesLogsDao().deleteAll(resourcesLogs);
    }

    @Override
    public void doRun() throws JobInterruptException {
        _logger.debug("Truncate ResourcesLogs job triggered...");
        truncateSensorLogs();
        _logger.debug("Truncate ResourcesLogs job completed...");
    }

}
