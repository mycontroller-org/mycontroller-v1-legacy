package org.mycontroller.standalone.api.jaxrs.mapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

public class About {
    private String timezone;
    private int timezoneMilliseconds;
    private String timezoneString;
    private String appVersion;
    private Date systemDate;
    private String appName;

    public About() {
        Date date = new Date();
        this.appName = AppProperties.APPLICATION_NAME;
        this.timezoneMilliseconds = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        this.systemDate = date;
        this.timezone = new SimpleDateFormat("Z").format(date);
        this.timezoneString = new SimpleDateFormat("z").format(date);
        this.appVersion = DaoUtils.getSettingsDao().get(Settings.MC_VERSION).getValue();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getTimezoneString() {
        return timezoneString;
    }

    public Date getSystemDate() {
        return systemDate;
    }

    public String getAppName() {
        return appName;
    }

    public int getTimezoneMilliseconds() {
        return timezoneMilliseconds;
    }
}
