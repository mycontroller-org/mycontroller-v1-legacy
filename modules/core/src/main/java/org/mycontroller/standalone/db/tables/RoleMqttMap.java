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

import org.mycontroller.standalone.db.DB_TABLES;

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
@DatabaseTable(tableName = DB_TABLES.ROLE_MQTT_MAP)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class RoleMqttMap {
    public static final String KEY_ROLE_ID = "roleId";
    public static final String KEY_PUBLISH = "publish";
    public static final String KEY_SUBSCRIBE = "subscribe";

    @DatabaseField(canBeNull = false, columnName = KEY_ROLE_ID, unique = true, foreign = true,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 0)
    private Role role;

    @DatabaseField(canBeNull = true, dataType = DataType.SERIALIZABLE, columnName = KEY_PUBLISH)
    private ArrayList<String> publish;

    @DatabaseField(canBeNull = true, dataType = DataType.SERIALIZABLE, columnName = KEY_SUBSCRIBE)
    private ArrayList<String> subscribe;
}