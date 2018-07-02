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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Builder
@ToString(includeFieldNames = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Variable {
    public static final String KEY_VARIABLES_REPOSITORY = "variablesRepository";
    public static final String SKEY_ID = "id";
    public static final String SKEY_KEY = "key";
    public static final String SKEY_VALUE = "value";
    public static final String SKEY_VALUE2 = "value2";
    public static final String SKEY_VALUE3 = "value3";

    private Integer id;
    private String key;//In settings this key will be treated as sub key
    private String value;
    private String value2;
    private String value3;

    public static Variable get(String key) {
        return get(SettingsUtils.getSettings(KEY_VARIABLES_REPOSITORY, key));
    }

    public static Variable get(Integer id) {
        return get(DaoUtils.getSettingsDao().getById(id));
    }

    public static Variable get(Settings settings) {
        if (settings != null && settings.getKey().equals(KEY_VARIABLES_REPOSITORY)) {
            return Variable.builder()
                    .id(settings.getId())
                    .key(settings.getSubKey())
                    .value(settings.getValue())
                    .value2(settings.getValue2())
                    .value3(settings.getValue3())
                    .build();
        }
        return null;
    }

    public void save() {
        if (id != null) {
            Settings settings = DaoUtils.getSettingsDao().getById(id);
            if (!settings.getKey().equals(KEY_VARIABLES_REPOSITORY)) {
                //This is not a Variable
                return;
            }
        }
        SettingsUtils.updateSettings(Settings.builder()
                .id(id)
                .key(KEY_VARIABLES_REPOSITORY)
                .subKey(key)
                .value(value)
                .value2(value2)
                .value3(value3)
                .build());
    }

}
