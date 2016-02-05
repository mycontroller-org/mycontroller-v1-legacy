package org.mycontroller.standalone.settings;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class DashboardSettings {
    public static List<Dashboard> getDashboards(User user) {
        List<Settings> settingsList = SettingsUtils.getSettingsList(user.getId(), Dashboard.KEY_DASHBOARD);
        List<Dashboard> dashboards = new ArrayList<Dashboard>();
        for (Settings settings : settingsList) {
            dashboards.add(Dashboard.get(settings));
        }
        return dashboards;
    }

    public static Dashboard getDashboard(User user, String title) {
        return Dashboard.get(SettingsUtils.getSettings(user.getId(), Dashboard.KEY_DASHBOARD, title));
    }

    public static Dashboard getDashboard(User user, Integer id) throws IllegalAccessException {
        Settings settings = DaoUtils.getSettingsDao().getById(id);
        if (settings != null && settings.getUserId() == user.getId()) {
            return Dashboard.get(settings);
        } else {
            throw new IllegalAccessException("you do not have access to see this resource!");
        }
    }

    public static void deleteDashboard(User user, Integer id) throws IllegalAccessException {
        Settings settings = DaoUtils.getSettingsDao().getById(id);
        if (settings != null && settings.getUserId() == user.getId()) {
            DaoUtils.getSettingsDao().deleteById(id);
        } else {
            throw new IllegalAccessException("you do not have access to see this resource!");
        }
    }

    public static Dashboard getDefaultDashboard(User user, String title) {
        if (title == null) {
            title = "Default dashboard";
        }
        Dashboard dashboard = Dashboard.builder()
                .userId(user.getId())
                .title(title)
                .structure("3-9 (12/6-6)")
                .rows("[{\"columns\":[{\"styleClass\":\"col-md-3\",\"widgets\":[{\"type\":\"clock\",\"config\":"
                        + "{\"timePattern\":\"HH:mm:ss\",\"datePattern\":\"YYYY-MM-DD\"},\"title\":\"Clock\","
                        + "\"titleTemplateUrl\":\"../src/templates/widget-title.html\",\"wid\":\"1454574618763-3\"}],"
                        + "\"cid\":\"1454574625329-4\"},{\"styleClass\":\"col-md-9\",\"rows\":[{\"columns\":"
                        + "[{\"styleClass\":\"col-md-12\",\"widgets\":[],\"cid\":\"1454574625351-6\"}]},{\"columns\":"
                        + "[{\"styleClass\":\"col-md-6\",\"widgets\":[],\"cid\":\"1454574625352-7\"},{\"styleClass\":"
                        + "\"col-md-6\",\"widgets\":[],\"cid\":\"1454574625354-8\"}]}],\"widgets\":[],"
                        + "\"cid\":\"1454574625330-5\"}]}]")
                .build();
        dashboard.update(true);
        return getDashboard(user, title);

    }
}
