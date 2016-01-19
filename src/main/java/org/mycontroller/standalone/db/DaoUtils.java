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

import java.sql.SQLException;

import org.mycontroller.standalone.db.dao.AlarmDefinitionDao;
import org.mycontroller.standalone.db.dao.AlarmDefinitionDaoImpl;
import org.mycontroller.standalone.db.dao.FirmwareDao;
import org.mycontroller.standalone.db.dao.FirmwareDaoImpl;
import org.mycontroller.standalone.db.dao.FirmwareTypeDao;
import org.mycontroller.standalone.db.dao.FirmwareTypeDaoImpl;
import org.mycontroller.standalone.db.dao.FirmwareVersionDao;
import org.mycontroller.standalone.db.dao.FirmwareVersionDaoImpl;
import org.mycontroller.standalone.db.dao.GatewayDao;
import org.mycontroller.standalone.db.dao.GatewayDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsBatteryUsageDao;
import org.mycontroller.standalone.db.dao.MetricsBatteryUsageDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsDoubleTypeDeviceDao;
import org.mycontroller.standalone.db.dao.MetricsDoubleTypeDeviceDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsBinaryTypeDeviceDao;
import org.mycontroller.standalone.db.dao.MetricsBinaryTypeDeviceDaoImpl;
import org.mycontroller.standalone.db.dao.NodeDao;
import org.mycontroller.standalone.db.dao.NodeDaoImpl;
import org.mycontroller.standalone.db.dao.ForwardPayloadDao;
import org.mycontroller.standalone.db.dao.ForwardPayloadDaoImpl;
import org.mycontroller.standalone.db.dao.SensorDao;
import org.mycontroller.standalone.db.dao.SensorDaoImpl;
import org.mycontroller.standalone.db.dao.ResourcesLogsDao;
import org.mycontroller.standalone.db.dao.ResourcesLogsDaoImpl;
import org.mycontroller.standalone.db.dao.SensorVariableDao;
import org.mycontroller.standalone.db.dao.SensorVariableDaoImpl;
import org.mycontroller.standalone.db.dao.ResourcesGroupDao;
import org.mycontroller.standalone.db.dao.ResourcesGroupDaoImpl;
import org.mycontroller.standalone.db.dao.ResourcesGroupMapDao;
import org.mycontroller.standalone.db.dao.ResourcesGroupMapDaoImpl;
import org.mycontroller.standalone.db.dao.SensorsVariablesMapDao;
import org.mycontroller.standalone.db.dao.SensorsVariablesMapDaoImpl;
import org.mycontroller.standalone.db.dao.SettingsDao;
import org.mycontroller.standalone.db.dao.SettingsDaoImpl;
import org.mycontroller.standalone.db.dao.SystemJobDao;
import org.mycontroller.standalone.db.dao.SystemJobDaoImpl;
import org.mycontroller.standalone.db.dao.TimerDao;
import org.mycontroller.standalone.db.dao.TimerDaoImpl;
import org.mycontroller.standalone.db.dao.UidTagDao;
import org.mycontroller.standalone.db.dao.UidTagDaoImpl;
import org.mycontroller.standalone.db.dao.UserDao;
import org.mycontroller.standalone.db.dao.UserDaoImpl;
import org.mycontroller.standalone.db.dao.UserSettingsDao;
import org.mycontroller.standalone.db.dao.UserSettingsDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class DaoUtils {
    private DaoUtils() {

    }

    private static final Logger _logger = LoggerFactory.getLogger(DaoUtils.class);

    private static NodeDao nodeDao = null;
    private static SensorDao sensorDao = null;
    private static SensorVariableDao sensorVariableDao = null;
    private static SettingsDao settingsDao = null;
    private static UserSettingsDao userSettingsDao = null;
    private static MetricsDoubleTypeDeviceDao metricsDoubleTypeDeviceDao = null;
    private static MetricsBinaryTypeDeviceDao metricsBinaryTypeDeviceDao = null;
    private static SystemJobDao systemJobDao = null;
    private static UserDao userDao = null;
    private static AlarmDefinitionDao alarmDefinitionDao = null;
    private static ResourcesLogsDao resourcesLogsDao = null;
    private static TimerDao timerDao = null;
    private static ForwardPayloadDao forwardPayloadDao = null;
    private static UidTagDao uidTagDao = null;
    private static FirmwareDao firmwareDao = null;
    private static FirmwareTypeDao firmwareTypeDao = null;
    private static FirmwareVersionDao firmwareVersionDao = null;
    private static MetricsBatteryUsageDao metricsBatteryUsageDao = null;
    private static SensorsVariablesMapDao sensorsVariablesMapDao = null;
    private static GatewayDao gatewayDao = null;
    private static ResourcesGroupDao resourcesGroupDao = null;
    private static ResourcesGroupMapDao resourcesGroupMapDao = null;

    public static void loadAllDao() {
        try {
            nodeDao = new NodeDaoImpl(DataBaseUtils.getConnectionSource());
            sensorDao = new SensorDaoImpl(DataBaseUtils.getConnectionSource());
            sensorVariableDao = new SensorVariableDaoImpl(DataBaseUtils.getConnectionSource());
            settingsDao = new SettingsDaoImpl(DataBaseUtils.getConnectionSource());
            metricsDoubleTypeDeviceDao = new MetricsDoubleTypeDeviceDaoImpl(DataBaseUtils.getConnectionSource());
            metricsBinaryTypeDeviceDao = new MetricsBinaryTypeDeviceDaoImpl(DataBaseUtils.getConnectionSource());
            systemJobDao = new SystemJobDaoImpl(DataBaseUtils.getConnectionSource());
            userDao = new UserDaoImpl(DataBaseUtils.getConnectionSource());
            alarmDefinitionDao = new AlarmDefinitionDaoImpl(DataBaseUtils.getConnectionSource());
            resourcesLogsDao = new ResourcesLogsDaoImpl(DataBaseUtils.getConnectionSource());
            timerDao = new TimerDaoImpl(DataBaseUtils.getConnectionSource());
            forwardPayloadDao = new ForwardPayloadDaoImpl(DataBaseUtils.getConnectionSource());
            uidTagDao = new UidTagDaoImpl(DataBaseUtils.getConnectionSource());
            firmwareTypeDao = new FirmwareTypeDaoImpl(DataBaseUtils.getConnectionSource());
            firmwareVersionDao = new FirmwareVersionDaoImpl(DataBaseUtils.getConnectionSource());
            firmwareDao = new FirmwareDaoImpl(DataBaseUtils.getConnectionSource());
            metricsBatteryUsageDao = new MetricsBatteryUsageDaoImpl(DataBaseUtils.getConnectionSource());
            sensorsVariablesMapDao = new SensorsVariablesMapDaoImpl(DataBaseUtils.getConnectionSource());
            gatewayDao = new GatewayDaoImpl(DataBaseUtils.getConnectionSource());
            resourcesGroupDao = new ResourcesGroupDaoImpl(DataBaseUtils.getConnectionSource());
            resourcesGroupMapDao = new ResourcesGroupMapDaoImpl(DataBaseUtils.getConnectionSource());
            userSettingsDao = new UserSettingsDaoImpl(DataBaseUtils.getConnectionSource());
        } catch (SQLException sqlEx) {
            _logger.error("Unable to load Dao,", sqlEx);
        } catch (DbException dbEx) {
            _logger.error("Unable to load Dao,", dbEx);
        }
    }

    public static NodeDao getNodeDao() {
        return nodeDao;
    }

    public static SensorDao getSensorDao() {
        return sensorDao;
    }

    public static SettingsDao getSettingsDao() {
        return settingsDao;
    }

    public static MetricsDoubleTypeDeviceDao getMetricsDoubleTypeDeviceDao() {
        return metricsDoubleTypeDeviceDao;
    }

    public static SystemJobDao getSystemJobDao() {
        return systemJobDao;
    }

    public static UserDao getUserDao() {
        return userDao;
    }

    public static MetricsBinaryTypeDeviceDao getMetricsBinaryTypeDeviceDao() {
        return metricsBinaryTypeDeviceDao;
    }

    public static AlarmDefinitionDao getAlarmDefinitionDao() {
        return alarmDefinitionDao;
    }

    public static ResourcesLogsDao getResourcesLogsDao() {
        return resourcesLogsDao;
    }

    public static TimerDao getTimerDao() {
        return timerDao;
    }

    public static ForwardPayloadDao getForwardPayloadDao() {
        return forwardPayloadDao;
    }

    public static UidTagDao getUidTagDao() {
        return uidTagDao;
    }

    public static FirmwareTypeDao getFirmwareTypeDao() {
        return firmwareTypeDao;
    }

    public static FirmwareVersionDao getFirmwareVersionDao() {
        return firmwareVersionDao;
    }

    public static FirmwareDao getFirmwareDao() {
        return firmwareDao;
    }

    public static MetricsBatteryUsageDao getMetricsBatteryUsageDao() {
        return metricsBatteryUsageDao;
    }

    public static SensorVariableDao getSensorVariableDao() {
        return sensorVariableDao;
    }

    public static SensorsVariablesMapDao getSensorsVariablesMapDao() {
        return sensorsVariablesMapDao;
    }

    public static GatewayDao getGatewayDao() {
        return gatewayDao;
    }

    public static ResourcesGroupDao getResourcesGroupDao() {
        return resourcesGroupDao;
    }

    public static ResourcesGroupMapDao getResourcesGroupMapDao() {
        return resourcesGroupMapDao;
    }

    public static UserSettingsDao getUserSettingsDao() {
        return userSettingsDao;
    }

}
