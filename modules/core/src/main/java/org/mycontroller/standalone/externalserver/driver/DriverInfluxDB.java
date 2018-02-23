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

import java.net.URISyntaxException;

import org.mycontroller.restclient.core.ClientResponse;
import org.mycontroller.restclient.influxdb.InfluxDBClient;
import org.mycontroller.restclient.influxdb.InfluxDBClientBuilder;
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
    private ExternalServerConfigInfluxDB _config = null;
    private InfluxDBClient _client = null;

    public DriverInfluxDB(ExternalServerConfigInfluxDB _config) {
        super(_config);
        this._config = _config;
    }

    @Override
    public synchronized void write(SensorVariable sensorVariable) {
        if (_client != null) {
            ClientResponse<String> clientResponse = _client.write(
                    getVariableKey(sensorVariable, _config.getKeyFormat()),
                    getVariableKey(sensorVariable, _config.getTags()),
                    sensorVariable.getTimestamp(), getValue(sensorVariable));
            if (!clientResponse.isSuccess()) {
                _logger.error("Failed to send data to remote server! {}, Remote server:{}, {}", clientResponse,
                        toString(), _config.getUrl());
            } else {
                _logger.debug("Remote server update status: {}, Remote server:{}, {}", clientResponse,
                        toString(), _config.getUrl());
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
        try {
            if (_config.getUsername() != null && _config.getUsername().length() > 0) {
                _client = new InfluxDBClientBuilder()
                        .uri(_config.getUrl(), _config.getTrustHostType())
                        .basicAuthentication(_config.getUsername(), _config.getPassword())
                        .addProperty(InfluxDBClient.KEY_DATABASE, _config.getDatabase())
                        .build();
            } else {
                _client = new InfluxDBClientBuilder()
                        .uri(_config.getUrl(), _config.getTrustHostType())
                        .addProperty(InfluxDBClient.KEY_DATABASE, _config.getDatabase())
                        .build();
            }
        } catch (URISyntaxException ex) {
            _logger.error("Exception,", ex);
        }
    }

    @Override
    public void disconnect() {
        _client = null;

    }
}
