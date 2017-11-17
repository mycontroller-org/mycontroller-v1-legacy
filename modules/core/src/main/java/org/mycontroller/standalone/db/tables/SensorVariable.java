/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsGraph.CHART_TYPE;
import org.mycontroller.standalone.units.UnitUtils;
import org.mycontroller.standalone.units.UnitUtils.UNIT_TYPE;

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
@ToString
public class SensorVariable {
    public static final String KEY_ID = "id";
    public static final String KEY_SENSOR_DB_ID = "sensorDbId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_VARIABLE_TYPE = "variableType";
    public static final String KEY_VALUE = "value";
    public static final String KEY_PREVIOUS_VALUE = "previousValue";
    public static final String KEY_METRIC = "metricType";
    public static final String KEY_UNIT_TYPE = "unitType";
    public static final String KEY_READ_ONLY = "readOnly";
    public static final String KEY_OFFSET = "offset";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_PROPERTIES = "graphProperties";

    public static final String KEY_GP_USE_GLOBAL = "useGlobal";
    public static final String KEY_GP_TYPE = "type";
    public static final String KEY_GP_INTERPOLATE = "interpolate";
    public static final String KEY_GP_SUBTYPE = "subType";
    public static final String KEY_GP_COLOR = "color";
    public static final String KEY_GP_MARGIN_LEFT = "marginLeft";
    public static final String KEY_GP_MARGIN_RIGHT = "marginRight";
    public static final String KEY_GP_MARGIN_TOP = "marginTop";
    public static final String KEY_GP_MARGIN_BOTTOM = "marginBottom";

    public static final String KEY_NAME = "name";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID, index = true)
    private Integer id;

    @DatabaseField(columnName = KEY_SENSOR_DB_ID, canBeNull = false, uniqueCombo = true, foreign = true,
            maxForeignAutoRefreshLevel = 3, foreignAutoRefresh = true)
    private Sensor sensor;

    @DatabaseField(columnName = KEY_VARIABLE_TYPE, canBeNull = false,
            uniqueCombo = true, dataType = DataType.ENUM_STRING)
    private MESSAGE_TYPE_SET_REQ variableType;

    @DatabaseField(columnName = KEY_METRIC, canBeNull = false, dataType = DataType.ENUM_STRING)
    private METRIC_TYPE metricType = METRIC_TYPE.NONE;

    @DatabaseField(columnName = KEY_TIMESTAMP, canBeNull = true)
    private Long timestamp;

    @DatabaseField(columnName = KEY_VALUE, canBeNull = true)
    private String value;

    @DatabaseField(columnName = KEY_PREVIOUS_VALUE, canBeNull = true)
    private String previousValue;

    @DatabaseField(columnName = KEY_UNIT_TYPE, canBeNull = true, dataType = DataType.ENUM_STRING)
    private UNIT_TYPE unitType;

    @DatabaseField(columnName = KEY_READ_ONLY, canBeNull = false, defaultValue = "false")
    private Boolean readOnly;

    @DatabaseField(columnName = KEY_OFFSET, canBeNull = false, defaultValue = "0.0")
    private Double offset;

    @DatabaseField(columnName = KEY_PRIORITY, canBeNull = false, defaultValue = "100")
    private Integer priority;

    @DatabaseField(columnName = KEY_PROPERTIES, canBeNull = true, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Object> properties;

    public SensorVariable updateUnitAndMetricType() {
        if (this.unitType == null) {
            this.unitType = UnitUtils.getUnit(variableType);
        }
        if (this.metricType == null) {
            this.metricType = McMessageUtils.getMetricType(this.variableType);
        }
        return this;
    }

    public void setValue(String value) {
        previousValue = this.value;
        this.value = value;
    }

    public HashMap<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        if (properties.get(KEY_GP_USE_GLOBAL) == null || (Boolean) properties.get(KEY_GP_USE_GLOBAL)) {
            properties.put(KEY_GP_USE_GLOBAL, true);
            properties.put(KEY_GP_TYPE, CHART_TYPE.LINE_CHART.getText());
            properties.put(KEY_GP_INTERPOLATE, "linear");
            properties.put(KEY_GP_SUBTYPE, "line");
            properties.put(KEY_GP_COLOR, "#ff7f0e");
            properties.put(KEY_GP_MARGIN_LEFT, 65);
            properties.put(KEY_GP_MARGIN_RIGHT, 20);
            properties.put(KEY_GP_MARGIN_TOP, 5);
            properties.put(KEY_GP_MARGIN_BOTTOM, 60);

        }
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        } else {
            if (properties.get(KEY_GP_USE_GLOBAL) == null || (Boolean) properties.get(KEY_GP_USE_GLOBAL)) {
                properties.remove(KEY_GP_TYPE);
                properties.remove(KEY_GP_INTERPOLATE);
                properties.remove(KEY_GP_SUBTYPE);
                properties.remove(KEY_GP_COLOR);
                properties.remove(KEY_GP_MARGIN_LEFT);
                properties.remove(KEY_GP_MARGIN_RIGHT);
                properties.remove(KEY_GP_MARGIN_TOP);
                properties.remove(KEY_GP_MARGIN_BOTTOM);
            }
            if (properties.get(KEY_NAME) != null && ((String) properties.get(KEY_NAME)).trim().length() == 0) {
                properties.remove(KEY_NAME);
            }
        }
        this.properties = properties;
    }

    public MetricsGraph getMetricsGraph() {
        if (getProperties().get(KEY_GP_USE_GLOBAL) == null || (boolean) getProperties().get(KEY_GP_USE_GLOBAL)) {
            return AppProperties.getInstance().getMetricsGraphSettings().getMetric(variableType.getText());
        } else {
            return MetricsGraph.get(properties);
        }
    }

    public String getName() {
        return (String) getProperties().get(KEY_NAME);
    }
}
