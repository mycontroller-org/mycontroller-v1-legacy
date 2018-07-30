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
package org.mycontroller.standalone.db.tables;

import java.util.List;

import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.SENSOR)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class Sensor {
    public static final String KEY_ID = "id";
    public static final String KEY_SENSOR_ID = "sensorId";
    public static final String KEY_NODE_ID = "nodeId";
    public static final String KEY_NODE_NAME = "nodeName";
    public static final String KEY_NODE_EUI = "nodeEui";
    public static final String KEY_NAME = "name";
    public static final String KEY_LAST_SEEN = "lastSeen";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ROOM_ID = "roomId";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, index = true, uniqueCombo = true, columnName = KEY_SENSOR_ID)
    private String sensorId;

    @DatabaseField(dataType = DataType.ENUM_STRING, columnName = KEY_TYPE)
    private MESSAGE_TYPE_PRESENTATION type;

    @DatabaseField(columnName = KEY_NAME)
    private String name;

    @DatabaseField(columnName = KEY_LAST_SEEN)
    private Long lastSeen;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_NODE_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Node node;

    @DatabaseField(canBeNull = true, foreign = true, columnName = KEY_ROOM_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private Room room;

    private List<String> variableTypes;

    public List<String> getVariableTypes() {
        if (this.variableTypes == null) {
            this.variableTypes = SensorUtils.getVariableTypes(this);
        }
        return this.variableTypes;
    }

    @JsonIgnore
    public List<SensorVariable> getVariables() {
        return DaoUtils.getSensorVariableDao().getAllBySensorId(this.getId());
    }

    //This method is used to create variables for this sensor,
    //But in mixins(SensorMixin) we need id of this sensor, if we use we 'getId'cannot generate 'id'
    //So added this ugly workaround.
    @JsonGetter
    private Integer getIdforVariables() {
        return this.id;
    }

}
