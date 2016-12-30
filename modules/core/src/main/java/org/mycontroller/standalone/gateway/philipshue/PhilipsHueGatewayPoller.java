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
package org.mycontroller.standalone.gateway.philipshue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mycontroller.restclient.philips.hue.PhilipsHueClient;
import org.mycontroller.restclient.philips.hue.PhilipsHueClientBuilder;
import org.mycontroller.restclient.philips.hue.model.LightState;
import org.mycontroller.restclient.philips.hue.model.State;
import org.mycontroller.standalone.gateway.model.GatewayPhilipsHue;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Fraid(https://github.com/Fraid)
 */
@Slf4j
public class PhilipsHueGatewayPoller implements Runnable {
    private boolean terminate = false;
    private boolean terminated = false;
    private PhilipsHueClient philipsHueClient;

    public PhilipsHueGatewayPoller() {
    }

    private GatewayPhilipsHue gateway = null;

    public PhilipsHueGatewayPoller(GatewayPhilipsHue gateway) throws Exception {
        this.gateway = gateway;
        philipsHueClient = new PhilipsHueClientBuilder().uri(gateway.getUrl())
                .addProperty(PhilipsHueClient.KEY_AUTHORIZED_USER, gateway.getAuthorizedUser()).build();

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
                Map<String, LightState> clientResponse = philipsHueClient.lights().listAll().getEntity();
                _logger.debug("Client response: {}", clientResponse);
                if (clientResponse != null) {
                    updateRecords(clientResponse);
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

    private void updateRecords(Map<String, LightState> records) throws ParseException {

        for (Entry<String, LightState> entry : records.entrySet()) {
            String key = entry.getKey();
            LightState value = entry.getValue();
            RawMessageQueue.getInstance().putMessage(RawMessage.builder()
                    .gatewayId(gateway.getId())
                    .data(Arrays.asList(key, value.getState().getOn() ? "1" : "0", value.getName()))
                    .networkType(gateway.getNetworkType())
                    .build());
        }

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
        if (gateway.getAuthorizedUser() != null && gateway.getAuthorizedUser().length() > 0) {
            _logger.info("Send data: {}, {}", this.gateway, rawMessage);
            //TODO Probably get hue state then update color according to the colorMode.
            //Handle other setting of state
            List<Object> data = (List<Object>) rawMessage.getData();
            MESSAGE_TYPE_SET_REQ msgType = MESSAGE_TYPE_SET_REQ.fromString(data.get(3).toString());
            State state = null;
            switch (msgType) {
                case V_STATUS:
                    state = State.builder()
                            .on("1".equalsIgnoreCase(data.get(1).toString())).build();
                    break;
                case V_RGB:
                    String rgb=data.get(1).toString();
                    //Apply RGB to HUE conversion here.
                    break;
                case V_RGBW:
                    break;
                case V_PERCENTAGE:
                    break;

                default:
                    break;
            }
            if (state != null)
                philipsHueClient.lights().updateState(data.get(0).toString(), state);
        } else {
            _logger.warn("Private key not set for this {}", gateway);
        }
    }

    public GatewayPhilipsHue getGateway() {
        return gateway;
    }

    public void close() {

    }

}
