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
package org.mycontroller.standalone.gateway.model;

import java.util.HashMap;

import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayUtils.SERIAL_PORT_DRIVER;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GatewaySerial extends Gateway {
    public static final String KEY_DRIVER = "dr";
    public static final String KEY_PORT_NAME = "pn";
    public static final String KEY_BAUD_RATE = "br";
    public static final String KEY_RETRY_FREQUENCY = "rf";
    public static final String KEY_RUNNING_DRIVER = "rdr";

    private SERIAL_PORT_DRIVER driver;
    private String portName;
    private Integer baudRate;
    private Long retryFrequency;
    private SERIAL_PORT_DRIVER runningDriver;

    public GatewaySerial() {

    }

    public GatewaySerial(GatewayTable gatewayTable) {
        updateGateway(gatewayTable);
    }

    @Override
    @JsonIgnore
    public GatewayTable getGatewayTable() {
        GatewayTable gatewayTable = super.getGatewayTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_DRIVER, driver.getText());
        properties.put(KEY_PORT_NAME, portName);
        properties.put(KEY_BAUD_RATE, baudRate);
        properties.put(KEY_RETRY_FREQUENCY, retryFrequency);
        if (runningDriver != null) {
            properties.put(KEY_RUNNING_DRIVER, runningDriver.getText());
        }
        gatewayTable.setProperties(properties);
        return gatewayTable;
    }

    @Override
    @JsonIgnore
    public void updateGateway(GatewayTable gatewayTable) {
        super.updateGateway(gatewayTable);
        driver = SERIAL_PORT_DRIVER.fromString((String) gatewayTable.getProperties().get(KEY_DRIVER));
        portName = (String) gatewayTable.getProperties().get(KEY_PORT_NAME);
        baudRate = (Integer) gatewayTable.getProperties().get(KEY_BAUD_RATE);
        retryFrequency = (Long) gatewayTable.getProperties().get(KEY_RETRY_FREQUENCY);
        runningDriver = SERIAL_PORT_DRIVER.fromString((String) gatewayTable.getProperties().get(KEY_RUNNING_DRIVER));
    }

    @JsonGetter("driver")
    private String getDriverString() {
        return driver.getText();
    }

    @JsonGetter("runningDriver")
    private String getRunningDriverString() {
        return runningDriver != null ? runningDriver.getText() : null;
    }
}
