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
import java.util.Map;

import org.mycontroller.restclient.emoncms.EmoncmsClient;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigEmoncms;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class DriverEmoncms extends DriverAbstract {
    private ExternalServerConfigEmoncms _config = null;
    private EmoncmsClient _client = null;

    public DriverEmoncms(ExternalServerConfigEmoncms _config) {
        super(_config);
        this._config = _config;
    }

    @Override
    public synchronized void write(SensorVariable sensorVariable) {
        if (_client != null) {
            try {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(getVariableKey(sensorVariable, _config.getKeyFormat()), sensorVariable.getValue());
                String response = _client.post("MyController", data);
                _logger.debug("Emoncms post response: {}", response);
            } catch (Exception ex) {
                _logger.error("Exception, {}", sensorVariable, ex);
            }
        }
    }

    @Override
    public void connect() {
        try {
            _client = new EmoncmsClient(_config.getUrl(), _config.getWriteApiKey(), _config.getTrustHostType());
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

    @Override
    public void disconnect() {
        _client = null;

    }

}
