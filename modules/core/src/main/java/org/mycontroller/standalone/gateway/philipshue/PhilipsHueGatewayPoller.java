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
import org.mycontroller.restclient.core.jaxrs.Empty;
import org.mycontroller.restclient.philips.hue.PhilipsHueClient;
import org.mycontroller.restclient.philips.hue.PhilipsHueClientBuilder;
import org.mycontroller.restclient.philips.hue.model.LightState;
import org.mycontroller.restclient.philips.hue.model.State;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.gateway.model.GatewayPhilipsHue;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
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
        // Initial delay, allow to add this object in McObject manager
        waitFor(McUtils.SECOND * 10);
        while (!isTerminate()) {
            try {
                _logger.debug("Getting hue lights...on user request : {} ", onRequestUpdate);
                //                if (onStart) {
                final ClientResponse<Map<String, LightState>> clientResponse = philipsHueClient.lights().listAll();
                if (clientResponse.isSuccess()) {
                    Map<String, LightState> lights = clientResponse.getEntity();
                    _logger.debug("Client response: {} {} ", clientResponse, onRequestUpdate);
                    if (clientResponse != null && !lights.isEmpty()) {
                        updateRecords(lights);
                    } else {
                        _logger.warn("Error no light found {} ", philipsHueClient);
                    }
                } else {
                    _logger.debug("Error while getting hue lights: {}", clientResponse);
                    //In case something wrong with the bridge we waiting before attempting a new call.
                    _logger.debug("On error retrying...");
                    //Wait before trying contact the bridge.
                    waitFor(2000);
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
                _logger.debug("On error retrying...");
                //Wait before trying contact the bridge.
                waitFor(2000);
            }

        }
        _logger.debug("PhilipsHueGatewayPoller Terminated...");
        this.terminated = true;
    }

    private void updateRecords(Map<String, LightState> records) throws ParseException {
        for (Entry<String, LightState> entry : records.entrySet()) {
            String key = entry.getKey();
            LightState value = entry.getValue();

            //Check does this sensor exists already.
            //If exists, check value. If there is a change update it.
            Sensor sensor = DaoUtils.getSensorDao().get(gateway.getId(), PhilipsHueUtils.NODE_EUI, key);
            boolean updateStatus = true;
            boolean updateLightLevel = true;
            boolean updateRGB = true;
            if (sensor != null) {
                for (SensorVariable variable : sensor.getVariables()) {
                    switch (variable.getVariableType()) {
                        case V_STATUS:
                            if (variable.getValue() == null || variable.getValue().equals(McMessage.PAYLOAD_EMPTY)) {
                                break;
                            }
                            if (variable.getValue().equals(value.getState().getOn() ? "1" : "0")) {
                                updateStatus = false;
                            }
                            break;
                        case V_LIGHT_LEVEL:
                            if (variable.getValue() == null || variable.getValue().equals(McMessage.PAYLOAD_EMPTY)) {
                                break;
                            }
                            if (variable.getValue().equals(
                                    PhilipsHueUtils.toPercent(value.getState().getBri()).toString())) {
                                updateLightLevel = false;
                            }
                            break;
                        case V_RGB:
                            if (variable.getValue() == null || variable.getValue().equals(McMessage.PAYLOAD_EMPTY)) {
                                break;
                            }
                            if (value.getState().getXy() != null && value.getState().getXy().length == 2) {
                                Float[] xy = value.getState().getXy();
                                if (variable.getValue().equals(
                                        PHUtilities.getHexFromXY(new float[] { xy[0], xy[1] }, value.getModelid()))) {
                                    updateRGB = false;
                                }
                            } else {
                                updateRGB = false;
                            }
                            break;
                        default:
                            _logger.warn("SenaorVariable type '{}' is not implemented!", variable.getVariableType()
                                    .getText());
                            break;

                    }
                }
            } else {
                //Update sensor name and type
                updateSensorNameAndType(MESSAGE_TYPE_PRESENTATION.S_RGB_LIGHT, key, value.getName());
            }

            //Update status payload
            if (updateStatus) {
                updateSetPayload(MESSAGE_TYPE_SET_REQ.V_STATUS, key, value.getState().getOn() ? "1" : "0");
            }

            //Update light level (0 to 100%), payload
            if (updateLightLevel) {
                updateSetPayload(MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL, key,
                        PhilipsHueUtils.toPercent(value.getState().getBri()).toString());
            }

            //Update RGB color, payload
            if (updateRGB) {
                //Set color from xy
                Float[] xy = value.getState().getXy();
                if (xy != null && xy.length == 2) {
                    updateSetPayload(MESSAGE_TYPE_SET_REQ.V_RGB, key,
                            PHUtilities.getHexFromXY(new float[] { xy[0], xy[1] }, value.getModelid()));
                }
            }
        }
    }

    private void updateSetPayload(MESSAGE_TYPE_SET_REQ subType, String sensorId, String sensorName) {
        putMessage(Arrays.asList(MESSAGE_TYPE.C_SET.getText(), subType.getText(), sensorId, sensorName));
    }

    private void updateSensorNameAndType(MESSAGE_TYPE_PRESENTATION subType, String sensorId, String sensorName) {
        putMessage(Arrays.asList(MESSAGE_TYPE.C_PRESENTATION.getText(), subType.getText(), sensorId, sensorName));
    }

    // Data order: messageType, subType, sensorId, payload
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
            if (data.size() == 4) {//messageType, subType, sensorId, payload
                MESSAGE_TYPE type = MESSAGE_TYPE.fromString(data.get(0));
                String subTypeString = data.get(1);
                String sensorId = data.get(2);
                String payload = data.get(3);
                if (type == MESSAGE_TYPE.C_SET) {
                    MESSAGE_TYPE_SET_REQ subType = MESSAGE_TYPE_SET_REQ.fromString(subTypeString);
                    try {
                        State state = getHueUpdateState(subType, sensorId, payload);
                        if (state != null) {
                            ClientResponse<Empty> updateState = philipsHueClient.lights().updateState(
                                    sensorId, state);
                            if (updateState != null && !updateState.isSuccess()) {
                                _logger.debug("Error while updating hue lights: {}, {} ", updateState, rawMessage);
                            }
                        } else
                            _logger.warn(" Unable to update {} ", rawMessage);
                    } catch (Exception ex) {
                        _logger.error("Exception, ", ex);
                    }
                } else if (type == MESSAGE_TYPE.C_INTERNAL) {
                    MESSAGE_TYPE_INTERNAL subType = MESSAGE_TYPE_INTERNAL.fromString(subTypeString);
                    switch (subType) {
                        case I_PRESENTATION:
                            setOnRequestUpdate(true);
                            break;
                        default:
                            _logger.error(" Not supported internal message: {} ", subType);
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

    private State getHueUpdateState(MESSAGE_TYPE_SET_REQ subType, String sensorId, String payload) {
        State state = null;
        switch (subType) {
            case V_LIGHT_LEVEL:
                double userValue = Double.valueOf(payload).doubleValue();
                state = State.builder().bri(PhilipsHueUtils.toBrightness(userValue).intValue()).build();
                break;
            case V_STATUS:
                state = State.builder().on("1".equalsIgnoreCase(payload)).build();
                break;
            case V_RGB:
                try {
                    ClientResponse<LightState> lightState = philipsHueClient.lights().state(sensorId);
                    if (lightState != null && lightState.isSuccess()) {
                        float[] xy = PHUtilities.calculateXY(
                                Color.parseColor(payload), lightState.getEntity().getModelid());
                        state = State.builder().xy(new Float[] { xy[0], xy[1] }).build();
                    } else if (lightState != null) {
                        _logger.debug(
                                "Error while getting hue lights state, subType:{}, sensorId:{}, payload:{}, {}",
                                subType, sensorId, payload, lightState);
                    }
                } catch (Exception ex) {
                    _logger.error("Error while getting hue lights state ", ex);
                }
                //To prevent flood to the bridge. Before any call we wait at least 100ms.
                //http://stackoverflow.com/questions/22101640/philips-hue-command-limitation
                //source https://developers.meethue.com/documentation/hue-system-performance
                waitFor(100);
                break;

            default:
                break;
        }
        return state;
    }

    private void waitFor(long delayTime) {
        try {
            Thread.sleep(delayTime);
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
