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
package org.mycontroller.standalone;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class AppShutdownHook {

    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                this.setName(AppProperties.APPLICATION_NAME + " Shutdown-Hook");
                _logger.debug("Shutdown hook called. Running stop services...");
                StartApp.stopServices();
                _logger.debug("Shutdown hook completed...");
                _logger.info(McObjectManager.getMcLocale().getString(MC_LOCALE.BYE_HAVE_A_NICE_DAY));
            }
        });
        _logger.debug("Shutdown hook attached...");
    }
}
