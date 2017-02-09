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
package org.mycontroller.standalone.mqttbroker;

import static io.moquette.BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME;
import static io.moquette.BrokerConstants.AUTHENTICATOR_CLASS_NAME;
import static io.moquette.BrokerConstants.AUTHORIZATOR_CLASS_NAME;
import static io.moquette.BrokerConstants.HOST_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PASSWORD_FILE_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PORT_PROPERTY_NAME;
import static io.moquette.BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME;

import java.util.Properties;

import org.h2.store.fs.FileUtils;
import org.mycontroller.standalone.AppProperties;

import io.moquette.server.config.IConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class BrokerConfiguration implements IConfig {

    private final Properties m_properties = new Properties();

    public BrokerConfiguration() {
        createDefaultLocations();
        loadProperties();
    }

    private void createDefaultLocations() {
        FileUtils.createDirectory(FileUtils.getParent(AppProperties.getInstance().
                getMqttBrokerPersistentStore()));
    }

    private void loadProperties() {
        m_properties.put(HOST_PROPERTY_NAME, AppProperties.getInstance().getMqttBrokerSettings().getBindAddress());
        m_properties.put(PORT_PROPERTY_NAME,
                String.valueOf(AppProperties.getInstance().getMqttBrokerSettings().getHttpPort()));
        m_properties.put(WEB_SOCKET_PORT_PROPERTY_NAME,
                String.valueOf(AppProperties.getInstance().getMqttBrokerSettings().getWebsocketPort()));

        m_properties.put(PASSWORD_FILE_PROPERTY_NAME, "");
        m_properties.put(PERSISTENT_STORE_PROPERTY_NAME,
                AppProperties.getInstance().getMqttBrokerPersistentStore());
        //Enable authentication and role based tpoics actions
        if (AppProperties.getInstance().getMqttBrokerSettings().getAllowAnonymous()) {
            m_properties.put(ALLOW_ANONYMOUS_PROPERTY_NAME, "true");
        } else {
            m_properties.put(ALLOW_ANONYMOUS_PROPERTY_NAME, "false");
            m_properties.put(AUTHENTICATOR_CLASS_NAME, MqttAuthenticatorImpl.class.getName());
            m_properties.put(AUTHORIZATOR_CLASS_NAME, MqttAuthorizatorImpl.class.getName());
        }

        _logger.debug("Properties:[{}]", m_properties);
    }

    @Override
    public void setProperty(String name, String value) {
        _logger.debug("Set property [name:{}, value:{}]", name, value);
        m_properties.setProperty(name, value);
    }

    @Override
    public String getProperty(String name) {
        _logger.debug("Get property [name:{}, value:{}]", name, m_properties.getProperty(name));
        return m_properties.getProperty(name);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        _logger.debug("Get property with default value [name:{}, value:{}, defaultValue:{}]", name,
                m_properties.getProperty(name), defaultValue);
        return m_properties.getProperty(name, defaultValue);
    }

}
