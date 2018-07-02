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
package org.mycontroller.standalone.scripts;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McScriptEngine implements Runnable {
    private McScript mcScript;
    private Bindings engineScopes;

    public McScriptEngine(McScript mcScript) {
        this.mcScript = mcScript;
    }

    public Object executeScript() throws McScriptException, ScriptException,
            FileNotFoundException {
        if (!mcScript.isValid()) {
            throw new McScriptException("Cannot create script engine, required field is missing!");
        }

        // create a JavaScript engine
        ScriptEngine engine = null;
        if (mcScript.getExtension() != null) {
            engine = McScriptEngineUtils.getScriptEngineManager().getEngineByExtension(mcScript.getExtension());
        } else if (mcScript.getEngineName() != null) {
            engine = McScriptEngineUtils.getScriptEngineManager().getEngineByName(mcScript.getEngineName());

        } else {
            engine = McScriptEngineUtils.getScriptEngineManager().getEngineByMimeType(mcScript.getMimeType());
        }
        //check requested engine is available
        if (engine == null) {
            //McScriptEngineUtils.listAvailableEngines();
            throw new McScriptException("Requested engine is not available! " + mcScript);
        }

        //Load pre-conditions
        McScriptEngineUtils.updateMcApi(engine);

        //Load bindings, if we have any
        if (mcScript.getBindings() != null) {
            for (String key : mcScript.getBindings().keySet()) {
                engine.put(key, mcScript.getBindings().get(key));
            }
        }

        Object result = null;
        // evaluate JavaScript code from String
        if (mcScript.getData() != null) {
            result = engine.eval(mcScript.getData());
        } else { // evaluate JavaScript code from file
            FileReader scriptFileReader = null;
            if (mcScript.getCanonicalPath() != null) {
                scriptFileReader = new FileReader(mcScript.getCanonicalPath());
            } else {
                scriptFileReader = new FileReader(mcScript.getName());
            }
            result = engine.eval(scriptFileReader);
        }

        if (result == null) {
            result = engine.get(McScriptEngineUtils.MC_SCRIPT_RESULT);
        } else if (engine.get(McScriptEngineUtils.MC_SCRIPT_RESULT) == null) {
            engine.put(McScriptEngineUtils.MC_SCRIPT_RESULT, result);
        }
        engineScopes = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        _logger.debug("{}, \nResult: {}", mcScript, result);
        _logger.debug("Script bindings:[{}]\n{}", getBindings(), mcScript);
        return result;
    }

    public HashMap<String, Object> getBindings() {
        if (engineScopes != null) {
            return McScriptEngineUtils.getBindings(engineScopes);
        } else {
            return new HashMap<String, Object>();
        }
    }

    @Override
    public void run() {
        try {
            _logger.debug("Script execution started for ", mcScript);
            Object result = executeScript();
            //If debug enabled this line will be printed in in executeScript method. do not duplicate
            if (!_logger.isDebugEnabled()) {
                _logger.info("{}, \nResult: {}", mcScript, result);
                if (engineScopes != null) {
                    _logger.info("ScriptEngine bindings:[{}]", getBindings());
                }
            }
            _logger.debug("Script execution completed for ", mcScript);
        } catch (Exception ex) {
            _logger.error("Exception happened when executing script: {},", mcScript, ex);
        }
    }
}
