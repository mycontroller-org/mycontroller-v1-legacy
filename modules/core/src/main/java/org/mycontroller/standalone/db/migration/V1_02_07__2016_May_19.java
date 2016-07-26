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

import org.mycontroller.standalone.settings.MqttBrokerSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_07__2016_May_19 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Added MQTT broker settings in to GUI
         **/

        /** Migration #1
         * Add default settings of MQTT in to GUI
         * steps
         * 1. Create MqttBrokerSettings and update
         * */
        MqttBrokerSettings.builder()
                .enabled(true)
                .allowAnonymous(false)
                .bindAddress("0.0.0.0")
                .httpPort(1883)
                .websocketPort(7080)
                .build().save();

        _logger.info("Migration completed successfully.");
    }

}
