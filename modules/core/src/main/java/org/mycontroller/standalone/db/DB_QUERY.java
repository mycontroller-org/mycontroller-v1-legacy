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

import java.text.MessageFormat;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsCounterTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class DB_QUERY {
    public static final String ORDER_BY_NODE_EUI = MessageFormat
            .format("SELECT ${0}$ FROM ${1}$ WHERE id=${2}$ ", Node.KEY_EUI, DB_TABLES.NODE, Sensor.KEY_NODE_ID);
    public static final String ORDER_BY_NODE_NAME = MessageFormat
            .format("SELECT ${0}$ FROM ${1}$ WHERE id=${2}$ ", Node.KEY_NAME, DB_TABLES.NODE, Sensor.KEY_NODE_ID);

    public static final String INSERT_METRICS_DOUBLE_AGGREGATION_BY_TYPE = MessageFormat
            .format("INSERT INTO ${5}$ (${0}$, ${1}$, ${2}$, ${3}$, ${4}$, ${7}$, ${6}$) SELECT ${0}$, MIN(${1}$) "
                    + "AS ${1}$, MAX(${2}$) AS ${2}$, SUM(${3}$*${4}$)/SUM(${4}$) AS ${3}$, SUM(${4}$) "
                    + "AS ${4}$, '{2}' AS ${7}$, '{3}' AS ${6}$ FROM ${5}$ "
                    + "WHERE ${6}$='{0}' AND ${7}$ > '{1}' AND ${7}$ <= '{2}' GROUP BY ${0}$ ",
                    MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID,
                    MetricsDoubleTypeDevice.KEY_MIN,
                    MetricsDoubleTypeDevice.KEY_MAX,
                    MetricsDoubleTypeDevice.KEY_AVG,
                    MetricsDoubleTypeDevice.KEY_SAMPLES,
                    DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE,
                    MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE,
                    MetricsDoubleTypeDevice.KEY_TIMESTAMP);

    public static final String INSERT_METRICS_BATTERY_AGGREGATION_BY_TYPE = MessageFormat
            .format("INSERT INTO ${5}$ (${0}$, ${1}$, ${2}$, ${3}$, ${4}$, ${7}$, ${6}$) SELECT ${0}$, MIN(${1}$) "
                    + "AS ${1}$, MAX(${2}$) AS ${2}$, SUM(${3}$*${4}$)/SUM(${4}$) AS ${3}$, SUM(${4}$) "
                    + "AS ${4}$, '{2}' AS ${7}$, '{3}' AS ${6}$ FROM ${5}$ "
                    + "WHERE ${6}$='{0}' AND ${7}$ > '{1}' AND ${7}$ <= '{2}' GROUP BY ${0}$ ",
                    MetricsBatteryUsage.KEY_NODE_ID,
                    MetricsBatteryUsage.KEY_MIN,
                    MetricsBatteryUsage.KEY_MAX,
                    MetricsBatteryUsage.KEY_AVG,
                    MetricsBatteryUsage.KEY_SAMPLES,
                    DB_TABLES.METRICS_BATTERY_USAGE,
                    MetricsBatteryUsage.KEY_AGGREGATION_TYPE,
                    MetricsBatteryUsage.KEY_TIMESTAMP);

    public static final String INSERT_METRICS_COUNTER_AGGREGATION_BY_TYPE = MessageFormat
            .format("INSERT INTO ${3}$ (${0}$, ${1}$, ${2}$, ${5}$, ${4}$) SELECT ${0}$, SUM(${1}$) "
                    + "AS ${1}$, SUM(${2}$) AS ${2}$, '{2}' AS ${5}$, '''''{3}''''' AS ${4}$ FROM ${3}$ "
                    + "WHERE ${4}$='''''{0}''''' AND ${5}$ > '{1}' AND ${5}$ <= '{2}' GROUP BY ${0}$ ",
                    MetricsCounterTypeDevice.KEY_SENSOR_VARIABLE_ID,
                    MetricsCounterTypeDevice.KEY_VALUE,
                    MetricsCounterTypeDevice.KEY_SAMPLES,
                    DB_TABLES.METRICS_COUNTER_TYPE_DEVICE,
                    MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE,
                    MetricsCounterTypeDevice.KEY_TIMESTAMP);

    public static final String DELETE_METRICS_DOUBLE_BY_TYPE = MessageFormat
            .format("DELETE FROM ${0}$ WHERE ${1}$='{0}' AND ${2}$ <= '{1}' ",
                    DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE,
                    MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE,
                    MetricsCounterTypeDevice.KEY_TIMESTAMP);

    public static final String DELETE_METRICS_BATTERY_BY_TYPE = MessageFormat
            .format("DELETE FROM ${0}$ WHERE ${1}$='{0}' AND ${2}$ <= '{1}' ",
                    DB_TABLES.METRICS_BATTERY_USAGE,
                    MetricsBatteryUsage.KEY_AGGREGATION_TYPE,
                    MetricsBatteryUsage.KEY_TIMESTAMP);

    public static final String DELETE_METRICS_COUNTER_BY_TYPE = MessageFormat
            .format("DELETE FROM ${0}$ WHERE ${1}$='''''{0}''''' AND ${2}$ <= '{1}' ",
                    DB_TABLES.METRICS_COUNTER_TYPE_DEVICE,
                    MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE,
                    MetricsCounterTypeDevice.KEY_TIMESTAMP);

    public static final String DELETE_METRICS_BINARY = MessageFormat
            .format("DELETE FROM ${0}$ WHERE ${1}$ > '{0}' AND ${1}$ <= '{1}' ",
                    DB_TABLES.METRICS_BINARY_TYPE_DEVICE,
                    MetricsCounterTypeDevice.KEY_TIMESTAMP);

    public static final String DELETE_METRICS_GPS = MessageFormat
            .format("DELETE FROM ${0}$ WHERE ${1}$ > '{0}' AND ${1}$ <= '{1}' ",
                    DB_TABLES.METRICS_GPS_TYPE_DEVICE,
                    MetricsCounterTypeDevice.KEY_TIMESTAMP);

    public static final String SELECT_METRICS_DOUBLE_BY_SENSOR_VARIABLE = MessageFormat
            .format("SELECT ${0}$, MIN(${1}$) AS ${1}$, MAX(${2}$) AS ${2}$, SUM(${3}$*${4}$)/SUM(${4}$) "
                    + "AS ${3}$, SUM(${4}$) AS ${4}$, '{2}' AS ${6}$ FROM ${5}$ WHERE ${0}$='{0}' "
                    + "AND ${6}$ > '{1}' AND ${6}$ <= '{2}' GROUP BY ${0}$",
                    MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID,
                    MetricsDoubleTypeDevice.KEY_MIN,
                    MetricsDoubleTypeDevice.KEY_MAX,
                    MetricsDoubleTypeDevice.KEY_AVG,
                    MetricsDoubleTypeDevice.KEY_SAMPLES,
                    DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE,
                    MetricsDoubleTypeDevice.KEY_TIMESTAMP);

    public static final String SELECT_METRICS_BATTERY_BY_NODE = MessageFormat
            .format("SELECT ${0}$, MIN(${1}$) AS ${1}$, MAX(${2}$) AS ${2}$, SUM(${3}$*${4}$)/SUM(${4}$) "
                    + "AS ${3}$, SUM(${4}$) AS ${4}$, '{2}' AS ${6}$ FROM ${5}$ WHERE ${0}$='{0}' "
                    + "AND ${6}$ > '{1}' AND ${6}$ <= '{2}'  GROUP BY ${0}$",
                    MetricsBatteryUsage.KEY_NODE_ID,
                    MetricsBatteryUsage.KEY_MIN,
                    MetricsBatteryUsage.KEY_MAX,
                    MetricsBatteryUsage.KEY_AVG,
                    MetricsBatteryUsage.KEY_SAMPLES,
                    DB_TABLES.METRICS_BATTERY_USAGE,
                    MetricsBatteryUsage.KEY_TIMESTAMP);

    public static final String SELECT_METRICS_COUNTER_BY_SENSOR_VARIABLE = MessageFormat
            .format("SELECT ${0}$, SUM(${1}$) AS ${1}$, SUM(${2}$) AS ${2}$, '{2}' AS ${4}$ FROM ${3}$ "
                    + "WHERE ${0}$='{0}' AND ${4}$ > '{1}' AND ${4}$ <= '{2}'  GROUP BY ${0}$",
                    MetricsCounterTypeDevice.KEY_SENSOR_VARIABLE_ID,
                    MetricsCounterTypeDevice.KEY_VALUE,
                    MetricsCounterTypeDevice.KEY_SAMPLES,
                    DB_TABLES.METRICS_COUNTER_TYPE_DEVICE,
                    MetricsCounterTypeDevice.KEY_TIMESTAMP);

    public static String getQuery(String query) {
        switch (AppProperties.getInstance().getDbType()) {
            case POSTGRESQL:
                return query.replaceAll("\\$", "\"");
            default:
                break;
        }
        return query.replaceAll("\\$", "");
    }
}
