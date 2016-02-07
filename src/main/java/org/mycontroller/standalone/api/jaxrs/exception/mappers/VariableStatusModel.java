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
package org.mycontroller.standalone.api.jaxrs.exception.mappers;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.model.ResourceModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@JsonAutoDetect
public class VariableStatusModel {

    private Integer id;
    private String type;
    private String metricType;
    private String unit;
    private Object value;
    private String friendlyValue;
    private Long timestamp;
    private String sensorName;
    private String resourceName;

    @JsonCreator
    private VariableStatusModel() {

    }

    public VariableStatusModel(SensorVariable sensorVariable) {
        this.id = sensorVariable.getId();
        if (sensorVariable.getVariableType() != null) {
            this.type = sensorVariable.getVariableType().getText();
        }
        if (sensorVariable.getMetricType() != null) {
            this.metricType = sensorVariable.getMetricType().getText();
        }
        this.unit = sensorVariable.getUnit();
        this.value = sensorVariable.getValue();
        this.friendlyValue = SensorUtils.getValue(sensorVariable);
        this.timestamp = sensorVariable.getTimestamp();
        this.sensorName = sensorVariable.getSensor().getSensorId() + ":" + sensorVariable.getSensor().getName();
        this.resourceName = new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getFriendlyValue() {
        return friendlyValue;
    }

    public void setFriendlyValue(String friendlyValue) {
        this.friendlyValue = friendlyValue;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getResourceName() {
        return resourceName;
    }

}
