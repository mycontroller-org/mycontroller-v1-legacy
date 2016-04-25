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
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@AllArgsConstructor
public class McScriptEngine implements Runnable {
    private McScript mcScript;

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
            listAvailableEngines();
            throw new McScriptException("Requested engine is not available! " + mcScript);
        }

        //Load pre-conditions
        McScriptEngineUtils.updateMcApi(engine);

        // evaluate JavaScript code from String
        FileReader scriptFileReader = null;
        if (mcScript.getCanonicalPath() != null) {
            scriptFileReader = new FileReader(mcScript.getCanonicalPath());
        } else {
            scriptFileReader = new FileReader(mcScript.getName());
        }
        Object result = engine.eval(scriptFileReader);
        if (result == null) {
            result = engine.get(McScriptEngineUtils.MC_SCRIPT_RESULT);
        }
        _logger.info("Script result:[{}], {}", result, mcScript);
        return result;
    }

    @Override
    public void run() {
        try {
            _logger.debug("Script execution started for ", mcScript);
            executeScript();
            _logger.debug("Script execution completed for ", mcScript);
        } catch (Exception ex) {
            _logger.error("Error,", ex);
        }
    }

    public void listAvailableEngines() {
        if (_logger.isInfoEnabled()) {
            ScriptEngineManager mgr = McScriptEngineUtils.getScriptEngineManager();
            List<ScriptEngineFactory> factories = mgr.getEngineFactories();
            StringBuilder builder = new StringBuilder();
            for (ScriptEngineFactory factory : factories) {
                builder.append("\n\n*****************************************************")
                        .append("\nEngineName:").append(factory.getEngineName())
                        .append("\nEngineVersion:").append(factory.getEngineVersion())
                        .append("\nLanguageName:").append(factory.getLanguageName())
                        .append("\nLanguageVersion:").append(factory.getLanguageVersion())
                        .append("\nExtensions:").append(factory.getExtensions())
                        .append("\nAlias:").append(factory.getNames())
                        .append("\n*****************************************************");
            }
            _logger.info("Available script engines information:{}", builder.toString());
        }

    }
}
