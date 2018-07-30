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
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;

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
@DatabaseTable(tableName = DB_TABLES.METRICS_COUNTER_TYPE_DEVICE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class MetricsCounterTypeDevice {
    public static final String KEY_SENSOR_VARIABLE_ID = "sensorVariableId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_AGGREGATION_TYPE = "aggregationType";
    public static final String KEY_VALUE = "value";
    public static final String KEY_SAMPLES = "samples";

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_SENSOR_VARIABLE_ID)
    private SensorVariable sensorVariable;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = KEY_TIMESTAMP)
    private Long timestamp;

    @DatabaseField(canBeNull = false, columnName = KEY_SAMPLES)
    private Integer samples;

    @DatabaseField(canBeNull = false, columnName = KEY_VALUE)
    private Long value;

    @DatabaseField(uniqueCombo = true, dataType = DataType.ENUM_STRING,
            canBeNull = false, columnName = KEY_AGGREGATION_TYPE)
    private AGGREGATION_TYPE aggregationType;

    private Long start;
    private Long end;

}
