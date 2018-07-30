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
package org.mycontroller.standalone.provider;

import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McThreadPoolFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.ExternalServerExecuter;
import org.mycontroller.standalone.fwpayload.ExecuteForwardPayload;
import org.mycontroller.standalone.metrics.DATA_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils;
import org.mycontroller.standalone.metrics.model.DataPointer;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.rule.McRuleEngine;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class ExecuteMessageDependentTask implements Runnable {
    private SensorVariable _sv;

    public ExecuteMessageDependentTask(SensorVariable _sv) {
        this._sv = _sv;
    }

    private void executeDependentTask() {
        // forward payload to another sensor variable
        List<ForwardPayload> _frwPls = DaoUtils.getForwardPayloadDao().getAllEnabled(_sv.getId());
        if (_frwPls != null && !_frwPls.isEmpty()) {
            McThreadPoolFactory.execute(new ExecuteForwardPayload(_frwPls, _sv));
        }

        // Send Payload to external server
        McThreadPoolFactory.execute(new ExternalServerExecuter(_sv));

        // update metric data to metric engine
        try {
            MetricsUtils.engine().post(DataPointer.builder()
                    .payload(_sv.getValue())
                    .timestamp(_sv.getTimestamp())
                    .resourceModel(new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, _sv))
                    .dataType(DATA_TYPE.SENSOR_VARIABLE)
                    .build());
        } catch (Exception ex) {
            _logger.error("Exception, [timestamp:{}, sensorVariableId:{}, payload:{}]",
                    _sv.getTimestamp(), _sv.getId(), _sv.getValue(), ex);
        }

        // execute Rules for this sensor variable
        // DO NOT START NEW THREAD
        try {
            new McRuleEngine(RESOURCE_TYPE.SENSOR_VARIABLE, _sv.getId()).run();
        } catch (Exception ex) {
            _logger.error("Exception, [timestamp:{}, sensorVariableId:{}, payload:{}]",
                    _sv.getTimestamp(), _sv.getId(), _sv.getValue(), ex);
        }
    }

    @Override
    public void run() {
        try {
            executeDependentTask();
        } catch (Exception ex) {
            _logger.error("Exception on executing {}", _sv, ex);
        }
    }

}
