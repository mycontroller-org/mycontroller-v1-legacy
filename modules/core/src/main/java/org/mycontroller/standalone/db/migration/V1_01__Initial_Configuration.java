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
package org.mycontroller.standalone.db.migration;

import java.sql.Connection;
import java.util.ArrayList;

import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.mycontroller.standalone.AppProperties.MC_TIME_FORMAT;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.auth.AuthUtils.PERMISSION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.jobs.NodeAliveStatusJob;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.settings.EmailSettings;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsGraph.CHART_TYPE;
import org.mycontroller.standalone.settings.MetricsGraphSettings;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.settings.MySensorsSettings;
import org.mycontroller.standalone.settings.SmsSettings;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_01__Initial_Configuration extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        loadDao();

        //Update metrics settings
        ArrayList<MetricsGraph> metrics = new ArrayList<MetricsGraph>();
        for (MESSAGE_TYPE_SET_REQ type : MetricsGraph.variables) {
            if (McMessageUtils.getMetricType(type) == METRIC_TYPE.BINARY) {
                metrics.add(MetricsGraph.builder()
                        .metricName(type.getText())
                        .type(CHART_TYPE.LINE_CHART.getText())
                        .interpolate("step-after")
                        .color("#ff7f0e")
                        .subType("line")
                        .build());
            } else if (McMessageUtils.getMetricType(type) == METRIC_TYPE.DOUBLE) {
                metrics.add(MetricsGraph.builder()
                        .metricName(type.getText())
                        .type(CHART_TYPE.LINE_CHART.getText())
                        .interpolate("linear")
                        .color("#ff7f0e")
                        .subType("line")
                        .build());
            } else {
                metrics.add(MetricsGraph.builder()
                        .metricName(type.getText())
                        .type(CHART_TYPE.LINE_CHART.getText())
                        .interpolate("linear")
                        .color("#ff7f0e")
                        .subType("line")
                        .build());
            }
        }
        MetricsGraph batteryGrapth = MetricsGraph.builder()
                .metricName(MetricsGraphSettings.SKEY_BATTERY)
                .type(CHART_TYPE.LINE_CHART.getText())
                .interpolate("linear")
                .color("#ff7f0e")
                .subType("line")
                .build();

        //Update Metrics reference data
        MetricsGraphSettings.builder()
                .enabledMinMax(true)
                .defaultTimeRange(McUtils.ONE_HOUR * 6)
                .metrics(metrics)
                .battery(batteryGrapth)
                .build().save();

        //Update Metrics retention data reference
        MetricsDataRetentionSettings.builder()
                .lastAggregationRawData(System.currentTimeMillis())
                .lastAggregationOneMinute(System.currentTimeMillis())
                .lastAggregationFiveMinutes(System.currentTimeMillis())
                .lastAggregationOneHour(System.currentTimeMillis())
                .lastAggregationSixHours(System.currentTimeMillis())
                .lastAggregationTwelveHours(System.currentTimeMillis())
                .lastAggregationOneDay(System.currentTimeMillis())
                .build().updateInternal();

        //Update location settings
        LocationSettings.builder()
                .name("Namakkal")
                .latitude("11.2333")
                .longitude("78.1667").build().save();

        LocationSettings.builder()
                .sunriseTime(0L)
                .sunsetTime(0L).build().updateInternal();

        //Update MySensors settings
        MySensorsSettings.builder()
                .defaultFirmware(null)
                .enbaledDefaultOnNoFirmware(false).build().save();

        //Update email settings
        EmailSettings.builder()
                .smtpHost(null)
                .smtpPort(null)
                .enableSsl(false)
                .fromAddress(null)
                .smtpUsername(null)
                .smtpPassword(null).build().save();

        //Update SMS settings
        SmsSettings.builder()
                .vendor(null)
                .authSid(null)
                .authToken(null)
                .fromNumber(null).build().save();

        //Add super admin role
        DaoUtils.getRoleDao().create(Role.builder()
                .name("Super admin role")
                .description("created by system on installation")
                .permission(PERMISSION_TYPE.SUPER_ADMIN)
                .build());

        // Add default User
        DaoUtils.getUserDao().create(User.builder()
                .username("admin")
                .enabled(true)
                .fullName("Admin")
                .email("admin@localhost.com")
                .password("admin")
                .build());
        //Get created role
        Role superAdminRole = DaoUtils.getRoleDao().getByRoleName("Super admin role");
        //Get created user
        User adminUser = DaoUtils.getUserDao().getByUsername("admin");
        //Map role and user
        DaoUtils.getRoleUserMapDao().create(RoleUserMap.builder()
                .user(adminUser)
                .role(superAdminRole)
                .build());

        //Update controller default settings
        MyControllerSettings.builder()
                .language(MC_LANGUAGE.EN_US.getText())
                .aliveCheckInterval(NodeAliveStatusJob.DEFAULT_ALIVE_CHECK_INTERVAL)
                .timeFormat(MC_TIME_FORMAT.HOURS_12.getText())
                .unitConfig(UNIT_CONFIG.METRIC.getText())
                .loginMessage("Default username: <b>admin</b>, password: <b>admin<b>")
                .grantAccessToChildResources(true)
                .resourcesLogLevel(LOG_LEVEL.NOTICE.getText())
                .globalPageRefreshTime(McUtils.ONE_SECOND * 30)
                .build().save();

        // Update Sensor Type and Variables Type mapping

        // Door sensor, V_TRIPPED, V_ARMED
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DOOR, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DOOR, MESSAGE_TYPE_SET_REQ.V_ARMED);

        // Motion sensor, V_TRIPPED, V_ARMED
        DataBaseUtils
                .createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOTION, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOTION, MESSAGE_TYPE_SET_REQ.V_ARMED);

        // Smoke sensor, V_TRIPPED, V_ARMED
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SMOKE, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SMOKE, MESSAGE_TYPE_SET_REQ.V_ARMED);

        // Binary light or relay, V_STATUS (or V_LIGHT), V_WATT
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BINARY, MESSAGE_TYPE_SET_REQ.V_STATUS);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BINARY, MESSAGE_TYPE_SET_REQ.V_WATT);

        // Dimmable light or fan device, V_STATUS (on/off),
        // V_DIMMER(V_PERCENTAGE) (dimmer level 0-100), V_WATT
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DIMMER, MESSAGE_TYPE_SET_REQ.V_STATUS);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DIMMER,
                MESSAGE_TYPE_SET_REQ.V_PERCENTAGE);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DIMMER, MESSAGE_TYPE_SET_REQ.V_WATT);

        // Blinds or window cover, V_UP, V_DOWN, V_STOP, V_DIMMER
        // (open/close to a percentage)
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER, MESSAGE_TYPE_SET_REQ.V_UP);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER, MESSAGE_TYPE_SET_REQ.V_DOWN);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER, MESSAGE_TYPE_SET_REQ.V_STOP);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COVER,
                MESSAGE_TYPE_SET_REQ.V_PERCENTAGE);

        // Temperature sensor, V_TEMP
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_TEMP, MESSAGE_TYPE_SET_REQ.V_TEMP);

        // Humidity sensor, V_HUM
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HUM, MESSAGE_TYPE_SET_REQ.V_HUM);

        // Barometer sensor, V_PRESSURE, V_FORECAST
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BARO, MESSAGE_TYPE_SET_REQ.V_PRESSURE);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_BARO, MESSAGE_TYPE_SET_REQ.V_FORECAST);

        // Wind sensor, V_WIND, V_GUST
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WIND, MESSAGE_TYPE_SET_REQ.V_WIND);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WIND, MESSAGE_TYPE_SET_REQ.V_GUST);

        // Rain sensor, V_RAIN, V_RAINRATE
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RAIN, MESSAGE_TYPE_SET_REQ.V_RAIN);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RAIN, MESSAGE_TYPE_SET_REQ.V_RAINRATE);

        // Uv sensor, V_UV
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_UV, MESSAGE_TYPE_SET_REQ.V_UV);

        // Personal scale sensor, V_WEIGHT, V_IMPEDANCE
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WEIGHT, MESSAGE_TYPE_SET_REQ.V_WEIGHT);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WEIGHT,
                MESSAGE_TYPE_SET_REQ.V_IMPEDANCE);

        // Power meter, V_WATT, V_KWH
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_POWER, MESSAGE_TYPE_SET_REQ.V_WATT);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_POWER, MESSAGE_TYPE_SET_REQ.V_KWH);

        // Header device, V_HVAC_SETPOINT_HEAT, V_HVAC_FLOW_STATE, V_TEMP
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HEATER,
                MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_HEAT);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HEATER,
                MESSAGE_TYPE_SET_REQ.V_HVAC_FLOW_STATE);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HEATER, MESSAGE_TYPE_SET_REQ.V_TEMP);

        // Distance sensor, V_DISTANCE
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DISTANCE,
                MESSAGE_TYPE_SET_REQ.V_DISTANCE);

        // Light level sensor, V_LIGHT_LEVEL (uncalibrated in percentage),
        // V_LEVEL (light level in lux)
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_LIGHT_LEVEL,
                MESSAGE_TYPE_SET_REQ.V_LIGHT_LEVEL);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_LIGHT_LEVEL,
                MESSAGE_TYPE_SET_REQ.V_LEVEL);

        // Lock device, V_LOCK_STATUS
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_LOCK,
                MESSAGE_TYPE_SET_REQ.V_LOCK_STATUS);

        // Ir device, V_IR_SEND, V_IR_RECEIVE
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_IR, MESSAGE_TYPE_SET_REQ.V_IR_SEND);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_IR, MESSAGE_TYPE_SET_REQ.V_IR_RECEIVE);

        // Water meter, V_FLOW, V_VOLUME
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER, MESSAGE_TYPE_SET_REQ.V_FLOW);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER, MESSAGE_TYPE_SET_REQ.V_VOLUME);

        // Air quality sensor, V_LEVEL
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_AIR_QUALITY,
                MESSAGE_TYPE_SET_REQ.V_LEVEL);

        // Custom sensor
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR1);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR2);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR3);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR4);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_CUSTOM, MESSAGE_TYPE_SET_REQ.V_VAR5);

        // Dust sensor, V_LEVEL
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_DUST, MESSAGE_TYPE_SET_REQ.V_LEVEL);

        // Scene controller device, V_SCENE_ON, V_SCENE_OFF.
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SCENE_CONTROLLER,
                MESSAGE_TYPE_SET_REQ.V_SCENE_ON);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SCENE_CONTROLLER,
                MESSAGE_TYPE_SET_REQ.V_SCENE_OFF);

        // RGB light. Send color component data using V_RGB. Also supports
        // V_WATT
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGB_LIGHT, MESSAGE_TYPE_SET_REQ.V_RGB);
        DataBaseUtils
                .createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGB_LIGHT, MESSAGE_TYPE_SET_REQ.V_WATT);

        // RGB light with an additional White component. Send data using
        // V_RGBW. Also supports V_WATT
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGBW_LIGHT,
                MESSAGE_TYPE_SET_REQ.V_RGBW);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_RGBW_LIGHT,
                MESSAGE_TYPE_SET_REQ.V_WATT);

        // Color sensor, send color information using V_RGB
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_COLOR_SENSOR,
                MESSAGE_TYPE_SET_REQ.V_RGB);

        // Thermostat/HVAC device. V_HVAC_SETPOINT_HEAT,
        // V_HVAC_SETPOINT_COOL, V_HVAC_FLOW_STATE, V_HVAC_FLOW_MODE, V_TEMP
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC,
                MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_HEAT);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC,
                MESSAGE_TYPE_SET_REQ.V_HVAC_SETPOINT_COOL);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC,
                MESSAGE_TYPE_SET_REQ.V_HVAC_FLOW_STATE);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC,
                MESSAGE_TYPE_SET_REQ.V_HVAC_FLOW_MODE);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_HVAC,
                MESSAGE_TYPE_SET_REQ.V_HVAC_SPEED);
        // Multimeter device, V_VOLTAGE, V_CURRENT, V_IMPEDANCE
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MULTIMETER,
                MESSAGE_TYPE_SET_REQ.V_VOLTAGE);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MULTIMETER,
                MESSAGE_TYPE_SET_REQ.V_CURRENT);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MULTIMETER,
                MESSAGE_TYPE_SET_REQ.V_IMPEDANCE);

        // Sprinkler, V_STATUS (turn on/off), V_TRIPPED (if fire detecting
        // device)
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SPRINKLER,
                MESSAGE_TYPE_SET_REQ.V_STATUS);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SPRINKLER,
                MESSAGE_TYPE_SET_REQ.V_TRIPPED);

        // Water leak sensor, V_TRIPPED, V_ARMED
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER_LEAK,
                MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_WATER_LEAK,
                MESSAGE_TYPE_SET_REQ.V_ARMED);

        // Sound sensor, V_TRIPPED, V_ARMED, V_LEVEL (sound level in dB)
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SOUND, MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SOUND, MESSAGE_TYPE_SET_REQ.V_ARMED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_SOUND, MESSAGE_TYPE_SET_REQ.V_LEVEL);

        // Vibration sensor, V_TRIPPED, V_ARMED, V_LEVEL (vibration in Hz)
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_VIBRATION,
                MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_VIBRATION,
                MESSAGE_TYPE_SET_REQ.V_ARMED);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_VIBRATION,
                MESSAGE_TYPE_SET_REQ.V_LEVEL);

        // Moisture sensor, V_TRIPPED, V_ARMED, V_LEVEL (water content or
        // moisture in percentage?)
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOISTURE,
                MESSAGE_TYPE_SET_REQ.V_TRIPPED);
        DataBaseUtils
                .createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOISTURE, MESSAGE_TYPE_SET_REQ.V_ARMED);
        DataBaseUtils
                .createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_MOISTURE, MESSAGE_TYPE_SET_REQ.V_LEVEL);

        // LCD text device / Simple information device on controller, V_TEXT
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_INFO, MESSAGE_TYPE_SET_REQ.V_TEXT);

        // Gas meter, V_FLOW, V_VOLUME
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_GAS, MESSAGE_TYPE_SET_REQ.V_FLOW);
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_GAS, MESSAGE_TYPE_SET_REQ.V_VOLUME);

        //GPS
        DataBaseUtils.createSensorsVariablesMap(MESSAGE_TYPE_PRESENTATION.S_GPS, MESSAGE_TYPE_SET_REQ.V_POSITION);

        _logger.info("Migration completed successfully.");
    }
}
