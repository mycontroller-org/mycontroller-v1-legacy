/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.externalserver;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.db.tables.Resource;
import org.mycontroller.standalone.db.tables.SensorVariable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalServerEngine implements Runnable {
    private SensorVariable sensorVariable = null;

    public ExternalServerEngine(SensorVariable sensorVariable) {
        this.sensorVariable = sensorVariable;
    }

    private void selectServers() {
        //Send gateway level servers
        Resource resource = DaoUtils.getResourceDao().get(RESOURCE_TYPE.GATEWAY,
                sensorVariable.getSensor().getNode().getGatewayTable().getId());
        executeSendPayload(resource);

        //Send node level servers
        resource = DaoUtils.getResourceDao().get(RESOURCE_TYPE.NODE, sensorVariable.getSensor().getNode().getId());
        executeSendPayload(resource);

        //Send sensor level servers
        resource = DaoUtils.getResourceDao().get(RESOURCE_TYPE.SENSOR, sensorVariable.getSensor().getId());
        executeSendPayload(resource);

        //Send sensor variable level servers
        resource = DaoUtils.getResourceDao().get(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId());
        executeSendPayload(resource);
    }

    private void executeSendPayload(Resource resource) {
        if (resource == null || !resource.getEnabled()) {
            return;
        }
        _logger.debug("Target resource: {}", resource);
        if (resource.getExternalServersObject() != null) {
            for (ExternalServerTable extServer : resource.getExternalServersObject()) {
                if (extServer.getEnabled()) {
                    IExternalServerEngine extServerEngine = ExternalServerUtils.getExternalServer(extServer);
                    try {
                        if (extServerEngine != null) {
                            extServerEngine.send(sensorVariable);
                        }
                    } catch (Exception ex) {
                        _logger.error("Exception when sending data to server: {}, ", extServerEngine.toString(), ex);
                    }
                }
            }
        }

    }

    @Override
    public void run() {
        _logger.debug("Executing send payload to external server");
        selectServers();
    }

}
