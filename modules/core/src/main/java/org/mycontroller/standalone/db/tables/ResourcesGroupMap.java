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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DB_TABLES;
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
 * @since 0.0.2
 */

@DatabaseTable(tableName = DB_TABLES.RESOURCES_GROUP_MAP)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class ResourcesGroupMap {
    public static final String KEY_ID = "id";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_PAYLOAD_ON = "payloadOn";
    public static final String KEY_PAYLOAD_OFF = "payloadOff";

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_GROUP_ID,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private ResourcesGroup resourcesGroup;

    @DatabaseField(dataType = DataType.ENUM_STRING, uniqueCombo = true,
            canBeNull = false, columnName = KEY_RESOURCE_TYPE)
    private RESOURCE_TYPE resourceType;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_RESOURCE_ID)
    private Integer resourceId;

    @DatabaseField(canBeNull = true, columnName = KEY_PAYLOAD_ON)
    private String payloadOn;

    @DatabaseField(canBeNull = true, columnName = KEY_PAYLOAD_OFF)
    private String payloadOff;

    public String getResource() {
        ResourceModel resourceModel = new ResourceModel(resourceType, resourceId);
        return resourceModel.getResourceLessDetails();
    }

}
