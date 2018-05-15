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

import org.mycontroller.standalone.message.IMessage;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public interface IExecutor {
    void execute(IMessage message);

    void executeInternal();

    void executePresentation();

    void executeRequest();

    void executeSet();

    void executeStream();

    void executeFirmwareRequest();

    void executeFirmwareConfigRequest();

    void nodeEuiRequest();

    String metricType();

    void firmwareUpdateStart(int totalBlocks);

    void firmwareUpdateFinished();

    void updateFirmwareStatus(int blocksSent);
}
