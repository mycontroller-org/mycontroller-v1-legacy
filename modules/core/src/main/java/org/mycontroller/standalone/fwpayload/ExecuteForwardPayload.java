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
package org.mycontroller.standalone.fwpayload;

import java.util.List;

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.SensorVariable;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class ExecuteForwardPayload implements Runnable {
    private List<ForwardPayload> _frwPls;
    private SensorVariable _sv;

    public ExecuteForwardPayload(List<ForwardPayload> _frwPls, SensorVariable _sv) {
        this._frwPls = _frwPls;
        this._sv = _sv;
    }

    private void execute(ForwardPayload forwardPayload) {
        _logger.debug("Sensor:[{}], Details of ForwardPayload:[{}]", _sv.getSensor(), forwardPayload);
        McObjectManager.getMcActionEngine().executeForwardPayload(forwardPayload, _sv.getValue());
    }

    @Override
    public void run() {
        for (ForwardPayload _frwPl : _frwPls) {
            try {
                if (!_frwPl.getDestination().getReadOnly()) {
                    execute(_frwPl);
                }
            } catch (Exception ex) {
                _logger.error("Unable to execute ForwardPayload:[{}], [{}]", _frwPl, _sv, ex);
            }
        }

    }
}
