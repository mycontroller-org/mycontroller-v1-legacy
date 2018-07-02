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

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.model.ResourceModel;

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
 * @since 0.0.3
 */

@DatabaseTable(tableName = DB_TABLES.RESOURCE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Resource {
    public static final String KEY_ID = "id";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING,
            columnName = KEY_RESOURCE_TYPE, uniqueCombo = true)
    private RESOURCE_TYPE resourceType;

    @DatabaseField(canBeNull = false, columnName = KEY_RESOURCE_ID, uniqueCombo = true)
    private Integer resourceId;

    private List<Integer> externalServers;

    private List<String> externalServersList;

    @JsonIgnore
    private List<ExternalServerTable> externalServersObject;

    private String resourceName;

    private void updateExternalServers() {
        List<ExternalServerResourceMap> resourceServerMapList = DaoUtils.getExternalServerResourceMapDao()
                .getAllByResourceId(id);
        externalServers = new ArrayList<Integer>();
        externalServersList = new ArrayList<String>();
        externalServersObject = new ArrayList<ExternalServerTable>();
        if (resourceServerMapList != null && !resourceServerMapList.isEmpty()) {
            for (ExternalServerResourceMap resourceServerMap : resourceServerMapList) {
                externalServers.add(resourceServerMap.getExternalServerTable().getId());
                externalServersList.add(resourceServerMap.getExternalServerTable().getName());
                externalServersObject.add(resourceServerMap.getExternalServerTable());
            }
        }
    }

    public List<Integer> getExternalServers() {
        if (externalServers == null) {
            updateExternalServers();
        }
        return externalServers;
    }

    public List<String> getExternalServersList() {
        if (externalServersList == null) {
            updateExternalServers();
        }
        return externalServersList;
    }

    public String getResourceName() {
        if (resourceName == null) {
            resourceName = new ResourceModel(resourceType, resourceId).getResourceLessDetails();
        }
        return resourceName;
    }

    public List<ExternalServerTable> getExternalServersObject() {
        if (externalServersObject == null) {
            updateExternalServers();
        }
        return externalServersObject;
    }
}
