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
package org.mycontroller.standalone.api.jaxrs.json;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.model.ResourceModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Data;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@JsonAutoDetect
@Data
public class SensorVariableJson {

    private Integer id;
    private LocaleString type;
    private String metricType;
    private String unit;
    private Object value;
    private String friendlyValue;
    private Long timestamp;
    private String sensorName;
    private String resourceName;

    @JsonCreator
    private SensorVariableJson() {

    }

    public SensorVariableJson(SensorVariable sensorVariable) {
        id = sensorVariable.getId();
        if (sensorVariable.getVariableType() != null) {
            type = LocaleString.builder().en(sensorVariable.getVariableType().getText())
                    .locale(McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name())).build();
        }
        if (sensorVariable.getMetricType() != null) {
            metricType = sensorVariable.getMetricType().getText();
        }
        unit = sensorVariable.getUnit();
        value = sensorVariable.getValue();
        friendlyValue = SensorUtils.getValue(sensorVariable);
        timestamp = sensorVariable.getTimestamp();
        sensorName = sensorVariable.getSensor().getName();
        resourceName = new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails();
    }

}
