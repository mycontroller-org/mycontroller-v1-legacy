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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;

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
@DatabaseTable(tableName = DB_TABLES.RULE_DEFINITION)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class RuleDefinitionTable {
    public static final String KEY_ID = "id";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_DISABLE_WHEN_TRIGGER = "disableWhenTrigger";
    public static final String KEY_NAME = "name";
    public static final String KEY_TRIGGERED = "triggered";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_LAST_TRIGGER = "lastTrigger";
    public static final String KEY_IGNORE_DUPLICATE = "ignoreDuplicate";
    public static final String KEY_CONDITION_TYPE = "conditionType";
    public static final String KEY_DAMPENING_TYPE = "dampeningType";
    public static final String KEY_CONDITION_PROPERTIES = "conditionProperties";
    public static final String KEY_DAMPENING_PROPERTIES = "dampeningProperties";
    public static final String KEY_RE_ENABLE = "reEnable";
    public static final String KEY_RE_ENABLE_DELAY = "reEnableDelay";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, columnName = KEY_DISABLE_WHEN_TRIGGER)
    private Boolean disableWhenTrigger;

    @DatabaseField(canBeNull = false, columnName = KEY_RE_ENABLE)
    private Boolean reEnable;

    @DatabaseField(canBeNull = true, columnName = KEY_RE_ENABLE_DELAY)
    private Long reEnableDelay;

    @DatabaseField(canBeNull = false, unique = true, columnName = KEY_NAME)
    private String name;

    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false, columnName = KEY_RESOURCE_TYPE)
    private RESOURCE_TYPE resourceType;

    @DatabaseField(canBeNull = false, columnName = KEY_RESOURCE_ID)
    private Integer resourceId;

    @DatabaseField(canBeNull = true, columnName = KEY_TIMESTAMP)
    private Long timestamp;

    @DatabaseField(canBeNull = true, columnName = KEY_LAST_TRIGGER)
    private Long lastTrigger;

    @DatabaseField(canBeNull = true, defaultValue = "true", columnName = KEY_IGNORE_DUPLICATE)
    private Boolean ignoreDuplicate;

    @DatabaseField(canBeNull = false, columnName = KEY_TRIGGERED)
    private Boolean triggered = false;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_DAMPENING_TYPE)
    private DAMPENING_TYPE dampeningType;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_CONDITION_TYPE)
    private CONDITION_TYPE conditionType;

    @DatabaseField(canBeNull = false, columnName = KEY_CONDITION_PROPERTIES, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Object> conditionProperties;

    @DatabaseField(canBeNull = true, columnName = KEY_DAMPENING_PROPERTIES, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Object> dampeningProperties;

}
