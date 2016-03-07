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
package org.mycontroller.standalone.db.tables;

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.db.DB_TABLES;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.UID_TAG)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UidTag {
    public static final String SENSOR_REF_ID = "sensor_ref_id";
    public static final String STARTS_WITH = "uid";
    public static final String SPLITER = "|"; //Check it in UidTagMapper, for special chars we have added '\\'

    public UidTag() {

    }

    public UidTag(Integer uid) {
        this(uid, null);
    }

    public UidTag(Integer uid, Sensor sensor) {
        this.uid = uid;
        this.sensor = sensor;
    }

    @DatabaseField(canBeNull = false, unique = true, id = true)
    private Integer uid;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = SENSOR_REF_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Sensor sensor;

    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private MESSAGE_TYPE_SET_REQ variableType;

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UID:").append(this.uid);
        builder.append(", Sensor:[").append(this.sensor).append("]");
        builder.append(", Variable Type:").append(this.variableType);
        return builder.toString();
    }

    public Integer getUid() {
        return uid;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public MESSAGE_TYPE_SET_REQ getVariableType() {
        return variableType;
    }

    public void setVariableType(MESSAGE_TYPE_SET_REQ variableType) {
        this.variableType = variableType;
    }

}