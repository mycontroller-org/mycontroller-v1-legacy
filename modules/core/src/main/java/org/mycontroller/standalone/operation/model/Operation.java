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
package org.mycontroller.standalone.operation.model;

import java.io.FileNotFoundException;
import java.util.HashMap;

import javax.script.ScriptException;

import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.operation.IOperationEngine;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;
import org.mycontroller.standalone.scripts.McScriptEngineUtils;
import org.mycontroller.standalone.scripts.McScriptEngineUtils.SCRIPT_TYPE;
import org.mycontroller.standalone.scripts.McScriptException;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
@Slf4j
public abstract class Operation implements IOperationEngine {
    private Integer id;
    private Boolean enabled;
    private User user;
    private String name;
    private OPERATION_TYPE type;
    private Long lastExecution;

    public abstract String getOperationString();

    public void updateOperation(OperationTable operationTable) {
        id = operationTable.getId();
        enabled = operationTable.getEnabled();
        user = operationTable.getUser();
        name = operationTable.getName();
        type = operationTable.getType();
        lastExecution = operationTable.getLastExecution();
    }

    @JsonIgnore
    public OperationTable getOperationTable() {
        return OperationTable.builder()
                .id(id)
                .enabled(enabled)
                .user(user)
                .name(name)
                .type(type)
                .lastExecution(lastExecution)
                .build();
    }

    //These methods are used for JSON
    @JsonGetter("type")
    private String getTypeString() {
        return type.getText();
    }

    @JsonGetter("user")
    @JsonIgnoreProperties({ "enabled", "username", "fullName", "email", "password", "validity", "permissions", "name",
            "permission", "permissions", "allowedResources" })
    private User getUserJson() {
        return user;
    }

    public static String updateTemplate(String source, HashMap<String, Object> bindings) {
        McScript mcTemplateScript = McScript.builder()
                .type(SCRIPT_TYPE.OPERATION)
                .engineName(McScriptEngineUtils.MC_TEMPLATE_ENGINE)
                .data(source)
                .bindings(bindings)
                .build();
        McScriptEngine templateEngine = new McScriptEngine(mcTemplateScript);
        try {
            return (String) templateEngine.executeScript();
        } catch (FileNotFoundException | McScriptException | ScriptException ex) {
            _logger.error("Exception: {}", mcTemplateScript, ex);
            return "<pre>Exception: " + ex.getMessage() + "</pre>";
        }
    }

}
