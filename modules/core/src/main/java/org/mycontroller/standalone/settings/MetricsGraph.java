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

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
import java.util.ArrayList;
import java.util.HashMap;

import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(includeFieldNames = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsGraph {
    private Integer id;
    private String metricName;
    private String type;
    private String interpolate;
    private String subType;
    private String color;
    private Integer marginLeft;
    private Integer marginRight;
    private Integer marginTop;
    private Integer marginBottom;

    public enum CHART_TYPE {
        LINE_CHART("lineChart"),
        HISTORICAL_BAR_CHART("historicalBarChart"),
        STACKED_AREA_CHART("stackedAreaChart"),
        MULTI_CHART("multiChart"),
        LINE_PLUS_BAR_CHART("linePlusBarChart");

        private final String name;

        private CHART_TYPE(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static CHART_TYPE get(int id) {
            for (CHART_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static CHART_TYPE fromString(String text) {
            if (text != null) {
                for (CHART_TYPE type : CHART_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static MetricsGraph get(HashMap<String, Object> gMap) {
        return MetricsGraph.builder()
                .type((String) gMap.get(SensorVariable.KEY_GP_TYPE))
                .interpolate((String) gMap.get(SensorVariable.KEY_GP_INTERPOLATE))
                .subType((String) gMap.get(SensorVariable.KEY_GP_SUBTYPE))
                .color((String) gMap.get(SensorVariable.KEY_GP_COLOR))
                .marginLeft((Integer) gMap.get(SensorVariable.KEY_GP_MARGIN_LEFT))
                .marginRight((Integer) gMap.get(SensorVariable.KEY_GP_MARGIN_RIGHT))
                .marginTop((Integer) gMap.get(SensorVariable.KEY_GP_MARGIN_TOP))
                .marginBottom((Integer) gMap.get(SensorVariable.KEY_GP_MARGIN_BOTTOM))
                .build();
    }

    @JsonIgnore
    public static ArrayList<MESSAGE_TYPE_SET_REQ> variables = new ArrayList<MESSAGE_TYPE_SET_REQ>();
    static {
        variables.add(MESSAGE_TYPE_SET_REQ.V_ARMED);
        variables.add(MESSAGE_TYPE_SET_REQ.V_CURRENT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_CUSTOM);
        variables.add(MESSAGE_TYPE_SET_REQ.V_DIRECTION);
        variables.add(MESSAGE_TYPE_SET_REQ.V_DISTANCE);
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
        variables.add(MESSAGE_TYPE_SET_REQ.V_UV);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VOLTAGE);
        variables.add(MESSAGE_TYPE_SET_REQ.V_VOLUME);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WATT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WEIGHT);
        variables.add(MESSAGE_TYPE_SET_REQ.V_WIND);
    }

}
