/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.scheduler.jobs;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.SensorLogUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
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
    public static final String TIMER_REF = "timerRef";

    private void executeTimer(Timer timer) {
        Sensor sensor = DaoUtils.getSensorDao().get(timer.getSensor().getId());
        RawMessage rawMessage = new RawMessage(
                sensor.getNode().getId(),
                sensor.getSensorId(),
                MESSAGE_TYPE.C_SET.ordinal(), //messageType
                0, //ack
                sensor.getMessageType(),//subType
                timer.getPayload(),
                true);// isTxMessage
        ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
    }

    @Override
    public void doRun() throws JobInterruptException {
        JobContext context = getJobContext();
        Timer timer = (Timer) context.map.get(TIMER_REF);
        try {
            this.executeTimer(timer);
            //Alarm Triggered Message, Log message data
            SensorLogUtils.setTimerLog(timer, null);
        } catch (Exception ex) {
            SensorLogUtils.setTimerLog(timer, ex.getMessage());
            _logger.error("Exception, ", ex);
        }
    }
}
