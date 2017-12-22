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
package org.mycontroller.standalone.exernalserver.model;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.mycontroller.standalone.AppProperties.ALPHABETICAL_CASE;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.ExternalServerUtils.EXTERNAL_SERVER_TYPE;
import org.mycontroller.standalone.externalserver.IExternalServerEngine;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
public abstract class ExternalServer implements IExternalServerEngine {
    public static final String DEFAULT_KEY_FORMAT = "$nodeEui_$sensorId_$variableType";
    public static final String KEY_ALPHABETICAL_CASE = "alpCase";
    private Integer id;
    private Boolean enabled;
    private String name;
    private EXTERNAL_SERVER_TYPE type;
    private String keyFormat;
    private ALPHABETICAL_CASE keyCase;

    //Variable name format
    //$gatewayName, $nodeName, $nodeEui, $sensorName, $sensorId, $variableType
    public abstract String getServerDetail();

    public void update(ExternalServerTable externalServerTable) {
        id = externalServerTable.getId();
        enabled = externalServerTable.getEnabled();
        name = externalServerTable.getName();
        type = externalServerTable.getType();
        keyFormat = externalServerTable.getKeyFormat();
        keyCase = (ALPHABETICAL_CASE) externalServerTable.getProperties().get(KEY_ALPHABETICAL_CASE);
    }

    @JsonIgnore
    public ExternalServerTable getExternalServerTable() {
        return ExternalServerTable.builder()
                .id(id)
                .enabled(enabled)
                .name(name)
                .type(type)
                .keyFormat(keyFormat)
                .build();
    }

    //These methods are used for JSON
    @JsonGetter("type")
    private String getTypeString() {
        return type.getText();
    }

    public ALPHABETICAL_CASE getKeyCase() {
        if (keyCase == null) {
            return ALPHABETICAL_CASE.DEFAULT;
        }
        return keyCase;
    }

    private String getFormatedKey(String key) {
        switch (getKeyCase()) {
            case LOWER:
                return key.toLowerCase();
            case UPPER:
                return key.toUpperCase();
            default:
                return key;
        }
    }

    @JsonIgnore
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_ALPHABETICAL_CASE, getKeyCase());
        return properties;
    }

    @JsonIgnore
    public String getVariableKey(SensorVariable sensorVariable, String keyFormat) {
        if (keyFormat == null) {
            keyFormat = DEFAULT_KEY_FORMAT;
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
        return keyFormat.replaceAll(" ", "_");
    }

}
