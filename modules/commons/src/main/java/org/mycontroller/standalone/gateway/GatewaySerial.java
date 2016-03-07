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
package org.mycontroller.standalone.gateway;

import org.mycontroller.standalone.MycUtils;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewayUtils.SERIAL_PORT_DRIVER;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class GatewaySerial extends GatewayBase {

    public GatewaySerial() {
        super(new Gateway());
    }

    public GatewaySerial(Gateway gateway) {
        super(gateway);
    }

    public SERIAL_PORT_DRIVER getDriver() {
        return SERIAL_PORT_DRIVER.fromString(super.getVariable1());
    }

    public void setDriver(SERIAL_PORT_DRIVER driver) {
        super.setVariable1(driver.getText());
    }

    public String getPortName() {
        return super.getVariable2();
    }

    public void setPortName(String portName) {
        super.setVariable2(portName);
    }

    public Integer getBaudRate() {
        return MycUtils.getInteger(super.getVariable3());
    }

    public void setBaudRate(Integer baudRate) {
        super.setVariable3(String.valueOf(baudRate));
    }

    public Integer getRetryFrequency() {
        return MycUtils.getInteger(super.getVariable4());
    }

    public void setRetryFrequency(Integer retryFrequency) {
        super.setVariable4(String.valueOf(retryFrequency));
    }

    public SERIAL_PORT_DRIVER getRunningDriver() {
        if (super.getVariable5() != null) {
            return SERIAL_PORT_DRIVER.fromString(super.getVariable5());
        }
        return null;
    }

    public void setRunningDriver(SERIAL_PORT_DRIVER runningDriver) {
        super.setVariable5(runningDriver.getText());
    }

}
