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
package org.mycontroller.standalone.settings;

import java.util.UUID;

import org.mycontroller.standalone.db.tables.Settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@ToString(includeFieldNames = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dashboard {
    public static final String KEY_DASHBOARD = "dashboard";

    private Integer id;
    private Integer userId;
    private String name;
    private String title;
    private String structure;
    private String rows;
    private String uuid;

    public String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public static Dashboard get(Settings settings, boolean updateRowData) {
        if (settings == null) {
            return Dashboard.builder().build();
        }
        Dashboard dashboard = Dashboard.builder()
                .id(settings.getId())
                .userId(settings.getUserId())
                .name(settings.getSubKey())
                .title(settings.getValue())
                .structure(settings.getValue3())
                .uuid(settings.getValue4())
                .build();
        if (updateRowData) {
            dashboard.setRows(DashboardSettings.loadFromDisk(dashboard.getUuid()));
        }
        return dashboard;
    }

    public void loadRows() {
        setRows(DashboardSettings.loadFromDisk(getUuid()));
    }

    public void update() {
        update(false);
    }

    public void update(boolean forceCreate) {
        SettingsUtils.updateSettings(Settings.builder()
                .id(getId())
                .key(KEY_DASHBOARD)
                .userId(getUserId())
                .subKey(getName())
                .value(getTitle())
                .value3(getStructure())
                .value4(getUuid())
                .build(), forceCreate);
        // store row data
        DashboardSettings.writeToDisk(getUuid(), getRows());
    }
}
