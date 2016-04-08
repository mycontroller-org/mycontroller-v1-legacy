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
package org.mycontroller.standalone.metrics;

import org.mycontroller.standalone.McUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsUtils {
    public static final long RAW_DATA_MAX_RETAIN_TIME = McUtils.ONE_MINUTE;        // 1 minute
    public static final long ONE_MINUTE_MAX_RETAIN_TIME = McUtils.ONE_HOUR * 6;    // 6 Hours
    public static final long FIVE_MINUTES_MAX_RETAIN_TIME = McUtils.ONE_DAY * 2;   // 2 days
    public static final long ONE_HOUR_MAX_RETAIN_TIME = McUtils.ONE_DAY * 30;      // 30 days
    public static final long SIX_HOURS_MAX_RETAIN_TIME = McUtils.ONE_DAY * 90;      // 90 days
    public static final long TWELVE_HOURS_MAX_RETAIN_TIME = McUtils.ONE_DAY * 180;  // 180 days
    public static final long ONE_DAY_MAX_RETAIN_TIME = McUtils.ONE_YEAR * 5;  // 5 years

    public static final long MILLISECONDS_2015 = 1420050600000L;

    public enum AGGREGATION_TYPE {
        RAW,
        ONE_MINUTE,
        FIVE_MINUTES,
        ONE_HOUR,
        SIX_HOURS,
        TWELVE_HOURS,
        ONE_DAY;

        public static AGGREGATION_TYPE get(int id) {
            for (AGGREGATION_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    public enum METRIC_TYPE {
        NONE("None"),
        DOUBLE("Double"),
        BINARY("Binary");

        public static METRIC_TYPE get(int id) {
            for (METRIC_TYPE metric_type : values()) {
                if (metric_type.ordinal() == id) {
                    return metric_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private METRIC_TYPE(String text) {
            this.text = text;
        }

        public static METRIC_TYPE fromString(String text) {
            if (text != null) {
                for (METRIC_TYPE type : METRIC_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum HEAT_MAP_TYPE {
        NODE_BATTERY("Node battery"),
        NODE_STATUS("Node status"),
        SENSOR_VARIABLE("Sensor variable"),
        SCRIPT("Script");

        public static HEAT_MAP_TYPE get(int id) {
            for (HEAT_MAP_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private HEAT_MAP_TYPE(String text) {
            this.text = text;
        }

        public static HEAT_MAP_TYPE fromString(String text) {
            if (text != null) {
                for (HEAT_MAP_TYPE type : HEAT_MAP_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }

    }
}
