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
package org.mycontroller.standalone.settings;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.MycUtils;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.db.tables.Settings;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Builder
@ToString(includeFieldNames = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsSettings {
    public static final String KEY_METRICS = "metrics";
    public static final String SKEY_LAST_AGGREGATION_RAW_DATA = "lastAggregationRawData";
    public static final String SKEY_LAST_AGGREGATION_ONE_MINUTE = "lastAggregationOneMinute";
    public static final String SKEY_LAST_AGGREGATION_FIVE_MINUTES = "lastAggregationFiveMinutes";
    public static final String SKEY_LAST_AGGREGATION_ONE_HOUR = "lastAggregationOneHour";
    public static final String SKEY_LAST_AGGREGATION_SIX_HOURS = "lastAggregationSixHours";
    public static final String SKEY_LAST_AGGREGATION_TWELVE_HOURS = "lastAggregationTwelveHours";
    public static final String SKEY_LAST_AGGREGATION_ONE_DAY = "lastAggregationOneDay";

    public static final String SKEY_ENABLED_MIN_MAX = "enabledMinMax";
    public static final String SKEY_DEFAULT_TIME_RANGE = "defaultTimeRange";
    public static final String SKEY_BATTERY = "battery";

    private Long lastAggregationRawData;
    private Long lastAggregationOneMinute;
    private Long lastAggregationFiveMinutes;
    private Long lastAggregationOneHour;
    private Long lastAggregationSixHours;
    private Long lastAggregationTwelveHours;
    private Long lastAggregationOneDay;

    private Boolean enabledMinMax;
    private Long defaultTimeRange;
    private MetricsGraph battery;

    private List<MetricsGraph> metrics;

    public static MetricsSettings get() {
        ArrayList<MetricsGraph> metrics = new ArrayList<MetricsGraph>();
        for (MESSAGE_TYPE_SET_REQ sVariable : MetricsGraph.variables) {
            metrics.add(getMetricsGraph(sVariable.getText()));
        }
        return MetricsSettings.builder()
                .lastAggregationRawData(MycUtils.getLong(getValue(SKEY_LAST_AGGREGATION_RAW_DATA)))
                .lastAggregationOneMinute(MycUtils.getLong(getValue(SKEY_LAST_AGGREGATION_ONE_MINUTE)))
                .lastAggregationFiveMinutes(MycUtils.getLong(getValue(SKEY_LAST_AGGREGATION_FIVE_MINUTES)))
                .lastAggregationOneHour(MycUtils.getLong(getValue(SKEY_LAST_AGGREGATION_ONE_HOUR)))
                .lastAggregationSixHours(MycUtils.getLong(getValue(SKEY_LAST_AGGREGATION_SIX_HOURS)))
                .lastAggregationTwelveHours(MycUtils.getLong(getValue(SKEY_LAST_AGGREGATION_TWELVE_HOURS)))
                .lastAggregationOneDay(MycUtils.getLong(getValue(SKEY_LAST_AGGREGATION_ONE_DAY)))
                .metrics(metrics)
                .battery(getMetricsGraph(SKEY_BATTERY))
                .enabledMinMax(MycUtils.getBoolean(getValue(SKEY_ENABLED_MIN_MAX)))
                .defaultTimeRange(MycUtils.getLong(getValue(SKEY_DEFAULT_TIME_RANGE)))
                .build();
    }

    public void save() {
        for (MetricsGraph metric : metrics) {
            if (MetricsGraph.variables.contains(MESSAGE_TYPE_SET_REQ.fromString(metric.getMetricName()))) {
                updateMetricsGraph(metric);
            }
        }
        if (battery != null) {
            updateMetricsGraph(battery);
        }
        if (enabledMinMax != null) {
            updateValue(SKEY_ENABLED_MIN_MAX, enabledMinMax);
        }
        if (defaultTimeRange != null) {
            updateValue(SKEY_DEFAULT_TIME_RANGE, defaultTimeRange);
        }
    }

    public MetricsGraph getMetric(String metricName) {
        for (MetricsGraph metric : metrics) {
            if (metric.getMetricName().equals(metricName)) {
                return metric;
            }
        }
        return null;
    }

    @JsonIgnore
    public void updateInternal() {
        if (lastAggregationRawData != null) {
            updateValue(SKEY_LAST_AGGREGATION_RAW_DATA, lastAggregationRawData);
        }
        if (lastAggregationOneMinute != null) {
            updateValue(SKEY_LAST_AGGREGATION_ONE_MINUTE, lastAggregationOneMinute);
        }
        if (lastAggregationFiveMinutes != null) {
            updateValue(SKEY_LAST_AGGREGATION_FIVE_MINUTES, lastAggregationFiveMinutes);
        }
        if (lastAggregationOneHour != null) {
            updateValue(SKEY_LAST_AGGREGATION_ONE_HOUR, lastAggregationOneHour);
        }
        if (lastAggregationSixHours != null) {
            updateValue(SKEY_LAST_AGGREGATION_SIX_HOURS, lastAggregationSixHours);
        }
        if (lastAggregationTwelveHours != null) {
            updateValue(SKEY_LAST_AGGREGATION_TWELVE_HOURS, lastAggregationTwelveHours);
        }
        if (lastAggregationOneDay != null) {
            updateValue(SKEY_LAST_AGGREGATION_ONE_DAY, lastAggregationOneDay);
        }
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_METRICS, subKey);
    }

    private static void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_METRICS, subKey, value);
    }

    private static MetricsGraph getMetricsGraph(String subKey) {
        Settings settings = SettingsUtils.getSettings(KEY_METRICS, subKey);
        if (settings != null) {
            return MetricsGraph.builder()
                    .id(settings.getId())
                    .metricName(subKey)
                    .type(settings.getValue())
                    .interpolate(settings.getAltValue())
                    .subType(settings.getValue3())
                    .color(settings.getValue4())
                    .build();
        }
        return null;
    }

    private static void updateMetricsGraph(MetricsGraph metric) {
        SettingsUtils.updateSettings(Settings.builder()
                .id(metric.getId())
                .key(KEY_METRICS)
                .subKey(metric.getMetricName())
                .value(metric.getType())
                .altValue(metric.getInterpolate())
                .value3(metric.getSubType())
                .value4(metric.getColor())
                .build());
    }

}
