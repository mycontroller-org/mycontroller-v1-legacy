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

import org.mycontroller.restclient.core.ClientResponse;
import org.mycontroller.restclient.philips.hue.PhilipsHueClient;
import org.mycontroller.restclient.philips.hue.PhilipsHueClientBuilder;
import org.mycontroller.restclient.philips.hue.model.LightState;
import org.mycontroller.restclient.philips.hue.model.State;
import org.mycontroller.standalone.gateway.model.GatewayPhilipsHue;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.model.philips.Color;
import org.mycontroller.standalone.model.philips.PHUtilities;
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
    private boolean onRequestUpdate = false;

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
                while (pollFrequency > 0 && !isTerminate() && !onRequestUpdate) {
                    Thread.sleep(100);
                    pollFrequency -= 100;
                }
                onRequestUpdate = false;
            } catch (InterruptedException | ParseException ex) {
                _logger.error("Exception, ", ex);
            }
        }
        _logger.debug("PhilipsHueGatewayPoller Terminated...");
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

    public boolean isOnRequestUpdate() {
        return onRequestUpdate;
    }

    public synchronized void setOnRequestUpdate(boolean onRequestUpdate) {
        this.onRequestUpdate = onRequestUpdate;
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
            @SuppressWarnings("unchecked")
            List<String> data = (List<String>) rawMessage.getData();
            MESSAGE_TYPE type = MESSAGE_TYPE.fromString(data.get(2));
            if (data.size() == 4) {//sensorId, payload,messageType,subType
                if (type == MESSAGE_TYPE.C_SET) {
                    MESSAGE_TYPE_SET_REQ msgType = MESSAGE_TYPE_SET_REQ.fromString(data.get(3));
                    State state = getHueUpdateState(msgType, data);
                    if (state != null)
                        philipsHueClient.lights().updateState(data.get(0), state);
                } else if (type == MESSAGE_TYPE.C_INTERNAL) {
                    switch (MESSAGE_TYPE_INTERNAL.fromString(data.get(3))) {
                        case I_PRESENTATION:
                            if (!isOnRequestUpdate()) {
                                setOnRequestUpdate(true);
                            }

                            break;

                        default:
                            break;
                    }
                }
            } else {
                _logger.error("data array size should be exactly 4, data:{}", data);
            }
        } else {
            _logger.warn("Private key not set for this {}", gateway);
        }

    }

    private State getHueUpdateState(MESSAGE_TYPE_SET_REQ msgType, List<String> data) {
        State state = null;
        switch (msgType) {
            case V_HUE:
                state = State.builder().hue(Integer.valueOf(data.get(1))).build();
                break;
            case V_BRIGHTNESS:
                state = State.builder().bri(Integer.valueOf(data.get(1))).build();
                break;
            case V_SATURATION:
                state = State.builder().sat(Integer.valueOf(data.get(1))).build();
                break;
            case V_MIRED_COLOR:
                state = State.builder().ct(Integer.valueOf(data.get(1))).build();
                break;
            case V_STATUS:
                state = State.builder().on("1".equalsIgnoreCase(data.get(1))).build();
                break;
            case V_RGB:
                String sensorId = data.get(0);
                ClientResponse<LightState> lightState = philipsHueClient.lights().state(sensorId);
                if (lightState != null) {
                    float[] xy = PHUtilities.calculateXY(
                            Color.parseColor(data.get(1)), lightState.getEntity().getModelid());
                    state = State.builder().xy(new Float[] { xy[0], xy[1] }).build();
                }
                break;

            default:
                break;
        }
        return state;
    }

    public GatewayPhilipsHue getGateway() {
        return gateway;
    }

    public void close() {

    }

}
