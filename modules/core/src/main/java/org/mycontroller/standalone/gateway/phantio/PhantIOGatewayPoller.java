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
package org.mycontroller.standalone.gateway.phantio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.gateway.model.GatewayPhantIO;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.phantio.PhantIOClient;
import org.mycontroller.standalone.restclient.phantio.PhantIOClientImpl;
import org.mycontroller.standalone.restclient.phantio.model.PostResponse;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class PhantIOGatewayPoller implements Runnable {
    private PhantIOClient phantIOClient = null;
    private boolean terminate = false;
    private boolean terminated = false;
    private GatewayPhantIO gateway = null;
    //2016-07-18T20:50:04+05:30
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    public PhantIOGatewayPoller(GatewayPhantIO gateway) throws Exception {
        this.gateway = gateway;
        phantIOClient = new PhantIOClientImpl(
                gateway.getUrl(),
                gateway.getPublicKey(),
                gateway.getPrivateKey(),
                gateway.getTrustHostType());
    }

    @Override
    public void run() {
        //Initial delay, allow to add this object in McObject manager
        try {
            Thread.sleep(McUtils.SECOND * 10);
        } catch (InterruptedException ex) {
            _logger.error("Exception, ", ex);
        }
        while (!isTerminate()) {
            try {
                ClientResponse<List<HashMap<String, String>>> clientResponse = phantIOClient
                        .get(gateway.getRecordsLimit());
                _logger.debug("Client response: {}", clientResponse);
                if (clientResponse.getEntity() != null) {
                    updateRecords(clientResponse.getEntity());
                }
                long pollFrequency = gateway.getPollFrequency() * McUtils.MINUTE;
                while (pollFrequency > 0 && !isTerminate()) {
                    Thread.sleep(100);
                    pollFrequency -= 100;
                }
            } catch (InterruptedException | ParseException ex) {
                _logger.error("Exception, ", ex);
            }
        }
        _logger.debug("PhantIOGatewayListener Terminated...");
        this.terminated = true;
    }

    private void updateRecords(List<HashMap<String, String>> records) throws ParseException {
        for (HashMap<String, String> record : records) {
            long timestamp = getTimestamp(record.get("timestamp"));
            if (gateway.getLastUpdate() == null || gateway.getLastUpdate() < timestamp) {
                for (String key : record.keySet()) {
                    if (!key.equals("timestamp")) {
                        RawMessageQueue.getInstance().putMessage(RawMessage.builder()
                                .gatewayId(gateway.getId())
                                //data order: key, value, timestamp
                                .data(Arrays.asList(key, record.get(key), timestamp))
                                .networkType(gateway.getNetworkType())
                                .build());
                        gateway.setLastUpdate(timestamp);
                    }
                }
                gateway.updateLastPollTime(timestamp);
            }
        }
    }

    private long getTimestamp(String timestamp) throws ParseException {
        return timestampFormat.parse(timestamp).getTime();
    }

    public boolean isTerminate() {
        return terminate;
    }

    public synchronized void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void write(RawMessage rawMessage) {
        if (gateway.getPrivateKey() != null && gateway.getPrivateKey().length() > 0) {
            _logger.debug("Send data: {}, {}", this.gateway, rawMessage);
            @SuppressWarnings("unchecked")
            List<String> data = (List<String>) rawMessage.getData();
            if (data.size() == 2) {
                ClientResponse<PostResponse> clientResponse = phantIOClient.post(data.get(0), data.get(1));
                if (!clientResponse.isSuccess()) {
                    _logger.error("Failed to send data:{}, {}, {}", rawMessage, this.gateway, clientResponse);
                }
            } else {
                _logger.error("data array size should be exactly 2, data:{}", data);
            }
        } else {
            _logger.warn("Private key not set for this {}", gateway);
        }
    }

    public GatewayPhantIO getGateway() {
        return gateway;
    }
}
