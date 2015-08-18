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

import org.mycontroller.standalone.db.tables.FirmwareType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class FirmwareTypeDaoImpl extends BaseAbstractDao<FirmwareType, Integer> implements FirmwareTypeDao {
    private static final Logger _logger = LoggerFactory.getLogger(FirmwareTypeDaoImpl.class);

    public FirmwareTypeDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, FirmwareType.class);
    }

    @Override
    public void create(FirmwareType firmwareType) {
        try {
            int count = this.getDao().create(firmwareType);
            _logger.debug("Created FirmwareType:[{}], Create count:{}", firmwareType, count);
        } catch (SQLException ex) {
            _logger.error("unable to add FirmwareType:[{}]", firmwareType, ex);
        }
    }

    @Override
    public void delete(FirmwareType firmwareType) {
        try {
            int count = this.getDao().delete(firmwareType);
            _logger.debug("FirmwareType:[{}] deleted, Delete count:{}", firmwareType, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete firmwareType:[{}]", firmwareType, ex);
        }
    }

    @Override
    public void delete(int id) {
        FirmwareType firmwareType = new FirmwareType(id);
        this.delete(firmwareType);
    }

    @Override
    public void update(FirmwareType firmwareType) {
        try {
            int count = this.getDao().update(firmwareType);
            _logger.debug("Updated FirmwareType:[{}], Update count:{}", firmwareType, count);
        } catch (SQLException ex) {
            _logger.error("unable to update firmwareType:[{}]", firmwareType, ex);
        }
    }

    @Override
    public List<FirmwareType> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all Nodes", ex);
            return null;
        }
    }

    @Override
    public FirmwareType get(FirmwareType firmwareType) {
        return this.get(firmwareType.getId());
    }

    @Override
    public FirmwareType get(int id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get FirmwareType", ex);
            return null;
        }
    }

    @Override
    public void createOrUpdate(FirmwareType firmwareType) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(firmwareType);
            _logger.debug("CreateOrUpdate FirmwareType:[{}],Create:{},Update:{},Lines Changed:{}",
                    firmwareType, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate FirmwareType:[{}]", firmwareType, ex);
        }
    }
}
