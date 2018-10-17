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

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Data
@ToString
public abstract class GatewayConfig {
    public static final String KEY_TX_DELAY = "txDelay";
    public static final String KEY_ACK_ENABLED = "ackEnabled";
    public static final String KEY_STREAM_ACK_ENABLED = "streamAckEnabled";
    public static final String KEY_FAILED_RETRY_COUNT = "failedRetryCount";
    public static final String KEY_ACK_WAIT_TIME = "ackWaitTime";
    public static final String KEY_RECONNECT_DELAY = "reconnectDelay";

    private Integer id;
    private Boolean enabled;
    private Boolean ackEnabled;
    private Boolean streamAckEnabled; // stream ack enabled
    private Integer failedRetryCount;
    private Long ackWaitTime;
    private String name;
    private GATEWAY_TYPE type;
    private NETWORK_TYPE networkType;
    private Long timestamp;
    private STATE state = STATE.UNAVAILABLE;
    private String statusMessage;
    private Long statusSince;
    private Integer reconnectDelay;
    private Long txDelay;

    public abstract String getConnectionDetails();

    @JsonIgnore
    public GatewayTable getGatewayTable() {
        GatewayTable gatewayTable = GatewayTable.builder()
                .id(getId())
                .enabled(getEnabled())
                .name(getName())
                .type(getType())
                .networkType(getNetworkType())
                .timestamp(getTimestamp())
                .state(getState())
                .statusMessage(getStatusMessage())
                .statusSince(getStatusSince())
                .properties(new HashMap<String, Object>()).build();
        gatewayTable.getProperties().put(KEY_TX_DELAY, txDelay);
        gatewayTable.getProperties().put(KEY_ACK_ENABLED, ackEnabled);
        gatewayTable.getProperties().put(KEY_STREAM_ACK_ENABLED, streamAckEnabled);
        gatewayTable.getProperties().put(KEY_FAILED_RETRY_COUNT, failedRetryCount);
        gatewayTable.getProperties().put(KEY_ACK_WAIT_TIME, ackWaitTime);
        gatewayTable.getProperties().put(KEY_RECONNECT_DELAY, reconnectDelay);
        return gatewayTable;
    }

    @JsonIgnore
    public void updateGateway(GatewayTable gatewayTable) {
        id = gatewayTable.getId();
        enabled = gatewayTable.getEnabled();
        name = gatewayTable.getName();
        type = gatewayTable.getType();
        networkType = gatewayTable.getNetworkType();
        timestamp = gatewayTable.getTimestamp();
        state = gatewayTable.getState();
        statusMessage = gatewayTable.getStatusMessage();
        statusSince = gatewayTable.getStatusSince();
        txDelay = (Long) gatewayTable.getProperty(KEY_TX_DELAY, 0L);
        ackEnabled = (Boolean) gatewayTable.getProperty(KEY_ACK_ENABLED, false);
        streamAckEnabled = (Boolean) gatewayTable.getProperty(KEY_STREAM_ACK_ENABLED, false);
        failedRetryCount = (Integer) gatewayTable.getProperty(KEY_FAILED_RETRY_COUNT, 3);
        ackWaitTime = (Long) gatewayTable.getProperty(KEY_ACK_WAIT_TIME, 500L);
        reconnectDelay = (Integer) gatewayTable.getProperty(KEY_RECONNECT_DELAY, 120);
    }

    public void setStatus(STATE state, String statusMessage) {
        GatewayTable gw = DaoUtils.getGatewayDao().getById(getId());
        if (gw.getState() != state) {
            gw.setState(state);
            gw.setStatusSince(System.currentTimeMillis());
        }
        gw.setStatusMessage(statusMessage);
        DaoUtils.getGatewayDao().update(gw);
        updateGateway(gw);
    }

    //For json
    @JsonGetter("type")
    private String getTypeString() {
        return type.getText();
    }

    @JsonGetter("networkType")
    private String getNetworkTypeString() {
        return networkType.getText();
    }

    @JsonGetter("state")
    private String getStateString() {
        return state.getText();
    }
}
