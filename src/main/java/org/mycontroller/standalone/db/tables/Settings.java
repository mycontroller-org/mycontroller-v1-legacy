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

import org.mycontroller.standalone.db.DB_TABLES;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

import lombok.ToString;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.SETTINGS)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class Settings {

    public static final String KEY_ID = "id";
    public static final String KEY_KEY = "key";
    public static final String KEY_SUB_KEY = "subKey";
    public static final String KEY_VALUE = "value";
    public static final String KEY_ALT_VALUE = "altValue";

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;
    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_KEY)
    private String key;
    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_SUB_KEY)
    private String subKey;
    @DatabaseField(canBeNull = true, columnName = KEY_VALUE)
    private String value;
    @DatabaseField(canBeNull = true, columnName = KEY_ALT_VALUE)
    private String altValue;

}
