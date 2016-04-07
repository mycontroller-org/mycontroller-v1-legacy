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
package org.mycontroller.standalone.scripts;

import java.io.FileNotFoundException;
import java.io.FileReader;

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

    public McScriptEngine(McScript mcScript) {
        this.mcScript = mcScript;
    }

    public Object executeScript() throws McScriptException, ScriptException, FileNotFoundException {
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
            throw new McScriptException("Requested engine is not available! " + mcScript);
        }

        //Load pre-conditions
        McScriptEngineUtils.updateMcApi(engine);

        // evaluate JavaScript code from String
        Object result = engine.eval(new FileReader(mcScript.getName()));
        if (result == null) {
            result = engine.get(McScriptEngineUtils.MC_SCRIPT_RESULT);
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Script result:[{}], {}", result, mcScript);
        }
        return result;
    }

    @Override
    public void run() {
        try {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Script execution started for ", mcScript);
            }
            executeScript();
            if (_logger.isDebugEnabled()) {
                _logger.debug("Script execution completed for ", mcScript);
            }
        } catch (Exception ex) {
            _logger.error("Error,", ex);
        }
    }
}
