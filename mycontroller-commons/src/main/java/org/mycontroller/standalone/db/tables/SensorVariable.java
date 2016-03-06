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

import org.mycontroller.standalone.MYCMessages;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;

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
@DatabaseTable(tableName = DB_TABLES.SENSOR_VARIABLE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class SensorVariable {
    public static final String KEY_ID = "id";
    public static final String KEY_SENSOR_DB_ID = "sensorDbId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_VARIABLE_TYPE = "variableType";
    public static final String KEY_VALUE = "value";
    public static final String KEY_PREVIOUS_VALUE = "previousValue";
    public static final String KEY_METRIC = "metricType";
    public static final String KEY_UNIT = "unit";

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(columnName = KEY_SENSOR_DB_ID, canBeNull = false, uniqueCombo = true, foreign = true,
            maxForeignAutoRefreshLevel = 3, foreignAutoRefresh = true)
    private Sensor sensor;

    @DatabaseField(columnName = KEY_VARIABLE_TYPE, canBeNull = false, uniqueCombo = true, dataType = DataType.ENUM_STRING)
    private MESSAGE_TYPE_SET_REQ variableType;

    @DatabaseField(columnName = KEY_METRIC, canBeNull = false, dataType = DataType.ENUM_STRING)
    private METRIC_TYPE metricType = METRIC_TYPE.NONE;

    @DatabaseField(columnName = KEY_TIMESTAMP, canBeNull = true)
    private Long timestamp;

    @DatabaseField(columnName = KEY_VALUE, canBeNull = true)
    private String value;

    @DatabaseField(columnName = KEY_PREVIOUS_VALUE, canBeNull = true)
    private String previousValue;

    @DatabaseField(columnName = KEY_UNIT, canBeNull = true)
    private String unit;

    public SensorVariable updateUnitAndMetricType() {
        if (this.unit == null) {
            String unit = SensorUtils.getUnit(variableType);
            this.unit = unit == null ? "" : unit;
        }
        if (this.metricType == null) {
            this.metricType = MYCMessages.getMetricType(this.variableType);
        }
        return this;
    }

    public void setValue(String value) {
        previousValue = this.value;
        this.value = value;
    }

}
