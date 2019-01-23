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
package org.mycontroller.standalone.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.sundial.SundialJobScheduler;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.jobs.ExecuteDiscoverJob;
import org.mycontroller.standalone.jobs.NodeAliveStatusJob;
import org.mycontroller.standalone.model.SystemJob;
import org.mycontroller.standalone.settings.BackupSettings;
import org.mycontroller.standalone.settings.SystemJobsSettings;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.WEEK_DAY;
import org.mycontroller.standalone.timer.jobs.TimerJob;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchedulerUtils {
    public static final String JOB_DATA = "job-data";
    public static final String SYSTEM_JOB_REF = "SYS_";
    public static final String TIMER_JOB_REF = "TIMER_";
    public static final String CRON_TRIGGER_REF = "_Cron_Trigger";
    private static final long FROM_TIME_DELAY = McUtils.ONE_SECOND;

    public static void startScheduler() {
        SundialJobScheduler.startScheduler();
        _logger.debug("SundialJobScheduler started.Jobs:[{}]",
                SundialJobScheduler.getAllJobNames());
        //Load all system jobs
        reloadSystemJobs();

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
        startExecuteDiscoverJob();

    }

    public static synchronized void reloadSystemJobs() {
        for (SystemJob _job : SystemJobsSettings.listAllJobs()) {
            removeSystemJob(_job);
            addSystemJob(_job);
        }
        _logger.info("System jobs reloaded.");
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
                new Date(System.currentTimeMillis() + (McUtils.ONE_SECOND * 10)),
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

    public static void removeJobIfStartsWith(String jobName) {
        List<String> jobNames = SundialJobScheduler.getAllJobNames();
        for (String jName : jobNames) {
            if (jName.startsWith(jobName)) {
                _logger.debug("There is a match: jName:[{}] will b removed.", jName);
                removeJob(jName);
            }
        }
    }

    public static String getTimerJobName(Timer _timer) {
        return getTimerJobName(_timer.getName(), _timer.getId());
    }

    private static String getTimerJobName(String _name, Integer _id) {
        if (_id != null) {
            return TIMER_JOB_REF + _id + "_" + _name;
        } else {
            return TIMER_JOB_REF + "_" + _name;
        }
    }

    public static String getCronTriggerName(String jobName) {
        return jobName + CRON_TRIGGER_REF;
    }

    public static synchronized void loadTimerJob(Timer timer) {
        loadTimerJob(timer, null);
    }

    public static synchronized void loadTimerJob(Timer timer, Map<String, Object> jobData) {
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

        if (jobData == null) {
            jobData = new HashMap<String, Object>();
        }
        jobData.put(TimerJob.TIMER_REF, timer);
        String jobName = getTimerJobName(timer);
        SundialJobScheduler.addJob(
                jobName,
                timer.getTargetClass() != null ? timer.getTargetClass() : TimerJob.class.getName(),
                jobData,
                false);

        //Load sunrise, sunset, normal timeCalender
        Calendar timeCalendar = TimerUtils.getSunriseSunsetCalendar(timer.getTimerType(), timer.getTriggerTime());
        String cronExpression = null;
        if (TIMER_TYPE.CRON == timer.getTimerType()) {
            cronExpression = timer.getFrequencyData();
        } else if (TIMER_TYPE.SIMPLE != timer.getTimerType()) {
            cronExpression = getCronExpression(
                    timeCalendar.get(Calendar.SECOND),
                    timeCalendar.get(Calendar.MINUTE),
                    timeCalendar.get(Calendar.HOUR_OF_DAY),
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

    public static synchronized void unloadTimerJobIfContains(Timer timer) {
        removeJobIfStartsWith(getTimerJobName(timer));
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
                NodeAliveStatusJob.MIN_ALIVE_CHECK_DURATION, //Run this job every x minutes
                //Start this job after 10 seconds
                new Date(System.currentTimeMillis() + (McUtils.ONE_SECOND * 10)),
                null);
    }

    public static void stopNodeAliveCheckJob() {
        SundialJobScheduler.removeJob(NodeAliveStatusJob.NAME);
    }

    public static void reloadMySensorHearbeatJob() {
        stopNodeAliveCheckJob();
        startNodeAliveCheckJob();
    }

    public static void startExecuteDiscoverJob() {
        if (AppProperties.getInstance().getControllerSettings().getExecuteDiscoverInterval() < McUtils.MINUTE) {
            //Nothing to do, just return from here
            return;
        }
        SundialJobScheduler.addJob(ExecuteDiscoverJob.NAME, ExecuteDiscoverJob.class.getName());
        SundialJobScheduler.addSimpleTrigger(
                ExecuteDiscoverJob.TRIGGER_NAME,
                ExecuteDiscoverJob.NAME,
                -1,
                AppProperties.getInstance().getControllerSettings().getExecuteDiscoverInterval(),
                //Start this job after 20 seconds
                new Date(System.currentTimeMillis() + (McUtils.ONE_SECOND * 20)),
                null);
    }

    public static void stopExecuteDiscoverJob() {
        SundialJobScheduler.removeJob(ExecuteDiscoverJob.NAME);
    }

    public static void reloadExecuteDiscoverJob() {
        stopExecuteDiscoverJob();
        startExecuteDiscoverJob();
    }

    public static synchronized void reloadControllerJobs() {
        reloadMySensorHearbeatJob();
        reloadExecuteDiscoverJob();
    }

    public static synchronized Long nextFireTime(String _name, Integer _id) {
        String _triggerName = getCronTriggerName(getTimerJobName(_name, _id));
        try {
            return SundialJobScheduler.getScheduler().getTrigger(_triggerName).getNextFireTime().getTime();
        } catch (Exception ex) {
            _logger.error("Error when fetching trigger[{}] next exeuction status", _triggerName, ex);
        }
        return null;
    }

}
