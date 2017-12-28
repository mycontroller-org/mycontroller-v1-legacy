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
package org.mycontroller.standalone.gateway.wunderground;

import java.text.ParseException;
import java.util.Arrays;

import org.mycontroller.restclient.core.ClientResponse;
import org.mycontroller.restclient.wunderground.WundergroundClient;
import org.mycontroller.restclient.wunderground.WundergroundClientBuilder;
import org.mycontroller.restclient.wunderground.model.Criteria;
import org.mycontroller.restclient.wunderground.model.CurrentObservation;
import org.mycontroller.restclient.wunderground.model.WUResponse;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.gateway.model.GatewayWunderground;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.provider.wunderground.WUUtils;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.1.0
 */
@Slf4j
public class WundergroundGatewayPoller implements Runnable {
    private WundergroundClient wundergroundClient = null;
    private boolean terminate = false;
    private boolean terminated = false;
    private GatewayWunderground gateway = null;
    private Criteria criteria = null;

    public WundergroundGatewayPoller(GatewayWunderground gateway) throws Exception {
        this.gateway = gateway;
        wundergroundClient = new WundergroundClientBuilder()
                .addProperty(WundergroundClient.KEY_API_KEY, gateway.getApiKey())
                .uri(WundergroundClient.URI, gateway.getTrustHostType())
                .build();
        criteria = Criteria.builder()
                .location(gateway.getLocation())
                .geoIP(gateway.getGeoIp())
                //.features(Features.builder().conditions(1).build())
                .build();
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
                ClientResponse<WUResponse> clientResponse = wundergroundClient.query(criteria);
                _logger.debug("Client response: {}", clientResponse);
                if (clientResponse.isSuccess() && clientResponse.getEntity() != null) {
                    if (clientResponse.getEntity().getResponse().getError() == null) {
                        updateRecords(clientResponse.getEntity());
                    } else {
                        _logger.error("Failed to query, fix the issue and reload manually to reconnect. {}, {}",
                                clientResponse.getEntity().getResponse().getError(), getGateway());
                        gateway.setStatus(STATE.DOWN, clientResponse.getEntity().getResponse().getError().toString());
                        break;
                    }
                } else {
                    _logger.warn("Unable to execute {}, {}", getGateway(), clientResponse);
                }

            } catch (Exception ex) {
                _logger.error("Exception, ", ex);
            }
            long pollFrequency = gateway.getPollFrequency() * McUtils.MINUTE;
            while (pollFrequency > 0 && !isTerminate()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    _logger.error("Exception,", ex);
                }
                pollFrequency -= 100;
            }
        }
        _logger.debug("WundergroundGatewayListener Terminated...");
        this.terminated = true;
    }

    private void updateRecords(WUResponse response) throws ParseException {
        Long timestamp = response.getCurrent_observation().getObservation_epoch() * 1000;

        //Update only, when new data available
        if (gateway.getLastObservationTime() != null && gateway.getLastObservationTime() >= timestamp) {
            _logger.debug("Skipping update, LastObservationTime:{}, CurrentObservationTime:{}. {}",
                    gateway.getLastObservationTime(), timestamp, gateway);
            return;
        }

        String nodeEui = response.getCurrent_observation().getStation_id();
        //Set unique nodeEui based on location name
        if (gateway.getMergeAllStations()) {
            nodeEui = WUUtils.NODE_EUI;
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
                MESSAGE_TYPE_INTERNAL.I_PROPERTIES.getText(), McMessage.SENSOR_BROADCAST_ID, builder.toString());
        //Update node name
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_ARDUINO_NODE.getText(), McMessage.SENSOR_BROADCAST_ID,
                response.getResponse().getVersion());
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_INTERNAL.getText(),
                MESSAGE_TYPE_INTERNAL.I_SKETCH_NAME.getText(), McMessage.SENSOR_BROADCAST_ID,
                observation.getObservation_location().getCity());

        //Update temperature
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_TEMP.getText(), WUUtils.S_ID_TEMPERATURE, "Temperature");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_TEMP.getText(),
                WUUtils.S_ID_TEMPERATURE, McUtils.getDoubleAsString(temperature));

        //Update humidity
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_HUM.getText(), WUUtils.S_ID_HUMIDITY, "Humidity");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_HUM.getText(),
                WUUtils.S_ID_HUMIDITY, observation.getRelative_humidity().replaceAll("[^0-9]", ""));

        //Update wind
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_WIND.getText(), WUUtils.S_ID_WIND, "Wind");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_WIND.getText(),
                WUUtils.S_ID_WIND, McUtils.getDoubleAsString(wind));
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_DIRECTION.getText(),
                WUUtils.S_ID_WIND, String.valueOf(observation.getWind_degrees()));
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_TEXT.getText(),
                WUUtils.S_ID_WIND, observation.getWind_string());

        //Update Barometer
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_PRESENTATION.getText(),
                MESSAGE_TYPE_PRESENTATION.S_WIND.getText(), WUUtils.S_ID_BAROMETER, "Barometer");
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_PRESSURE.getText(),
                WUUtils.S_ID_BAROMETER, pressure);
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_FORECAST.getText(),
                WUUtils.S_ID_BAROMETER, observation.getIcon());
        updateRecord(timestamp, nodeEui, MESSAGE_TYPE.C_SET.getText(), MESSAGE_TYPE_SET_REQ.V_VAR1.getText(),
                WUUtils.S_ID_BAROMETER, observation.getWeather());

        //Update last ObservationTime
        gateway.updateLastObservationTime(timestamp);
    }

    private void updateRecord(Long timestamp, String nodeEui, String messageType, String subType, String sensorId,
            String payload) {
        RawMessageQueue.getInstance().putMessage(RawMessage.builder()
                .gatewayId(gateway.getId())
                //data order: node EUI, messageType, subType, sensorId, payload
                .data(Arrays.asList(nodeEui, messageType, subType, sensorId, payload))
                .timestamp(timestamp)
                .networkType(gateway.getNetworkType())
                .build());
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
        //not implemented
        _logger.warn("Send to Wunderground. not implemented. Dropping:{}", rawMessage);
    }

    public GatewayWunderground getGateway() {
        return gateway;
    }
}
