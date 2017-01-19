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
package org.mycontroller.standalone.exernalserver.model;

import java.util.regex.Pattern;

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
    private Integer id;
    private Boolean enabled;
    private String name;
    private EXTERNAL_SERVER_TYPE type;
    private String keyFormat;

    //Variable name format
    //$gatewayName, $nodeName, $nodeEui, $sensorName, $sensorId, $variableType
    public abstract String getServerDetail();

    public void update(ExternalServerTable externalServerTable) {
        id = externalServerTable.getId();
        enabled = externalServerTable.getEnabled();
        name = externalServerTable.getName();
        type = externalServerTable.getType();
        keyFormat = externalServerTable.getKeyFormat();

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

    @JsonIgnore
    public String getVariableKey(SensorVariable sensorVariable, String keyFormat) {
        if (keyFormat == null) {
            keyFormat = DEFAULT_KEY_FORMAT;
        }
        keyFormat = keyFormat.replaceAll("$gatewayName", sensorVariable.getSensor().getNode().getGatewayTable()
                .getName());
        keyFormat = keyFormat.replaceAll(Pattern.quote("$nodeName"), sensorVariable.getSensor().getNode().getName());
        keyFormat = keyFormat.replaceAll(Pattern.quote("$nodeEui"), sensorVariable.getSensor().getNode().getEui());
        keyFormat = keyFormat.replaceAll(Pattern.quote("$sensorName"), sensorVariable.getSensor().getName());
        keyFormat = keyFormat.replaceAll(Pattern.quote("$sensorId"), sensorVariable.getSensor().getSensorId());
        keyFormat = keyFormat.replaceAll(Pattern.quote("$variableType"), sensorVariable.getVariableType().getText());
        keyFormat = keyFormat.replaceAll(Pattern.quote("$variableTypeId"),
                String.valueOf(sensorVariable.getVariableType().ordinal()));
        return keyFormat.replaceAll(" ", "_");
    }

}
