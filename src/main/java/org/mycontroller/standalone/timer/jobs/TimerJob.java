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
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.ResourcesLogsUtils;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xeiam.sundial.Job;
import com.xeiam.sundial.JobContext;
import com.xeiam.sundial.exceptions.JobInterruptException;

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

        if (resourceModel.getResourceType() == RESOURCE_TYPE.GATEWAY) { //If it's gateway task we should handle this
            GatewayUtils.executeGatewayOperation(resourceModel, payloadOperation);
        } else {
            ObjectFactory.getIActionEngine(resourceModel.getNetworkType()).executeSendPayload(resourceModel,
                    payloadOperation);
        }
        //ObjectFactory.getIActionEngine(resourceModel.getNetworkType()).executeTimer(resourceModel, timer);
    }

    @Override
    public void doRun() throws JobInterruptException {
        JobContext context = getJobContext();
        Timer timer = (Timer) context.map.get(TIMER_REF);
        _logger.debug("Executing timer:[{}]", timer);
        try {
            this.executeTimer(timer);
            //AlarmDefinition Triggered Message, ResourcesLogs message data
            ResourcesLogsUtils.setTimerLog(LOG_LEVEL.INFO, timer, null);
        } catch (Exception ex) {
            ResourcesLogsUtils.setTimerLog(LOG_LEVEL.ERROR, timer, ex.getMessage());
            _logger.error("Exception, ", ex);
        }
    }
}
