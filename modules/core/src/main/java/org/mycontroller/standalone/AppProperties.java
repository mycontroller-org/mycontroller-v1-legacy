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
package org.mycontroller.standalone;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.db.LoggerMySql;
import org.mycontroller.standalone.settings.BackupSettings;
import org.mycontroller.standalone.settings.EmailSettings;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.mycontroller.standalone.settings.MetricsGraphSettings;
import org.mycontroller.standalone.settings.MqttBrokerSettings;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.settings.MySensorsSettings;
import org.mycontroller.standalone.settings.PushbulletSettings;
import org.mycontroller.standalone.settings.SmsSettings;
import org.mycontroller.standalone.settings.TelegramBotSettings;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@ToString(exclude = { "dbPassword", "webSslKeystorePassword", "mqttSslKeystorePassword" })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppProperties {
    private static AppProperties _instance = new AppProperties();

    public static final String APPLICATION_NAME = "MyController.org";
    private static final String TEMPLATES_DIRECTORY = "templates" + File.separator;
    private static final String SCRIPTS_DIRECTORY = "scripts" + File.separator;
    public static final String CONDITIONS_SCRIPTS_DIRECTORY = "conditions" + File.separator;
    public static final String OPERATIONS_SCRIPTS_DIRECTORY = "operations" + File.separator;
    public static final String FIRMWARE_DATA_DIRECTORY = "firmwares" + File.separator;
    public static final String DASHBOARD_CONFIG_DIRECTORY = "dashboards" + File.separator;
    private static final String WEB_CONFIGURATIONS_DIR = "_configurations";
    private static final String HTML_HEADERS_FILE = "html-headers.json";

    public static final String GOOGLE_ANALYTICS_TID = "UA-127071169-1";

    private String tmpLocation;
    private String resourcesLocation;
    private String appDirectory;

    private DB_TYPE dbType;
    private Boolean dbBackupInclude;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private String webFileLocation;
    private boolean isWebHttpsEnabled = false;
    private int webHttpPort;
    private String webSslKeystoreFile;
    private String webSslKeystorePassword;
    private String webSslKeystoreType;
    private String webBindAddress;

    private boolean mqttBrokerEnabled = false;
    private String mqttSslKeystoreFile;
    private String mqttSslKeystorePassword;

    private String mqttBrokerPersistentStore;
    private String mcPersistentStoresLocation;
    private Boolean clearMessagesQueueOnStart;
    private Boolean clearSmartSleepMsgQueueOnStart;

    private Boolean mDNSserviceEnabled = false;

    private boolean googleAnalyticsEnabled = true;

    MyControllerSettings controllerSettings;
    EmailSettings emailSettings;
    MySensorsSettings mySensorsSettings;
    SmsSettings smsSettings;
    PushbulletSettings pushbulletSettings;
    TelegramBotSettings telegramBotSettings;
    LocationSettings locationSettings;
    MetricsGraphSettings metricsGraphSettings;
    MetricsDataRetentionSettings metricsDataRetentionSettings;
    BackupSettings backupSettings;
    MqttBrokerSettings mqttBrokerSettings;

    public enum DB_TYPE {
        H2DB_EMBEDDED("H2 database embedded"),
        H2DB("H2 database"),
        MYSQL("MySQL"),
        MARIADB("MariaDB"),
        POSTGRESQL("PostgreSQL");

        private final String name;

        private DB_TYPE(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static DB_TYPE get(int id) {
            for (DB_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static DB_TYPE fromString(String text) {
            if (text != null) {
                for (DB_TYPE type : DB_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum MC_LANGUAGE {
        CA_ES("català (ES)"),
        CS_CZ("čeština (CZ)"),
        DA_DK("Dansk (DK)"),
        DE_DE("Deutsch (DE)"),
        EN_US("English (US)"),
        ES_AR("Español (AR)"),
        ES_ES("Español (ES)"),
        FR_FR("Français (FR)"),
        HE_IL("עִברִית (IL)"),
        HU_HU("Magyar (HU)"),
        ID_ID("bahasa Indonesia (ID)"),
        IT_IT("italiano (IT)"),
        MK_MK("Македонски (MK)"),
        NL_NL("Nederlands (NL)"),
        NO_NO("norsk (NO)"),
        PL_PL("język polski (PL)"),
        PT_BR("Português (BR)"),
        RO_RO("Română (RO)"),
        RU_RU("Русский (RU)"),
        TA_IN("தமிழ் (IN)"),
        ZH_CN("中文 (CN)"),
        ZH_TW("中華民國國語 (TW)");

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
        MY_SENSORS("MySensors"),
        PHANT_IO("Sparkfun [phant.io]"),
        MY_CONTROLLER("MyController"),
        RF_LINK("RFLink"),
        PHILIPS_HUE("Philips Hue"),
        WUNDERGROUND("Weather Underground");

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
        RULE_DEFINITION("Rule definition"),
        TIMER("Timer"),
        SCRIPT("Script"),
        UID_TAG("UID tag"),
        FORWARD_PAYLOAD("Forward payload");
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

    public enum ALPHABETICAL_CASE {
        DEFAULT,
        LOWER,
        UPPER;
    }

    public static AppProperties getInstance() {
        return _instance;
    }

    public void loadProperties(Properties properties) {
        //Application Directory
        try {
            appDirectory = McUtils.getDirectoryLocation(FileUtils.getFile(McUtils.getDirectoryLocation("../"))
                    .getCanonicalPath());
        } catch (IOException ex) {
            appDirectory = McUtils.getDirectoryLocation("../");
            _logger.error("Unable to set application directory!", ex);
        }
        //Create tmp location
        tmpLocation = McUtils.getDirectoryLocation(getValue(properties, "mcc.tmp.location", "/tmp"));

        createDirectoryLocation(tmpLocation);

        //get/create resources location
        resourcesLocation = McUtils.getDirectoryLocation(
                getValue(properties, "mcc.resources.location", "../conf/resources"));

        //create resources location
        createDirectoryLocation(resourcesLocation);
        //create templates location
        createDirectoryLocation(getTemplatesLocation());
        //create scripts location
        createDirectoryLocation(getScriptsLocation());
        //create scripts, conditions directory
        createDirectoryLocation(getScriptsConditionsLocation());
        //create scripts, operations directory
        createDirectoryLocation(getScriptsOperationsLocation());

        //database location
        String dbType = getValue(properties, "mcc.db.type", "H2DB_EMBEDDED");
        this.dbType = DB_TYPE.valueOf(dbType.toUpperCase());
        this.dbBackupInclude = McUtils.getBoolean(getValue(properties, "mcc.db.backup.include", "False"));
        dbUrl = getValue(properties, "mcc.db.url", "jdbc:h2:file:../conf/mycontroller;MVCC=TRUE");
        if (this.dbType == DB_TYPE.MYSQL) {
            String mySqlLogger = "logger=" + LoggerMySql.class.getCanonicalName();
            if (dbUrl.indexOf('?') != -1) {
                dbUrl = dbUrl + "&" + mySqlLogger;
            } else {
                dbUrl = dbUrl + "?" + mySqlLogger;
            }
        }
        dbUsername = getValue(properties, "mcc.db.username", "mycontroller");
        dbPassword = getValue(properties, "mcc.db.password", "mycontroller");

        //mycontroller web location
        webFileLocation = McUtils.getDirectoryLocation(getValue(properties, "mcc.web.file.location", "../www"));

        //create WEB configurations directory
        createDirectoryLocation(getWebFileLocation() + WEB_CONFIGURATIONS_DIR);

        //update web details
        webHttpPort = Integer.valueOf(getValue(properties, "mcc.web.http.port", "8443"));
        isWebHttpsEnabled = Boolean.valueOf(getValue(properties, "mcc.web.enable.https", "true"));
        if (isWebHttpsEnabled) {
            webSslKeystoreFile = getValue(properties, "mcc.web.ssl.keystore.file", "../conf/keystore.jks");
            webSslKeystorePassword = getValue(properties, "mcc.web.ssl.keystore.password", "mycontroller");
            webSslKeystoreType = getValue(properties, "mcc.web.ssl.keystore.type", "JKS");
        }
        webBindAddress = getValue(properties, "mcc.web.bind.address", "0.0.0.0");

        //MQTT broker settings
        mqttBrokerEnabled = Boolean.valueOf(getValue(properties, "mcc.mqtt.broker.enabled", "true"));
        // for now do not support for SSL
        /*
        if (mqttBrokerEnabled) {
            mqttSslKeystoreFile = getValue(properties, "mcc.mqtt.broker.ssl.keystore.file", "../conf/keystore.jks");
            mqttSslKeystorePassword = getValue(properties, "mcc.mqtt.broker.ssl.keystore.password", "mycontroller");
        }
        */

        //MyController PersistentStore
        mcPersistentStoresLocation = McUtils.getDirectoryLocation(getValue(properties,
                "mcc.persistent.stores.location", "../conf/persistent_stores/"));
        createDirectoryLocation(mcPersistentStoresLocation);
        //MQTT Broker mqttBrokerPersistentStore
        mqttBrokerPersistentStore = mcPersistentStoresLocation + "moquette/moquette_store.mapdb";
        clearMessagesQueueOnStart = McUtils.getBoolean(getValue(properties,
                "mcc.clear.message.queue.on.start", "true"));
        clearSmartSleepMsgQueueOnStart = McUtils.getBoolean(getValue(properties,
                "mcc.clear.smart.sleep.msg.queue.on.start", "true"));
        //mDNS service, enabled or disabled
        mDNSserviceEnabled = McUtils.getBoolean(getValue(properties,
                "mcc.mdns.service.enable", "false"));

        googleAnalyticsEnabled = McUtils.getBoolean(getValue(properties,
                "mcc.collect.anonymous.data", "true"));
    }

    public void createDirectoryLocation(String directoryLocation) {
        File dir = FileUtils.getFile(directoryLocation);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                try {
                    _logger.info("Created directory location: [{}]", dir.getCanonicalPath());
                } catch (IOException ex) {
                    _logger.error("Failed to get CanonicalPath path of '{}'", directoryLocation);
                }
            } else {
                _logger.error("Unable to create directory location: {}", directoryLocation);
            }
        }
    }

    private String getValue(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        if (!key.contains("password")) {
            _logger.debug("Key:{}-->{}", key, value);
        }
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
        metricsGraphSettings = MetricsGraphSettings.get();
        metricsDataRetentionSettings = MetricsDataRetentionSettings.get();
        backupSettings = BackupSettings.get();
        pushbulletSettings = PushbulletSettings.get();
        telegramBotSettings = TelegramBotSettings.get();
        mqttBrokerSettings = MqttBrokerSettings.get();
    }

    private boolean is12HoursSelected() {
        if (controllerSettings.getTimeFormat().equalsIgnoreCase(MC_TIME_FORMAT.HOURS_12.getText())) {
            return true;
        }
        return false;
    }

    public String getDateFormatWithTimezone() {
        if (is12HoursSelected()) {
            return "MMM dd, yyyy hh:mm:ss a z";
        } else {
            return "MMM dd, yyyy HH:mm:ss z";
        }
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

    public String getTmpLocation() {
        return tmpLocation;
    }

    public DB_TYPE getDbType() {
        return dbType;
    }

    public Boolean includeDbBackup() {
        return dbBackupInclude;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
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

    public LocationSettings getLocationSettings() {
        return locationSettings;
    }

    public MetricsGraphSettings getMetricsGraphSettings() {
        return metricsGraphSettings;
    }

    public void setLocationSettings(LocationSettings locationSettings) {
        this.locationSettings = locationSettings;
    }

    public BackupSettings getBackupSettings() {
        return backupSettings;
    }

    public void setBackupSettings(BackupSettings backupSettings) {
        this.backupSettings = backupSettings;
    }

    public MetricsDataRetentionSettings getMetricsDataRetentionSettings() {
        return metricsDataRetentionSettings;
    }

    public void setMetricsDataRetentionSettings(MetricsDataRetentionSettings metricsDataRetentionSettings) {
        this.metricsDataRetentionSettings = metricsDataRetentionSettings;
    }

    public PushbulletSettings getPushbulletSettings() {
        return pushbulletSettings;
    }

    public void setPushbulletSettings(PushbulletSettings pushbulletSettings) {
        this.pushbulletSettings = pushbulletSettings;
    }

    public TelegramBotSettings getTelegramBotSettings() {
        return telegramBotSettings;
    }

    public void setTelegramBotSettings(TelegramBotSettings telegramBotSettings) {
        this.telegramBotSettings = telegramBotSettings;
    }

    public String getResourcesLocation() {
        return resourcesLocation;
    }

    public String getScriptsLocation() {
        return getResourcesLocation() + SCRIPTS_DIRECTORY;
    }

    public String getTemplatesLocation() {
        return getResourcesLocation() + TEMPLATES_DIRECTORY;
    }

    public String getScriptsConditionsLocation() {
        return getScriptsLocation() + CONDITIONS_SCRIPTS_DIRECTORY;
    }

    public String getScriptsOperationsLocation() {
        return getScriptsLocation() + OPERATIONS_SCRIPTS_DIRECTORY;
    }

    public String getHtmlHeadersFile() {
        return getResourcesLocation() + HTML_HEADERS_FILE;
    }

    public String getFirmwaresDataDirectory() {
        return getResourcesLocation() + FIRMWARE_DATA_DIRECTORY;
    }

    public String getDashboardConfigDirectory() {
        return getResourcesLocation() + DASHBOARD_CONFIG_DIRECTORY;
    }

    public String getWebConfigurationsLocation() {
        return McUtils.getDirectoryLocation(getWebFileLocation() + WEB_CONFIGURATIONS_DIR);
    }

    public String getAppDirectory() {
        return appDirectory;
    }

    public MqttBrokerSettings getMqttBrokerSettings() {
        return mqttBrokerSettings;
    }

    public Boolean getClearMessagesQueueOnStart() {
        return clearMessagesQueueOnStart;
    }

    public Boolean getClearSmartSleppMsgQueueOnStart() {
        return clearSmartSleepMsgQueueOnStart;
    }

    public String getMcPersistentStoresLocation() {
        return mcPersistentStoresLocation;
    }

    public String getMqttClientPersistentStoresLocation() {
        return getMcPersistentStoresLocation() + "/mqtt_clients/";
    }

    public boolean isMDNSserviceEnabled() {
        return mDNSserviceEnabled;
    }

    public boolean isMqttBrokerEnabled() {
        return mqttBrokerEnabled;
    }

    public String getMqttSslKeystoreFile() {
        return mqttSslKeystoreFile;
    }

    public String getMqttSslKeystorePassword() {
        return mqttSslKeystorePassword;
    }

    public boolean isGoogleAnalyticsEnabled() {
        return googleAnalyticsEnabled;
    }
}
