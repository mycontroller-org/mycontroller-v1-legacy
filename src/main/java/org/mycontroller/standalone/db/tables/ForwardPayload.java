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
package org.mycontroller.standalone.db.tables;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = "forward_payload")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForwardPayload {
    public static final String SENSOR_REF_ID = "sensor_ref_id";
    public static final String FORWARD_SENSOR_REF_ID = "forward_sensor_ref_id";
    public static final String SOURCE_TYPE = "s_var_type";
    public static final String DESTINATION_TYPE = "d_var_type";

    public ForwardPayload() {

    }

    public ForwardPayload(int id) {
        this.id = id;
    }

    public ForwardPayload(Sensor sensorSource, Sensor sensorProxy, Integer sourceType, Integer destinationType) {
        this.sensorSource = sensorSource;
        this.sensorDestination = sensorProxy;
        this.sourceType = sourceType;
        this.destinationType = destinationType;
    }

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = SENSOR_REF_ID)
    private Sensor sensorSource;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = FORWARD_SENSOR_REF_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Sensor sensorDestination;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = SOURCE_TYPE)
    private Integer sourceType;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = DESTINATION_TYPE)
    private Integer destinationType;

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Integer getId() {
        return id;
    }

    public Sensor getSensorSource() {
        return sensorSource;
    }

    public Sensor getSensorDestination() {
        return sensorDestination;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSensorSource(Sensor sensorSource) {
        this.sensorSource = sensorSource;
    }

    public void setSensorDestination(Sensor sensorDestination) {
        this.sensorDestination = sensorDestination;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public Integer getDestinationType() {
        return destinationType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public void setDestinationType(Integer destinationType) {
        this.destinationType = destinationType;
    }

    public String getSourceTypeString() {
        return MESSAGE_TYPE_SET_REQ.get(sourceType).toString();
    }

    public String getDestinationTypeString() {
        return MESSAGE_TYPE_SET_REQ.get(destinationType).toString();
    }

}