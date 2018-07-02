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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.scripts.api.McScriptApi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class McScriptEngineUtils {
    private static ScriptEngineManager scriptEngineManager = null;
    public static final String MC_API = "mcApi";
    public static final String MC_SCRIPT_RESULT = "mcResult";
    public static final String MC_TEMPLATE_ENGINE = "freemarker";
    public static final String MC_SCRIPT_NAME = "scriptName";

    public static synchronized ScriptEngineManager getScriptEngineManager() {
        if (scriptEngineManager == null) {
            scriptEngineManager = new ScriptEngineManager();
        }
        return scriptEngineManager;
    }

    public static File getScriptFile(String scriptFileName) throws IllegalAccessException, IOException {
        File scriptFile = FileUtils.getFile(AppProperties.getInstance().getScriptsLocation() + scriptFileName);
        String scriptCanonicalPath = scriptFile.getCanonicalPath();
        String scriptLocation = FileUtils.getFile(AppProperties.getInstance().getScriptsLocation())
                .getCanonicalPath();
        //Check is file available and has access to read
        if (!scriptFile.exists() || !scriptFile.canRead()) {
            throw new IllegalAccessException("Unable to access this file '" + scriptCanonicalPath + "'!");
        }
        //Check file location inside scripts location
        if (!scriptCanonicalPath.startsWith(scriptLocation)) {
            throw new IllegalAccessException("Selected file is not under script location! '" + scriptCanonicalPath
                    + "'!");
        }
        return scriptFile;
    }

    //Load mc api details
    public static void updateMcApi(ScriptEngine engine) {
        engine.put(MC_API, new McScriptApi());
    }

    public static HashMap<String, Object> getBindings(Bindings bindings) {
        HashMap<String, Object> engineScopes = new HashMap<String, Object>();
        for (String key : bindings.keySet()) {
            //Do not add mcApi and __builtins__
            if (!key.equals(MC_API) && !key.startsWith("__")) {
                engineScopes.put(key, bindings.get(key));
            }
        }
        return engineScopes;
    }

    public static List<HashMap<String, Object>> getScriptEnginesDetail() {
        List<HashMap<String, Object>> engines = new ArrayList<HashMap<String, Object>>();
        ScriptEngineManager mgr = McScriptEngineUtils.getScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory : factories) {
            HashMap<String, Object> engine = new HashMap<String, Object>();
            engine.put("engineName", factory.getEngineName());
            engine.put("engineVersion", factory.getEngineVersion());
            engine.put("languageName", factory.getLanguageName());
            engine.put("languageVersion", factory.getLanguageVersion());
            engine.put("extensions", factory.getExtensions());
            engine.put("alias", factory.getNames());
            engines.add(engine);
        }
        return engines;
    }

    public static void listAvailableEngines() {
        if (_logger.isDebugEnabled()) {
            List<HashMap<String, Object>> engines = getScriptEnginesDetail();
            StringBuilder builder = new StringBuilder();
            builder.append("\n************ Available script engines ***************");
            for (HashMap<String, Object> engine : engines) {
                builder.append("\nEngineName      :").append(engine.get("engineName"))
                        .append("\nEngineVersion   :").append(engine.get("engineVersion"))
                        .append("\nLanguageName    :").append(engine.get("languageName"))
                        .append("\nLanguageVersion :").append(engine.get("languageVersion"))
                        .append("\nExtensions      :").append(engine.get("extensions"))
                        .append("\nAlias           :").append(engine.get("alias"));
                builder.append("\n*****************************************************");
            }
            _logger.debug("Script engines list:{}", builder.toString());
        }
    }

    public enum SCRIPT_TYPE {
        CONDITION("Condition"),
        OPERATION("Operation");

        private final String name;

        private SCRIPT_TYPE(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static SCRIPT_TYPE get(int id) {
            for (SCRIPT_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static SCRIPT_TYPE fromString(String text) {
            if (text != null) {
                for (SCRIPT_TYPE type : SCRIPT_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

}
