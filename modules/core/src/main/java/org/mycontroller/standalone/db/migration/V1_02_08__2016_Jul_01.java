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
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsGraph.CHART_TYPE;
import org.mycontroller.standalone.settings.MetricsGraphSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_08__2016_Jul_01 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Removed units from database
         *  2. Added new columns in 'Sensor variable' table and update unit column
         *  3. Update Custom metric graph style
         **/

        /** Migration #1
         * Remove all the units from database
         * steps
         * 1. remove all the units
         * */
        sqlClient().executeRaw(
                "DELETE FROM " + sqlClient().getTableName("settings") + " WHERE "
                        + sqlClient().getColumnName("subKey") + "='variableUnit'");

        /** Migration #2
         * Added/update new columns in 'Sensor variable' table
         * steps
         * 1. remove 'units' column
         * 2. add columns, unittype,readonly, offset, priority, graphproperties
         * 3. reload dao
         * 4. update units and metrictype
         * */
        if (sqlClient().hasColumn("sensor_variable", "unit")) {
            sqlClient().dropColumn("sensor_variable", "unit");
            sqlClient().addColumn("sensor_variable", "unitType", "VARCHAR(100)");
            sqlClient().addColumn("sensor_variable", "readOnly", "TINYINT DEFAULT FALSE NOT NULL");
            sqlClient().addColumn("sensor_variable", "\"offset\"", "DOUBLE PRECISION DEFAULT 0.0 NOT NULL");
            sqlClient().addColumn("sensor_variable", "priority", "INTEGER DEFAULT 100 NOT NULL");
            sqlClient().addColumn("sensor_variable", "graphProperties", "BLOB");
            reloadDao();
            List<SensorVariable> sVariables = DaoUtils.getSensorVariableDao().getAll();
            for (SensorVariable sVariable : sVariables) {
                sVariable.updateUnitAndMetricType();
                DaoUtils.getSensorVariableDao().update(sVariable);
            }
        }

        /** Migration #3
         * Add new metric type - Custom
         * steps
         * 1. Get all metrics
         * 2. Update custom metrics
         * 3. Save all metrics
         * */
        MetricsGraphSettings metricSettings = MetricsGraphSettings.get();
        //Update if "Custom" metrics not available
        if (metricSettings.getMetric(MESSAGE_TYPE_SET_REQ.V_CUSTOM.getText()) == null) {
            MetricsGraph customMetric = MetricsGraph.builder()
                    .metricName(MESSAGE_TYPE_SET_REQ.V_CUSTOM.getText())
                    .type(CHART_TYPE.LINE_CHART.getText())
                    .interpolate("linear")
                    .color("#ff7f0e")
                    .subType("line")
                    .build();
            metricSettings.getMetrics().add(customMetric);
            metricSettings.save();
            _logger.debug("'Custom' metrics updated successfully!");
        } else {
            _logger.debug("'Custom' metrics already available!");
        }

        _logger.info("Migration completed successfully.");
    }

}
