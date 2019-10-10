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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.backup.mixins.MixinExternalServerResourceMap;
import org.mycontroller.standalone.backup.mixins.MixinFirmware;
import org.mycontroller.standalone.backup.mixins.MixinFirmwareType;
import org.mycontroller.standalone.backup.mixins.MixinFirmwareVersion;
import org.mycontroller.standalone.backup.mixins.MixinForwardPayload;
import org.mycontroller.standalone.backup.mixins.MixinMetricsBatteryUsage;
import org.mycontroller.standalone.backup.mixins.MixinMetricsData;
import org.mycontroller.standalone.backup.mixins.MixinNode;
import org.mycontroller.standalone.backup.mixins.MixinOperationRuleDefinitionMap;
import org.mycontroller.standalone.backup.mixins.MixinOperationTable;
import org.mycontroller.standalone.backup.mixins.MixinOperationTimerMap;
import org.mycontroller.standalone.backup.mixins.MixinResource;
import org.mycontroller.standalone.backup.mixins.MixinResourcesLogs;
import org.mycontroller.standalone.backup.mixins.MixinRoleGatewayMap;
import org.mycontroller.standalone.backup.mixins.MixinRoleMqttMap;
import org.mycontroller.standalone.backup.mixins.MixinRoleNodeMap;
import org.mycontroller.standalone.backup.mixins.MixinRoleSensorMap;
import org.mycontroller.standalone.backup.mixins.MixinRoleUserMap;
import org.mycontroller.standalone.backup.mixins.MixinRoom;
import org.mycontroller.standalone.backup.mixins.MixinSensor;
import org.mycontroller.standalone.backup.mixins.MixinSensorVariable;
import org.mycontroller.standalone.backup.mixins.MixinTimer;
import org.mycontroller.standalone.backup.mixins.MixinUidTag;
import org.mycontroller.standalone.backup.mixins.MixinUser;
import org.mycontroller.standalone.db.tables.ExternalServerResourceMap;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.db.tables.ForwardPayload;
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
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.db.tables.RoleGatewayMap;
import org.mycontroller.standalone.db.tables.RoleMqttMap;
import org.mycontroller.standalone.db.tables.RoleNodeMap;
import org.mycontroller.standalone.db.tables.RoleSensorMap;
import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.db.tables.User;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */

@Slf4j
public class JsonUtils {
    private static final String ENCODING = "UTF-8";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MAPPER.addMixIn(User.class, MixinUser.class);
        MAPPER.addMixIn(Resource.class, MixinResource.class);
        MAPPER.addMixIn(Node.class, MixinNode.class);
        MAPPER.addMixIn(MetricsBinaryTypeDevice.class, MixinMetricsData.class);
        MAPPER.addMixIn(MetricsDoubleTypeDevice.class, MixinMetricsData.class);
        MAPPER.addMixIn(MetricsCounterTypeDevice.class, MixinMetricsData.class);
        MAPPER.addMixIn(MetricsGPSTypeDevice.class, MixinMetricsData.class);
        MAPPER.addMixIn(MetricsBatteryUsage.class, MixinMetricsBatteryUsage.class);
        MAPPER.addMixIn(ExternalServerResourceMap.class, MixinExternalServerResourceMap.class);
        MAPPER.addMixIn(Firmware.class, MixinFirmware.class);
        MAPPER.addMixIn(FirmwareType.class, MixinFirmwareType.class);
        MAPPER.addMixIn(FirmwareVersion.class, MixinFirmwareVersion.class);
        MAPPER.addMixIn(ForwardPayload.class, MixinForwardPayload.class);
        MAPPER.addMixIn(OperationTable.class, MixinOperationTable.class);
        MAPPER.addMixIn(OperationRuleDefinitionMap.class, MixinOperationRuleDefinitionMap.class);
        MAPPER.addMixIn(OperationTimerMap.class, MixinOperationTimerMap.class);
        MAPPER.addMixIn(ResourcesLogs.class, MixinResourcesLogs.class);
        MAPPER.addMixIn(RoleGatewayMap.class, MixinRoleGatewayMap.class);
        MAPPER.addMixIn(RoleMqttMap.class, MixinRoleMqttMap.class);
        MAPPER.addMixIn(RoleNodeMap.class, MixinRoleNodeMap.class);
        MAPPER.addMixIn(RoleSensorMap.class, MixinRoleSensorMap.class);
        MAPPER.addMixIn(RoleUserMap.class, MixinRoleUserMap.class);
        MAPPER.addMixIn(Room.class, MixinRoom.class);
        MAPPER.addMixIn(Sensor.class, MixinSensor.class);
        MAPPER.addMixIn(SensorVariable.class, MixinSensorVariable.class);
        MAPPER.addMixIn(Timer.class, MixinTimer.class);
        MAPPER.addMixIn(UidTag.class, MixinUidTag.class);
    }

    public static void dumps(Object data, String... names) {
        try {
            String stringData = MAPPER.writeValueAsString(data);
            File file = FileUtils.getFile(names);
            FileUtils.write(file, stringData, ENCODING);
            _logger.trace("Json saved: {}", file.getAbsolutePath());
        } catch (IOException ex) {
            _logger.error("Exception, fileName:{}", names, ex);
        }
    }

    public static Object loads(Class<?> clazz, String... names) {
        return loads(clazz, FileUtils.getFile(names));
    }

    public static Object loads(Class<?> clazz, File file) {
        try {
            String content = FileUtils.readFileToString(file, ENCODING);
            return MAPPER.readValue(content, clazz);
        } catch (IOException ex) {
            _logger.error("Exception when loading {}", file.getAbsolutePath(), ex);
        }
        return null;
    }

    public static Object loads(JavaType javaType, String... names) {
        return loads(javaType, FileUtils.getFile(names));
    }

    public static Object loads(JavaType javaType, File file) {
        try {
            String content = FileUtils.readFileToString(file, ENCODING);
            return MAPPER.readValue(content, javaType);
        } catch (IOException ex) {
            _logger.error("Exception when loading {}", file.getAbsolutePath(), ex);
        }
        return null;
    }
}