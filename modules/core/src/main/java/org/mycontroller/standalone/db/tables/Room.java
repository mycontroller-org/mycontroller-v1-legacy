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

import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.RoomUtils;

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
@DatabaseTable(tableName = DB_TABLES.ROOM)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Room {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PARENT_ID = "parentId";
    public static final String KEY_ICON = "icon";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(columnName = KEY_NAME, uniqueCombo = true, canBeNull = false)
    private String name;

    @DatabaseField(columnName = KEY_DESCRIPTION, canBeNull = true)
    private String description;

    @DatabaseField(columnName = KEY_PARENT_ID, uniqueCombo = true, canBeNull = true)
    private Integer parentId;

    @DatabaseField(columnName = KEY_ICON, canBeNull = true)
    private String icon;

    private String fullPath;

    public String getFullPath() {
        if (fullPath == null) {
            fullPath = RoomUtils.getFullPath(this);
        }
        return fullPath;
    }
}
