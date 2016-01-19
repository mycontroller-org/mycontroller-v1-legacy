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

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@JsonTypeName("unit")
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
public class Unit {
    private String variable;
    private String metric;
    private String imperial;

    @JsonIgnore
    public static ArrayList<MESSAGE_TYPE_SET_REQ> variables = new ArrayList<MESSAGE_TYPE_SET_REQ>();
    static {
        variables.add(MESSAGE_TYPE_SET_REQ.V_CURRENT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_CUSTOM);
        variables.add(MESSAGE_TYPE_SET_REQ.V_DIRECTION);
        variables.add(MESSAGE_TYPE_SET_REQ.V_DISTANCE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_FLOW);
        variables.add(MESSAGE_TYPE_SET_REQ.V_GUST);
        variables.add(MESSAGE_TYPE_SET_REQ.V_HUM);
        variables.add(MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_COOL);
        variables.add(MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_HEAT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_IMPEDANCE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_KWH);
        variables.add(MESSAGE_TYPE_SET_REQ.V_LEVEL);
        variables.add(MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL);
        variables.add(MESSAGE_TYPE_SET_REQ.V_PERCENTAGE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_RAIN);
        variables.add(MESSAGE_TYPE_SET_REQ.V_RAINRATE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_TEMP);
        variables.add(MESSAGE_TYPE_SET_REQ.V_UV);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VAR1);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VAR2);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VAR3);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VAR4);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VAR5);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VOLTAGE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VOLUME);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WATT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WEIGHT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WIND);
    }

    public Unit(String vaiable, String metric, String imperial) {
        this.variable = vaiable;
        this.metric = metric;
        this.imperial = imperial;
    }
}