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
package org.mycontroller.standalone.db;

import java.util.List;

import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
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

    public static void deleteSensor(Sensor sensor) {
        //Delete from timer
        List<Timer> timers = DaoUtils.getTimerDao().getAll(sensor.getId());
        SchedulerUtils.unloadTimerJobs(timers);
        DaoUtils.getTimerDao().deleteBySensorRefId(sensor.getId());

        //Delete Alarm list
        DaoUtils.getAlarmDao().deleteBySensorRefId(sensor.getId());

        //Delete all variable Types
        List<SensorValue> sensorValues = DaoUtils.getSensorValueDao().getAll(sensor.getId());
        for (SensorValue sensorValue : sensorValues) {
            deleteSensorValue(sensorValue);
        }

        //Clear Forward Payload Table
        DaoUtils.getForwardPayloadDao().deleteBySensorRefId(sensor.getId());

        //Delete from sensor logs
        DaoUtils.getSensorLogDao().deleteBySensorId(sensor.getId());

        //Delete UID tags
        DaoUtils.getUidTagDao().deleteBySensorRefId(sensor.getId());

        //Delete Sensor
        DaoUtils.getSensorDao().delete(sensor);
        _logger.debug("Deleted sensor trace for sensor:[{}]", sensor);

    }

    public static void deleteSensorValue(SensorValue sensorValue) {
        //Delete from metrics table
        DaoUtils.getMetricsDoubleTypeDeviceDao().deleteBySensorRefId(sensorValue.getId());
        DaoUtils.getMetricsBinaryTypeDeviceDao().deleteBySensorRefId(sensorValue.getId());

        //Delete SensorValue entry
        DaoUtils.getSensorValueDao().delete(sensorValue);
    }

    public static void deleteNode(Node node) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAll(node.getId());
        for (Sensor sensor : sensors) {
            deleteSensor(sensor);
        }
        DaoUtils.getNodeDao().delete(node.getId());
        _logger.debug("Deleted node trace for node:[{}]", node);
    }
}
