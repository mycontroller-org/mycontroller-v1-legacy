/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;

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
public class UnitsSettings {
    public static final String KEY_VARIABLE_UNIT = "variableUnit";

    private List<Unit> variables;

    public static UnitsSettings get() {
        ArrayList<Unit> variables = new ArrayList<Unit>();
        Settings settings = null;
        for (MESSAGE_TYPE_SET_REQ sVariable : Unit.variables) {
            settings = getSettings(sVariable.getText());
            if (settings != null) {
                variables.add(new Unit(sVariable.getText(), settings.getValue(), settings.getAltValue()));
            }
        }
        return UnitsSettings.builder().variables(variables).build();
    }

    public void save() {
        for (Unit unit : variables) {
            if (Unit.variables.contains(MESSAGE_TYPE_SET_REQ.fromString(unit.getVariable()))) {
                updateValue(unit.getVariable(), unit.getMetric(), unit.getImperial());
            }
        }
    }

    private static Settings getSettings(String subKey) {
        return SettingsUtils.getSettings(KEY_VARIABLE_UNIT, subKey);
    }

    private void updateValue(String subKey, Object value, Object altValue) {
        SettingsUtils.updateValue(KEY_VARIABLE_UNIT, subKey, value, altValue);
    }
}
