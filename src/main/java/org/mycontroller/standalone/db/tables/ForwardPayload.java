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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.FORWARD_PAYLOAD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForwardPayload {
    public static final String KEY_ID = "id";
    public static final String KEY_SOURCE_ID = "sourceId";
    public static final String KEY_DESTINATION_ID = "destinationId";
    public static final String KEY_ENABLED = "enabled";

    public ForwardPayload() {

    }

    public ForwardPayload(int id) {
        this.id = id;
    }

    public ForwardPayload(SensorVariable source, SensorVariable destination) {
        this.source = source;
        this.destination = destination;
    }

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_SOURCE_ID,
            foreignAutoRefresh = true, foreignAutoCreate = true, maxForeignAutoRefreshLevel = 4)
    private SensorVariable source;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = KEY_DESTINATION_ID,
            foreignAutoRefresh = true, foreignAutoCreate = true, maxForeignAutoRefreshLevel = 4)
    private SensorVariable destination;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public SensorVariable getSource() {
        return source;
    }

    public void setSource(SensorVariable source) {
        this.source = source;
    }

    public SensorVariable getDestination() {
        return destination;
    }

    public void setDestination(SensorVariable destination) {
        this.destination = destination;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Enabled:").append(this.enabled);
        builder.append("Source:[").append(this.source).append("]");
        builder.append("Destination:[").append(this.destination).append("]");
        return builder.toString();
    }
}