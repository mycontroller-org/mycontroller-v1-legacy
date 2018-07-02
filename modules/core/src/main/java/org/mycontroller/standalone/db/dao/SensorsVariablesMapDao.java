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
package org.mycontroller.standalone.db.dao;

import java.util.List;

import org.mycontroller.standalone.db.tables.SensorsVariablesMap;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface SensorsVariablesMapDao {
    void create(SensorsVariablesMap sensorsVariablesMap);

    void create(MESSAGE_TYPE_PRESENTATION sensorType, MESSAGE_TYPE_SET_REQ variableType);

    void delete(SensorsVariablesMap sensorsVariablesMap);

    void delete(MESSAGE_TYPE_PRESENTATION sensorType);

    List<SensorsVariablesMap> getAll(MESSAGE_TYPE_PRESENTATION sensorType);

    List<SensorsVariablesMap> getAll();

    SensorsVariablesMap get(SensorsVariablesMap sensorsVariablesMap);

    SensorsVariablesMap get(MESSAGE_TYPE_PRESENTATION sensorType, MESSAGE_TYPE_SET_REQ variableType);
}
