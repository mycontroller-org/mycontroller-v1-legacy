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

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
import java.util.ArrayList;

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsGraph {
    private Integer id;
    private String metricName;
    private String type;
    private String interpolate;
    private Boolean area;
    private Boolean bar;
    private String color;

    @JsonIgnore
    public static ArrayList<MESSAGE_TYPE_SET_REQ> variables = new ArrayList<MESSAGE_TYPE_SET_REQ>();
    static {
        variables.add(MESSAGE_TYPE_SET_REQ.V_ARMED);
        variables.add(MESSAGE_TYPE_SET_REQ.V_CURRENT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_DIRECTION);
        variables.add(MESSAGE_TYPE_SET_REQ.V_DISTANCE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_DOWN);
        variables.add(MESSAGE_TYPE_SET_REQ.V_FLOW);
        variables.add(MESSAGE_TYPE_SET_REQ.V_GUST);
        variables.add(MESSAGE_TYPE_SET_REQ.V_HUM);
        variables.add(MESSAGE_TYPE_SET_REQ.V_IMPEDANCE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_KWH);
        variables.add(MESSAGE_TYPE_SET_REQ.V_LEVEL);
        variables.add(MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL);
        variables.add(MESSAGE_TYPE_SET_REQ.V_LOCK_STATUS);
        variables.add(MESSAGE_TYPE_SET_REQ.V_PERCENTAGE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_PRESSURE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_SCENE_OFF);
        variables.add(MESSAGE_TYPE_SET_REQ.V_SCENE_ON);
        variables.add(MESSAGE_TYPE_SET_REQ.V_STATUS);
        variables.add(MESSAGE_TYPE_SET_REQ.V_RAIN);
        variables.add(MESSAGE_TYPE_SET_REQ.V_RAINRATE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_TEMP);
        variables.add(MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        variables.add(MESSAGE_TYPE_SET_REQ.V_UP);
        variables.add(MESSAGE_TYPE_SET_REQ.V_UV);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VOLTAGE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VOLUME);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WATT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WEIGHT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WIND);
    }

}
