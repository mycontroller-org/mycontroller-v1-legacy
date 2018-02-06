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
package org.mycontroller.standalone.externalserver.driver;

import java.util.regex.Pattern;

import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfig;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public abstract class DriverAbstract implements IExternalServerDriver {
    private ExternalServerConfig _config = null;

    DriverAbstract(ExternalServerConfig _config) {
        this._config = _config;
    }

    public String getVariableKey(SensorVariable sensorVariable, String keyFormat) {
        if (keyFormat == null) {
            keyFormat = ExternalServerConfig.DEFAULT_KEY_FORMAT;
        }
        keyFormat = keyFormat.replaceAll(Pattern.quote("$gatewayName"),
                getFormatedKey(sensorVariable.getSensor().getNode().getGatewayTable().getName()));
        keyFormat = keyFormat.replaceAll(Pattern.quote("$networkType"),
                getFormatedKey(sensorVariable.getSensor().getNode().getGatewayTable().getNetworkType().getText()));
        //fill with nodeEui, if there is no nodeName
        if (sensorVariable.getSensor().getNode().getName() != null) {
            keyFormat = keyFormat.replaceAll(Pattern.quote("$nodeName"),
                    getFormatedKey(sensorVariable.getSensor().getNode().getName()));
        } else {
            keyFormat = keyFormat.replaceAll(Pattern.quote("$nodeName"),
                    getFormatedKey(sensorVariable.getSensor().getNode().getEui()));
        }
        keyFormat = keyFormat.replaceAll(Pattern.quote("$nodeEui"),
                getFormatedKey(sensorVariable.getSensor().getNode().getEui()));
        //fill with sensorId, if there is no sensorName
        if (sensorVariable.getSensor().getName() != null) {
            keyFormat = keyFormat.replaceAll(Pattern.quote("$sensorName"),
                    getFormatedKey(sensorVariable.getSensor().getName()));
        } else {
            keyFormat = keyFormat.replaceAll(Pattern.quote("$sensorName"),
                    getFormatedKey(sensorVariable.getSensor().getSensorId()));
        }
        keyFormat = keyFormat.replaceAll(Pattern.quote("$sensorId"),
                getFormatedKey(sensorVariable.getSensor().getSensorId()));
        keyFormat = keyFormat.replaceAll(Pattern.quote("$variableType"),
                getFormatedKey(sensorVariable.getVariableType().getText()));
        //Update sensorVariableName
        if (sensorVariable.getName() != null) {
            keyFormat = keyFormat.replaceAll(Pattern.quote("$variableName"),
                    getFormatedKey(sensorVariable.getName()));
        } else {
            keyFormat = keyFormat.replaceAll(Pattern.quote("$variableName"),
                    getFormatedKey(String.valueOf(sensorVariable.getId())));
        }
        return keyFormat.replaceAll(" ", "_");
    }

    private String getFormatedKey(String key) {
        switch (_config.getKeyCase()) {
            case LOWER:
                return key.toLowerCase();
            case UPPER:
                return key.toUpperCase();
            default:
                return key;
        }
    }

    public ExternalServerConfig config() {
        return _config;
    }

}
