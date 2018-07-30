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
package org.mycontroller.standalone.jobs;

import java.util.ArrayList;
import java.util.List;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.api.GatewayApi;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.utils.McUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class ExecuteDiscoverJob extends Job {
    public static final String NAME = "discover_job";
    public static final String TRIGGER_NAME = "discover_trigger";
    private static final Logger _logger = LoggerFactory.getLogger(ExecuteDiscoverJob.class);
    public static final long DEFAULT_EXECUTE_DISCOVER_INTERVAL = 30 * McUtils.MINUTE;

    @Override
    public void doRun() throws JobInterruptException {
        _logger.debug("Executing 'node discover' job");
        if (AppProperties.getInstance().getControllerSettings().getExecuteDiscoverInterval() < McUtils.MINUTE) {
            //Nothing to do, just return from here
            return;
        }
        try {
            List<GatewayTable> gateways = DaoUtils.getGatewayDao().getAllEnabled();
            List<Integer> gatewayIds = new ArrayList<Integer>();
            for (GatewayTable gateway : gateways) {
                //For now supports only for mySensors
                if (gateway.getNetworkType() == NETWORK_TYPE.MY_SENSORS) {
                    gatewayIds.add(gateway.getId());
                }
            }
            new GatewayApi().executeNodeDiscover(gatewayIds);
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }

}
