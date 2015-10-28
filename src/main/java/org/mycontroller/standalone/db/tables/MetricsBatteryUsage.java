/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@DatabaseTable(tableName = "metrics_battery")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsBatteryUsage {
    public static final String NODE_REF_ID = "node_ref_id";
    public static final String TIMESTAMP = "timestamp";

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true, columnName = NODE_REF_ID)
    private Node node;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = TIMESTAMP)
    private Long timestamp;

    @DatabaseField(canBeNull = false)
    private Double value;

    public MetricsBatteryUsage(Node node, Long timestamp, Double value) {
        this.node = node;
        this.timestamp = timestamp;
        this.value = value;
    }

    public MetricsBatteryUsage() {

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(",Node:[").append(this.node).append("]");
        builder.append(", Timestamp:").append(this.timestamp);
        builder.append(", Value:").append(this.value);
        return builder.toString();
    }

    public Integer getId() {
        return id;
    }

    public Node getNode() {
        return node;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
