/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.externalserver.ExternalServerUtils.EXTERNAL_SERVER_TYPE;

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

@DatabaseTable(tableName = DB_TABLES.EXTERNAL_SERVER)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ExternalServerTable {
    public static final String KEY_ID = "id";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_KEY_FORMAT = "keyFormat";
    public static final String KEY_PROPERTIES = "properties";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_TYPE)
    private EXTERNAL_SERVER_TYPE type;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, unique = true, columnName = KEY_NAME)
    private String name;

    @DatabaseField(canBeNull = true, columnName = KEY_KEY_FORMAT)
    private String keyFormat;

    @DatabaseField(canBeNull = true, columnName = KEY_PROPERTIES, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Object> properties;

}
