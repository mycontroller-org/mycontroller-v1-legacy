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
package org.mycontroller.standalone.timer.jobs;

import java.util.Map;

import org.knowm.sundial.Job;
import org.knowm.sundial.JobContext;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.operation.model.OperationSendPayload;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class TimerJob extends Job {
    public static final String TIMER_REF = "timer_referance_key";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_PAYLOAD = "payload";

    private void executeTimer(Timer timer, Map<String, Object> properties) {
        if (timer.getId() != null) {
            if (timer.getTimerType() == TIMER_TYPE.SIMPLE) {
                TimerSimple timerSimple = new TimerSimple(timer);
                timerSimple.incrementExecutedCount();
                timerSimple.update();
            }
            _logger.debug("Operations going to execute:{}", timer.getOperations());
            for (Operation operation : timer.getOperations()) {
                try {
                    operation.execute(timer);
                } catch (Exception ex) {
                    _logger.error("Exception,", ex);
                }

            }
        } else if (timer.getTimerType() == TIMER_TYPE.SIMPLE) {
            TimerSimple timerSimple = new TimerSimple(timer);
            //Do not allow to run than one time, if it's repeat count set to 1
            if (timerSimple.getRepeatCount() == 1) {
                SchedulerUtils.unloadTimerJob(timer);
            }

            if (properties.get(KEY_RESOURCE_TYPE) != null) {
                new OperationSendPayload().sendPayload(
                        (RESOURCE_TYPE) properties.get(KEY_RESOURCE_TYPE),
                        (Integer) properties.get(KEY_RESOURCE_ID),
                        (String) properties.get(KEY_PAYLOAD));
            } else {
                _logger.error("Can not run this job without properties! {}", timer);
            }
        }

    }

    @Override
    public void doRun() throws JobInterruptException {
        JobContext context = getJobContext();
        Timer timer = (Timer) context.map.get(TIMER_REF);
        //Update timer recent changes
        if (timer.getId() != null) {
            timer = DaoUtils.getTimerDao().getById(timer.getId());
        }
        Map<String, Object> properties = context.map;
        _logger.debug("Executing timer:[{}]", timer);
        try {
            this.executeTimer(timer, properties);
            //RuleDefinitionTable Triggered Message, ResourcesLogs message data
            //if you have resource id, it's timer job, other wise it might come from alarm definition, etc.,
            if (timer.getId() != null) {
                //Update last fire
                timer.setLastFire(System.currentTimeMillis());
                DaoUtils.getTimerDao().update(timer);
                //Update in log file
                ResourcesLogsUtils.setTimerLog(LOG_LEVEL.INFO, timer, null);
            }
        } catch (Exception ex) {
            if (timer.getId() != null) {
                ResourcesLogsUtils.setTimerLog(LOG_LEVEL.ERROR, timer, ex.getMessage());
            }
            _logger.error("Exception on timer execution: {}, ", timer, ex);
        }
    }
}
