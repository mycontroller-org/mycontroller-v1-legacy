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

import org.mycontroller.standalone.NumericUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("metricsSettings")
public class MetricsSettings {
    public static final String KEY_METRICS = "metrics";
    public static final String SKEY_LAST_AGGREGATION_RAW_DATA = "lastAggregationRawData";
    public static final String SKEY_LAST_AGGREGATION_ONE_MINUTE = "lastAggregationOneMinute";
    public static final String SKEY_LAST_AGGREGATION_FIVE_MINUTES = "lastAggregationFiveMinutes";
    public static final String SKEY_LAST_AGGREGATION_ONE_HOUR = "lastAggregationOneHour";
    public static final String SKEY_LAST_AGGREGATION_SIX_HOURS = "lastAggregationSixHours";
    public static final String SKEY_LAST_AGGREGATION_TWELVE_HOURS = "lastAggregationTwelveHours";
    public static final String SKEY_LAST_AGGREGATION_ONE_DAY = "lastAggregationOneDay";

    private Long lastAggregationRawData;
    private Long lastAggregationOneMinute;
    private Long lastAggregationFiveMinutes;
    private Long lastAggregationOneHour;
    private Long lastAggregationSixHours;
    private Long lastAggregationTwelveHours;
    private Long lastAggregationOneDay;

    public static MetricsSettings get() {
        return MetricsSettings.builder()
                .lastAggregationRawData(NumericUtils.getLong(getValue(SKEY_LAST_AGGREGATION_RAW_DATA)))
                .lastAggregationOneMinute(NumericUtils.getLong(getValue(SKEY_LAST_AGGREGATION_ONE_MINUTE)))
                .lastAggregationFiveMinutes(NumericUtils.getLong(getValue(SKEY_LAST_AGGREGATION_FIVE_MINUTES)))
                .lastAggregationOneHour(NumericUtils.getLong(getValue(SKEY_LAST_AGGREGATION_ONE_HOUR)))
                .lastAggregationSixHours(NumericUtils.getLong(getValue(SKEY_LAST_AGGREGATION_SIX_HOURS)))
                .lastAggregationTwelveHours(NumericUtils.getLong(getValue(SKEY_LAST_AGGREGATION_TWELVE_HOURS)))
                .lastAggregationOneDay(NumericUtils.getLong(getValue(SKEY_LAST_AGGREGATION_ONE_DAY)))
                .build();
    }

    public void save() {
        //may be add later
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
}
