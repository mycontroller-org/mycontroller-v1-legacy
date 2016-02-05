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

import org.mycontroller.standalone.db.tables.Settings;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

    public static Dashboard get(Settings settings) {
        if (settings == null) {
            return Dashboard.builder().build();
        }
        return Dashboard.builder()
                .id(settings.getId())
                .userId(settings.getUserId())
                .name(settings.getSubKey())
                .title(settings.getValue())
                .rows(settings.getValue2())
                .structure(settings.getValue3())
                .build();
    }

    public void update() {
        update(false);
    }

    public void update(boolean forceCreate) {
        SettingsUtils.updateSettings(Settings.builder()
                .id(id)
                .key(KEY_DASHBOARD)
                .userId(userId)
                .subKey(name)
                .value(title)
                .value2(rows)
                .value3(structure)
                .build(), forceCreate);
    }
}
