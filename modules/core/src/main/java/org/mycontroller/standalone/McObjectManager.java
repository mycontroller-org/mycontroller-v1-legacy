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
package org.mycontroller.standalone;

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.message.IMcActionEngine;
import org.mycontroller.standalone.message.McActionEngine;
import org.mycontroller.standalone.message.RawMessageQueue;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class McObjectManager {
    private McObjectManager() {

    }

    private static AppProperties appProperties;
    private static RawMessageQueue rawMessageQueue;
    private static HashMap<Integer, IGateway> gateways = new HashMap<Integer, IGateway>();
    private static ResourceBundle mcLocale;
    private static IMcActionEngine mcActionEngine = new McActionEngine();

    public static AppProperties getAppProperties() {
        return appProperties;
    }

    public static void setAppProperties(AppProperties appProperties) {
        McObjectManager.appProperties = appProperties;
    }

    public static RawMessageQueue getRawMessageQueue() {
        return rawMessageQueue;
    }

    public static void setRawMessageQueue(RawMessageQueue rawMessageQueue) {
        McObjectManager.rawMessageQueue = rawMessageQueue;
    }

    public static synchronized IGateway getGateway(Integer gatewayId) {
        return gateways.get(gatewayId);
    }

    public static synchronized void addGateway(IGateway iGateway) {
        gateways.put(iGateway.getGateway().getId(), iGateway);
    }

    public static synchronized void removeGateway(Integer gatewayId) {
        gateways.remove(gatewayId);
    }

    public static synchronized Set<Integer> getGatewayIds() {
        return gateways.keySet();
    }

    /* This method is used for restore operation, never call on normal time */
    public static synchronized void clearAllReferences() {
        appProperties = null;
        rawMessageQueue = null;
        gateways = new HashMap<Integer, IGateway>();
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
