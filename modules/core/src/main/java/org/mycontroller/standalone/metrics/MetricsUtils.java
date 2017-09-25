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
package org.mycontroller.standalone.metrics;

import java.io.IOException;
import java.net.URISyntaxException;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.engine.conf.InfluxDBConf;
import org.mycontroller.standalone.metrics.engine.conf.MetricEngineConf;
import org.mycontroller.standalone.metrics.engine.conf.MyControllerConf;
import org.mycontroller.standalone.metrics.engines.InfluxDBEngine;
import org.mycontroller.standalone.metrics.engines.McMetricEngine;
import org.mycontroller.standalone.metrics.model.Pong;
import org.mycontroller.standalone.utils.McUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
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
        BINARY("Binary"),
        COUNTER("Counter"),
        GPS("GPS");

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

    //Metric engine related
    private static final String KEY_METRIC_ENGINE = "metricEngine";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CONF = "conf";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static IMetric metricEngine = null;
    private static MetricEngineConf engineConf = null;

    public static IMetric engine() {
        return metricEngine;
    }

    public static void loadEngine() throws URISyntaxException {
        engineConf = getEngineConf();
        metricEngine = getEngine(engineConf);
    }

    private static IMetric getEngine(MetricEngineConf engineConf) throws URISyntaxException {
        switch (engineConf.getType()) {
            case INFLUXDB:
                return new InfluxDBEngine((InfluxDBConf) engineConf);
            case MY_CONTROLLER:
                return new McMetricEngine();
            default:
                break;

        }
        throw new RuntimeException("Not implemented yet. Type:" + engineConf.getType());
    }

    public static METRIC_ENGINE type() {
        return engineConf.getType();
    }

    private static METRIC_ENGINE getEngineType() {
        return METRIC_ENGINE.valueOf(getMetricEngineDataInDB(KEY_TYPE));
    }

    public static MetricEngineConf getEngineConf() {
        String conf = getMetricEngineDataInDB(KEY_CONF);
        if (conf == null) {
            MyControllerConf mcConf = new MyControllerConf();
            mcConf.setType(METRIC_ENGINE.MY_CONTROLLER);
            try {
                updateEngine(mcConf);
                conf = getMetricEngineDataInDB(KEY_CONF);
            } catch (McBadRequestException ex) {
                _logger.error("Exception,", ex);
            }
        }
        try {
            switch (getEngineType()) {
                case INFLUXDB:
                    return OBJECT_MAPPER.readValue(conf, InfluxDBConf.class);
                case MY_CONTROLLER:
                    return OBJECT_MAPPER.readValue(conf, MyControllerConf.class);
            }
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
        }
        return null;
    }

    private static String getMetricEngineDataInDB(String subKey) {
        Settings settings = DaoUtils.getSettingsDao().get(null, KEY_METRIC_ENGINE, subKey);
        if (settings == null) {
            return null;
        }
        return settings.getValue();
    }

    private static void saveMetricEngineDataInDB(String subKey, String data) {
        DaoUtils.getSettingsDao().update(KEY_METRIC_ENGINE, subKey, data);
    }

    public static void updateEngine(MetricEngineConf conf) throws McBadRequestException {
        String data = null;
        switch (conf.getType()) {
            case INFLUXDB:
            case MY_CONTROLLER:
                try {
                    data = OBJECT_MAPPER.writeValueAsString(conf);
                    saveMetricEngineDataInDB(KEY_TYPE, conf.getType().name());
                    saveMetricEngineDataInDB(KEY_CONF, data);
                    loadEngine();
                    if (conf.isPurgeEveryThing()) {
                        _logger.info("Purging existing data triggered for {}", engineConf);
                        engine().purgeEverything();
                    }
                } catch (JsonProcessingException | URISyntaxException ex) {
                    _logger.error("Exception,", ex);
                    throw new McBadRequestException(ex.getMessage());
                }
                break;
            default:
                throw new RuntimeException("This type not available. " + conf);
        }
    }

    public static Pong ping(MetricEngineConf conf) throws McBadRequestException {
        try {
            return getEngine(conf).ping();
        } catch (URISyntaxException ex) {
            throw new McBadRequestException(ex.getMessage(), ex);
        }
    }

}
