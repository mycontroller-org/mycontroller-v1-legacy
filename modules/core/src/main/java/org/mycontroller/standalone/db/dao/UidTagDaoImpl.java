/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.db.tables.UidTag;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class UidTagDaoImpl extends BaseAbstractDaoImpl<UidTag, Integer> implements UidTagDao {

    public UidTagDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, UidTag.class);
    }

    @Override
    public void create(UidTag uidTag) {
        try {
            Integer count = this.getDao().create(uidTag);
            _logger.debug("Created UidTag:[{}], Create count:{}", uidTag, count);
        } catch (SQLException ex) {
            _logger.error("unable to add UidTag:[{}]", uidTag, ex);
        }
    }

    @Override
    public void delete(int id) {
        UidTag uidTag = new UidTag(id);
        try {
            int count = this.getDao().delete(uidTag);
            _logger.debug("UidTag:[{}] deleted, Delete count:{}", uidTag, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete UidTag:[{}]", uidTag, ex);
        }
    }

    @Override
    public void deleteBySensorRefId(int sensorRefId) {
        try {
            DeleteBuilder<UidTag, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(UidTag.SENSOR_REF_ID, sensorRefId);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted sensorRefId:[{}], delete count:{}", sensorRefId, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete sensorRefId:{}", sensorRefId, ex);
        }
    }

    @Override
    public List<UidTag> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public UidTag get(int uid) {
        try {
            return this.getDao().queryForId(uid);
        } catch (SQLException ex) {
            _logger.error("unable to fetch uid:[{}]", uid, ex);
            return null;
        }
    }

    @Override
    public UidTag getBySensorRefId(int sensorRefId) {
        try {
            QueryBuilder<UidTag, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(UidTag.SENSOR_REF_ID, sensorRefId);
            return queryBuilder.queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to fetch uid:[{}]", sensorRefId, ex);
            return null;
        }
    }

}
