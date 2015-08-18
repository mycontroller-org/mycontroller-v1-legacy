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

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.db.DbException;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.mysensors.MyMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SensorDaoImpl extends BaseAbstractDao<Sensor, Integer> implements SensorDao {
    private static final Logger _logger = LoggerFactory.getLogger(SensorDaoImpl.class);

    public SensorDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Sensor.class);
    }

    @Override
    public void create(Sensor sensor) {
        try {
            this.nodeIdSensorIdnullCheck(sensor);
            Integer count = this.getDao().create(sensor);
            _logger.debug("Created Sensor:[{}], Create count:{}", sensor, count);
        } catch (SQLException ex) {
            _logger.error("unable to add Sensor:[{}]", sensor, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to create, sensor:{}", sensor, dbEx);
        }
    }

    @Override
    public void create(Integer nodeId, Sensor sensor) {
        sensor.setNode(new Node(nodeId));
        this.create(sensor);
    }

    @Override
    public void create(Integer nodeId, Integer sensorId) {
        Sensor sensor = new Sensor(sensorId);
        sensor.setNode(new Node(nodeId));
        this.create(sensor);
    }

    @Override
    public void createOrUpdate(Sensor sensor) {
        try {
            sensor.updateDefault();
            this.nodeIdSensorIdnullCheck(sensor);
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(sensor);
            _logger.debug("CreateOrUpdate Sensor:[{}],Create:{},Update:{},Lines Changed:{}",
                    sensor, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("CreateOrUpdate failed, Sensor:[{}]", sensor, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to createOrUpdate, sensor:{}", sensor, dbEx);
        }
    }

    @Override
    public void createOrUpdate(Integer nodeId, Sensor sensor) {
        sensor.setNode(new Node(nodeId));
        this.createOrUpdate(sensor);
    }

    @Override
    public void delete(Sensor sensor) {
        try {
            this.nodeIdSensorIdnullCheck(sensor);
            DeleteBuilder<Sensor, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(Sensor.NODE_ID, sensor.getNode().getId())
                    .and().eq(Sensor.SENSOR_ID, sensor.getSensorId());
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted senosor:[{}], delete count:{}", sensor, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete, sensor:{}", sensor, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to delete, sensor:{}", sensor, dbEx);
        }
    }

    @Override
    public void delete(Integer nodeId, Integer sensorId) {
        Sensor sensor = new Sensor(sensorId);
        sensor.setNode(new Node(nodeId));
        this.delete(sensor);
    }

    @Override
    public void update(Sensor sensor) {
        try {
            this.nodeIdSensorIdnullCheck(sensor);
            sensor.updateDefault();
            UpdateBuilder<Sensor, Integer> updateBuilder = this.getDao().updateBuilder();
            if (sensor.getType() != null) {
                updateBuilder.updateColumnValue("type", sensor.getType());
            }
            if (sensor.getName() != null) {
                updateBuilder.updateColumnValue("name", sensor.getName());
            }
            if (sensor.getUpdateTime() != null) {
                updateBuilder.updateColumnValue("updateTime", sensor.getUpdateTime());
            }
            if (sensor.getStatus() != null) {
                updateBuilder.updateColumnValue("status", sensor.getStatus());
            }
            if (sensor.getLastValue() != null) {
                updateBuilder.updateColumnValue("lastValue", sensor.getLastValue());
            }

            //Update Unit column
            updateBuilder.updateColumnValue("unit", sensor.getUnit());
            if (sensor.getMessageType() != null) {
                updateBuilder.updateColumnValue("messageType", sensor.getMessageType());
            }
            updateBuilder.where()
                    .eq(Sensor.NODE_ID, sensor.getNode().getId())
                    .and().eq(Sensor.SENSOR_ID, sensor.getSensorId());
            int updateCount = updateBuilder.update();
            _logger.debug("Updated senosor:[{}], update count:{}", sensor, updateCount);
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to update, sensor:{}", sensor, dbEx);
        }
    }

    @Override
    public void update(Integer nodeId, Sensor sensor) {
        sensor.setNode(new Node(nodeId));
        this.update(sensor);
    }

    @Override
    public List<Sensor> getAll(Integer nodeId) {
        try {
            if (nodeId == null) {
                return null;
            }
            return this.getDao().queryForEq(Sensor.NODE_ID, nodeId);
        } catch (SQLException ex) {
            _logger.error("unable to get all list wit node id:{}", nodeId, ex);
            return null;
        }
    }

    @Override
    public List<Sensor> getByType(String typeString) {
        try {
            return this.getDao()
                    .queryForEq("type", MyMessages.MESSAGE_TYPE_PRESENTATION.valueOf(typeString).ordinal());
        } catch (SQLException ex) {
            _logger.error("unable to get all list with typeString: {}", typeString, ex);
            return null;
        }
    }

    @Override
    public List<Sensor> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public Sensor get(Integer id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get, id:{}", id, ex);
        }
        return null;
    }

    @Override
    public Sensor get(Integer nodeId, Integer sensorId) {
        try {
            nodeIdSensorIdnullCheck(nodeId, sensorId);
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(Sensor.NODE_ID, nodeId)
                            .and().eq(Sensor.SENSOR_ID, sensorId).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to get, nodeId:{},sensorId:{}", nodeId, sensorId, dbEx);
        }
        return null;
    }

    @Override
    public Sensor get(Sensor sensor) {
        try {
            this.nodeIdSensorIdnullCheck(sensor);
            return this.get(sensor.getNode().getId(), sensor.getSensorId());
        } catch (DbException ex) {
            _logger.error("unable to get", ex);
            return null;
        }
    }

    private void nodeIdSensorIdnullCheck(Integer nodeId, Integer sensorId) throws DbException {
        if (nodeId != null && sensorId != null) {
            return;
        } else {
            throw new DbException("SensorId or NodeId should not be a NULL, NodeId:" + nodeId + ",SensorId:"
                    + sensorId);
        }
    }

    private void nodeIdSensorIdnullCheck(Sensor sensor) throws DbException {
        if (sensor != null && sensor.getSensorId() != null && sensor.getNode() != null
                && sensor.getNode().getId() != null) {
            return;
        } else {
            throw new DbException("SensorId or NodeId should not be a NULL, Sensor:" + sensor);
        }
    }
}