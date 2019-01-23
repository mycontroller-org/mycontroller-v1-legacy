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
package org.mycontroller.standalone.db;

import java.sql.SQLException;

import org.mycontroller.standalone.db.dao.ExternalServerDao;
import org.mycontroller.standalone.db.dao.ExternalServerDaoImpl;
import org.mycontroller.standalone.db.dao.ExternalServerResourceMapDao;
import org.mycontroller.standalone.db.dao.ExternalServerResourceMapDaoImpl;
import org.mycontroller.standalone.db.dao.FirmwareDao;
import org.mycontroller.standalone.db.dao.FirmwareDaoImpl;
import org.mycontroller.standalone.db.dao.FirmwareDataDao;
import org.mycontroller.standalone.db.dao.FirmwareDataDaoImpl;
import org.mycontroller.standalone.db.dao.FirmwareTypeDao;
import org.mycontroller.standalone.db.dao.FirmwareTypeDaoImpl;
import org.mycontroller.standalone.db.dao.FirmwareVersionDao;
import org.mycontroller.standalone.db.dao.FirmwareVersionDaoImpl;
import org.mycontroller.standalone.db.dao.ForwardPayloadDao;
import org.mycontroller.standalone.db.dao.ForwardPayloadDaoImpl;
import org.mycontroller.standalone.db.dao.GatewayDao;
import org.mycontroller.standalone.db.dao.GatewayDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsBatteryUsageDao;
import org.mycontroller.standalone.db.dao.MetricsBatteryUsageDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsBinaryTypeDeviceDao;
import org.mycontroller.standalone.db.dao.MetricsBinaryTypeDeviceDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsCounterTypeDeviceDao;
import org.mycontroller.standalone.db.dao.MetricsCounterTypeDeviceDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsDoubleTypeDeviceDao;
import org.mycontroller.standalone.db.dao.MetricsDoubleTypeDeviceDaoImpl;
import org.mycontroller.standalone.db.dao.MetricsGPSTypeDeviceDao;
import org.mycontroller.standalone.db.dao.MetricsGPSTypeDeviceDaoImpl;
import org.mycontroller.standalone.db.dao.NodeDao;
import org.mycontroller.standalone.db.dao.NodeDaoImpl;
import org.mycontroller.standalone.db.dao.OperationDao;
import org.mycontroller.standalone.db.dao.OperationDaoImpl;
import org.mycontroller.standalone.db.dao.OperationRuleDefinitionMapDao;
import org.mycontroller.standalone.db.dao.OperationRuleDefinitionMapDaoImpl;
import org.mycontroller.standalone.db.dao.OperationTimerMapDao;
import org.mycontroller.standalone.db.dao.OperationTimerMapDaoImpl;
import org.mycontroller.standalone.db.dao.ResourceDao;
import org.mycontroller.standalone.db.dao.ResourceDaoImpl;
import org.mycontroller.standalone.db.dao.ResourcesGroupDao;
import org.mycontroller.standalone.db.dao.ResourcesGroupDaoImpl;
import org.mycontroller.standalone.db.dao.ResourcesGroupMapDao;
import org.mycontroller.standalone.db.dao.ResourcesGroupMapDaoImpl;
import org.mycontroller.standalone.db.dao.ResourcesLogsDao;
import org.mycontroller.standalone.db.dao.ResourcesLogsDaoImpl;
import org.mycontroller.standalone.db.dao.RoleDao;
import org.mycontroller.standalone.db.dao.RoleDaoImpl;
import org.mycontroller.standalone.db.dao.RoleGatewayMapDao;
import org.mycontroller.standalone.db.dao.RoleGatewayMapDaoImpl;
import org.mycontroller.standalone.db.dao.RoleMqttMapDao;
import org.mycontroller.standalone.db.dao.RoleMqttMapDaoImpl;
import org.mycontroller.standalone.db.dao.RoleNodeMapDao;
import org.mycontroller.standalone.db.dao.RoleNodeMapDaoImpl;
import org.mycontroller.standalone.db.dao.RoleSensorMapDao;
import org.mycontroller.standalone.db.dao.RoleSensorMapDaoImpl;
import org.mycontroller.standalone.db.dao.RoleUserMapDao;
import org.mycontroller.standalone.db.dao.RoleUserMapDaoImpl;
import org.mycontroller.standalone.db.dao.RoomDao;
import org.mycontroller.standalone.db.dao.RoomDaoImpl;
import org.mycontroller.standalone.db.dao.RuleDefinitionDao;
import org.mycontroller.standalone.db.dao.RuleDefinitionDaoImpl;
import org.mycontroller.standalone.db.dao.SensorDao;
import org.mycontroller.standalone.db.dao.SensorDaoImpl;
import org.mycontroller.standalone.db.dao.SensorVariableDao;
import org.mycontroller.standalone.db.dao.SensorVariableDaoImpl;
import org.mycontroller.standalone.db.dao.SensorsVariablesMapDao;
import org.mycontroller.standalone.db.dao.SensorsVariablesMapDaoImpl;
import org.mycontroller.standalone.db.dao.SettingsDao;
import org.mycontroller.standalone.db.dao.SettingsDaoImpl;
import org.mycontroller.standalone.db.dao.TimerDao;
import org.mycontroller.standalone.db.dao.TimerDaoImpl;
import org.mycontroller.standalone.db.dao.UidTagDao;
import org.mycontroller.standalone.db.dao.UidTagDaoImpl;
import org.mycontroller.standalone.db.dao.UserDao;
import org.mycontroller.standalone.db.dao.UserDaoImpl;
import org.mycontroller.standalone.db.dao.UserSettingsDao;
import org.mycontroller.standalone.db.dao.UserSettingsDaoImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class DaoUtils {
    private DaoUtils() {

    }

    private static boolean isDaoInitialized = false;

    private static ExternalServerDao externalServerDao = null;
    private static ExternalServerResourceMapDao externalServerResourceMapDao = null;
    private static FirmwareDao firmwareDao = null;
    private static FirmwareDataDao firmwareDataDao = null;
    private static FirmwareTypeDao firmwareTypeDao = null;
    private static FirmwareVersionDao firmwareVersionDao = null;
    private static ForwardPayloadDao forwardPayloadDao = null;
    private static GatewayDao gatewayDao = null;
    private static MetricsBatteryUsageDao metricsBatteryUsageDao = null;
    private static MetricsBinaryTypeDeviceDao metricsBinaryTypeDeviceDao = null;
    private static MetricsCounterTypeDeviceDao metricsCounterTypeDeviceDao = null;
    private static MetricsDoubleTypeDeviceDao metricsDoubleTypeDeviceDao = null;
    private static MetricsGPSTypeDeviceDao metricsGPSTypeDeviceDao = null;
    private static NodeDao nodeDao = null;
    private static OperationDao operationDao = null;
    private static OperationRuleDefinitionMapDao operationRuleDefinitionMapDao = null;
    private static OperationTimerMapDao operationTimerMapDao = null;
    private static ResourceDao resourceDao = null;
    private static ResourcesGroupDao resourcesGroupDao = null;
    private static ResourcesGroupMapDao resourcesGroupMapDao = null;
    private static ResourcesLogsDao resourcesLogsDao = null;
    private static RoleDao roleDao = null;
    private static RoleGatewayMapDao roleGatewayMapDao = null;
    private static RoleMqttMapDao roleMqttMapDao = null;
    private static RoleNodeMapDao roleNodeMapDao = null;
    private static RoleSensorMapDao roleSensorMapDao = null;
    private static RoleUserMapDao roleUserMapDao = null;
    private static RoomDao roomDao = null;
    private static RuleDefinitionDao ruleDefinitionDao = null;
    private static SensorDao sensorDao = null;
    private static SensorsVariablesMapDao sensorsVariablesMapDao = null;
    private static SensorVariableDao sensorVariableDao = null;
    private static SettingsDao settingsDao = null;
    private static TimerDao timerDao = null;
    private static UidTagDao uidTagDao = null;
    private static UserDao userDao = null;
    private static UserSettingsDao userSettingsDao = null;

    public static void loadAllDao() {
        if (isDaoInitialized) {
            _logger.warn("DAO already initialized. Nothing to do now.");
            return;
        }
        try {
            externalServerDao = new ExternalServerDaoImpl(DataBaseUtils.getConnectionSource());
            externalServerResourceMapDao = new ExternalServerResourceMapDaoImpl(DataBaseUtils.getConnectionSource());
            firmwareDao = new FirmwareDaoImpl(DataBaseUtils.getConnectionSource());
            firmwareDataDao = new FirmwareDataDaoImpl(DataBaseUtils.getConnectionSource());
            firmwareTypeDao = new FirmwareTypeDaoImpl(DataBaseUtils.getConnectionSource());
            firmwareVersionDao = new FirmwareVersionDaoImpl(DataBaseUtils.getConnectionSource());
            forwardPayloadDao = new ForwardPayloadDaoImpl(DataBaseUtils.getConnectionSource());
            gatewayDao = new GatewayDaoImpl(DataBaseUtils.getConnectionSource());
            metricsBatteryUsageDao = new MetricsBatteryUsageDaoImpl(DataBaseUtils.getConnectionSource());
            metricsBinaryTypeDeviceDao = new MetricsBinaryTypeDeviceDaoImpl(DataBaseUtils.getConnectionSource());
            metricsCounterTypeDeviceDao = new MetricsCounterTypeDeviceDaoImpl(DataBaseUtils.getConnectionSource());
            metricsDoubleTypeDeviceDao = new MetricsDoubleTypeDeviceDaoImpl(DataBaseUtils.getConnectionSource());
            metricsGPSTypeDeviceDao = new MetricsGPSTypeDeviceDaoImpl(DataBaseUtils.getConnectionSource());
            nodeDao = new NodeDaoImpl(DataBaseUtils.getConnectionSource());
            operationDao = new OperationDaoImpl(DataBaseUtils.getConnectionSource());
            operationRuleDefinitionMapDao = new OperationRuleDefinitionMapDaoImpl(DataBaseUtils.getConnectionSource());
            operationTimerMapDao = new OperationTimerMapDaoImpl(DataBaseUtils.getConnectionSource());
            resourceDao = new ResourceDaoImpl(DataBaseUtils.getConnectionSource());
            resourcesGroupDao = new ResourcesGroupDaoImpl(DataBaseUtils.getConnectionSource());
            resourcesGroupMapDao = new ResourcesGroupMapDaoImpl(DataBaseUtils.getConnectionSource());
            resourcesLogsDao = new ResourcesLogsDaoImpl(DataBaseUtils.getConnectionSource());
            roleDao = new RoleDaoImpl(DataBaseUtils.getConnectionSource());
            roleGatewayMapDao = new RoleGatewayMapDaoImpl(DataBaseUtils.getConnectionSource());
            roleMqttMapDao = new RoleMqttMapDaoImpl(DataBaseUtils.getConnectionSource());
            roleNodeMapDao = new RoleNodeMapDaoImpl(DataBaseUtils.getConnectionSource());
            roleSensorMapDao = new RoleSensorMapDaoImpl(DataBaseUtils.getConnectionSource());
            roleUserMapDao = new RoleUserMapDaoImpl(DataBaseUtils.getConnectionSource());
            roomDao = new RoomDaoImpl(DataBaseUtils.getConnectionSource());
            ruleDefinitionDao = new RuleDefinitionDaoImpl(DataBaseUtils.getConnectionSource());
            sensorDao = new SensorDaoImpl(DataBaseUtils.getConnectionSource());
            sensorsVariablesMapDao = new SensorsVariablesMapDaoImpl(DataBaseUtils.getConnectionSource());
            sensorVariableDao = new SensorVariableDaoImpl(DataBaseUtils.getConnectionSource());
            settingsDao = new SettingsDaoImpl(DataBaseUtils.getConnectionSource());
            timerDao = new TimerDaoImpl(DataBaseUtils.getConnectionSource());
            uidTagDao = new UidTagDaoImpl(DataBaseUtils.getConnectionSource());
            userDao = new UserDaoImpl(DataBaseUtils.getConnectionSource());
            userSettingsDao = new UserSettingsDaoImpl(DataBaseUtils.getConnectionSource());
            //Initialized dao
            isDaoInitialized = true;
        } catch (SQLException sqlEx) {
            _logger.error("Unable to load Dao,", sqlEx);
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

    public static MetricsGPSTypeDeviceDao getMetricsGPSTypeDeviceDao() {
        return metricsGPSTypeDeviceDao;
    }

    public static UserDao getUserDao() {
        return userDao;
    }

    public static MetricsBinaryTypeDeviceDao getMetricsBinaryTypeDeviceDao() {
        return metricsBinaryTypeDeviceDao;
    }

    public static RuleDefinitionDao getRuleDefinitionDao() {
        return ruleDefinitionDao;
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

    public static RoleDao getRoleDao() {
        return roleDao;
    }

    public static RoleUserMapDao getRoleUserMapDao() {
        return roleUserMapDao;
    }

    public static RoleGatewayMapDao getRoleGatewayMapDao() {
        return roleGatewayMapDao;
    }

    public static RoleNodeMapDao getRoleNodeMapDao() {
        return roleNodeMapDao;
    }

    public static RoleSensorMapDao getRoleSensorMapDao() {
        return roleSensorMapDao;
    }

    public static RoleMqttMapDao getRoleMqttMapDao() {
        return roleMqttMapDao;
    }

    public static synchronized boolean isDaoInitialized() {
        return isDaoInitialized;
    }

    public static synchronized void setIsDaoInitialized(boolean state) {
        isDaoInitialized = state;
    }

    public static OperationDao getOperationDao() {
        return operationDao;
    }

    public static OperationRuleDefinitionMapDao getOperationRuleDefinitionMapDao() {
        return operationRuleDefinitionMapDao;
    }

    public static OperationTimerMapDao getOperationTimerMapDao() {
        return operationTimerMapDao;
    }

    public static RoomDao getRoomDao() {
        return roomDao;
    }

    public static MetricsCounterTypeDeviceDao getMetricsCounterTypeDeviceDao() {
        return metricsCounterTypeDeviceDao;
    }

    public static ExternalServerDao getExternalServerTableDao() {
        return externalServerDao;
    }

    public static ExternalServerResourceMapDao getExternalServerResourceMapDao() {
        return externalServerResourceMapDao;
    }

    public static ResourceDao getResourceDao() {
        return resourceDao;
    }

    public static FirmwareDataDao getFirmwareDataDao() {
        return firmwareDataDao;
    }

}
