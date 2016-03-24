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
package org.mycontroller.standalone.operation.model;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
public class OperationExecuteScript extends Operation {
    private static final Logger _logger = LoggerFactory.getLogger(OperationSendEmail.class);

    public static final String KEY_SCRIPT_FILE = "scriptFile";

    private String scriptFile;

    public OperationExecuteScript() {

    }

    public OperationExecuteScript(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        scriptFile = (String) operationTable.getProperties().get(KEY_SCRIPT_FILE);
    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_SCRIPT_FILE, scriptFile);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(scriptFile).append(" ]");
        return stringBuilder.toString();
    }

    @Override
    public void execute(RuleDefinition ruleDefinition) {
        if (scriptFile == null) {
            throw new RuntimeException("Cannot execute script without script file name! Rule definition: "
                    + ruleDefinition.getName());
        }
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
        try {
            File script = FileUtils.getFile(
                    ObjectFactory.getAppProperties().getScriptOperationsLocation() + scriptFile);
            McScript mcScript = McScript.builder()
                    .file(script.getCanonicalPath())
                    .extension(FilenameUtils.getExtension(script.getCanonicalPath()))
                    .build();
            McScriptEngine mcScriptEngine = new McScriptEngine(mcScript);
            Object result = mcScriptEngine.executeScript();
            if (_logger.isDebugEnabled()) {
                _logger.debug("script executed. Result:{}, {}", result, mcScript);
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());
    }

}
