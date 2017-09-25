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
package org.mycontroller.standalone.db;

import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Resource;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.message.SmartSleepMessageQueue;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class DeleteResourceUtils {

    private DeleteResourceUtils() {

    }

    public static void deleteSensor(Integer sensorId) {
        deleteSensor(DaoUtils.getSensorDao().getById(sensorId));
    }

    /* delete timers */
    //TODO:
    /*
    public static void deleteTimers(RESOURCE_TYPE resourceType, Integer resourceId) {
        List<Timer> timers = DaoUtils.getTimerDao().getAll(resourceType, resourceId);
        for (Timer timer : timers) {
            TimerUtils.deleteTimer(timer);
        }
    }

    /* delete alarm definitions */
    //TODO:
    /*
    public static void deleteAlarmDefinitions(RESOURCE_TYPE resourceType, Integer resourceId) {
        List<RuleDefinitionTable> ruleDefinitions = DaoUtils.getRuleDefinitionDao().getAll(resourceType, resourceId);
        for (RuleDefinitionTable ruleDefinition : ruleDefinitions) {
            RuleUtils.deleteRuleDefinition(ruleDefinition);
        }
    }
     */
    public static void deleteSensorVariable(SensorVariable sensorVariable) {
        //Delete timers
        //deleteTimers(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId());

        //Delete alarmDefinitions
        //deleteAlarmDefinitions(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId());

        //Delete from metrics table
        DaoUtils.getMetricsDoubleTypeDeviceDao().deleteBySensorVariableRefId(sensorVariable.getId());
        DaoUtils.getMetricsBinaryTypeDeviceDao().deleteBySensorVariableRefId(sensorVariable.getId());

        //Delete from resources log
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId());

        //Clear Forward Payload Table
        DaoUtils.getForwardPayloadDao().delete(ForwardPayload.KEY_SOURCE_ID, sensorVariable.getId());
        DaoUtils.getForwardPayloadDao().delete(ForwardPayload.KEY_DESTINATION_ID, sensorVariable.getId());

        //Delete from resource table
        deleteResource(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId());

        //Delete UID tags
        DaoUtils.getUidTagDao().delete(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId());

        //Delete SensorVariable entry
        DaoUtils.getSensorVariableDao().delete(sensorVariable);
        _logger.debug("Item removed:{}", sensorVariable);
    }

    public static void deleteSensor(Sensor sensor) {
        //Remove smart sleep messages
        SmartSleepMessageQueue.getInstance().removeMessages(sensor.getNode().getGatewayTable().getId(),
                sensor.getNode().getEui(), sensor.getSensorId());

        //Delete timers
        //deleteTimers(RESOURCE_TYPE.SENSOR, sensor.getId());

        //Delete AlarmDefinitions
        //deleteAlarmDefinitions(RESOURCE_TYPE.SENSOR, sensor.getId());

        //Clear Forward Payload Table
        DaoUtils.getForwardPayloadDao().deleteBySensorId(sensor.getId());

        //Delete from sensor logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.SENSOR, sensor.getId());

        //Delete from resource table
        deleteResource(RESOURCE_TYPE.SENSOR, sensor.getId());

        //Delete UID tags
        DaoUtils.getUidTagDao().delete(RESOURCE_TYPE.SENSOR, sensor.getId());

        //Delete all variable Types
        for (SensorVariable sensorVariable : DaoUtils.getSensorVariableDao().getAllBySensorId(sensor.getId())) {
            deleteSensorVariable(sensorVariable);
        }

        //Delete Sensor
        DaoUtils.getSensorDao().delete(sensor);
        _logger.debug("Item removed:{}", sensor);
    }

    public static void deleteNode(Node node) {
        //Remove smart sleep messages
        SmartSleepMessageQueue.getInstance().removeQueue(node.getGatewayTable().getId(), node.getEui());

        //Delete sensors
        for (Sensor sensor : DaoUtils.getSensorDao().getAllByNodeId(node.getId())) {
            deleteSensor(sensor);
        }

        //Delete timers
        // deleteTimers(RESOURCE_TYPE.NODE, node.getId());

        //Delete AlarmDefinitions
        //deleteAlarmDefinitions(RESOURCE_TYPE.NODE, node.getId());

        //Delete UID tags
        DaoUtils.getUidTagDao().delete(RESOURCE_TYPE.NODE, node.getId());

        //Delete from resource table
        deleteResource(RESOURCE_TYPE.NODE, node.getId());

        //Delete from resources logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.NODE, node.getId());

        DaoUtils.getNodeDao().deleteById(node.getId());
        _logger.debug("Item removed:{}", node);
    }

    public static void deleteGateway(Integer id) {
        //Unload gateway
        GatewayUtils.unloadGateway(id);

        //Delete nodes
        for (Node node : DaoUtils.getNodeDao().getAllByGatewayId(id)) {
            deleteNode(node);
        }

        //Delete timers
        // deleteTimers(RESOURCE_TYPE.GATEWAY, id);

        //Delete AlarmDefinitions
        // deleteAlarmDefinitions(RESOURCE_TYPE.GATEWAY, id);

        //Delete UID tags
        DaoUtils.getUidTagDao().delete(RESOURCE_TYPE.GATEWAY, id);

        //Delete from resource table
        deleteResource(RESOURCE_TYPE.GATEWAY, id);

        //Delete resources logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.GATEWAY, id);

        //Delete gateway
        DaoUtils.getGatewayDao().deleteById(id);
        _logger.debug("Item removed, gatewayId:{}", id);
    }

    public static synchronized void deleteResourcesGroup(Integer id) {
        ResourcesGroup resourcesGroup = DaoUtils.getResourcesGroupDao().get(id);
        //Delete timers
        // deleteTimers(RESOURCE_TYPE.RESOURCES_GROUP, resourcesGroup.getId());

        //Delete AlarmDefinitions
        // deleteAlarmDefinitions(RESOURCE_TYPE.RESOURCES_GROUP, resourcesGroup.getId());

        //Delete from resources logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.RESOURCES_GROUP, resourcesGroup.getId());

        //Delete resourceGroupMap and resourcesGroup
        DaoUtils.getResourcesGroupMapDao().delete(ResourcesGroup.builder().id(id).build());
        DaoUtils.getResourcesGroupDao().delete(id);
        _logger.debug("Item removed:{}", resourcesGroup);
    }

    public static void deleteNodes(List<Integer> nodeIds) {
        for (Node node : DaoUtils.getNodeDao().getAll(nodeIds)) {
            deleteNode(node);
        }
    }

    public static void deleteSensors(List<Integer> sensorIds) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAllByIds(sensorIds);
        for (Sensor sensor : sensors) {
            deleteSensor(sensor);
        }
    }

    public static void deleteGateways(List<Integer> ids) {
        for (Integer id : ids) {
            deleteGateway(id);
        }
    }

    public static synchronized void deleteResourcesGroup(List<Integer> ids) {
        for (Integer id : ids) {
            deleteResourcesGroup(id);
        }
    }

    //Delete resource and mappings
    public static void deleteResource(RESOURCE_TYPE resourceType, Integer resourceId) {
        Resource resource = DaoUtils.getResourceDao().get(resourceType, resourceId);
        if (resource != null) {
            //Delete external server and resource mapping
            DaoUtils.getExternalServerResourceMapDao().deleteByResourceId(resource.getId());
            //Delete resource
            DaoUtils.getResourceDao().delete(resource);
        }
    }
}
