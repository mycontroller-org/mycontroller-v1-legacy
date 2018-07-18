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
package org.mycontroller.standalone.externalserver.driver;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigInfluxDB;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class DriverInfluxDB extends DriverAbstract {
    private static final int FLUSH_POINTS = 200;
    private static final int FLUSH_DURATION = 2000;

    private ExternalServerConfigInfluxDB _config = null;
    private InfluxDB _client = null;

    public DriverInfluxDB(ExternalServerConfigInfluxDB _config) {
        super(_config);
        this._config = _config;
    }

    @Override
    public synchronized void write(SensorVariable sensorVariable) {
        if (_client != null) {
            StringBuilder data = new StringBuilder();
            data
                    .append(getVariableKey(sensorVariable, _config.getKeyFormat()))
                    .append(",").append(getVariableKey(sensorVariable, _config.getTags()))
                    .append(" value=").append(getValue(sensorVariable))
                    .append(" ").append(sensorVariable.getTimestamp()).append("000000");
            try {
                _client.write(data.toString());
                _logger.debug("data[{}] sent", data.toString());
            } catch (Exception ex) {
                _logger.error("Exception, {}", data.toString(), ex);
            }
        }
    }

    private String getValue(SensorVariable sensorVariable) {
        METRIC_TYPE mType = sensorVariable.getMetricType();
        if (mType == METRIC_TYPE.BINARY || mType == METRIC_TYPE.COUNTER || mType == METRIC_TYPE.DOUBLE) {
            return sensorVariable.getValue();
        } else {
            return "\"" + sensorVariable.getValue() + "\"";
        }
    }

    @Override
    public void connect() {
        if (_config.getUsername() != null && _config.getUsername().trim().length() > 0) {
            _client = InfluxDBFactory.connect(_config.getUrl(), _config.getUsername(), _config.getPassword());
        } else {
            _client = InfluxDBFactory.connect(_config.getUrl());
        }
        _client.setDatabase(_config.getDatabase());
        _client.enableBatch(BatchOptions.DEFAULTS.actions(FLUSH_POINTS).flushDuration(FLUSH_DURATION));
        _logger.debug("External server:{}, Influxdb client BatchSettings[flush, points:{}, duration:{} ms]",
                _config.getName(), FLUSH_POINTS, FLUSH_DURATION);
    }

    @Override
    public void disconnect() {
        if (_client != null) {
            _client.close();
            _logger.debug("Influxdb client connection closed.");
        }
    }
}
