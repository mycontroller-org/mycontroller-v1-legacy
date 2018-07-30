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
package org.mycontroller.standalone.api.jaxrs.model;

import java.util.ArrayList;

import org.mycontroller.standalone.settings.MetricsGraph.CHART_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsChartDataNVD3 {
    private Integer id;
    private String resourceName;
    private String key;
    private ArrayList<Object> values;
    private Double mean;
    private String type;
    private Integer yAxis;
    private Boolean area;
    private Boolean bar;
    private String color;
    private String interpolate;

    @JsonProperty("yAxis")
    @JsonGetter("yAxis")
    public Integer getYAxis() {
        return yAxis;
    }

    private void updateType() {
        if (type.equalsIgnoreCase("area")) {
            area = true;
        } else if (type.equalsIgnoreCase("bar")) {
            bar = true;
        }
        type = null;
    }

    private void updateYAxis() {
        if (yAxis == null) {
            yAxis = 1;
        }
    }

    public MetricsChartDataNVD3 updateSubType(String mainType) {
        switch (CHART_TYPE.fromString(mainType)) {
            case LINE_CHART:
                if (type.equalsIgnoreCase("area")) {
                    area = true;
                }
                type = null;
                break;
            case HISTORICAL_BAR_CHART:
            case STACKED_AREA_CHART:
                updateType();
                break;
            case LINE_PLUS_BAR_CHART:
                updateType();
                updateYAxis();
                break;
            case MULTI_CHART:
                updateYAxis();
                break;
            default:
        }
        return this;
    }
}