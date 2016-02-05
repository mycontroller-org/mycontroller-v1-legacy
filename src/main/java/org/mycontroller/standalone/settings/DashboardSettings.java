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
package org.mycontroller.standalone.settings;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.NumericUtils;
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
        String name = NumericUtils.getRandomAlphanumeric(5) + "_" + System.currentTimeMillis();

        Dashboard dashboard = Dashboard.builder()
                .userId(user.getId())
                .title(title)
                .structure("3-9 (12/6-6)")
                .name(name)
                .rows("[{\"columns\":[{\"styleClass\":\"col-md-3\",\"widgets\":[{\"type\":\"mycTime\","
                        + "\"config\":{\"datePattern\":\"MMM dd, yyyy\"},\"title\":\"MyController time\","
                        + "\"titleTemplateUrl\":\"../src/templates/widget-title.html\",\"wid\":\"1454676806001-1\"},"
                        + "{\"type\":\"mycSunriseTime\",\"config\":{},\"title\":\"Sunrise and sunset time\","
                        + "\"titleTemplateUrl\":\"../src/templates/widget-title.html\",\"wid\":\"1454685464871-1\"}],"
                        + "\"cid\":\"1454699630217-7\"},{\"styleClass\":\"col-md-9\",\"rows\":[{\"columns\":"
                        + "[{\"styleClass\":\"col-md-12\",\"widgets\":[],\"cid\":\"1454699630245-9\"}]},{\"columns\":"
                        + "[{\"styleClass\":\"col-md-6\",\"widgets\":[],\"cid\":\"1454699630246-10\"},"
                        + "{\"styleClass\":\"col-md-6\",\"widgets\":[],\"cid\":\"1454699630247-11\"}]}],"
                        + "\"widgets\":[],\"cid\":\"1454699630217-8\"}]}]")
                .build();
        dashboard.update(true);
        return getDashboard(user, name);

    }
}
