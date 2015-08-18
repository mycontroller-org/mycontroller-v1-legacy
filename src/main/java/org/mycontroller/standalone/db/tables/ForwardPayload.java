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

import com.j256.ormlite.field.DatabaseField;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ForwardPayload {
    public static final String SENSOR_REF_ID = "sensor_ref_id";
    public static final String FORWARD_SENSOR_REF_ID = "forward_sensor_ref_id";

    public ForwardPayload() {

    }

    public ForwardPayload(int id) {
        this.id = id;
    }

    public ForwardPayload(Sensor sensorSource, Sensor sensorProxy) {
        this.sensorSource = sensorSource;
        this.sensorDestination = sensorProxy;
    }

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = SENSOR_REF_ID)
    private Sensor sensorSource;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = FORWARD_SENSOR_REF_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Sensor sensorDestination;


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

}