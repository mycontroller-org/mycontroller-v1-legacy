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

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DB_TABLES;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@DatabaseTable(tableName = DB_TABLES.RESOURCES_GROUP)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourcesGroup {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_STATE = "state";
    public static final String KEY_STATE_SINCE = "stateSince";

    public ResourcesGroup() {
    }

    public ResourcesGroup(Integer id) {
        this(id, null);
    }

    public ResourcesGroup(String name) {
        this(null, name);
    }

    public ResourcesGroup(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_NAME)
    private String name;

    @DatabaseField(canBeNull = true, columnName = KEY_DESCRIPTION)
    private String description;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_STATE)
    private STATE state = STATE.UNAVAILABLE;

    @DatabaseField(canBeNull = true, columnName = KEY_STATE_SINCE)
    private Long stateSince;

    @JsonGetter(value = "id")
    public Integer getId() {
        return id;
    }

    @JsonIgnore
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public STATE getState() {
        return state;
    }

    @JsonIgnore
    public void setState(STATE state) {
        this.state = state;
    }

    @JsonGetter(value = "state")
    public String getStateString() {
        return state.getText();
    }

    public Long getStateSince() {
        return stateSince;
    }

    public void setStateSince(Long stateSince) {
        this.stateSince = stateSince;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id).append(", Name:").append(this.name).append(", Description:")
                .append(this.description).append(", State:").append(this.state.getText()).append(", State since:")
                .append(this.stateSince);
        return builder.toString();
    }

}
