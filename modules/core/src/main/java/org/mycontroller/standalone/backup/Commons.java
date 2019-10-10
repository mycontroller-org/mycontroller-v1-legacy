/*
 * Copyright 2015-2019 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.backup;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ExternalServerResourceMap;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareData;
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsCounterTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsGPSTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.OperationRuleDefinitionMap;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.OperationTimerMap;
import org.mycontroller.standalone.db.tables.Resource;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.RoleGatewayMap;
import org.mycontroller.standalone.db.tables.RoleMqttMap;
import org.mycontroller.standalone.db.tables.RoleNodeMap;
import org.mycontroller.standalone.db.tables.RoleSensorMap;
import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.SensorsVariablesMap;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.db.tables.UserSettings;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class Commons {

    // common
    public static final String APP_PROPERTIES_FILENAME = "mycontroller.properties";
    public static final String APP_CONF_LOCATION = "../conf/";
    public static final String RESOURCES_LOCATION = "resources" + File.separator;

    // backup
    public static final String BACKUP_DATABASE_FILENAME = "database_backup.zip";
    public static final String BACKUP_FILE_NAME_IDENTITY = "_mc_backup";
    public static final AtomicBoolean IS_BACKUP_RESTORE_RUNNING = new AtomicBoolean(false);

    // export    
    public static final String DATABASE_FILES = "database_tables";
    public static final String EXPORT_FILE_NAME_IDENTITY = "_mc_export";
    public static final AtomicBoolean IS_IMPORT_EXPORT_RUNNING = new AtomicBoolean(false);
    public static final Map<String, ExportMap> EXPORT_MAP = new HashMap<>();
    static {
        EXPORT_MAP.put(
                "01_role",
                ExportMap.get("role_data",
                        Role.class,
                        DaoUtils.getRoleDao()));
        EXPORT_MAP.put(
                "02_user",
                ExportMap.get("user_data",
                        User.class,
                        DaoUtils.getUserDao()));
        EXPORT_MAP.put(
                "03_user_settings",
                ExportMap.get("user_settings_data",
                        UserSettings.class,
                        DaoUtils.getUserSettingsDao()));
        EXPORT_MAP.put(
                "04_role_user_map",
                ExportMap.get("role_user_map_data",
                        RoleUserMap.class,
                        DaoUtils.getRoleUserMapDao(),
                        RoleUserMap.KEY_ROLE_ID));
        EXPORT_MAP.put(
                "05_gateway",
                ExportMap.get("gateway_data",
                        GatewayTable.class,
                        DaoUtils.getGatewayDao()));
        EXPORT_MAP.put(
                "06_node",
                ExportMap.get("node_data",
                        Node.class,
                        DaoUtils.getNodeDao()));
        EXPORT_MAP.put(
                "07_sensor",
                ExportMap.get("sensor_data",
                        Sensor.class,
                        DaoUtils.getSensorDao()));
        EXPORT_MAP.put(
                "08_sensor_variable",
                ExportMap.get("sensor_variable_data",
                        SensorVariable.class,
                        DaoUtils.getSensorVariableDao()));
        EXPORT_MAP.put(
                "09_sensor_variable_map",
                ExportMap.get("sensor_variable_map_data",
                        SensorsVariablesMap.class,
                        DaoUtils.getSensorsVariablesMapDao()));
        EXPORT_MAP.put(
                "10_ext_server",
                ExportMap.get("external_server_data",
                        ExternalServerTable.class,
                        DaoUtils.getExternalServerTableDao()));
        EXPORT_MAP.put(
                "11_ext_server_resource_map",
                ExportMap.get("external_server_resource_map_data",
                        ExternalServerResourceMap.class,
                        DaoUtils.getExternalServerResourceMapDao(),
                        ExternalServerResourceMap.KEY_RESOURCE_ID));
        EXPORT_MAP.put(
                "12_firmware_type",
                ExportMap.get("firmware_type_data",
                        FirmwareType.class,
                        DaoUtils.getFirmwareTypeDao()));
        EXPORT_MAP.put(
                "13_firmware_version",
                ExportMap.get("firmware_version_data",
                        FirmwareVersion.class,
                        DaoUtils.getFirmwareVersionDao()));
        EXPORT_MAP.put(
                "14_firmware",
                ExportMap.get("firmware_data",
                        Firmware.class,
                        DaoUtils.getFirmwareDao()));
        EXPORT_MAP.put(
                "15_firmware_bytes",
                ExportMap.get("firmware_bytes_data",
                        FirmwareData.class,
                        DaoUtils.getFirmwareDataDao()));
        EXPORT_MAP.put(
                "16_forward_payload",
                ExportMap.get("forward_payload_data",
                        ForwardPayload.class,
                        DaoUtils.getForwardPayloadDao()));
        EXPORT_MAP.put(
                "17_operation",
                ExportMap.get("operation_data",
                        OperationTable.class,
                        DaoUtils.getOperationDao()));
        EXPORT_MAP.put(
                "18_rule_definition",
                ExportMap.get("rule_definition_data",
                        RuleDefinitionTable.class,
                        DaoUtils.getRuleDefinitionDao()));
        EXPORT_MAP.put(
                "19_timer",
                ExportMap.get("timer_data",
                        Timer.class,
                        DaoUtils.getTimerDao()));
        EXPORT_MAP.put(
                "20_operation_rule_def_map",
                ExportMap.get("operation_rule_definition_map_data",
                        OperationRuleDefinitionMap.class,
                        DaoUtils.getOperationRuleDefinitionMapDao(),
                        OperationRuleDefinitionMap.KEY_OPERATION_ID));
        EXPORT_MAP.put(
                "21_operation_timer_map",
                ExportMap.get("operation_timer_map_data",
                        OperationTimerMap.class,
                        DaoUtils.getOperationTimerMapDao(),
                        OperationTimerMap.KEY_OPERATION_ID));
        EXPORT_MAP.put(
                "22_resource",
                ExportMap.get("resource_data",
                        Resource.class,
                        DaoUtils.getResourceDao()));
        EXPORT_MAP.put(
                "23_resource_group",
                ExportMap.get("resources_group_data",
                        ResourcesGroup.class,
                        DaoUtils.getResourcesGroupDao()));
        EXPORT_MAP.put(
                "24_resource_group-map",
                ExportMap.get("resources_group_map_table",
                        ResourcesGroup.class,
                        DaoUtils.getResourcesGroupMapDao()));
        EXPORT_MAP.put(
                "25_resource_logs",
                ExportMap.get("resources_logs_data",
                        ResourcesLogs.class,
                        DaoUtils.getResourcesLogsDao()));
        EXPORT_MAP.put(
                "26_role_gateway_map",
                ExportMap.get("role_gateway_map_data",
                        RoleGatewayMap.class,
                        DaoUtils.getRoleGatewayMapDao(),
                        RoleGatewayMap.KEY_ROLE_ID));
        EXPORT_MAP.put(
                "27_role_mqtt_map",
                ExportMap.get("role_mqtt_map_data",
                        RoleMqttMap.class,
                        DaoUtils.getRoleMqttMapDao(),
                        RoleMqttMap.KEY_ROLE_ID));
        EXPORT_MAP.put(
                "28_role_node_map",
                ExportMap.get("role_node_map_data",
                        RoleNodeMap.class,
                        DaoUtils.getRoleNodeMapDao(),
                        RoleNodeMap.KEY_ROLE_ID));
        EXPORT_MAP.put(
                "29_role_sensor_map",
                ExportMap.get("role_sensor_map_data",
                        RoleSensorMap.class,
                        DaoUtils.getRoleSensorMapDao(),
                        RoleSensorMap.KEY_ROLE_ID));
        EXPORT_MAP.put(
                "30_room",
                ExportMap.get("room_data",
                        Room.class,
                        DaoUtils.getRoomDao()));
        EXPORT_MAP.put(
                "31_settings",
                ExportMap.get("settings_data",
                        Settings.class,
                        DaoUtils.getSettingsDao()));
        EXPORT_MAP.put(
                "32_uid_tag",
                ExportMap.get("uid_tag_data",
                        UidTag.class,
                        DaoUtils.getUidTagDao()));
        EXPORT_MAP.put(
                "33_metrics_battery_usage",
                ExportMap.get("metrics_battery_usage_data",
                        MetricsBatteryUsage.class,
                        DaoUtils.getMetricsBatteryUsageDao(),
                        MetricsBatteryUsage.KEY_TIMESTAMP));

        EXPORT_MAP.put(
                "34_metrics_binary_type",
                ExportMap.get("metrics_binary_type_data",
                        MetricsBinaryTypeDevice.class,
                        DaoUtils.getMetricsBinaryTypeDeviceDao(),
                        MetricsBinaryTypeDevice.KEY_TIMESTAMP));
        EXPORT_MAP.put(
                "35_metrics_counter_type",
                ExportMap.get("metrics_counter_type_data",
                        MetricsCounterTypeDevice.class,
                        DaoUtils.getMetricsCounterTypeDeviceDao(),
                        MetricsCounterTypeDevice.KEY_TIMESTAMP));
        EXPORT_MAP.put(
                "36_metrics_double_type",
                ExportMap.get("metrics_double_type_data",
                        MetricsDoubleTypeDevice.class,
                        DaoUtils.getMetricsDoubleTypeDeviceDao(),
                        MetricsDoubleTypeDevice.KEY_TIMESTAMP));
        EXPORT_MAP.put(
                "37_metrics_gps_type",
                ExportMap.get("metrics_gps_type_data",
                        MetricsGPSTypeDevice.class,
                        DaoUtils.getMetricsGPSTypeDeviceDao(),
                        MetricsGPSTypeDevice.KEY_TIMESTAMP));

    }

}
