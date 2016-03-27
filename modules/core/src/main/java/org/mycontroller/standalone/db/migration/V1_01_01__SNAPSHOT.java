/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.settings.MetricsGraph;
import org.mycontroller.standalone.settings.MetricsGraphSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class V1_01_01__SNAPSHOT extends MigrationBase {
    private static final Logger _logger = LoggerFactory.getLogger(V1_01_01__SNAPSHOT.class.getName());

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        /* //Load Dao's if not loaded already
         if (!DaoUtils.isDaoInitialized()) {
             DaoUtils.loadAllDao();
         }
         //Load properties from database
         McObjectManager.getAppProperties().loadPropertiesFromDb();*/

        loadDao();

        /** Migrations
            1. Added new column 'previousValue' under sensorvariables table
            2. Modified metrics chart type from boolean to string (default: line)
         **/

        //Migration #1
        int alterCount = DaoUtils.getSensorVariableDao().getDao()
                .executeRaw("ALTER TABLE sensor_variable ADD COLUMN IF NOT EXISTS previousValue VARCHAR(255);");
        _logger.debug("Alter count:{}", alterCount);

        //Migration #2
        String defaultSubType = "line";
        MetricsGraphSettings metricsGraphSettings = MetricsGraphSettings.get();
        for (MetricsGraph metricsGraph : metricsGraphSettings.getMetrics()) {
            metricsGraph.setSubType(defaultSubType);
        }
        metricsGraphSettings.getBattery().setSubType(defaultSubType);
        metricsGraphSettings.save();

        _logger.info("Migration completed successfully.");
    }

}
