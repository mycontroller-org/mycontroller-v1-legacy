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
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.utils.McUtils;

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
@DatabaseTable(tableName = DB_TABLES.METRICS_GPS_TYPE_DEVICE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MetricsGPSTypeDevice {
    public static final String KEY_SENSOR_VARIABLE_ID = "sensorVariableId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_AGGREGATION_TYPE = "aggregationType";
    public static final String KEY_SAMPLES = "samples";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ALTITUDE = "altitude";
    public static final int SCALE = 12;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_SENSOR_VARIABLE_ID)
    private SensorVariable sensorVariable;

    @DatabaseField(index = true, uniqueCombo = true, canBeNull = false, columnName = KEY_TIMESTAMP)
    private Long timestamp;

    @DatabaseField(canBeNull = false, columnName = KEY_SAMPLES)
    private Integer samples;

    @DatabaseField(columnName = KEY_LATITUDE)
    private Double lantitude;

    @DatabaseField(columnName = KEY_LONGITUDE)
    private Double longitude;

    @DatabaseField(canBeNull = false, columnName = KEY_ALTITUDE, defaultValue = "0")
    private Double altitude;

    @DatabaseField(uniqueCombo = true, dataType = DataType.ENUM_STRING,
            canBeNull = false, columnName = KEY_AGGREGATION_TYPE)
    private AGGREGATION_TYPE aggregationType;

    private Long start;
    private Long end;

    public String getPosition() {
        return this.lantitude + ";" + this.longitude + ";" + this.altitude;
    }

    public static MetricsGPSTypeDevice get(String position, long timestamp) throws McBadRequestException {
        String[] elements = position.trim().split(";");
        Double lantitude = null;
        Double longitude = null;
        Double altitude = 0.0;
        if (elements.length < 2) {
            throw new McBadRequestException("Unknown format position string: " + position);
        }
        lantitude = McUtils.getDouble(elements[0], SCALE);
        longitude = McUtils.getDouble(elements[1], SCALE);
        if (elements.length > 2) {
            altitude = McUtils.getDouble(elements[2], SCALE);
        }
        return MetricsGPSTypeDevice.builder()
                .lantitude(lantitude)
                .longitude(longitude)
                .altitude(altitude)
                .samples(1)
                .timestamp(timestamp)
                .aggregationType(AGGREGATION_TYPE.RAW)
                .build();
    }

}
