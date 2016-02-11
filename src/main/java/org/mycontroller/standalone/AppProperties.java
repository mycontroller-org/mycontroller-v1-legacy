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
package org.mycontroller.standalone;

import java.util.Properties;

import org.mycontroller.standalone.settings.EmailSettings;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.settings.MetricsSettings;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.settings.MySensorsSettings;
import org.mycontroller.standalone.settings.SmsSettings;
import org.mycontroller.standalone.settings.UnitsSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@ToString(includeFieldNames = true)
public class AppProperties {
    private static final Logger _logger = LoggerFactory.getLogger(AppProperties.class.getName());

    public static final String APPLICATION_NAME = "MyController.org";
    public static final String EMAIL_TEMPLATE_ALARM = "../conf/templates/emailTemplateAlarm.html";

    private String dbH2DbLocation;
    private String webFileLocation;
    private boolean isWebHttpsEnabled = false;
    private int webHttpPort;
    private String webSslKeystoreFile;
    private String webSslKeystorePassword;
    private String webSslKeystoreType;
    private String webBindAddress;

    private boolean mqttBrokerEnable;
    private String mqttBrokerBindAddress;
    private Integer mqttBrokerPort;
    private Integer mqttBrokerWebsocketPort;
    private String mqttBrokerPersistentStore;

    MyControllerSettings controllerSettings;
    EmailSettings emailSettings;
    MySensorsSettings mySensorsSettings;
    SmsSettings smsSettings;
    UnitsSettings unitsSettings;
    LocationSettings locationSettings;
    MetricsSettings metricsSettings;

    public enum MC_LANGUAGE {
        EN_US("English (US)"),
        TA_IN("தமிழ் (IN)"),
        DE_DE("Deutsch (DE)"),
        RU_RU("русский (RU)"),
        ES_AR("Español (ES)");

        private final String name;

