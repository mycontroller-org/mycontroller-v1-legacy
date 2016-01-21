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
package org.mycontroller.standalone.db;

import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class DeleteResourceUtils {
    private static final Logger _logger = LoggerFactory.getLogger(DeleteResourceUtils.class.getName());

    private DeleteResourceUtils() {

    }

    public static void deleteSensor(Integer sensorId) {
        deleteSensor(DaoUtils.getSensorDao().getById(sensorId));
    }

    public static void deleteSensor(Sensor sensor) {
        //Delete from timer
        List<Timer> timers = DaoUtils.getTimerDao().getAll(RESOURCE_TYPE.SENSOR, sensor.getId());
        SchedulerUtils.unloadTimerJobs(timers);
        DaoUtils.getTimerDao().delete(RESOURCE_TYPE.SENSOR, sensor.getId());

        //Delete AlarmDefinition list
        DaoUtils.getAlarmDefinitionDao().delete(RESOURCE_TYPE.SENSOR, sensor.getId());
        //TODO: Delete timer created by alarmDefinition

        //Delete all variable Types
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(sensor.getId());
        for (SensorVariable sensorVariable : sensorVariables) {
            deleteSensorValue(sensorVariable);
        }

        //Clear Forward Payload Table
        DaoUtils.getForwardPayloadDao().deleteBySensorId(sensor.getId());

        //Delete from sensor logs
        DaoUtils.getResourcesLogsDao().deleteAll(RESOURCE_TYPE.SENSOR, sensor.getId());

        //Delete UID tags
        DaoUtils.getUidTagDao().deleteBySensorRefId(sensor.getId());

        //Delete Sensor
        DaoUtils.getSensorDao().delete(sensor);
        _logger.debug("Deleted sensor trace for sensor:[{}]", sensor);

    }

    public static void deleteSensorValue(SensorVariable sensorVariable) {
        //Delete from metrics table
        DaoUtils.getMetricsDoubleTypeDeviceDao().deleteBySensorVariableRefId(sensorVariable.getId());
        DaoUtils.getMetricsBinaryTypeDeviceDao().deleteBySensorValueRefId(sensorVariable.getId());

        //Delete SensorVariable entry
        DaoUtils.getSensorVariableDao().delete(sensorVariable);
    }

    public static void deleteNode(Node node) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAll(node.getId());
        for (Sensor sensor : sensors) {
            deleteSensor(sensor);
        }
        DaoUtils.getNodeDao().delete(node.getId());
        _logger.debug("Deleted node trace for node:[{}]", node);
    }

    public static void deleteNodes(List<Integer> nodeIds) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAllByNodeIds(nodeIds);
        for (Sensor sensor : sensors) {
            deleteSensor(sensor);
        }
        DaoUtils.getNodeDao().delete(nodeIds);
        _logger.debug("Deleted node trace for nodeIds:[{}]", nodeIds);
    }

    public static void deleteSensors(List<Integer> sensorIds) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAllByIds(sensorIds);
        for (Sensor sensor : sensors) {
            deleteSensor(sensor);
        }
    }

    public static void deleteGateway(Integer id) {
        GatewayUtils.unloadGateway(id);
        List<Node> nodes = DaoUtils.getNodeDao().getAll(id);
        for (Node node : nodes) {
            deleteNode(node);
        }
        DaoUtils.getGatewayDao().deleteById(id);
    }

    public static void deleteGateways(List<Integer> ids) {
        for (Integer id : ids) {
            deleteGateway(id);
        }
    }

    public static void deleteAlarmDefinitions(List<Integer> ids) {
        for (Integer id : ids) {
            deleteAlarmDefinition(id);
        }
    }

    public static synchronized void deleteAlarmDefinition(AlarmDefinition alarmDefinition) {
        //Unload timer job if any
        Timer timer = new Timer();
        timer.setName(AlarmUtils.getAlarmTimerJobName(alarmDefinition));
        SchedulerUtils.unloadTimerJob(timer);

        //Remove log entries
        //TODO: add code to remove logs

        //TODO: remove timers loaded by alarm

        //Delete alarm definition
        DaoUtils.getAlarmDefinitionDao().deleteById(alarmDefinition.getId());
    }

    public static synchronized void deleteAlarmDefinition(Integer id) {
        deleteAlarmDefinition(DaoUtils.getAlarmDefinitionDao().getById(id));

    }

    public static synchronized void deleteTimer(Timer timer) {
        //Unload timer job if any
        SchedulerUtils.unloadTimerJob(timer);

        //Remove log entries
        //TODO: add code to remove logs

        //Delete alarm definition
        DaoUtils.getTimerDao().delete(timer.getId());
    }

    public static synchronized void deleteTimer(Integer id) {
        deleteTimer(DaoUtils.getTimerDao().get(id));
    }

    public static synchronized void deleteResourcesGroup(List<Integer> ids) {
        for (Integer id : ids) {
            deleteResourcesGroup(id);
        }
    }

    public static synchronized void deleteResourcesGroup(Integer id) {
        ResourcesGroup resourcesGroup = DaoUtils.getResourcesGroupDao().get(id);

        //Delete all timers
        List<Timer> timers = DaoUtils.getTimerDao().getAll(RESOURCE_TYPE.RESOURCES_GROUP, resourcesGroup.getId());
        for (Timer timer : timers) {
            TimerUtils.deleteTimer(timer);
        }

        //Delete all alarmDefinition references
        List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll(
                RESOURCE_TYPE.RESOURCES_GROUP, resourcesGroup.getId());

        for (AlarmDefinition alarmDefinition : alarmDefinitions) {
            deleteAlarmDefinition(alarmDefinition);
        }

        //Remove log entries
        //TODO: add code to remove logs

        //Delete resourceGroupMap and resourcesGroup
        DaoUtils.getResourcesGroupMapDao().delete(ResourcesGroup.builder().id(id).build());
        DaoUtils.getResourcesGroupDao().delete(id);
    }
}
