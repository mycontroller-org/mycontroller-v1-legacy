/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class AppProperties {
    public static final String APPLICATION_NAME = "Mycontroller.org";
    private String serialPortName;
    private String serialPortDriver;
    private int serialPortBaudRate;
    private String h2DbLocation;
    private String wwwFileLocation;
    private boolean isHttpsEnabled = false;
    private int httpPort;
    private String sslKeystoreFile;
    private String sslKeystorePassword;
    private String sslKeystoreType;

    public enum SERIAL_PORT_DRIVER {
        AUTO,
        PI4J,
        JSSC,
        JSERIALCOMM;
    }

    public AppProperties() {
    }

    public AppProperties(Properties properties) {
        this.loadProperties(properties);
    }

    public void loadProperties(Properties properties) {
        this.serialPortName = (properties.getProperty("mcc.serialport.name").trim());
        this.serialPortDriver = (properties.getProperty("mcc.serialport.driver.type").trim());
        this.serialPortBaudRate = Integer.valueOf(properties.getProperty("mcc.serialport.baud.rate").trim());
        this.h2DbLocation = properties.getProperty("mcc.h2db.location").trim();
        this.wwwFileLocation = properties.getProperty("www.file.location").trim();
        this.httpPort = Integer.valueOf(properties.getProperty("http.port").trim());
        if (properties.getProperty("enable.https") != null) {
            if (Boolean.valueOf(properties.getProperty("enable.https").trim())) {
                this.isHttpsEnabled = true;
                this.sslKeystoreFile = properties.getProperty("ssl.keystore.file").trim();
                this.sslKeystorePassword = properties.getProperty("ssl.keystore.password").trim();
                this.sslKeystoreType = properties.getProperty("ssl.keystore.type").trim();
            }
        }
    }

    public String getSerialPortName() {
        return serialPortName;
    }

    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    public String getSerialPortDriver() {
        return serialPortDriver;
    }

    public void setSerialPortDriver(String serialPortDriver) {
        this.serialPortDriver = serialPortDriver;
    }

    public int getSerialPortBaudRate() {
        return serialPortBaudRate;
    }

    public void setSerialPortBaudRate(int serialPortBaudRate) {
        this.serialPortBaudRate = serialPortBaudRate;
    }

    public int getNodeId() {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.AUTO_NODE_ID);
        return Integer.valueOf(settings.getValue());
    }

    //TODO: Get this one from database
    public String getMetricType() {
        return "M";
    }

    public int getNextNodeId() throws NodeIdException {
        int nodeId = this.getNodeId();
        nodeId++;
        boolean isIdAvailable = false;
        int nodeIdRef = nodeId;
        for (; nodeId < 255; nodeId++) {
            if (DaoUtils.getNodeDao().get(nodeId) == null) {
                isIdAvailable = true;
                break;
            }
        }
        if (!isIdAvailable) {
            for (nodeId = 1; nodeId <= nodeIdRef; nodeIdRef++) {
                if (DaoUtils.getNodeDao().get(nodeId) == null) {
                    isIdAvailable = true;
                    break;
                }
            }
        }

        if (isIdAvailable) {
            Settings settings = DaoUtils.getSettingsDao().get(Settings.AUTO_NODE_ID);
            settings.setValue(String.valueOf(nodeId));
            DaoUtils.getSettingsDao().update(settings);
            return nodeId;
        } else {
            throw new NodeIdException("Reached Node Id 254, that is the maximum limit.");
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

    public String getH2DbLocation() {
        return h2DbLocation;
    }

    public String getWwwFileLocation() {
        return wwwFileLocation;
    }

    public boolean isHttpsEnabled() {
        return isHttpsEnabled;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getSslKeystoreFile() {
        return sslKeystoreFile;
    }

    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }

    public String getSslKeystoreType() {
        return sslKeystoreType;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
