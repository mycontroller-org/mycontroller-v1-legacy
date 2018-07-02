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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Builder
@ToString
@Data
@DatabaseTable(tableName = DB_TABLES.UID_TAG)
@NoArgsConstructor
@AllArgsConstructor
public class UidTag {
    public static final String KEY_ID = "id";
    public static final String KEY_UID = "uid";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, unique = true, columnName = KEY_UID)
    private String uid;

    @DatabaseField(canBeNull = false, columnName = KEY_RESOURCE_TYPE, dataType = DataType.ENUM_STRING)
    private RESOURCE_TYPE resourceType;

    @DatabaseField(canBeNull = false, columnName = KEY_RESOURCE_ID)
    private Integer resourceId;

    @JsonIgnore
    Object resource;

    @JsonIgnore
    public Object getResource() {
        if (resource == null) {
            ResourceModel model = new ResourceModel(getResourceType(), getResourceId());
            resource = model != null ? model.getResource() : null;
        }
        return resource;
    }

    @JsonProperty("resourceName")
    private String getResourceString() {
        return new ResourceModel(getResourceType(), getResourceId()).getResourceLessDetails();
    }
}