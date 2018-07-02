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
package org.mycontroller.standalone.settings;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
public class MetricsGraphSettings {
    public static final String KEY_METRICS = "metrics";

    public static final String SKEY_ENABLED_MIN_MAX = "enabledMinMax";
    public static final String SKEY_DEFAULT_TIME_RANGE = "defaultTimeRange";
    public static final String SKEY_BATTERY = "battery";

    private Boolean enabledMinMax;
    private Long defaultTimeRange;
    private MetricsGraph battery;

    private List<MetricsGraph> metrics;

    public static MetricsGraphSettings get() {
        ArrayList<MetricsGraph> metrics = new ArrayList<MetricsGraph>();
        MetricsGraph metric = null;
        for (MESSAGE_TYPE_SET_REQ sVariable : MetricsGraph.variables) {
            metric = getMetricsGraph(sVariable.getText());
            if (metric != null) {
                metrics.add(metric);
            }
        }
        return MetricsGraphSettings.builder()
                .metrics(metrics)
                .battery(getMetricsGraph(SKEY_BATTERY))
                .enabledMinMax(McUtils.getBoolean(getValue(SKEY_ENABLED_MIN_MAX)))
                .defaultTimeRange(McUtils.getLong(getValue(SKEY_DEFAULT_TIME_RANGE)))
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
        MESSAGE_TYPE_SET_REQ type = MESSAGE_TYPE_SET_REQ.fromString(metricName);
        if (type == null || !MetricsGraph.variables.contains(type)) {
            metricName = MESSAGE_TYPE_SET_REQ.V_CUSTOM.getText();
        }
        MetricsGraph metricFinal = null;

        for (MetricsGraph metric : metrics) {
            if (metric.getMetricName().equals(metricName)) {
                metricFinal = metric;
                break;
            }
        }
        return metricFinal;
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
