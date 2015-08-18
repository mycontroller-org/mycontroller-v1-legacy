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

import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class FirmwareVersionDaoImpl extends BaseAbstractDao<FirmwareVersion, Integer> implements FirmwareVersionDao {
    private static final Logger _logger = LoggerFactory.getLogger(FirmwareVersionDaoImpl.class);

    public FirmwareVersionDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, FirmwareVersion.class);
    }

    @Override
    public void create(FirmwareVersion firmwareVersion) {
        try {
            int count = this.getDao().create(firmwareVersion);
            _logger.debug("Created FirmwareVersion:[{}], Create count:{}", firmwareVersion, count);
        } catch (SQLException ex) {
            _logger.error("unable to add FirmwareVersion:[{}]", firmwareVersion, ex);
        }
    }

    @Override
    public void delete(FirmwareVersion firmwareVersion) {
        try {
            int count = this.getDao().delete(firmwareVersion);
            _logger.debug("FirmwareVersion:[{}] deleted, Delete count:{}", firmwareVersion, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete firmwareVersion:[{}]", firmwareVersion, ex);
        }
    }

    @Override
    public void delete(int id) {
        FirmwareVersion firmwareVersion = new FirmwareVersion(id);
        this.delete(firmwareVersion);
    }

    @Override
    public void update(FirmwareVersion firmwareVersion) {
        try {
            int count = this.getDao().update(firmwareVersion);
            _logger.debug("Updated FirmwareVersion:[{}], Update count:{}", firmwareVersion, count);
        } catch (SQLException ex) {
            _logger.error("unable to update firmwareVersion:[{}]", firmwareVersion, ex);
        }
    }

    @Override
    public List<FirmwareVersion> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all Nodes", ex);
            return null;
        }
    }

    @Override
    public FirmwareVersion get(FirmwareVersion firmwareVersion) {
        return this.get(firmwareVersion.getId());
    }

    @Override
    public FirmwareVersion get(int id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get FirmwareVersion", ex);
            return null;
        }
    }

    @Override
    public void createOrUpdate(FirmwareVersion firmwareVersion) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(firmwareVersion);
            _logger.debug("CreateOrUpdate FirmwareVersion:[{}],Create:{},Update:{},Lines Changed:{}",
                    firmwareVersion, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate FirmwareVersion:[{}]", firmwareVersion, ex);
        }
    }
}
