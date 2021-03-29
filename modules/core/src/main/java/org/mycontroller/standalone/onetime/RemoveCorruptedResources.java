/*
 * Copyright 2015-2021 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.onetime;

import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */
@Slf4j
public class RemoveCorruptedResources {

    public void execute() {
        _logger.debug("started: removing corrupted resources");
        // remove invalid sensor variables
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll();
        for (SensorVariable sv : sensorVariables) {
            if (sv.getSensor() == null || sv.getSensor().getNode() == null
                    || sv.getSensor().getNode().getGatewayTable() == null
                    || sv.getSensor().getNode().getGatewayTable().getNetworkType() == null) {
                _logger.info("removing a corrupted sensor variable. id:{}, name:{}", sv.getId(), sv.getName());
                DaoUtils.getSensorVariableDao().deleteById(sv.getId());
            }
        }

        // remove invalid sensors
        List<Sensor> sensors = DaoUtils.getSensorDao().getAll();
        for (Sensor sen : sensors) {
            if (sen.getNode() == null || sen.getNode().getGatewayTable() == null
                    || sen.getNode().getGatewayTable().getNetworkType() == null) {
                _logger.info("removing a corrupted sensor. id:{}, name:{}", sen.getId(), sen.getName());
                DaoUtils.getSensorDao().deleteById(sen.getId());
            }
        }

        // remove invalid nodes
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        for (Node node : nodes) {
            if (node.getGatewayTable() == null || node.getGatewayTable().getNetworkType() == null) {
                _logger.info("removing a corrupted node. id:{}, name:{}", node.getId(), node.getName());
                DaoUtils.getNodeDao().deleteById(node.getId());
            }
        }
        _logger.debug("completed: removing corrupted resources");
    }
}
