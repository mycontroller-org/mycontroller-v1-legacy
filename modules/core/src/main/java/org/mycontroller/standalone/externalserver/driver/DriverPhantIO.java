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

import org.mycontroller.restclient.phantio.PhantIOClient;
import org.mycontroller.restclient.phantio.model.PostResponse;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigPhantIO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class DriverPhantIO extends DriverAbstract {
    private ExternalServerConfigPhantIO _config = null;
    private PhantIOClient _client = null;

    public DriverPhantIO(ExternalServerConfigPhantIO _config) {
        super(_config);
        this._config = _config;
    }

    @Override
    public synchronized void write(SensorVariable sensorVariable) {
        if (_client != null) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put(getVariableKey(sensorVariable, _config.getKeyFormat()), sensorVariable.getValue());
            try {
                PostResponse response = _client.post(data);
                _logger.debug("{}", response);
            } catch (Exception ex) {
                _logger.error("Exception, data:[{}]", data, ex);
            }
        }
    }

    @Override
    public void connect() {
        _client = new PhantIOClient(_config.getUrl(), _config.getPublicKey(),
                _config.getPrivateKey(), _config.getTrustHostType());
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub
    }

}
