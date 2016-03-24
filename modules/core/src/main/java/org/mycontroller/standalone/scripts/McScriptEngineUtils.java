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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.mycontroller.standalone.scripts.api.McScriptApi;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class McScriptEngineUtils {
    private static ScriptEngineManager scriptEngineManager = null;
    public static final String MC_API = "mcApi";
    public static final String MC_SCRIPT_RESULT = "mcResult";

    private McScriptEngineUtils() {

    }

    public static synchronized ScriptEngineManager getScriptEngineManager() {
        if (scriptEngineManager == null) {
            scriptEngineManager = new ScriptEngineManager();
        }
        return scriptEngineManager;
    }

    //Load mc api details
    public static synchronized void updateMcApi(ScriptEngine engine) {
        engine.put(MC_API, new McScriptApi());
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
