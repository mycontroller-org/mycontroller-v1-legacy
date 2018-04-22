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
package org.mycontroller.standalone.settings;

import org.mycontroller.standalone.AppProperties;
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
@ToString(exclude = { "sslKeystorePassword" })
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MqttBrokerSettings {

    public static final String KEY_MQTT_BROKER = "mqttBroker";
    public static final String SKEY_ENABLED = "enabled";
    public static final String SKEY_SSL_ENABLED = "sslEnabled";
    public static final String SKEY_BIND_ADDRESS = "bindAddress";
    public static final String SKEY_MQTT_PORT = "mqttPort";
    public static final String SKEY_MQTTS_PORT = "mqttsPort";
    public static final String SKEY_WEBSOCKET_PORT = "websocketPort";
    public static final String SKEY_ALLOW_ANONYMOUS = "allowAnonymous";

    private Boolean enabled;
    private Boolean sslEnabled;
    private String bindAddress;
    private Integer mqttPort;
    private Integer mqttsPort;
    private Integer websocketPort;
    private Boolean allowAnonymous;

    private Boolean enabledOnBackend;
    private String sslKeystoreFile;
    private String sslKeystorePassword;

    public Boolean getEnabled() {
        return enabled == null ? false : enabled;
    }

    public Boolean getAllowAnonymous() {
        return allowAnonymous == null ? false : allowAnonymous;
    }

    public static MqttBrokerSettings get() {
        return MqttBrokerSettings
                .builder()
                .enabled(McUtils.getBoolean(getValue(SKEY_ENABLED, "false"))
                        && AppProperties.getInstance().isMqttBrokerEnabled())
                .sslEnabled(McUtils.getBoolean(getValue(SKEY_SSL_ENABLED, "false")))
                .bindAddress(getValue(SKEY_BIND_ADDRESS, "0.0.0.0"))
                .mqttPort(McUtils.getInteger(getValue(SKEY_MQTT_PORT, "1883")))
                .mqttsPort(McUtils.getInteger(getValue(SKEY_MQTTS_PORT, "8883")))
                .websocketPort(McUtils.getInteger(getValue(SKEY_WEBSOCKET_PORT, "8080")))
                .allowAnonymous(McUtils.getBoolean(getValue(SKEY_ALLOW_ANONYMOUS, "false")))
                .enabledOnBackend(AppProperties.getInstance().isMqttBrokerEnabled())
                .sslKeystoreFile(AppProperties.getInstance().getMqttSslKeystoreFile())
                .sslKeystorePassword(AppProperties.getInstance().getMqttSslKeystorePassword())
                .build();
    }

    public void save() {
        if (enabled != null) {
            updateValue(SKEY_ENABLED, enabled);
        }
        if (sslEnabled != null) {
            updateValue(SKEY_SSL_ENABLED, sslEnabled);
        }
        if (bindAddress != null) {
            updateValue(SKEY_BIND_ADDRESS, bindAddress.trim());
        }
        if (mqttPort != null) {
            updateValue(SKEY_MQTT_PORT, mqttPort);
        }
        if (mqttsPort != null) {
            updateValue(SKEY_MQTTS_PORT, mqttsPort);
        }
        if (websocketPort != null) {
            updateValue(SKEY_WEBSOCKET_PORT, websocketPort);
        }
        if (allowAnonymous != null) {
            updateValue(SKEY_ALLOW_ANONYMOUS, allowAnonymous);
        }
    }

    private static String getValue(String subKey, String defaultValue) {
        return SettingsUtils.getValue(KEY_MQTT_BROKER, subKey) != null ?
                SettingsUtils.getValue(KEY_MQTT_BROKER, subKey) : defaultValue;
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_MQTT_BROKER, subKey, value);
    }
}
