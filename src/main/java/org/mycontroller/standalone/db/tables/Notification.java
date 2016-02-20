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
import org.mycontroller.standalone.notification.NotificationUtils;
import org.mycontroller.standalone.notification.NotificationUtils.NOTIFICATION_TYPE;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@DatabaseTable(tableName = DB_TABLES.NOTIFICATION)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class Notification {
    public static final String KEY_ID = "id";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_PUBLIC_ACCESS = "publicAccess";
    public static final String KEY_NAME = "name";
    public static final String KEY_LAST_EXECUTION = "lastExecution";
    public static final String KEY_TYPE = "type";

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, columnName = KEY_PUBLIC_ACCESS)
    private Boolean publicAccess;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = KEY_USER_ID, foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 0)
    private User user;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_NAME)
    private String name;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_INTEGER, columnName = KEY_TYPE)
    private NOTIFICATION_TYPE type;

    @DatabaseField(canBeNull = true, columnName = KEY_LAST_EXECUTION)
    private Long lastExecution;

    @DatabaseField(canBeNull = true)
    private String variable1;

    @DatabaseField(canBeNull = true, width = 500)
    private String variable2;

    @DatabaseField(canBeNull = true)
    private String variable3;

    @DatabaseField(canBeNull = true)
    private String variable4;

    @DatabaseField(canBeNull = true)
    private String variable5;

    @DatabaseField(canBeNull = true)
    private String variable6;

    @DatabaseField(canBeNull = true)
    private String variable7;

    public String getNotificationString() {
        return NotificationUtils.getNotificationString(this);
    }

}
