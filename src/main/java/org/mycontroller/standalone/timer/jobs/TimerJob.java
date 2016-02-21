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
package org.mycontroller.standalone.timer.jobs;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.group.ResourcesGroupUtils;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.knowm.sundial.Job;
import org.knowm.sundial.JobContext;
import org.knowm.sundial.exceptions.JobInterruptException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class TimerJob extends Job {
    private static final Logger _logger = LoggerFactory.getLogger(TimerJob.class);
    public static final String TIMER_REF = "timer_referance_key";

    private void executeTimer(Timer timer) {
        if (timer.getId() != null) {
            if (timer.getTimerType() == TIMER_TYPE.SIMPLE) {
                TimerSimple timerSimple = new TimerSimple(timer);
                timerSimple.incrementExecutedCount();
                timerSimple.update();
            }
        }

        PayloadOperation payloadOperation = new PayloadOperation(timer.getPayload());
        ResourceModel resourceModel = new ResourceModel(timer.getResourceType(), timer.getResourceId());

        //we have to handle gateway,alarm,resource groups and timer operations
        switch (resourceModel.getResourceType()) {
            case GATEWAY:
                GatewayUtils.executeGatewayOperation(resourceModel, payloadOperation);
                break;
            case ALARM_DEFINITION:
                AlarmUtils.executeAlarmDefinitionOperation(resourceModel, payloadOperation);
                break;
            case TIMER:
                TimerUtils.executeTimerOperation(resourceModel, payloadOperation);
            case RESOURCES_GROUP:
                ResourcesGroupUtils.executeResourceGroupsOperation(resourceModel, payloadOperation);
                break;
            default:
                ObjectFactory.getIActionEngine(
                        resourceModel.getNetworkType()).executeSendPayload(resourceModel, payloadOperation);
                break;
        }
    }

    @Override
    public void doRun() throws JobInterruptException {
        JobContext context = getJobContext();
        Timer timer = (Timer) context.map.get(TIMER_REF);
        _logger.debug("Executing timer:[{}]", timer);
        try {
            this.executeTimer(timer);
            //AlarmDefinition Triggered Message, ResourcesLogs message data
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
            _logger.error("Exception, ", ex);
        }
    }
}
