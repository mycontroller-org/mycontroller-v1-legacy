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
package org.mycontroller.standalone.operation.model;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.operation.Notification;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@Slf4j
@NoArgsConstructor
public class OperationExecuteScript extends Operation {

    public static final String KEY_SCRIPT_FILE = "scriptFile";
    public static final String KEY_SCRIPT_BINDINGS = "scriptBindings";

    private String scriptFile;
    private HashMap<String, Object> scriptBindings;
    private HashMap<String, Object> scriptBindingsTemp = new HashMap<String, Object>();

    public OperationExecuteScript(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        scriptFile = (String) operationTable.getProperties().get(KEY_SCRIPT_FILE);
        scriptBindings = (HashMap<String, Object>) operationTable.getProperties().get(KEY_SCRIPT_BINDINGS);
    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_SCRIPT_FILE, scriptFile);
        properties.put(KEY_SCRIPT_BINDINGS, scriptBindings);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(scriptFile).append(" <= ").append(scriptBindings).append(" ]");
        return stringBuilder.toString();
    }

    public HashMap<String, Object> getScriptBindings() {
        if (scriptBindings == null) {
            return new HashMap<String, Object>();
        }
        return scriptBindings;
    }

    @Override
    public void execute(RuleDefinition ruleDefinition) {
        if (scriptFile == null) {
            throw new RuntimeException("Cannot execute script without script file name! Rule definition: "
                    + ruleDefinition.getName());
        }
        scriptBindingsTemp.put("notification", new Notification(ruleDefinition));
        //execute script
        executeScript();
    }

    @Override
    public void execute(Timer timer) {
        if (scriptFile == null) {
            throw new RuntimeException("Cannot execute script without script file name! Timer: "
                    + timer.getName());
        }
        //execute script
        executeScript();
    }

    private void executeScript() {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        McScript mcScript = null;
        try {
            scriptBindingsTemp.putAll(getScriptBindings());
            File script = FileUtils.getFile(
                    AppProperties.getInstance().getScriptsLocation() + scriptFile);
            mcScript = McScript.builder()
                    .name(script.getCanonicalPath())
                    .extension(FilenameUtils.getExtension(script.getCanonicalPath()))
                    .bindings(scriptBindingsTemp)
                    .build();
            McScriptEngine mcScriptEngine = new McScriptEngine(mcScript);
            Object result = mcScriptEngine.executeScript();
            _logger.debug("script executed. QueryResponse:{}, {}", result, mcScript);
        } catch (Exception ex) {
            _logger.error("Exception on {}", mcScript, ex);
        }
        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());
    }

}
