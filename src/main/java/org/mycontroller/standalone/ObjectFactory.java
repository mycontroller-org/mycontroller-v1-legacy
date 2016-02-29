/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.interfaces.IActionEngine;
import org.mycontroller.standalone.message.RawMessageQueue;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ObjectFactory {
    private ObjectFactory() {

    }

    private static AppProperties appProperties;
    private static RawMessageQueue rawMessageQueue;
    private static HashMap<Integer, IGateway> gateways = new HashMap<Integer, IGateway>();
    private static HashMap<NETWORK_TYPE, IActionEngine> iActionEngine = new HashMap<NETWORK_TYPE, IActionEngine>();
    private static ResourceBundle mcLocale;

    public static AppProperties getAppProperties() {
        return appProperties;
    }

    public static void setAppProperties(AppProperties appProperties) {
        ObjectFactory.appProperties = appProperties;
    }

    public static RawMessageQueue getRawMessageQueue() {
        return rawMessageQueue;
    }

    public static void setRawMessageQueue(RawMessageQueue rawMessageQueue) {
        ObjectFactory.rawMessageQueue = rawMessageQueue;
    }

    public synchronized static IGateway getGateway(Integer gatewayId) {
        return gateways.get(gatewayId);
    }

    public synchronized static void addGateway(IGateway iGateway) {
        gateways.put(iGateway.getGateway().getId(), iGateway);
    }

    public synchronized static void removeGateway(Integer gatewayId) {
        gateways.remove(gatewayId);
    }

    public synchronized static Set<Integer> getGatewayIds() {
        return gateways.keySet();
    }

    public synchronized static IActionEngine getIActionEngine(NETWORK_TYPE networktype) {
        return iActionEngine.get(networktype);
    }

    public synchronized static void addIActionEngine(NETWORK_TYPE networktype, IActionEngine actionEngine) {
        iActionEngine.put(networktype, actionEngine);
    }

    /* This method is used for restore operation, never call on normal time */
    public synchronized static void clearAllReferences() {
        appProperties = null;
        rawMessageQueue = null;
        gateways = new HashMap<Integer, IGateway>();
        iActionEngine = new HashMap<NETWORK_TYPE, IActionEngine>();
    }

    public static ResourceBundle getMcLocale() {
        return mcLocale;
    }

    public static void setMcLocale(ResourceBundle mcLocale) {
        ObjectFactory.mcLocale = mcLocale;
    }
}
