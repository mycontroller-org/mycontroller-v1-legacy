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
import org.mycontroller.standalone.db.SensorLogUtils.LOG_TYPE;

import com.j256.ormlite.field.DatabaseField;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SensorLog {
    public static final String SENSOR_REF_ID = "sensor_ref_id";

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = true, foreign = true, columnName = SENSOR_REF_ID)
    private Sensor sensor;

    @DatabaseField(canBeNull = false)
    private Long timestamp;

    @DatabaseField(canBeNull = false)
    private Integer logType;

    @DatabaseField(canBeNull = true)
    private Boolean sent;

    @DatabaseField(canBeNull = false)
    private String log;

    public SensorLog() {

    }

    public SensorLog(Integer id) {
        this.id = id;
    }

    public SensorLog(Integer id, Long timestamp, Integer logType) {
        this(id, timestamp, logType, null);
    }

    public SensorLog(Integer id, Long timestamp, Integer logType, Boolean sent) {
        this.id = id;
        this.timestamp = timestamp;
        this.logType = logType;
        this.sent = sent;
    }

    public Integer getId() {
        return id;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getLogType() {
        return logType;
    }

    public String getLog() {
        return log;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLogType(Integer logType) {
        this.logType = logType;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getLogTypeString() {
        if (this.logType != null) {
            return LOG_TYPE.get(this.logType).value();
        }
        return "-";
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public String getStatus() {
        if (this.sent != null) {
            return sent ? "Sent" : "Received";
        } else {
            return "-";
        }
    }

}
