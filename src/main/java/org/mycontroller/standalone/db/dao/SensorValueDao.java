/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.db.tables.SensorValue;
import com.j256.ormlite.dao.Dao;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface SensorValueDao {
    void create(SensorValue sensorValue);

    void createOrUpdate(SensorValue sensorValue);

    void delete(SensorValue sensorValue);

    void update(SensorValue sensorValue);

    List<SensorValue> getAll(Integer sensorRefId);

    List<SensorValue> getAllDoubleMetric(Integer sensorRefId);

    List<SensorValue> getByVariableType(Integer messageVariableTypeId);

    List<SensorValue> getAll();

    SensorValue get(SensorValue sensorValue);
    
    SensorValue get(int id);

    SensorValue get(Integer sensorRefId, Integer variableTypeId);

    Dao<SensorValue, Integer> getDao();
}
