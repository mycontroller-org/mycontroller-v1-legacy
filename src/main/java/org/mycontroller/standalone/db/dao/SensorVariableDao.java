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
package org.mycontroller.standalone.db.dao;

import java.util.List;

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.db.tables.SensorVariable;

import com.j256.ormlite.dao.Dao;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface SensorVariableDao {
    void create(SensorVariable sensorVariable);

    void createOrUpdate(SensorVariable sensorVariable);

    void delete(SensorVariable sensorVariable);

    void update(SensorVariable sensorVariable);

    List<SensorVariable> getAll(Integer sensorRefId);

    List<SensorVariable> getAllDoubleMetric(Integer sensorRefId);

    List<SensorVariable> getByVariableType(MESSAGE_TYPE_SET_REQ messageVariableType);

    List<SensorVariable> getAll();

    SensorVariable get(SensorVariable sensorVariable);
    
    SensorVariable get(int id);

    SensorVariable get(Integer sensorRefId, MESSAGE_TYPE_SET_REQ variableType);

    Dao<SensorVariable, Integer> getDao();
    
    List<Integer> getSensorVariableIds(Integer sensorRefId);
}
