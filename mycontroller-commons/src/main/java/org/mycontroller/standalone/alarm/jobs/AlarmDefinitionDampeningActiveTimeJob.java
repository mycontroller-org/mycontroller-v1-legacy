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
package org.mycontroller.standalone.alarm.jobs;

import java.util.List;

import org.mycontroller.standalone.alarm.AlarmEngine;
import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class AlarmDefinitionDampeningActiveTimeJob extends Job {
    private static boolean isRunning = false;
    private static final Logger _logger = LoggerFactory.getLogger(AlarmDefinitionDampeningActiveTimeJob.class
            .getName());

    @Override
    public void doRun() throws JobInterruptException {
        if (isRunning) {
            _logger.warn("Previous run of 'Alarm definition dampening active time job' is not completed! Terminating this job...");
        }
        isRunning = true;//Prevent to run multiple runs.
        try {
            List<AlarmDefinition> activeTimeDefinitions = DaoUtils.getAlarmDefinitionDao().getAll(
                    DAMPENING_TYPE.ACTIVE_TIME);
            _logger.debug("Number of definitions:{}, Alarm definitions:{}", activeTimeDefinitions.size(),
                    activeTimeDefinitions);
            //As already I'm in a thread, I will not start another thread. Going with normal method call
            AlarmEngine.builder().alarmDefinitions(activeTimeDefinitions).build().run();
        } catch (Exception ex) {
            _logger.error("Unable to run alarm dampening active time definitions, ", ex);
        }
        isRunning = false;
    }

}
