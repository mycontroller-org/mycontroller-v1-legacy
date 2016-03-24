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
import org.mycontroller.standalone.ObjectFactory;
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
        this.id = sensorVariable.getId();
        if (sensorVariable.getVariableType() != null) {
            this.type = LocaleString.builder().en(sensorVariable.getVariableType().getText())
                    .locale(ObjectFactory.getMcLocale().getString(sensorVariable.getVariableType().name())).build();
        }
        if (sensorVariable.getMetricType() != null) {
            this.metricType = sensorVariable.getMetricType().getText();
        }
        this.unit = sensorVariable.getUnit();
        this.value = sensorVariable.getValue();
        this.friendlyValue = SensorUtils.getValue(sensorVariable);
        this.timestamp = sensorVariable.getTimestamp();
        this.sensorName = sensorVariable.getSensor().getName();
        this.resourceName = new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails();
    }

}
