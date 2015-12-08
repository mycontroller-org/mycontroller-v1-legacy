/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.api.jaxrs.mapper;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@JsonRootName(value = "status")
public class SensorStatus {

    private String variableType;
    private Object value;
    private String unit;
    private String friendlyValue;
    private Long timestamp;

    public SensorStatus() {

    }

    public SensorStatus(String variableType, Object value, String unit) {
        this(variableType, value, unit, null, null);
    }

    public SensorStatus(String variableType, Object value, String unit, Long timestamp) {
        this(variableType, value, unit, null, timestamp);
    }

    public SensorStatus(String variableType, Object value, String unit, String friendlyValue, Long timestamp) {
        this.variableType = variableType;
        this.value = value;
        this.unit = unit;
        this.friendlyValue = friendlyValue;
        this.timestamp = timestamp;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
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
}
