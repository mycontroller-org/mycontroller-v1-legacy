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

import org.mycontroller.standalone.api.jaxrs.model.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.Sensor;

import com.j256.ormlite.dao.Dao;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public interface SensorDao extends BaseDao<Sensor, Integer> {

    void create(Integer gatewayId, String nodeEui, Sensor sensor);

    void create(Integer nodeId, Sensor sensor);

    void create(Integer gatewayId, String nodeEui, String sensorId);

    void createOrUpdate(Integer gatewayId, String nodeEui, Sensor sensor);

    void createOrUpdate(Integer nodeId, Sensor sensor);

    void delete(Integer gatewayId, String nodeEui, String sensorId);

    void update(Integer nodeId, Sensor sensor);

    void update(Integer gatewayId, String nodeEui, Sensor sensor);

    List<Sensor> getAll(String nodeEui, Integer gatewayId);

    List<Sensor> getAllByNodeId(Integer nodeId);

    Sensor getByRoomId(String sensorName, Integer roomId);

    List<Sensor> getAllByRoomId(Integer roomId);

    List<Sensor> getAllByNodeIds(List<Integer> nodeIds);

    List<Sensor> getAllByIds(List<Integer> ids);

    List<Sensor> getByType(String typeString);

    QueryResponse getAll(Query query);

    Sensor get(Integer nodeId, String sensorId);

    Sensor get(Integer gatewayId, String nodeEui, String sensorId);

    @Override
    Dao<Sensor, Integer> getDao();

    List<Integer> getSensorIds(String nodeEui, Integer gatewayId);

    long countOf(Integer nodeId);

    List<Integer> getSensorIdsByNodeIds(List<Integer> ids);

    List<Sensor> getAll(Query query, String filter, AllowedResources allowedResources);

}
