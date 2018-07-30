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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.mycontroller.standalone.db.DB_TABLES;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.USER_SETTINGS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSettings {
    public static final String USER_ID = "user_id";

    public UserSettings() {

    }

    public UserSettings(User user, String key, String value) {
        this.user = user;
        this.key = key;
        this.value = value;
    }

    public UserSettings(User user, String key) {
        this.user = user;
        this.key = key;
    }

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;

    @DatabaseField(uniqueCombo = true, canBeNull = true, columnName = USER_ID, foreign = true,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private User user;

    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private String key;

    @DatabaseField(canBeNull = true)
    private String value;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String id) {
        this.key = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
