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
package org.mycontroller.standalone;

import java.util.HashMap;
import java.util.ResourceBundle;

import org.mycontroller.standalone.message.IMcActionEngine;
import org.mycontroller.standalone.message.McActionEngine;
import org.mycontroller.standalone.provider.IEngine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class McObjectManager {

    private static ResourceBundle mcLocale;
    private static IMcActionEngine mcActionEngine = new McActionEngine();

    private static HashMap<Integer, IEngine> engines = new HashMap<Integer, IEngine>();

    public static IEngine getEngine(Integer gatewayId) {
        return engines.get(gatewayId);
    }

    public static void addEngine(IEngine engine) {
        engines.put(engine.config().getId(), engine);
    }

    public static void removeEngine(Integer gatewayId) {
        engines.remove(gatewayId);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<Integer, IEngine> getEngines() {
        return (HashMap<Integer, IEngine>) engines.clone();
    }

    /* This method is used for restore operation, never call on normal time */
    public static synchronized void clearAllReferences() {
        engines = new HashMap<Integer, IEngine>();
    }

    public static ResourceBundle getMcLocale() {
        return mcLocale;
    }

    public static void setMcLocale(ResourceBundle mcLocale) {
        McObjectManager.mcLocale = mcLocale;
    }

    public static IMcActionEngine getMcActionEngine() {
        return mcActionEngine;
    }
}
