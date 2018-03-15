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
package org.mycontroller.standalone.gateway.phantio;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mycontroller.restclient.phantio.PhantIOClient;
import org.mycontroller.restclient.phantio.model.PostResponse;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfigPhantIO;
import org.mycontroller.standalone.gateway.rest.RestDriverAbstract;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;
import org.mycontroller.standalone.provider.phantio.MessageParserPhantIO;
import org.mycontroller.standalone.provider.phantio.MessagePhantIO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class PhantIODriver extends RestDriverAbstract {

    private GatewayConfigPhantIO _config = null;
    private IMessageParser<MessagePhantIO> _parser = new MessageParserPhantIO();
    private IQueue<IMessage> _queue = null;
    private PhantIOClient _client = null;
    //2016-07-18T20:50:04+05:30
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    public PhantIODriver(GatewayConfigPhantIO _config, IQueue<IMessage> _queue) {
        super(_config, _config.getPollFrequency() * 1000 * 60L);
        this._config = _config;
        this._queue = _queue;

    }

    @Override
    public void connect() {
        try {
            _client = new PhantIOClient(
                    this._config.getUrl(),
                    this._config.getPublicKey(),
                    this._config.getPrivateKey(),
                    this._config.getTrustHostType());
            _config.setStatus(STATE.UP, "Connected Successfully");
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
        }
    }

    @Override
    public void write(IMessage message) throws MessageParserException {
        if (_config.getPrivateKey() != null && _config.getPrivateKey().length() > 0) {
            _logger.debug("Send data: {}, {}", _config, message);
            MessagePhantIO rawMessage = _parser.getGatewayData(message);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put(rawMessage.getKey(), rawMessage.getValue());
            try {
                PostResponse _response = _client.post(data);
                _logger.debug("{}", _response);
            } catch (Exception ex) {
                _logger.error("Exception: data:[{}]", data, ex);
            }
        } else {
            _logger.warn("Private key not set for this {}", _config);
        }
    }

    @Override
    public void read() {
        try {
            List<Map<String, Object>> _response = _client.get(_config.getRecordsLimit());
            _logger.debug("Client response: {}", _response);
            if (_response != null) {
                for (Map<String, Object> record : _response) {
                    long timestamp = TIMESTAMP_FORMAT.parse((String) record.get("timestamp")).getTime();
                    if (_config.getLastUpdate() == null || _config.getLastUpdate() < timestamp) {
                        for (String key : record.keySet()) {
                            if (!key.equals("timestamp")) {
                                _queue.add(_parser.getMessage(_config,
                                        MessagePhantIO.builder()
                                                .key(key)
                                                .value((String) record.get(key))
                                                .timestamp(timestamp)
                                                .build()));
                                _config.setLastUpdate(timestamp);
                            }
                        }
                        _config.updateLastPollTime(timestamp);
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

}