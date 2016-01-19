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
package org.mycontroller.standalone.db.tables;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_DIRECTION;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.model.ResourceModel;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.Builder;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.RESOURCES_LOGS)
@Builder
@ToString(includeFieldNames = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourcesLogs {

    public static final String KEY_ID = "id";
    public static final String KEY_LOG_LEVEL = "logLevel";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_MESSAGE_TYPE = "messageType";
    public static final String KEY_LOG_DIRECTION = "logDirection";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    @DatabaseField(generatedId = true, columnName = KEY_ID)
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

}
