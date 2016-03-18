/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.mqttbroker;

import java.util.Properties;

import org.h2.store.fs.FileUtils;
import org.mycontroller.standalone.ObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.server.config.IConfig;
import static io.moquette.BrokerConstants.*;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class BrokerConfiguration implements IConfig {
    private static final Logger _logger = LoggerFactory.getLogger(BrokerConfiguration.class.getName());

    private final Properties m_properties = new Properties();

    public BrokerConfiguration() {
        createDefaultLocations();
        loadProperties();
    }

    private void createDefaultLocations() {
        FileUtils
                .createDirectory(FileUtils.getParent(ObjectManager.getAppProperties().getMqttBrokerPersistentStore()));
    }

    private void loadProperties() {
        m_properties.put(HOST_PROPERTY_NAME, ObjectManager.getAppProperties().getMqttBrokerBindAddress());
        m_properties.put(PORT_PROPERTY_NAME, String.valueOf(ObjectManager.getAppProperties().getMqttBrokerPort()));
        m_properties.put(WEB_SOCKET_PORT_PROPERTY_NAME,
                String.valueOf(ObjectManager.getAppProperties().getMqttBrokerWebsocketPort()));

        m_properties.put(PASSWORD_FILE_PROPERTY_NAME, "");
        m_properties.put(PERSISTENT_STORE_PROPERTY_NAME,
                ObjectManager.getAppProperties().getMqttBrokerPersistentStore());
        m_properties.put(ALLOW_ANONYMOUS_PROPERTY_NAME, true);

        m_properties.put(AUTHENTICATOR_CLASS_NAME, MqttAuthenticatorImpl.class.getName());
        m_properties.put(AUTHORIZATOR_CLASS_NAME, MqttAuthorizatorImpl.class.getName());
        _logger.debug("Properties:[{}]", m_properties);
    }

    @Override
    public void setProperty(String name, String value) {
        m_properties.setProperty(name, value);
    }

    @Override
    public String getProperty(String name) {
        return m_properties.getProperty(name);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return m_properties.getProperty(name, defaultValue);
    }

}
