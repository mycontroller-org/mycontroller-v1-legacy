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
package org.mycontroller.standalone.gateway.config;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.utils.McUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { "apiKey" })
public class GatewayConfigWunderground extends GatewayConfig {
    public static final String KEY_TRUST_HOST_TYPE = "trustHostType";
    public static final String KEY_API_KEY = "apiKey";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_MERGE_ALL_STATIONS = "mergeAllStations";
    public static final String KEY_GEO_IP = "geoIp";
    public static final String KEY_POLL_FREQUENCY = "pollFrequency";
    public static final String KEY_LAST_UPDATE = "lastUpdate";
    public static final String KEY_LAST_OBSERVATION_TIME = "lastOBTime";

    private TRUST_HOST_TYPE trustHostType;
    private String apiKey;
    private String location;
    private String geoIp;
    private Boolean mergeAllStations;
    private Integer pollFrequency;
    private Long lastUpdate;
    private Long lastObservationTime;

    public GatewayConfigWunderground() {

    }

    public GatewayConfigWunderground(GatewayTable gatewayTable) {
        updateGateway(gatewayTable);
    }

    @Override
    @JsonIgnore
    public GatewayTable getGatewayTable() {
        GatewayTable gatewayTable = super.getGatewayTable();
        gatewayTable.getProperties().put(KEY_TRUST_HOST_TYPE, trustHostType.getText());
        gatewayTable.getProperties().put(KEY_API_KEY, apiKey);
        gatewayTable.getProperties().put(KEY_LOCATION, location);
        gatewayTable.getProperties().put(KEY_MERGE_ALL_STATIONS, mergeAllStations);
        gatewayTable.getProperties().put(KEY_GEO_IP, geoIp);
        gatewayTable.getProperties().put(KEY_POLL_FREQUENCY, pollFrequency);
        gatewayTable.getProperties().put(KEY_LAST_UPDATE, lastUpdate);
        gatewayTable.getProperties().put(KEY_LAST_OBSERVATION_TIME, lastObservationTime);
        return gatewayTable;
    }

    @Override
    @JsonIgnore
    public void updateGateway(GatewayTable gatewayTable) {
        super.updateGateway(gatewayTable);
        trustHostType = TRUST_HOST_TYPE.fromString((String) gatewayTable.getProperty(KEY_TRUST_HOST_TYPE,
                TRUST_HOST_TYPE.DEFAULT.getText()));
        apiKey = (String) gatewayTable.getProperty(KEY_API_KEY);
        location = (String) gatewayTable.getProperty(KEY_LOCATION);
        mergeAllStations = (Boolean) gatewayTable.getProperty(KEY_MERGE_ALL_STATIONS);
        if (mergeAllStations == null) {
            mergeAllStations = false;
        }
        geoIp = (String) gatewayTable.getProperty(KEY_GEO_IP);
        if (geoIp != null && geoIp.length() == 0) {
            geoIp = null;
        }
        pollFrequency = (Integer) gatewayTable.getProperty(KEY_POLL_FREQUENCY, 120);
        lastUpdate = McUtils.getLong(gatewayTable.getProperty(KEY_LAST_UPDATE));
        lastObservationTime = McUtils.getLong(gatewayTable.getProperty(KEY_LAST_OBSERVATION_TIME));
    }

    @Override
    public String getConnectionDetails() {
        StringBuilder builder = new StringBuilder();
        builder.append("Location:[").append(getLocation()).append("]");
        if (getGeoIp() != null) {
            builder.append(", GeoIp:[").append(getGeoIp()).append("]");
        }
        builder.append(", PollFrequency:").append(getPollFrequency());
        if (getPollFrequency() > 1) {
            builder.append(" minutes");
        } else {
            builder.append(" minute");
        }
        return builder.toString();
    }

    public void updateLastPollTime(long timestamp) {
        lastUpdate = timestamp;
        update();
    }

    public void updateLastObservationTime(long timestamp) {
        lastObservationTime = timestamp;
        update();
    }

    public void update() {
        DaoUtils.getGatewayDao().update(getGatewayTable());
    }

    @JsonGetter("trustHostType")
    private String getTrustHost() {
        return getTrustHostType().getText();
    }
}
