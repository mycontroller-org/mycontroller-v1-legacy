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
package org.mycontroller.standalone.gateway.rest;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.message.IMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public abstract class RestDriverAbstract implements Runnable {
    private volatile boolean _terminate = false;
    private volatile boolean _terminated = false;
    private long pollInterval;
    private GatewayConfig _config = null;

    public abstract void write(IMessage message) throws MessageParserException;

    public abstract void read() throws MessageParserException;

    public abstract void connect();

    public RestDriverAbstract(GatewayConfig _config, long pollInterval) {
        this._config = _config;
        this.pollInterval = pollInterval;
    }

    public void disconnect() {
        _terminate = true;
    }

    public boolean isDisconnected() {
        return _terminated;
    }

    public void routine() {
        // do implement on your driver
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            _logger.error("Exception,", ex);
        }
    }

    @Override
    public void run() {
        try {
            while (!_terminate) {
                // call read method
                read();
                // polling interval
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < pollInterval) {
                    Thread.sleep(100);
                    // execute routine
                    routine();
                    if (_terminate) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
        _terminated = true;
        _config.setStatus(STATE.DOWN, "Stopped.");
    }
}
