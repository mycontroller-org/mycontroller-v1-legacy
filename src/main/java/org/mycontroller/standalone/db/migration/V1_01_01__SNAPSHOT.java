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
package org.mycontroller.standalone.db.migration;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class V1_01_01__SNAPSHOT implements JdbcMigration {
    private static final Logger _logger = LoggerFactory.getLogger(V1_01_01__SNAPSHOT.class.getName());

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load Dao's if not loaded already
        if (!DaoUtils.isDaoInitialized()) {
            DaoUtils.loadAllDao();
        }
        //Load properties from database
        ObjectFactory.getAppProperties().loadPropertiesFromDb();

        /** Migrations
            1. Added new column 'previousValue' under sensorvariables table
            2. Modified metrics chart type from boolean to string (default: line)
        **/

        //Migration #1
        Dao<SensorVariable, Integer> sensorVariableDao = DaoUtils.getSensorVariableDao().getDao();
        int alterCount = sensorVariableDao.executeRaw("ALTER TABLE " + DB_TABLES.SENSOR_VARIABLE
                + " ADD COLUMN IF NOT EXISTS " + SensorVariable.KEY_PREVIOUS_VALUE + " VARCHAR(255);");
        _logger.debug("Alter count:{}", alterCount);

        //Migration #2
        String defaultSubType = "line";
        MetricsSettings metricsSettings = MetricsSettings.get();
        for (MetricsGraph metricsGraph : metricsSettings.getMetrics()) {
            metricsGraph.setSubType(defaultSubType);
        }
        metricsSettings.getBattery().setSubType(defaultSubType);
        metricsSettings.save();

        _logger.info("Migration completed successfully.");
    }

}
