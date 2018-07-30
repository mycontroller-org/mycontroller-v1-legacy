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

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_DIRECTION;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.model.ResourceModel;

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
@DatabaseTable(tableName = DB_TABLES.RESOURCES_LOGS)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class ResourcesLogs {

    public static final String KEY_ID = "id";
    public static final String KEY_LOG_LEVEL = "logLevel";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_MESSAGE_TYPE = "messageType";
    public static final String KEY_LOG_DIRECTION = "logDirection";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(dataType = DataType.ENUM_INTEGER, columnName = KEY_LOG_LEVEL)
    private LOG_LEVEL logLevel;

    @DatabaseField(dataType = DataType.ENUM_STRING, columnName = KEY_RESOURCE_TYPE)
    private RESOURCE_TYPE resourceType;

    @DatabaseField(canBeNull = true, columnName = KEY_RESOURCE_ID)
    private Integer resourceId; /* log reference from where, example: sensorId, NodeId, etc.,*/

    @DatabaseField(dataType = DataType.ENUM_INTEGER, columnName = KEY_MESSAGE_TYPE)
    private MESSAGE_TYPE messageType;

    @DatabaseField(dataType = DataType.ENUM_INTEGER, columnName = KEY_LOG_DIRECTION)
    private LOG_DIRECTION logDirection;

    @DatabaseField(canBeNull = false, columnName = KEY_MESSAGE)
    private String message;

    @DatabaseField(canBeNull = false, columnName = KEY_TIMESTAMP)
    private Long timestamp;

    public String getResource() {
        try {
            return new ResourceModel(this.resourceType, this.resourceId).getResourceLessDetails();
        } catch (Exception ex) {
            //This resource not available.
        }
        return "Resource not available!";
    }

    public static ResourcesLogs get(HashMap<String, Object> filters) {
        return ResourcesLogs.builder()
                .id((Integer) filters.get(KEY_ID))
                .logLevel(LOG_LEVEL.fromString((String) filters.get(KEY_LOG_LEVEL)))
                .resourceType(RESOURCE_TYPE.fromString((String) filters.get(KEY_RESOURCE_TYPE)))
                .resourceId((Integer) filters.get(KEY_RESOURCE_ID))
                .messageType(MESSAGE_TYPE.fromString((String) filters.get(KEY_MESSAGE_TYPE)))
                .logDirection(LOG_DIRECTION.fromString((String) filters.get(KEY_LOG_DIRECTION)))
                .message((String) filters.get(KEY_MESSAGE))
                .timestamp((Long) filters.get(KEY_TIMESTAMP))
                .build();
    }

}
