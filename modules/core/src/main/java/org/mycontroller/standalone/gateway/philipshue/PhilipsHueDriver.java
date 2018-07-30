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
package org.mycontroller.standalone.gateway.philipshue;

import java.util.Map;
import java.util.Map.Entry;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.restclient.philipshue.PhilipsHueClient;
import org.mycontroller.restclient.philipshue.model.LightState;
import org.mycontroller.restclient.philipshue.model.State;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfigPhilipsHue;
import org.mycontroller.standalone.gateway.rest.RestDriverAbstract;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;
import org.mycontroller.standalone.provider.philipshue.Color;
import org.mycontroller.standalone.provider.philipshue.MessageParserPhilipsHue;
import org.mycontroller.standalone.provider.philipshue.MessagePhilipsHue;
import org.mycontroller.standalone.provider.philipshue.PHUtilities;
import org.mycontroller.standalone.provider.philipshue.PhilipsHue;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class PhilipsHueDriver extends RestDriverAbstract {

    private GatewayConfigPhilipsHue _config = null;
    private IMessageParser<MessagePhilipsHue> _parser = new MessageParserPhilipsHue();
    private IQueue<IMessage> _queue = null;
    private PhilipsHueClient _client = null;
    private volatile boolean _readRunning = false;

    public PhilipsHueDriver(GatewayConfigPhilipsHue _config, IQueue<IMessage> _queue) {
        super(_config, _config.getPollFrequency() * 1000 * 60L);
        this._config = _config;
        this._queue = _queue;
    }

    @Override
    public void connect() {
        try {
            _client = new PhilipsHueClient(_config.getUrl(), _config.getAuthorizedUser(), TRUST_HOST_TYPE.ANY);
            _config.setStatus(STATE.UP, "Connected Successfully");
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
        }
    }

    @Override
    public void write(IMessage message) throws MessageParserException {
        try {
            if (_config.getAuthorizedUser() != null && _config.getAuthorizedUser().length() > 0) {
                _logger.debug("Send data: {}, {}", _config, message);
                MessagePhilipsHue rawMessage = _parser.getGatewayData(message);
                MESSAGE_TYPE type = MESSAGE_TYPE.fromString(rawMessage.getType());
                if (type == MESSAGE_TYPE.C_SET) {
                    MESSAGE_TYPE_SET_REQ subType = MESSAGE_TYPE_SET_REQ.fromString(rawMessage.getSubType());
                    try {
                        State state = getHueUpdateState(subType, rawMessage.getSensorId(), rawMessage.getPayload());
                        if (state != null) {
                            _client.lights().updateState(rawMessage.getSensorId(), state);
                        } else
                            _logger.warn(" Unable to update {} ", rawMessage);
                    } catch (Exception ex) {
                        _logger.error("Exception, ", ex);
                    }
                } else if (type == MESSAGE_TYPE.C_INTERNAL) {
                    MESSAGE_TYPE_INTERNAL subType = MESSAGE_TYPE_INTERNAL.fromString(rawMessage.getSubType());
                    switch (subType) {
                        case I_PRESENTATION:
                            read();
                            break;
                        default:
                            _logger.error(" Not supported internal message: {} ", subType);
                            break;
                    }
                }
            } else {
                _logger.warn("Private key not set for this {}", _config);
            }
        } catch (Exception ex) {
            _logger.error("Exception, {}", message, ex);
        }
    }

    private State getHueUpdateState(MESSAGE_TYPE_SET_REQ subType, String sensorId, String payload) {
        State state = null;
        try {
            switch (subType) {
                case V_LIGHT_LEVEL:
                    double userValue = Double.valueOf(payload).doubleValue();
                    state = State.builder().bri(toBrightness(userValue).intValue()).build();
                    break;
                case V_STATUS:
                    state = State.builder().on("1".equalsIgnoreCase(payload)).build();
                    break;
                case V_RGB:
                    try {
                        LightState lightState = _client.lights().state(sensorId);
                        if (lightState != null) {
                            float[] xy = PHUtilities.calculateXY(
                                    Color.parseColor(payload), lightState.getModelid());
                            state = State.builder().xy(new Float[] { xy[0], xy[1] }).build();
                        }
                    } catch (Exception ex) {
                        _logger.error("Error while getting hue lights state ", ex);
                    }
                    //To prevent flood to the bridge. Before any call we wait at least 100ms.
                    //http://stackoverflow.com/questions/22101640/philips-hue-command-limitation
                    //source https://developers.meethue.com/documentation/hue-system-performance
                    sleep(100);
                    break;

                default:
                    break;
            }
        } catch (Exception ex) {
            _logger.error("Exception: data[subType:{}, sensorId:{}, payload:{}]", subType, sensorId, payload, ex);
        }
        return state;
    }

    private Integer toPercent(int brightness) {
        return (brightness * 100) / 255;
    }

    private Double toBrightness(double percentageValue) {
        //Verify user entry (0-100%)
        if (percentageValue > 100)
            percentageValue = 100;
        else if (percentageValue < 0)
            percentageValue = 0;
        return ((percentageValue * 255) / 100);
    }

    private void updateRecords(Map<String, LightState> records) throws MessageParserException {
        for (Entry<String, LightState> entry : records.entrySet()) {
            String key = entry.getKey();
            LightState value = entry.getValue();

            //Check does this sensor exists already.
            //If exists, check value. If there is a change update it.
            Sensor sensor = DaoUtils.getSensorDao().get(_config.getId(), PhilipsHue.NODE_EUI, key);
            boolean updateStatus = true;
            boolean updateLightLevel = true;
            boolean updateRGB = true;
            if (sensor != null) {
                for (SensorVariable variable : sensor.getVariables()) {
                    switch (variable.getVariableType()) {
                        case V_STATUS:
                            if (variable.getValue() == null || variable.getValue().equals(IMessage.PAYLOAD_EMPTY)) {
                                break;
                            }
                            if (variable.getValue().equals(value.getState().getOn() ? "1" : "0")) {
                                updateStatus = false;
                            }
                            break;
                        case V_LIGHT_LEVEL:
                            if (variable.getValue() == null || variable.getValue().equals(IMessage.PAYLOAD_EMPTY)) {
                                break;
                            }
                            if (variable.getValue().equals(toPercent(value.getState().getBri()).toString())) {
                                updateLightLevel = false;
                            }
                            break;
                        case V_RGB:
                            if (variable.getValue() == null || variable.getValue().equals(IMessage.PAYLOAD_EMPTY)) {
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
                        toPercent(value.getState().getBri()).toString());
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

    private void updateSetPayload(MESSAGE_TYPE_SET_REQ subType, String sensorId, String payload)
            throws MessageParserException {
        sendMessage(MessagePhilipsHue.builder()
                .timestamp(System.currentTimeMillis())
                .type(MESSAGE_TYPE.C_SET.getText())
                .subType(subType.getText())
                .payload(payload)
                .build());
    }

    private void updateSensorNameAndType(MESSAGE_TYPE_PRESENTATION subType, String sensorId, String sensorName)
            throws MessageParserException {
        sendMessage(MessagePhilipsHue.builder()
                .timestamp(System.currentTimeMillis())
                .type(MESSAGE_TYPE.C_PRESENTATION.getText())
                .subType(subType.getText())
                .payload(sensorName)
                .build());
    }

    private void sendMessage(MessagePhilipsHue rawMessage) throws MessageParserException {
        _queue.add(_parser.getMessage(_config, rawMessage));
    }

    @Override
    public void read() throws MessageParserException {
        if (_readRunning) {
            return;
        }
        try {
            _readRunning = true;
            _logger.debug("Getting hue lights...");
            final Map<String, LightState> lights = _client.lights().listAll();
            _logger.debug("{}", lights);
            if (lights != null && !lights.isEmpty()) {
                updateRecords(lights);
            } else {
                _logger.warn("Error no light found {} ", _client);
            }
        } catch (Exception ex) {
            _logger.error("Error while getting hue lights", ex);
            //In case something wrong with the bridge we waiting before attempting a new call.
            _logger.debug("On error retrying...");
            //Wait before trying contact the bridge.
            sleep(2000);
        } finally {
            _readRunning = false;
        }
    }
}