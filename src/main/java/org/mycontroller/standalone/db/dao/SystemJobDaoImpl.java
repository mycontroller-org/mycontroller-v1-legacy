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

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.db.tables.SystemJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SystemJobDaoImpl extends BaseAbstractDaoImpl<SystemJob, Integer> implements SystemJobDao {
    private static final Logger _logger = LoggerFactory.getLogger(SystemJobDaoImpl.class);

    public SystemJobDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, SystemJob.class);
    }

    @Override
    public void create(SystemJob systemJob) {
        try {
            int count = this.getDao().create(systemJob);
            _logger.debug("Created SystemJob:[{}], Create count:{}", systemJob, count);

        } catch (SQLException ex) {
            _logger.error("unable to add SystemJob:[{}]", systemJob, ex);
        }
    }

    @Override
    public void createOrUpdate(SystemJob systemJob) {
        try {
            CreateOrUpdateStatus count = this.getDao().createOrUpdate(systemJob);
            _logger.debug("createOrUpdate SystemJob:[{}], Status[create:{},update:{}], Number of line changed:{}",
                    systemJob, count.isCreated(), count.isUpdated(), count.getNumLinesChanged());

        } catch (SQLException ex) {
            _logger.error("unable to add SystemJob:[{}]", systemJob, ex);
        }

    }

    @Override
    public void delete(SystemJob systemJob) {
        if (systemJob == null) {
            return;
        }
        try {
            //SchedulerUtils.removeJob(systemJob.getName());
            this.getDao().deleteById(systemJob.getId());
            _logger.debug("Job[{}] deleted from db.", systemJob);
        } catch (SQLException ex) {
            _logger.error("unable to get SystemJob", ex);
        }

    }

    @Override
    public void update(SystemJob systemJob) {
        try {
            int count = this.getDao().update(systemJob);
            _logger.debug("Update SystemJob:[{}], Change Count:{}",
                    systemJob, count);
        } catch (SQLException ex) {
            _logger.error("Unable to update SystemJob:[{}]", systemJob, ex);
        }
    }

    @Override
    public SystemJob get(SystemJob systemJob) {
        return this.get(systemJob.getId());
    }

    @Override
    public SystemJob get(Integer id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get SystemJob", ex);
            return null;
        }
    }

    @Override
    public List<SystemJob> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public List<SystemJob> getAllEnabled() {
        try {
            return this.getDao().queryBuilder().where()
                    .eq(SystemJob.ENABLED, true).query();
        } catch (SQLException ex) {
            _logger.error("Unable to get list of enabled systemjobs", ex);
            return null;
        }
    }
}
