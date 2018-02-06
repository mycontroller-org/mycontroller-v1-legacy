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

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.api.HttpApi;
import org.mycontroller.standalone.api.jaxrs.model.McHttpResponse;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigWUnderground;
import org.mycontroller.standalone.units.UnitUtils.UNIT_TYPE;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class DriverWUnderground extends DriverAbstract {
    private ExternalServerConfigWUnderground _config = null;
    private HttpApi _client = null;
    private HashMap<String, Object> _queryParams = new HashMap<String, Object>();

    public DriverWUnderground(ExternalServerConfigWUnderground _config) {
        super(_config);
        this._config = _config;
    }

    @Override
    public synchronized void write(SensorVariable sensorVariable) {
        if (_client != null) {
            clearValues();
            if (sensorVariable.getUnitType() == UNIT_TYPE.U_TEMPERATURE) {
                if (AppProperties.getInstance().getControllerSettings().getUnitConfig()
                        .equals(UNIT_CONFIG.METRIC.getText())) {
                    _queryParams.put("tempf", McUtils.getDouble(sensorVariable.getValue()) * 1.8 + 32);
                } else {
                    _queryParams.put("tempf", sensorVariable.getValue());
                }
            } else if (sensorVariable.getUnitType() == UNIT_TYPE.U_HUMIDITY) {
                _queryParams.put("humidity", sensorVariable.getValue());
            } else {
                _logger.warn("This type of sensor not supported! {} for {}", sensorVariable.getUnitType(), toString());
                return;
            }
            McHttpResponse response = _client.get(_config.getUrl(), _queryParams);
            _logger.debug("{}", response);
            if (response.getException() != null || response.getResponseCode() != 200) {
                _logger.error("Failed to send data to remote server! {}, Remote server:{}, {}", response,
                        toString(), _config.getUrl());
            } else {
                _logger.debug("Remote server update status: {}, Remote server:{}, {}", response,
                        toString(), _config.getUrl());
            }
        }
    }

    private void clearValues() {
        _queryParams.remove("tempf");
        _queryParams.remove("humidity");
    }

    @Override
    public void connect() {
        _client = new HttpApi(_config.getTrustHostType());
        _queryParams.clear();
        _queryParams.put("action", "updateraw");
        _queryParams.put("ID", _config.getStationId());
        _queryParams.put("PASSWORD", _config.getStationPassword());
        _queryParams.put("dateutc", "now");
    }

    @Override
    public void disconnect() {
        _client = null;
    }

}
