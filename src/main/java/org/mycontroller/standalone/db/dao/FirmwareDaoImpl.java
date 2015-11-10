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

import org.mycontroller.standalone.db.tables.Firmware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class FirmwareDaoImpl extends BaseAbstractDao<Firmware, Integer> implements FirmwareDao {
    private static final Logger _logger = LoggerFactory.getLogger(FirmwareDaoImpl.class);

    public FirmwareDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Firmware.class);
    }

    @Override
    public void create(Firmware firmware) {
        try {
            int count = this.getDao().create(firmware);
            _logger.debug("Created Firmware:[{}], Create count:{}", firmware, count);
        } catch (SQLException ex) {
            _logger.error("unable to add Firmware:[{}]", firmware, ex);
        }
    }

    @Override
    public void delete(Firmware firmware) {
        try {
            int count = this.getDao().delete(firmware);
            _logger.debug("Firmware:[{}] deleted, Delete count:{}", firmware, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete firmware:[{}]", firmware, ex);
        }
    }

    @Override
    public void delete(int id) {
        Firmware firmware = new Firmware(id);
        this.delete(firmware);
    }

    @Override
    public void update(Firmware firmware) {
        try {
            int count = this.getDao().update(firmware);
            _logger.debug("Updated Firmware:[{}], Update count:{}", firmware, count);
        } catch (SQLException ex) {
            _logger.error("unable to update firmware:[{}]", firmware, ex);
        }
    }

    @Override
    public Firmware get(Firmware firmware) {
        return this.get(firmware.getId());
    }

    @Override
    public Firmware get(int id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get Firmware", ex);
            return null;
        }
    }

    @Override
    public void createOrUpdate(Firmware firmware) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(firmware);
            _logger.debug("CreateOrUpdate Firmware:[{}],Create:{},Update:{},Lines Changed:{}",
                    firmware, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate Firmware:[{}]", firmware, ex);
        }
    }

    @Override
    public Firmware get(Integer typeId, Integer versionId) {
        QueryBuilder<Firmware, Integer> queryBuilder = getDao().queryBuilder();
        try {
            queryBuilder.where().eq(Firmware.TYPE_ID, typeId).and().eq(Firmware.VERSION_ID, versionId);
            return queryBuilder.queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to fetch Firmware:[typeId:{},versionId:{}]", typeId, versionId, ex);
            return null;
        }
    }

    @Override
    public void delete(Integer typeId, Integer versionId) {
        DeleteBuilder<Firmware, Integer> deleteBuilder = getDao().deleteBuilder();
        try {
            deleteBuilder.where().eq(Firmware.TYPE_ID, typeId).and().eq(Firmware.VERSION_ID, versionId);
            int count = deleteBuilder.delete();
            _logger.error("Deleted Firmware(s) count:[{}]", count);
        } catch (SQLException ex) {
            _logger.error("unable to delete Firmware:[typeId:{},versionId:{}]", typeId, versionId, ex);
        }
    }

    @Override
    public List<Firmware> getAll() {
        return getAll(null, null);
    }

    @Override
    public List<Firmware> getAllFirmwareByType(int typeId) {
        return getAll(true, typeId);
    }

    @Override
    public List<Firmware> getAllFirmwareByVersion(int versionId) {
        return getAll(false, versionId);
    }

    private List<Firmware> getAll(Boolean isType, Integer id) {
        try {
            QueryBuilder<Firmware, Integer> queryBuilder = getDao().queryBuilder();
            queryBuilder.selectColumns("id", Firmware.TYPE_ID, Firmware.VERSION_ID, "timestamp", "blocks", "crc");
            if (isType == null) {
                //Nothing to do, no filter, get all firmwares
            } else if (isType) {
                queryBuilder.where().eq(Firmware.TYPE_ID, id);
            } else {
                queryBuilder.where().eq(Firmware.VERSION_ID, id);
            }
            queryBuilder.orderBy(Firmware.TYPE_ID, true).orderBy(Firmware.VERSION_ID, true);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get selected type[isType:{},id:{}]", isType, id, ex);
            return null;
        }
    }

}
