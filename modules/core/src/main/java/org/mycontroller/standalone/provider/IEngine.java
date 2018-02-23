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
package org.mycontroller.standalone.provider;

import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.message.IMessage;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public interface IEngine extends Runnable {
    void start();

    void stop();

    boolean isRunning();

    void distory();

    void routineTasks();

    void send(IMessage message);

    void sendSleepNode(IMessage message);

    void clearSleepQueue(String nodeEui);

    void auditQueue();

    boolean validate(Sensor sensor);

    boolean validate(Node node);

    GatewayConfig config();

    EngineStatistics processingRate();
}
