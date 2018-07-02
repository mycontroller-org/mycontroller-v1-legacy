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

import org.mycontroller.standalone.utils.McUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MySensorsSettings {
    public static final String KEY_MY_SENSORS = "mySensors";
    public static final String SKEY_DEFAULT_FIRMWARE = "defaultFirmware";
    public static final String SKEY_ENABLE_DEFAULT_ON_NO_FIRMWARE = "enableDefaultOnNoFirmware";

    private Integer defaultFirmware;
    private Boolean enbaledDefaultOnNoFirmware;

    public static MySensorsSettings get() {
        return MySensorsSettings
                .builder()
                .defaultFirmware(McUtils.getInteger(getValue(SKEY_DEFAULT_FIRMWARE)))
                .enbaledDefaultOnNoFirmware(McUtils.getBoolean(getValue(SKEY_ENABLE_DEFAULT_ON_NO_FIRMWARE)))
                .build();
    }

    public void save() {
        updateValue(SKEY_DEFAULT_FIRMWARE, this.defaultFirmware);
        updateValue(SKEY_ENABLE_DEFAULT_ON_NO_FIRMWARE, this.enbaledDefaultOnNoFirmware);
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_MY_SENSORS, subKey);
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_MY_SENSORS, subKey, value);
    }
}
