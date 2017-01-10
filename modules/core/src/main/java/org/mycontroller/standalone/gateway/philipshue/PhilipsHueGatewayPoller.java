/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
import org.mycontroller.standalone.provider.philipshue.PhilipsHueUtils;
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
                _logger.debug("Getting hue lights...on user request : {} ", onRequestUpdate);
                final ClientResponse<Map<String, LightState>> listAll = philipsHueClient.lights().listAll();
                if (listAll.isSuccess()) {
                    Map<String, LightState> clientResponse = listAll.getEntity();
                    _logger.debug("Client response: {} {} ", clientResponse, onRequestUpdate);
                    if (clientResponse != null && !clientResponse.isEmpty()) {
                        updateRecords(clientResponse);
                    } else {
                        _logger.warn("Error no light found {} ", philipsHueClient);
                    }
                } else {
                    _logger.debug("Error while getting hue lights: {} - {} ", listAll.getStatusCode(),
                            listAll.getErrorMsg());
                    //In case something wrong with the bridge we waiting before attempting a new call.
                    waitOnError();
                }
                long pollFrequency = gateway.getPollFrequency() * McUtils.MINUTE;
                while (pollFrequency > 0 && !isTerminate() && !onRequestUpdate) {
                    Thread.sleep(100);
                    pollFrequency -= 100;
                }
                onRequestUpdate = false;
            } catch (Exception ex) {
                //On January 6, 2017, PhilipsHueClient will crash when network is unreachable. And unable to reconnect.
                _logger.error("Exception, ", ex);
                waitOnError();
            }

        }
        _logger.debug("PhilipsHueGatewayPoller Terminated...");
        this.terminated = true;
    }

    private void waitOnError() {
        try {
            _logger.debug("On error retrying...");
            //Wait before trying contact the bridge.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            _logger.error("Exception,", e);
        }
    }

    private void updateRecords(Map<String, LightState> records) throws ParseException {
        for (Entry<String, LightState> entry : records.entrySet()) {
            String key = entry.getKey();
            LightState value = entry.getValue();
            //Set status
            String subType = MESSAGE_TYPE_SET_REQ.V_STATUS.getText();
            putMessage(Arrays.asList(key, value.getState().getOn() ? "1" : "0", value.getName(), subType));

            //Set light level (0 to 100%)
            subType = MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL.getText();
            Integer percentVal = PhilipsHueUtils.toPercent(value.getState().getBri());
            putMessage(Arrays.asList(key, percentVal.toString(), value.getName(), subType));

            //Set color from xy
            Float[] xy = value.getState().getXy();
            if (xy != null && xy.length == 2) {
                subType = MESSAGE_TYPE_SET_REQ.V_RGB.getText();
                String hexColor = PHUtilities.getHexFromXY(new float[] { xy[0], xy[1] }, value.getModelid());
                putMessage(Arrays.asList(key, hexColor, value.getName(), subType));
            }
        }
    }

    private void putMessage(List<String> data) {
        RawMessageQueue.getInstance().putMessage(RawMessage.builder()
                .gatewayId(gateway.getId())
                .data(data)
                .networkType(gateway.getNetworkType())
                .build());
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
            _logger.debug("Send data: {}, {}", this.gateway, rawMessage);
            @SuppressWarnings("unchecked")
            List<String> data = (List<String>) rawMessage.getData();
            MESSAGE_TYPE type = MESSAGE_TYPE.fromString(data.get(2));
            if (data.size() == 4) {//sensorId, payload,messageType,subType
                if (type == MESSAGE_TYPE.C_SET) {
                    MESSAGE_TYPE_SET_REQ subType = MESSAGE_TYPE_SET_REQ.fromString(data.get(3));
                    try {
                        State state = getHueUpdateState(subType, data);
                        if (state != null) {
                            ClientResponse<String> updateState = philipsHueClient.lights().updateState(data.get(0),
                                    state);
                            if (updateState != null && !updateState.isSuccess()) {
                                _logger.debug("Error while updating hue lights: {} - {} - {} ",
                                        updateState.getStatusCode(),
                                        updateState.getErrorMsg(), rawMessage);
                            }
                        } else
                            _logger.warn(" Unable to update {} ", rawMessage);
                    } catch (Exception ex) {
                        _logger.error("Exception, ", ex);
                    }
                } else if (type == MESSAGE_TYPE.C_INTERNAL) {
                    switch (MESSAGE_TYPE_INTERNAL.fromString(data.get(3))) {
                        case I_PRESENTATION:
                            setOnRequestUpdate(true);
                            break;
                        default:
                            _logger.error(" Unknow internal messager for PhilipsHueGateway {} ", data);
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

    private State getHueUpdateState(MESSAGE_TYPE_SET_REQ subType, List<String> data) {
        State state = null;
        switch (subType) {
            case V_LIGHT_LEVEL:
                double userValue = Double.valueOf(data.get(1)).doubleValue();
                state = State.builder().bri(PhilipsHueUtils.toBrightness(userValue).intValue()).build();
                break;
            case V_STATUS:
                state = State.builder().on("1".equalsIgnoreCase(data.get(1))).build();
                break;
            case V_RGB:
                String sensorId = data.get(0);
                try {
                    ClientResponse<LightState> lightState = philipsHueClient.lights().state(sensorId);
                    if (lightState != null && lightState.isSuccess()) {
                        float[] xy = PHUtilities.calculateXY(
                                Color.parseColor(data.get(1)), lightState.getEntity().getModelid());
                        state = State.builder().xy(new Float[] { xy[0], xy[1] }).build();
                    } else if (lightState != null) {
                        _logger.debug("Error while getting hue lights state: {} - {} - {} ",
                                lightState.getStatusCode(),
                                lightState.getErrorMsg(), data);
                    }
                } catch (Exception e) {
                    _logger.error("Error while getting hue lights state ", e);
                }
                waitForHueBridge();
                break;

            default:
                break;
        }
        return state;
    }

    private void waitForHueBridge() {
        //To prevent flood to the bridge. Before any call we wait at least 100ms.
        //http://stackoverflow.com/questions/22101640/philips-hue-command-limitation
        //source https://developers.meethue.com/documentation/hue-system-performance
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    public GatewayPhilipsHue getGateway() {
        return gateway;
    }

    public void close() {

    }

}
