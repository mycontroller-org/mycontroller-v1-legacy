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
package org.mycontroller.standalone.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.TimerUtils;
import org.mycontroller.standalone.db.TimerUtils.FREQUENCY;
import org.mycontroller.standalone.db.TimerUtils.TYPE;
import org.mycontroller.standalone.db.TimerUtils.WEEK_DAY;
import org.mycontroller.standalone.db.tables.SystemJob;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.jobs.timer.TimerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xeiam.sundial.SundialJobScheduler;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SchedulerUtils {
    public static final String JOB_DATA = "job-data";
    public static final String SYSTEM_JOB_REF = "SYS_";
    public static final String TIMER_JOB_REF = "TIMER_";
    public static final String CRON_TRIGGER_REF = "_Cron_Trigger";
    private static final Logger _logger = LoggerFactory.getLogger(SchedulerUtils.class);

    private SchedulerUtils() {
    }

    public static void startScheduler() {
        SundialJobScheduler.startScheduler();
        _logger.debug("SundialJobScheduler started.Jobs:[{}]",
                SundialJobScheduler.getAllJobNames());
        //Load all system jobs
        List<SystemJob> systemJobs = DaoUtils.getSystemJobDao().getAllEnabled();
        for (SystemJob systemJob : systemJobs) {
            addSystemJob(systemJob);
        }
        //Load Timer jobs
        List<Timer> timers = DaoUtils.getTimerDao().getAllEnabled();
        for (Timer timer : timers) {
            try {
                loadTimerJob(timer);
            } catch (Exception ex) {
                _logger.error("Unable to load timer[{}]", timer, ex);
            }
        }
    }

    public static List<String> getJobs() {
        return SundialJobScheduler.getAllJobNames();
    }

    public static void stop() {
        SundialJobScheduler.shutdown();
        _logger.debug("Scheduler stopped...");
    }

    public static String getSystemJobName(SystemJob systemJob) {
        return SYSTEM_JOB_REF + systemJob.getId() + "_" + systemJob.getName();
    }

    public static void addSystemJob(SystemJob systemJob) {
        String jobName = getSystemJobName(systemJob);
        SundialJobScheduler.addJob(
                jobName,
                systemJob.getClassName());

        SundialJobScheduler.addCronTrigger(
                getCronTriggerName(jobName),
                jobName,
                systemJob.getCron());
        _logger.debug("New job added:{}", systemJob);
    }

    public static void removeSystemJob(SystemJob systemJob) {
        removeJob(getSystemJobName(systemJob));
    }

    public static void removeJob(String jobName) {
        SundialJobScheduler.removeJob(jobName);
        SundialJobScheduler.removeTrigger(getCronTriggerName(jobName));
        _logger.debug("Job removed:[Name:{},CronName:{}]", jobName, getCronTriggerName(jobName));
    }

    public static String getTimerJobName(Timer timer) {
        return TIMER_JOB_REF + timer.getId() + "_" + timer.getName();
    }

    public static String getCronTriggerName(String jobName) {
        return jobName + CRON_TRIGGER_REF;
    }

    public static synchronized void loadTimerJob(Timer timer) {
        //Check Valid To, if available 
        if (timer.getValidTo() != null) {
            if (timer.getValidTo() <= System.currentTimeMillis()) {
                _logger.warn("This timer job expired! Timer:[{}]", timer);
                return;
            }
        }

        Map<String, Object> jobData = new HashMap<String, Object>();
        jobData.put(TimerJob.TIMER_REF, timer);
        String jobName = getTimerJobName(timer);
        SundialJobScheduler.addJob(
                jobName,
                TimerJob.class.getName(),
                jobData,
                false);

        Calendar timeCal = null;
        Calendar sunssCal = null;
        if (TYPE.CRON.ordinal() != timer.getType()) {
            timeCal = Calendar.getInstance();
            timeCal.setTimeInMillis(timer.getTime());
            sunssCal = Calendar.getInstance();
        }

        switch (TYPE.get(timer.getType())) {
            case NORMAL:
                break;
            case BEFORE_SUNRISE:
                sunssCal.setTime(TimerUtils.getSunriseTime());
                sunssCal.add(Calendar.HOUR_OF_DAY, -(timeCal.get(Calendar.HOUR_OF_DAY)));
                sunssCal.add(Calendar.MINUTE, -(timeCal.get(Calendar.MINUTE)));
                sunssCal.add(Calendar.SECOND, -(timeCal.get(Calendar.SECOND)));
                timeCal = sunssCal;
                break;
            case AFTER_SUNRISE:
                sunssCal.setTime(TimerUtils.getSunriseTime());
                sunssCal.add(Calendar.HOUR_OF_DAY, (timeCal.get(Calendar.HOUR_OF_DAY)));
                sunssCal.add(Calendar.MINUTE, (timeCal.get(Calendar.MINUTE)));
                sunssCal.add(Calendar.SECOND, (timeCal.get(Calendar.SECOND)));
                timeCal = sunssCal;
                break;
            case BEFORE_SUNSET:
                sunssCal.setTime(TimerUtils.getSunsetTime());
                sunssCal.add(Calendar.HOUR_OF_DAY, -(timeCal.get(Calendar.HOUR_OF_DAY)));
                sunssCal.add(Calendar.MINUTE, -(timeCal.get(Calendar.MINUTE)));
                sunssCal.add(Calendar.SECOND, -(timeCal.get(Calendar.SECOND)));
                timeCal = sunssCal;
                break;
            case AFTER_SUNSET:
                sunssCal.setTime(TimerUtils.getSunsetTime());
                sunssCal.add(Calendar.HOUR_OF_DAY, (timeCal.get(Calendar.HOUR_OF_DAY)));
                sunssCal.add(Calendar.MINUTE, (timeCal.get(Calendar.MINUTE)));
                sunssCal.add(Calendar.SECOND, (timeCal.get(Calendar.SECOND)));
                timeCal = sunssCal;
                break;
            default:
                break;
        }
        String cronExpression;
        if (TYPE.CRON.ordinal() == timer.getType()) {
            cronExpression = timer.getFrequencyData();
        } else {
            cronExpression = getCronExpression(
                    timeCal.get(Calendar.SECOND),
                    timeCal.get(Calendar.MINUTE),
                    timeCal.get(Calendar.HOUR_OF_DAY),
                    timer);
        }

        //Check Valid from, if available and active,
        //Change valid from as future seconds to avoid immediate trigger
        if (timer.getValidFrom() != null) {
            if (timer.getValidFrom() <= System.currentTimeMillis()) {
                timer.setValidFrom(System.currentTimeMillis() + (1000 * 5));
            }
        }

        SundialJobScheduler.addCronTrigger(
                getCronTriggerName(jobName),
                jobName,
                cronExpression,
                timer.getValidFrom() != null ? new Date(timer.getValidFrom()) : null,
                timer.getValidTo() != null ? new Date(timer.getValidTo()) : null);
        _logger.debug("New Timer job added:[{}], CornExpression:[{}]", timer, cronExpression);
    }

    public static String getCronExpression(int sec, int min, int hour, Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sec).append(" ") // Seconds
                .append(min).append(" ")      // Minutes
                .append(hour).append(" ");    // Hours
        switch (FREQUENCY.get(timer.getFrequency())) {
            case DAILY:
            case WEEKLY:
                stringBuilder.append("? * ");
                String[] days = timer.getFrequencyData().split(",");
                for (String day : days) {
                    stringBuilder.append(WEEK_DAY.get(Integer.valueOf(day)).value()).append(",");
                }
                stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.lastIndexOf(",") + 1);
                break;
            case MONTHLY:
                stringBuilder.append(timer.getFrequencyData());
                stringBuilder.append(" * ?");
                break;
            default:
                break;
        }
        return stringBuilder.toString();
    }

    public static synchronized void unloadTimerJob(Timer timer) {
        removeJob(getTimerJobName(timer));
    }

    public static synchronized void unloadTimerJobs(List<Timer> timers) {
        for (Timer timer : timers) {
            unloadTimerJob(timer);
        }
    }

    public static synchronized void reloadTimerJob(Timer timer) {
        unloadTimerJob(timer);
        loadTimerJob(timer);

    }
}
