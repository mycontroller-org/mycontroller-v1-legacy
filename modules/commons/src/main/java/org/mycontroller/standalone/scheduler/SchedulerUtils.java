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
package org.mycontroller.standalone.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.SystemJob;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.jobs.NodeAliveStatusJob;
import org.mycontroller.standalone.settings.BackupSettings;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.WEEK_DAY;
import org.mycontroller.standalone.timer.jobs.TimerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.knowm.sundial.SundialJobScheduler;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SchedulerUtils {
    public static final String JOB_DATA = "job-data";
    public static final String SYSTEM_JOB_REF = "SYS_";
    public static final String TIMER_JOB_REF = "TIMER_";
    public static final String CRON_TRIGGER_REF = "_Cron_Trigger";
    private static final long FROM_TIME_DELAY = TIME_REF.ONE_SECOND;
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

        //Load system backup job
        BackupSettings.reloadJob();//Reload backup job

        //Update all other jobs
        //MySensorsSettings heartbeat job
        startNodeAliveCheckJob();

    }

    public static void stop() {
        //Stop NodeAliveStatusJob, which has thread sleep
        NodeAliveStatusJob.setTerminateAliveCheck(true);
        if (SundialJobScheduler.getScheduler() != null) {
            SundialJobScheduler.shutdown();
        }
        _logger.debug("Scheduler stopped...");
    }

    public static List<String> getJobs() {
        return SundialJobScheduler.getAllJobNames();
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
                systemJob.getCron(),
                //Start all the system jobs after 10 seconds
                new Date(System.currentTimeMillis() + (TIME_REF.ONE_SECOND * 10)),
                null);
        _logger.debug("New job added:{}", systemJob);
    }

    public static List<String> getAllJobNames() {
        return SundialJobScheduler.getAllJobNames();
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
        if (timer.getId() != null) {
            return TIMER_JOB_REF + timer.getId() + "_" + timer.getName();
        } else {
            return TIMER_JOB_REF + "_" + timer.getName();
        }
    }

    public static String getCronTriggerName(String jobName) {
        return jobName + CRON_TRIGGER_REF;
    }

    public static synchronized void loadTimerJob(Timer timer) {
        //Check is it enabled or disabled
        if (!timer.getEnabled()) {
            _logger.debug("Timer[{}] disabled. No action needed", timer);
            return;
        }

        _logger.debug("Timer loading:{}", timer);

        //Check Valid To, if available 
        if (timer.getValidityTo() != null) {
            if (timer.getValidityTo() <= System.currentTimeMillis()) {
                _logger.warn("This timer expired! Timer:[{}]", timer);
                if (timer.getId() != null) {
                    _logger.warn("Disabling this timer now. Timer name:", timer.getName());
                    timer.setEnabled(false);
                    DaoUtils.getTimerDao().update(timer);
                }
                return;
            }
        }

        Map<String, Object> jobData = new HashMap<String, Object>();
        jobData.put(TimerJob.TIMER_REF, timer);
        String jobName = getTimerJobName(timer);
        SundialJobScheduler.addJob(
                jobName,
                timer.getTargetClass() != null ? timer.getTargetClass() : TimerJob.class.getName(),
                jobData,
                false);

        Calendar timeCal = null;
        Calendar sunssCal = null;
        if (TIMER_TYPE.CRON != timer.getTimerType() && TIMER_TYPE.SIMPLE != timer.getTimerType()) {
            timeCal = Calendar.getInstance();
            timeCal.setTimeInMillis(timer.getTriggerTime());
            sunssCal = Calendar.getInstance();
        }

        switch (timer.getTimerType()) {
            case CRON:
            case SIMPLE:
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
        String cronExpression = null;
        if (TIMER_TYPE.CRON == timer.getTimerType()) {
            cronExpression = timer.getFrequencyData();
        } else if (TIMER_TYPE.SIMPLE != timer.getTimerType()) {
            cronExpression = getCronExpression(
                    timeCal.get(Calendar.SECOND),
                    timeCal.get(Calendar.MINUTE),
                    timeCal.get(Calendar.HOUR_OF_DAY),
                    timer);
        }

        //Check Valid from, if available and active,
        //Change valid from as future seconds to avoid immediate trigger
        //For simple job validity from should from current time + interval
        if (TIMER_TYPE.SIMPLE != timer.getTimerType() && timer.getValidityFrom() != null) {
            if (timer.getValidityFrom() <= System.currentTimeMillis()) {
                timer.setValidityFrom(System.currentTimeMillis() + FROM_TIME_DELAY);
            }
        }
        if (TIMER_TYPE.SIMPLE == timer.getTimerType()) {
            TimerSimple timerSimple = new TimerSimple(timer);
            if (timerSimple.isValid()) {
                SundialJobScheduler.addSimpleTrigger(
                        getCronTriggerName(jobName),
                        jobName,
                        timerSimple.getRepeatCount(), //repeatCount
                        timerSimple.getRepeatInterval(), //repeatInterval
                        //Start time should be - current time + repeat interval
                        timer.getValidityFrom() != null ? new Date(timer.getValidityFrom()) :
                                new Date(System.currentTimeMillis() + timerSimple.getRepeatInterval()),
                        timer.getValidityTo() != null ? new Date(timer.getValidityTo()) : null);
                _logger.debug("New simple timer job added:[{}], simple timer:[{}]", timer, timerSimple);
            } else {
                _logger.warn("Invalid timer job:[{}]", timer);
            }
        } else {
            SundialJobScheduler.addCronTrigger(
                    getCronTriggerName(jobName),
                    jobName,
                    cronExpression,
                    timer.getValidityFrom() != null ? new Date(timer.getValidityFrom()) : null,
                    timer.getValidityTo() != null ? new Date(timer.getValidityTo()) : null);
            _logger.debug("New timer job added:[{}], CornExpression:[{}]", timer, cronExpression);
        }

    }

    public static String getCronExpression(int sec, int min, int hour, Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sec).append(" ") // Seconds
                .append(min).append(" ")      // Minutes
                .append(hour).append(" ");    // Hours
        switch (timer.getFrequencyType()) {
            case DAILY:
            case WEEKLY:
                stringBuilder.append("? * ");
                String[] days = timer.getFrequencyData().split(",");
                for (String day : days) {
                    stringBuilder.append(WEEK_DAY.fromString(day).getText()).append(",");
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

    public static void startNodeAliveCheckJob() {
        SundialJobScheduler.addJob(NodeAliveStatusJob.NAME, NodeAliveStatusJob.class.getName());
        SundialJobScheduler.addSimpleTrigger(
                NodeAliveStatusJob.TRIGGER_NAME,
                NodeAliveStatusJob.NAME,
                -1,
                ObjectFactory.getAppProperties().getControllerSettings().getAliveCheckInterval(),
                //Start this job after 10 seconds
                new Date(System.currentTimeMillis() + (TIME_REF.ONE_SECOND * 10)),
                null);
    }

    public static void stopNodeAliveCheckJob() {
        SundialJobScheduler.removeJob(NodeAliveStatusJob.NAME);
    }

    public static void reloadMySensorHearbeatJob() {
        stopNodeAliveCheckJob();
        startNodeAliveCheckJob();
    }
}
