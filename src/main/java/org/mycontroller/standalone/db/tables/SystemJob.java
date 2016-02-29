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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.SYSTEM_JOB)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class SystemJob {
    public static final String ENABLED = "enabled";

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(unique = true, canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String cron;
    @DatabaseField(canBeNull = false, columnName = ENABLED)
    private Boolean enabled;
    @DatabaseField(canBeNull = false)
    private String className;

}
