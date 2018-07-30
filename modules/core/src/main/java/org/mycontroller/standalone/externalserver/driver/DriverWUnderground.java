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

import org.mycontroller.restclient.wunderground.WundergroundClient;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
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
    private WundergroundClient _client = null;
    private HashMap<String, Object> data = new HashMap<String, Object>();

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
                    data.put("tempf", McUtils.getDouble(sensorVariable.getValue()) * 1.8 + 32);
                } else {
                    data.put("tempf", sensorVariable.getValue());
                }
            } else if (sensorVariable.getUnitType() == UNIT_TYPE.U_HUMIDITY) {
                data.put("humidity", sensorVariable.getValue());
            } else {
                _logger.warn("This type of sensor not supported! {} for {}", sensorVariable.getUnitType(), toString());
                return;
            }
            try {
                String response = _client.send(data);
                _logger.debug("write response: {}", response);
            } catch (Exception ex) {
                _logger.error("Exception: data:[{}]", data, ex);
            }
        }
    }

    private void clearValues() {
        data.clear();
        data.put("action", "updateraw");
        data.put("ID", _config.getStationId());
        data.put("PASSWORD", _config.getStationPassword());
        data.put("dateutc", "now");
    }

    @Override
    public void connect() {
        _client = new WundergroundClient(null, _config.getTrustHostType());
    }

    @Override
    public void disconnect() {
        _client = null;
    }

}