        private MC_LANGUAGE(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static MC_LANGUAGE get(int id) {
            for (MC_LANGUAGE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static MC_LANGUAGE fromString(String text) {
            if (text != null) {
                for (MC_LANGUAGE type : MC_LANGUAGE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum MC_TIME_FORMAT {
        HOURS_12("12 hours"),
        HOURS_24("24 hours");

        private final String name;

        private MC_TIME_FORMAT(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static MC_TIME_FORMAT get(int id) {
            for (MC_TIME_FORMAT type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static MC_TIME_FORMAT fromString(String text) {
            if (text != null) {
                for (MC_TIME_FORMAT type : MC_TIME_FORMAT.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum STATE {
        UP("Up"),
        DOWN("Down"),
        UNAVAILABLE("Unavailable"),
        ON("On"),
        OFF("Off");

        private final String type;

        private STATE(String type) {
            this.type = type;
        }

        public String getText() {
            return this.type;
        }

        public static STATE get(int id) {
            for (STATE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static STATE fromString(String text) {
            if (text != null) {
                for (STATE type : STATE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum NETWORK_TYPE {
        MY_SENSORS("MySensors");

        private final String type;

        private NETWORK_TYPE(String type) {
            this.type = type;
        }

        public String getText() {
            return this.type;
        }

        public static NETWORK_TYPE get(int id) {
            for (NETWORK_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static NETWORK_TYPE fromString(String text) {
            if (text != null) {
                for (NETWORK_TYPE type : NETWORK_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum RESOURCE_TYPE {
        GATEWAY("Gateway"),
        NODE("Node"),
        SENSOR("Sensor"),
        SENSOR_VARIABLE("Sensor variable"),
        RESOURCES_GROUP("Resources group"),
        ALARM_DEFINITION("Alarm definition"),
        TIMER("Timer");
        public static RESOURCE_TYPE get(int id) {
            for (RESOURCE_TYPE trigger_type : values()) {
                if (trigger_type.ordinal() == id) {
                    return trigger_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private RESOURCE_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static RESOURCE_TYPE fromString(String text) {
            if (text != null) {
                for (RESOURCE_TYPE type : RESOURCE_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum UNIT_CONFIG {
        METRIC("Metric"), IMPERIAL("Imperial");
        public static UNIT_CONFIG get(int id) {
            for (UNIT_CONFIG type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private UNIT_CONFIG(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static UNIT_CONFIG fromString(String text) {
            if (text != null) {
                for (UNIT_CONFIG type : UNIT_CONFIG.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum SMS_VENDOR {
        PLIVO("Plivo"),
        TWILIO("Twilio");

        private final String name;

        private SMS_VENDOR(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static SMS_VENDOR get(int id) {
            for (SMS_VENDOR type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static SMS_VENDOR fromString(String text) {
            if (text != null) {
                for (SMS_VENDOR type : SMS_VENDOR.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public AppProperties() {
    }

    public AppProperties(Properties properties) {
        this.loadProperties(properties);
    }

    public void loadProperties(Properties properties) {
        this.dbH2DbLocation = getValue(properties, "mcc.db.h2db.location");
        this.webFileLocation = getValue(properties, "mcc.web.file.location");
        if (!this.webFileLocation.endsWith("/")) {
            this.webFileLocation = this.webFileLocation + "/";
        }
        this.webHttpPort = Integer.valueOf(getValue(properties, "mcc.web.http.port"));
        if (getValue(properties, "mcc.web.enable.https") != null) {
            if (Boolean.valueOf(getValue(properties, "mcc.web.enable.https"))) {
                this.isWebHttpsEnabled = true;
                this.webSslKeystoreFile = getValue(properties, "mcc.web.ssl.keystore.file");
                this.webSslKeystorePassword = getValue(properties, "mcc.web.ssl.keystore.password");
                this.webSslKeystoreType = getValue(properties, "mcc.web.ssl.keystore.type");
            }
        }
        this.webBindAddress = getValue(properties, "mcc.web.bind.address");

        //MQTT Broker
        this.mqttBrokerEnable = Boolean.valueOf(getValue(properties, "mcc.mqtt.broker.enable"));
        if (this.mqttBrokerEnable) {
            this.mqttBrokerBindAddress = getValue(properties, "mcc.mqtt.broker.bind.address");
            this.mqttBrokerPort = Integer.valueOf(getValue(properties, "mcc.mqtt.broker.port"));
            this.mqttBrokerWebsocketPort = Integer.valueOf(getValue(properties, "mcc.mqtt.broker.websocket.port"));
            this.mqttBrokerPersistentStore = getValue(properties, "mcc.mqtt.broker.persistent.store");
        }
    }

    private String getValue(Properties properties, String key) {
        String value = properties.getProperty(key);
        _logger.debug("Key:{}-->{}", key, value);
        if (value != null) {
            return value.trim();
        } else {
            return null;
        }
    }

    public MC_LANGUAGE getLanguage() {
        return MC_LANGUAGE.fromString(controllerSettings.getLanguage());
    }

    public void loadPropertiesFromDb() {
        locationSettings = LocationSettings.get();
        controllerSettings = MyControllerSettings.get();
        mySensorsSettings = MySensorsSettings.get();
        emailSettings = EmailSettings.get();
        smsSettings = SmsSettings.get();
        unitsSettings = UnitsSettings.get();
        metricsSettings = MetricsSettings.get();
    }

    private boolean is12HoursSelected() {
        if (controllerSettings.getTimeFormat().equalsIgnoreCase(MC_TIME_FORMAT.HOURS_12.getText())) {
            return true;
        }
        return false;
    }

    public String getDateFormat() {
        if (is12HoursSelected()) {
            return "MMM dd, yyyy hh:mm:ss a";
        } else {
            return "MMM dd, yyyy HH:mm:ss";
        }
    }

    public String getDateFormatWithoutSeconds() {
        if (is12HoursSelected()) {
            return "MMM dd, yyyy hh:mm a";
        } else {
            return "MMM dd, yyyy HH:mm";
        }
    }

    public String getTimeFormatWithoutSeconds() {
        if (is12HoursSelected()) {
            return "hh:mm a";
        } else {
            return "HH:mm";
        }
    }

    public String getTimeFormat() {
        if (is12HoursSelected()) {
            return "hh:mm:ss a";
        } else {
            return "HH:mm:ss";
        }
    }

    public static String getOsName() {
        return System.getProperties().getProperty("os.name");
    }

    public static String getOsArch() {
        return System.getProperties().getProperty("os.arch");
    }

    public static String getOsVersion() {
        return System.getProperties().getProperty("os.version");
    }

    public String getDbH2DbLocation() {
        return dbH2DbLocation;
    }

    public String getWebFileLocation() {
        return webFileLocation;
    }

    public boolean isWebHttpsEnabled() {
        return isWebHttpsEnabled;
    }

    public int getWebHttpPort() {
        return webHttpPort;
    }

    public String getWebSslKeystoreFile() {
        return webSslKeystoreFile;
    }

    public String getWebSslKeystorePassword() {
        return webSslKeystorePassword;
    }

    public String getWebSslKeystoreType() {
        return webSslKeystoreType;
    }

    public String getWebBindAddress() {
        return webBindAddress;
    }

    public boolean isMqttBrokerEnabled() {
        return mqttBrokerEnable;
    }

    public String getMqttBrokerBindAddress() {
        return mqttBrokerBindAddress;
    }

    public Integer getMqttBrokerPort() {
        return mqttBrokerPort;
    }

    public Integer getMqttBrokerWebsocketPort() {
        return mqttBrokerWebsocketPort;
    }

    public String getMqttBrokerPersistentStore() {
        return mqttBrokerPersistentStore;
    }

    public MyControllerSettings getControllerSettings() {
        return controllerSettings;
    }

    public EmailSettings getEmailSettings() {
        return emailSettings;
    }

    public MySensorsSettings getMySensorsSettings() {
        return mySensorsSettings;
    }

    public SmsSettings getSmsSettings() {
        return smsSettings;
    }

    public UnitsSettings getUnitsSettings() {
        return unitsSettings;
    }

    public LocationSettings getLocationSettings() {
        return locationSettings;
    }

    public MetricsSettings getMetricsSettings() {
        return metricsSettings;
    }

    public void setMetricsSettings(MetricsSettings metricsSettings) {
        this.metricsSettings = metricsSettings;
    }

    public void setLocationSettings(LocationSettings locationSettings) {
        this.locationSettings = locationSettings;
    }

}
