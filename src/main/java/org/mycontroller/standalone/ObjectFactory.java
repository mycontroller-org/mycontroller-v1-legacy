/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.mysensors.RawMessageQueue;
import org.mycontroller.standalone.serialport.ISerialPort;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ObjectFactory {
    private ObjectFactory() {

    }

    private static AppProperties appProperties;
    private static RawMessageQueue rawMessageQueue;
    private static ISerialPort iSerialPort;

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

    public synchronized static ISerialPort getiSerialPort() {
        return iSerialPort;
    }

    public synchronized static void setiSerialPort(ISerialPort iSerialPort) {
        ObjectFactory.iSerialPort = iSerialPort;
    }
}
