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
package org.mycontroller.standalone.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.jobs.ManageSunRiseSetJobs;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class TimerUtils {
    private static final Logger _logger = LoggerFactory.getLogger(TimerUtils.class);
    public static final String DATE_TIME_FORMAT = "dd-MMM-yyyy, HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT_DISPLAY = "dd-MMM-yyyy, HH:mm";

    private TimerUtils() {

    }

    public static Date sunriseTime;
    public static Date sunsetTime;

    public enum TYPE {
        NORMAL("Normal"),
        CRON("Cron"),
        BEFORE_SUNRISE("Before Sunrise"),
        AFTER_SUNRISE("After Sunrise"),
        BEFORE_SUNSET("Before Sunset"),
        AFTER_SUNSET("After Sunset");
        public static TYPE get(int id) {
            for (TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private TYPE(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public enum FREQUENCY {
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly");
        public static FREQUENCY get(int id) {
            for (FREQUENCY frequency : values()) {
                if (frequency.ordinal() == id) {
                    return frequency;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private FREQUENCY(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
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

        public String value() {
            return this.value;
        }
    }

    public static void updateSunriseSunset() throws Exception {
        Date tmpSunriseTime = sunriseTime;
        //https://github.com/mikereedell/sunrisesunsetlib-java
        String latitude = DaoUtils.getSettingsDao().get(Settings.CITY_LATITUDE).getValue();
        String longitude = DaoUtils.getSettingsDao().get(Settings.CITY_LONGITUDE).getValue();
        Location location = new Location(latitude, longitude);
        SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

        sunriseTime = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(Calendar.getInstance()).getTime();
        sunsetTime = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(Calendar.getInstance()).getTime();

        //Update Sunrise in db
        Settings settings = DaoUtils.getSettingsDao().get(Settings.SUNRISE_TIME);
        settings.setValue(String.valueOf(sunriseTime.getTime()));
        DaoUtils.getSettingsDao().createOrUpdate(settings);
        _logger.info("Updated Time:[SunRise:{}, SunSet:{}], City:[latitude:{}, longitude:{}]",
                sunriseTime, sunsetTime, latitude, longitude);

        //Update sunset in db
        settings = DaoUtils.getSettingsDao().get(Settings.SUNSET_TIME);
        settings.setValue(String.valueOf(sunsetTime.getTime()));
        DaoUtils.getSettingsDao().createOrUpdate(settings);

        if (!(tmpSunriseTime == null || sunriseTime == null)) {
            //call Manage sun rise sun set jobs, if value changed
            if (tmpSunriseTime.getTime() != sunriseTime.getTime()) {
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
        builder.append("[").append(FREQUENCY.get(timer.getFrequency()).value()).append("-->");
        switch (FREQUENCY.get(timer.getFrequency())) {
            case DAILY:
            case WEEKLY:
                String[] days = timer.getFrequencyData().split(",");
                for (String day : days) {
                    builder.append(WEEK_DAY.get(Integer.valueOf(day)).value()).append(",");
                }
                builder.delete(builder.lastIndexOf(","), builder.lastIndexOf(",") + 1);
                break;
            case MONTHLY:
                builder.append(timer.getFrequencyData());
                break;
            default:
                break;
        }
        builder.append("], [Time: ").append(new SimpleDateFormat(TIME_FORMAT).format(new Date(timer.getTime())))
                .append("]");
        return builder.toString();
    }

    public static String getTimerDataString(Timer timer) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TYPE.get(timer.getType()).value());
        stringBuilder.append(", ");
        switch (TYPE.get(timer.getType())) {
            case CRON:
                stringBuilder.append("[").append(timer.getFrequencyData()).append("]");
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
        if (timer.getValidFrom() != null && timer.getValidTo() != null) {
            stringBuilder
                    .append(new SimpleDateFormat(DATE_TIME_FORMAT_DISPLAY).format(new Date(timer.getValidFrom())));
            stringBuilder.append(" ~ ");
            stringBuilder.append(new SimpleDateFormat(DATE_TIME_FORMAT_DISPLAY).format(new Date(timer.getValidTo())));
        } else if (timer.getValidFrom() != null) {
            stringBuilder.append("From ");
            stringBuilder
                    .append(new SimpleDateFormat(DATE_TIME_FORMAT_DISPLAY).format(new Date(timer.getValidFrom())));
        } else if (timer.getValidTo() != null) {
            stringBuilder.append("Till ");
            stringBuilder.append(new SimpleDateFormat(DATE_TIME_FORMAT_DISPLAY).format(new Date(timer.getValidTo())));
        }
        return stringBuilder.toString();
    }

    public static Long getValidFromToTime(String date) {
        try {
            return new SimpleDateFormat(DATE_TIME_FORMAT).parse(date).getTime();
        } catch (ParseException ex) {
            _logger.error("exception, ", ex);
            return null;
        }
    }

    public static Long getTime(String time) {
        try {
            return new SimpleDateFormat(TIME_FORMAT).parse(time).getTime();
        } catch (ParseException ex) {
            _logger.error("exception, ", ex);
            return null;
        }
    }

    public static void updateTimer(Timer timer) {
        timer.setTimestamp(System.currentTimeMillis()); //Set current time
        DaoUtils.getTimerDao().update(timer);
        SchedulerUtils.reloadTimerJob(timer);
    }

}
