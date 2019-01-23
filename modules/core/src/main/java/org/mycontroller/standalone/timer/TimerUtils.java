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
package org.mycontroller.standalone.timer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.GoogleAnalyticsApi;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.ResourcesLogsUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.OperationTimerMap;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.jobs.ManageSunRiseSetJobs;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.utils.McUtils;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimerUtils {

    public static Date sunriseTime;
    public static Date sunsetTime;

    public static final SimpleDateFormat DATE_FORMAT_HHMMSS = new SimpleDateFormat("HH:mm:ss");

    public enum TIMER_TYPE {
        SIMPLE("Simple"),
        NORMAL("Normal"),
        CRON("Cron"),
        BEFORE_SUNRISE("Before sunrise"),
        AFTER_SUNRISE("After sunrise"),
        BEFORE_SUNSET("Before sunset"),
        AFTER_SUNSET("After sunset");
        public static TIMER_TYPE get(int id) {
            for (TIMER_TYPE timer_type : values()) {
                if (timer_type.ordinal() == id) {
                    return timer_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private TIMER_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static TIMER_TYPE fromString(String text) {
            if (text != null) {
                for (TIMER_TYPE type : TIMER_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum FREQUENCY_TYPE {
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly");
        public static FREQUENCY_TYPE get(int id) {
            for (FREQUENCY_TYPE frequency_type : values()) {
                if (frequency_type.ordinal() == id) {
                    return frequency_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private FREQUENCY_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static FREQUENCY_TYPE fromString(String text) {
            if (text != null) {
                for (FREQUENCY_TYPE type : FREQUENCY_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum WEEK_DAY {
        MONDAY("MON"),
        TUESDAY("TUE"),
        WEDNESDAY("WED"),
        THURSDAY("THU"),
        FRIDAY("FRI"),
        SATURDAY("SAT"),
        SUNDAY("SUN");
        public static WEEK_DAY get(int id) {
            for (WEEK_DAY weekday : values()) {
                if (weekday.ordinal() == id) {
                    return weekday;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private WEEK_DAY(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static WEEK_DAY fromString(String text) {
            if (text != null) {
                for (WEEK_DAY type : WEEK_DAY.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static void updateSunriseSunset() throws Exception {
        Date tmpSunriseTime = sunriseTime;
        Date tmpSunsetTime = sunsetTime;
        //https://github.com/mikereedell/sunrisesunsetlib-java
        Location location = new Location(
                AppProperties.getInstance().getLocationSettings().getLatitude(),
                AppProperties.getInstance().getLocationSettings().getLongitude());
        SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

        sunriseTime = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(Calendar.getInstance()).getTime();
        sunsetTime = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(Calendar.getInstance()).getTime();

        //Update Sunrise, sunset in to database
        LocationSettings.builder().sunriseTime(sunriseTime.getTime()).sunsetTime(sunsetTime.getTime()).build()
                .updateInternal();
        //update location settings to object factory
        AppProperties.getInstance().setLocationSettings(LocationSettings.get());
        _logger.debug("Location settings after updated:{}", AppProperties.getInstance().getLocationSettings());

        if (!(tmpSunriseTime == null || sunriseTime == null)) {
            //call Manage sun rise sun set jobs, if value changed
            if ((tmpSunriseTime.getTime() != sunriseTime.getTime())
                    || (tmpSunsetTime.getTime() != sunsetTime.getTime())) {
                new Thread(new ManageSunRiseSetJobs()).start();
            }
        }
    }

    public static Date getSunriseTime() {
        return sunriseTime;
    }

    public static Date getSunsetTime() {
        return sunsetTime;
    }

    public static String getFrequencyData(Timer timer) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(timer.getFrequencyType().getText()).append("-->");
        switch (timer.getFrequencyType()) {
            case DAILY:
            case WEEKLY:
                String[] days = timer.getFrequencyData().split(",");
                for (String day : days) {
                    builder.append(WEEK_DAY.fromString(day).getText()).append(",");
                }
                builder.delete(builder.lastIndexOf(","), builder.lastIndexOf(",") + 1);
                break;
            case MONTHLY:
                builder.append(timer.getFrequencyData());
                break;
            default:
                break;
        }
        if (timer.getTimerType() == TIMER_TYPE.NORMAL) {
            builder.append("], [Time: ")
                    .append(new SimpleDateFormat(AppProperties.getInstance().getTimeFormat()).format(new Date(
                            timer.getTriggerTime())))
                    .append("]");
        } else {
            builder.append("], [Offset: ");
            switch (timer.getTimerType()) {
                case AFTER_SUNRISE:
                case AFTER_SUNSET:
                    builder.append("+");
                    break;
                case BEFORE_SUNRISE:
                case BEFORE_SUNSET:
                    builder.append("-");
                    break;
                default:
                    break;

            }
            builder.append(DATE_FORMAT_HHMMSS.format(new Date(
                    timer.getTriggerTime())))
                    .append("], [Fires at: ")
                    .append(new SimpleDateFormat(
                            AppProperties.getInstance().getTimeFormat()).format(getSunriseSunsetCalendar(
                            timer.getTimerType(), timer.getTriggerTime()).getTime()))
                    .append("]");
        }

        return builder.toString();
    }

    public static String getTimerDataString(Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timer.getTimerType().getText());
        stringBuilder.append(", ");
        switch (timer.getTimerType()) {
            case CRON:
                stringBuilder.append("[").append(timer.getFrequencyData()).append("]");
                break;
            case SIMPLE:
                TimerSimple timerSimple = new TimerSimple(timer);
                stringBuilder.append("Repeat[Interval:")
                        .append(McUtils.getFriendlyTime(timerSimple.getRepeatInterval(), true))
                        .append(", Count:").append(timerSimple.getRepeatCount()).append("], ")
                        .append("Executed count:").append(timerSimple.getExecutedCount());
                break;
            case NORMAL:
            case BEFORE_SUNRISE:
            case AFTER_SUNRISE:
            case BEFORE_SUNSET:
            case AFTER_SUNSET:
                stringBuilder.append(getFrequencyData(timer));
                break;
            default:
                break;
        }
        return stringBuilder.toString();
    }

    public static String getValidityString(Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        if (timer.getValidityFrom() != null && timer.getValidityTo() != null) {
            stringBuilder
                    .append(new SimpleDateFormat(AppProperties.getInstance().getDateFormatWithoutSeconds())
                            .format(new Date(timer.getValidityFrom())));
            stringBuilder.append(" ~ ");
            stringBuilder.append(new SimpleDateFormat(AppProperties.getInstance()
                    .getDateFormatWithoutSeconds()).format(new Date(timer.getValidityTo())));
        } else if (timer.getValidityFrom() != null) {
            stringBuilder.append("From ");
            stringBuilder
                    .append(new SimpleDateFormat(AppProperties.getInstance().getDateFormatWithoutSeconds())
                            .format(new Date(timer.getValidityFrom())));
        } else if (timer.getValidityTo() != null) {
            stringBuilder.append("Till ");
            stringBuilder.append(new SimpleDateFormat(AppProperties.getInstance()
                    .getDateFormatWithoutSeconds()).format(new Date(timer.getValidityTo())));
        }
        return stringBuilder.toString();
    }

    public static Long getValidFromToTime(String date) {
        try {
            return new SimpleDateFormat(AppProperties.getInstance().getDateFormat()).parse(date)
                    .getTime();
        } catch (ParseException ex) {
            _logger.error("exception, ", ex);
            return null;
        }
    }

    public static Long getTime(String time) {
        try {
            return DATE_FORMAT_HHMMSS.parse(time).getTime();
        } catch (ParseException ex) {
            _logger.error("exception, ", ex);
            return null;
        }
    }

    public static synchronized void updateTimer(Timer timer) {
        //Unload timer
        Timer timerOld = DaoUtils.getTimerDao().getById(timer.getId());
        SchedulerUtils.unloadTimerJob(timerOld);

        //Update operations
        updateOperationTimerMap(timer);

        //update details of timer and reload it
        timer.setLastFire(null); //clear last fired
        timer.setInternalVariable1(null);
        DaoUtils.getTimerDao().update(timer);
        SchedulerUtils.loadTimerJob(timer);
    }

    public static synchronized void addTimer(Timer timer) {
        timer.setLastFire(null); //clear last fired
        timer.setInternalVariable1(null);
        //add timer
        DaoUtils.getTimerDao().create(timer);
        GoogleAnalyticsApi.instance().trackTimerCreation(timer.getTimerType().getText());
        //Keep operations id
        List<Integer> operationIds = timer.getOperationIds();
        //update details of timer and load it
        timer = DaoUtils.getTimerDao().getByName(timer.getName());
        timer.setOperationIds(operationIds);
        //update operations map
        updateOperationTimerMap(timer);
        if (timer.getEnabled()) {
            SchedulerUtils.loadTimerJob(timer);
        }
    }

    public static synchronized void enableTimer(Timer timer) {
        if (timer.getEnabled()) {
            _logger.debug("Timer already in enabled state, nothing to do, [{}]", timer);
            return;
        }
        //Clear internal references
        timer.setInternalVariable1(null);
        //Update enabled
        timer.setEnabled(true);
        DaoUtils.getTimerDao().update(timer);
        //load timer on scheduler
        SchedulerUtils.loadTimerJob(timer);
    }

    public static synchronized void disableTimer(Timer timer) {
        if (!timer.getEnabled()) {
            _logger.debug("Timer already in disabled state, nothing to do, [{}]", timer);
            return;
        }

        //unload timer on scheduler
        SchedulerUtils.unloadTimerJob(timer);

        //Update disabled
        timer.setEnabled(false);
        DaoUtils.getTimerDao().update(timer);
    }

    public static synchronized void deleteTimer(Integer id) {
        deleteTimer(DaoUtils.getTimerDao().getById(id));
    }

    public static synchronized void deleteTimer(Timer timer) {
        //unload operations timers job
        OperationUtils.unloadOperationTimerJobs(timer);

        SchedulerUtils.unloadTimerJob(timer);
        //Delete from resources log
        ResourcesLogsUtils.deleteResourcesLog(RESOURCE_TYPE.TIMER, timer.getId());
        //Delete alarm
        DaoUtils.getTimerDao().deleteById(timer.getId());
        //Remove from operations map
        DaoUtils.getOperationTimerMapDao().deleteByTimerId(timer.getId());
        _logger.debug("Item removed:{}", timer);
    }

    public static synchronized void enableTimers(List<Integer> ids) {
        for (Integer id : ids) {
            Timer timer = DaoUtils.getTimerDao().getById(id);
            enableTimer(timer);
        }
    }

    public static synchronized void disableTimers(List<Integer> ids) {
        for (Integer id : ids) {
            Timer timer = DaoUtils.getTimerDao().getById(id);
            //unload operations timers job
            OperationUtils.unloadOperationTimerJobs(timer);
            disableTimer(timer);
        }
    }

    public static synchronized void deleteTimers(List<Integer> ids) {
        for (Integer id : ids) {
            deleteTimer(id);
        }
    }

    private static void updateOperationTimerMap(Timer timer) {
        //clear all old mapping
        DaoUtils.getOperationTimerMapDao().deleteByTimerId(timer.getId());
        //Update operations
        if (timer.getOperationIds() != null && !timer.getOperationIds().isEmpty()) {
            OperationTable operationTable = OperationTable.builder().build();
            OperationTimerMap operationTimerMap = OperationTimerMap.builder().timer(timer).build();
            for (Integer operationId : timer.getOperationIds()) {
                operationTable.setId(operationId);
                operationTimerMap.setOperationTable(operationTable);
                DaoUtils.getOperationTimerMapDao().create(operationTimerMap);
            }
        }
    }

    public static void executeTimerOperation(ResourceModel resourceModel, ResourceOperation operation) {
        switch (operation.getOperationType()) {
            case ENABLE:
                enableTimer((Timer) resourceModel.getResource());
                break;
            case DISABLE:
                disableTimer((Timer) resourceModel.getResource());
                break;
            default:
                _logger.warn("Timer not support for this operation!:[{}]",
                        operation.getOperationType().getText());
                break;
        }
    }

    public static Calendar getSunriseSunsetCalendar(TIMER_TYPE timerType, Long triggerTime) {
        if (triggerTime == null) {
            return null;
        }
        Calendar timeCal = Calendar.getInstance();
        Calendar sunssCal = Calendar.getInstance();
        timeCal.setTimeInMillis(triggerTime);

        switch (timerType) {
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
        return timeCal;
    }

}
