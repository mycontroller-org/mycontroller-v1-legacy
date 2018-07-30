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

import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.scheduler.SchedulerUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class ManageSunRiseSetJobs implements Runnable {

    private void unloadLoadSunRiseSetJobs() {
        List<Timer> timers = DaoUtils.getTimerDao().getAllEnabled();
        //reload all jobs
        for (Timer timer : timers) {
            switch (timer.getTimerType()) {
                case BEFORE_SUNRISE:
                case AFTER_SUNRISE:
                case BEFORE_SUNSET:
                case AFTER_SUNSET:
                    SchedulerUtils.reloadTimerJob(timer);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void run() {
        try {
            unloadLoadSunRiseSetJobs();
            _logger.debug("SunRise, SunSet jobs reseting completed...");
        } catch (Exception ex) {
            _logger.error("exception, ", ex);
        }
    }

}
