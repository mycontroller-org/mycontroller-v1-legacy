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
package org.mycontroller.standalone.settings;

import org.mycontroller.standalone.utils.McUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MqttBrokerSettings {

    public static final String KEY_MQTT_BROKER = "mqttBroker";
    public static final String SKEY_ENABLED = "enabled";
    public static final String SKEY_BIND_ADDRESS = "bindAddress";
    public static final String SKEY_HTTP_PORT = "httpPort";
    public static final String SKEY_WEBSOCKET_PORT = "websocketPort";
    public static final String SKEY_ALLOW_ANONYMOUS = "allowAnonymous";

    private Boolean enabled;
    private String bindAddress;
    private Integer httpPort;
    private Integer websocketPort;
    private Boolean allowAnonymous;

    public Boolean getEnabled() {
        return enabled == null ? false : enabled;
    }

    public Boolean getAllowAnonymous() {
        return allowAnonymous == null ? false : allowAnonymous;
    }

    public static MqttBrokerSettings get() {
        return MqttBrokerSettings.builder()
                .enabled(McUtils.getBoolean(getValue(SKEY_ENABLED)))
                .bindAddress(getValue(SKEY_BIND_ADDRESS))
                .httpPort(McUtils.getInteger(getValue(SKEY_HTTP_PORT)))
                .websocketPort(McUtils.getInteger(getValue(SKEY_WEBSOCKET_PORT)))
                .allowAnonymous(McUtils.getBoolean(getValue(SKEY_ALLOW_ANONYMOUS)))
                .build();
    }

    public void save() {
        if (enabled != null) {
            updateValue(SKEY_ENABLED, enabled);
        }
        if (bindAddress != null) {
            updateValue(SKEY_BIND_ADDRESS, bindAddress.trim());
        }
        if (httpPort != null) {
            updateValue(SKEY_HTTP_PORT, httpPort);
        }
        if (websocketPort != null) {
            updateValue(SKEY_WEBSOCKET_PORT, websocketPort);
        }
        if (allowAnonymous != null) {
            updateValue(SKEY_ALLOW_ANONYMOUS, allowAnonymous);
        }
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_MQTT_BROKER, subKey);
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_MQTT_BROKER, subKey, value);
    }
}
