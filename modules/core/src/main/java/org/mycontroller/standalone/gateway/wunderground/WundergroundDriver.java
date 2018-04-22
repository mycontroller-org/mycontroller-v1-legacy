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
package org.mycontroller.standalone.gateway.wunderground;

import java.text.ParseException;

import org.mycontroller.restclient.wunderground.WundergroundClient;
import org.mycontroller.restclient.wunderground.model.Criteria;
import org.mycontroller.restclient.wunderground.model.CurrentObservation;
import org.mycontroller.restclient.wunderground.model.WUResponse;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.exceptions.NotSupportedException;
import org.mycontroller.standalone.gateway.config.GatewayConfigWunderground;
import org.mycontroller.standalone.gateway.rest.RestDriverAbstract;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.wunderground.Wunderground;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class WundergroundDriver extends RestDriverAbstract {

    private GatewayConfigWunderground _config = null;
    private IQueue<IMessage> _queue = null;
    private WundergroundClient _client = null;
    private Criteria _criteria = null;

    public WundergroundDriver(GatewayConfigWunderground _config, IQueue<IMessage> _queue) {
        super(_config, _config.getPollFrequency() * 1000 * 60L);
        this._config = _config;
        this._queue = _queue;

    }

    @Override
    public void connect() {
        try {
            _client = new WundergroundClient(_config.getApiKey(), _config.getTrustHostType());
            _criteria = Criteria.builder()
                    .location(_config.getLocation())
                    .geoIP(_config.getGeoIp())
                    //.features(Features.builder().conditions(1).build())
                    .build();
            _config.setStatus(STATE.UP, "Connected Successfully");
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
        }
    }

    @Override
    public void write(IMessage message) throws MessageParserException {
        _logger.debug("Sending data to Wunderground, not supported or not implemented! Dropping:{}", message);
        throw new NotSupportedException("Sending data to Wunderground, not supported or not implemented!");
    }

    @Override
    public void read() {
        try {
            WUResponse _response = _client.query(_criteria);
            _logger.debug("Client response: {}", _response);
            if (_response != null) {
                if (_response.getResponse().getError() == null) {
                    updateRecords(_response);
                } else {
                    _logger.error("Failed to query, fix the issue and reload manually to reconnect. {}, {}",
                            _response.getResponse().getError(), _config);
                    _config.setStatus(STATE.DOWN, _response.getResponse().getError().toString());
                }
            } else {
                _logger.warn("Unable to execute {}, {}", _config, _response);
            }
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void updateRecords(WUResponse response) throws ParseException {
        Long timestamp = response.getCurrent_observation().getObservation_epoch() * 1000;

        //Update only, when new data available
        if (_config.getLastObservationTime() != null && _config.getLastObservationTime() >= timestamp) {
            _logger.debug("Skipping update, LastObservationTime:{}, CurrentObservationTime:{}. {}",
                    _config.getLastObservationTime(), timestamp, _config);
            return;
        }

        String nodeEui = response.getCurrent_observation().getStation_id();
        //Set unique nodeEui based on location name
        if (_config.getMergeAllStations()) {
            nodeEui = Wunderground.NODE_EUI;
        }

        CurrentObservation observation = response.getCurrent_observation();
        UNIT_CONFIG unitConfig = UNIT_CONFIG.fromString(AppProperties.getInstance().getControllerSettings()
                .getUnitConfig());

        Double temperature = null;
        Float wind = null;
        String pressure = null;
        if (unitConfig == UNIT_CONFIG.METRIC) {
            temperature = observation.getTemp_c();
            wind = observation.getWind_kph();
            pressure = observation.getPressure_mb();
        } else {
            temperature = observation.getTemp_f();
            wind = observation.getWind_mph();
            pressure = observation.getPressure_in();
        }

        //Update presentation and value for that

        //Update node properties
        StringBuilder builder = new StringBuilder();
        builder.append("dp_city").append("=").append(observation.getDisplay_location().getCity()).append(";");
        builder.append("dp_country").append("=").append(observation.getDisplay_location().getCountry()).append(";");
        builder.append("dp_latitude").append("=").append(observation.getDisplay_location().getLatitude()).append(";");
        builder.append("dp_longitude").append("=").append(observation.getDisplay_location().getLongitude())
                .append(";");
        builder.append("dp_elevation").append("=").append(observation.getDisplay_location().getElevation())
                .append(";");

        builder.append("ob_city").append("=").append(observation.getObservation_location().getCity()).append(";");
        builder.append("ob_country").append("=").append(observation.getObservation_location().getCountry())
                .append(";");
        builder.append("ob_latitude").append("=").append(observation.getObservation_location().getLatitude())
                .append(";");
        builder.append("ob_longitude").append("=").append(observation.getObservation_location().getLongitude())
                .append(";");
        builder.append("ob_elevation").append("=").append(observation.getObservation_location().getElevation())
                .append(";");
        builder.append("forecast_url").append("=").append(observation.getForecast_url());

        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_INTERNAL.getText(),
                MESSAGE_TYPE_INTERNAL.I_PROPERTIES.getText(), IMessage.SENSOR_BROADCAST_ID, builder.toString());
        //Update node name
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_ARDUINO_NODE.getText(), IMessage.SENSOR_BROADCAST_ID,
                response.getResponse().getVersion());
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_INTERNAL.getText(),
                MESSAGE_TYPE_INTERNAL.I_SKETCH_NAME.getText(), IMessage.SENSOR_BROADCAST_ID,
                observation.getObservation_location().getCity());

        //Update temperature
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_TEMP.getText(), Wunderground.S_ID_TEMPERATURE, "Temperature");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_TEMP.getText(),
                Wunderground.S_ID_TEMPERATURE, McUtils.getDoubleAsString(temperature));

        //Update humidity
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_HUM.getText(), Wunderground.S_ID_HUMIDITY, "Humidity");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_HUM.getText(),
                Wunderground.S_ID_HUMIDITY, observation.getRelative_humidity().replaceAll("[^0-9]", ""));

        //Update wind
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_WIND.getText(), Wunderground.S_ID_WIND, "Wind");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_WIND.getText(),
                Wunderground.S_ID_WIND, McUtils.getDoubleAsString(wind.doubleValue()));
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_DIRECTION.getText(),
                Wunderground.S_ID_WIND, String.valueOf(observation.getWind_degrees()));
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_TEXT.getText(),
                Wunderground.S_ID_WIND, observation.getWind_string());

        //Update Barometer
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_WIND.getText(), Wunderground.S_ID_BAROMETER, "Barometer");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_PRESSURE.getText(),
                Wunderground.S_ID_BAROMETER, pressure);
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_FORECAST.getText(),
                Wunderground.S_ID_BAROMETER, observation.getIcon());
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_VAR1.getText(),
                Wunderground.S_ID_BAROMETER, observation.getWeather());

        //Update last ObservationTime
        _config.updateLastObservationTime(timestamp);
    }

    private void updateRecord(Long timestamp, String nodeEui, String type, String subType, String sensorId,
            String payload) {
        _queue.add(IMessage.builder()
                .isTxMessage(false)
                .gatewayId(_config.getId())
                .nodeEui(nodeEui)
                .sensorId(sensorId)
                .type(type)
                .subType(subType)
                .payload(payload)
                .timestamp(timestamp)
                .build());
    }

}