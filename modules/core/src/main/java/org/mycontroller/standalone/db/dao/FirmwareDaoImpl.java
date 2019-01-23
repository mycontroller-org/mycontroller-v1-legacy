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

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.exceptions.McDatabaseException;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class FirmwareDaoImpl extends BaseAbstractDaoImpl<Firmware, Integer> implements FirmwareDao {

    public FirmwareDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Firmware.class);
    }

    @Override
    public Firmware get(Firmware firmware) {
        return this.getById(firmware.getId());
    }

    @Override
    public Firmware get(Integer typeId, Integer versionId) {
        QueryBuilder<Firmware, Integer> queryBuilder = getDao().queryBuilder();
        try {
            queryBuilder.where().eq(Firmware.KEY_TYPE_ID, typeId).and().eq(Firmware.KEY_VERSION_ID, versionId);
            return queryBuilder.queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to fetch Firmware:[typeId:{},versionId:{}]", typeId, versionId, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void delete(Integer typeId, Integer versionId) {
        DeleteBuilder<Firmware, Integer> deleteBuilder = getDao().deleteBuilder();
        try {
            deleteBuilder.where().eq(Firmware.KEY_TYPE_ID, typeId).and().eq(Firmware.KEY_VERSION_ID, versionId);
            int count = deleteBuilder.delete();
            _logger.error("Deleted Firmware(s) count:[{}]", count);
        } catch (SQLException ex) {
            _logger.error("unable to delete Firmware:[typeId:{},versionId:{}]", typeId, versionId, ex);
            throw new McDatabaseException(ex);
        }
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
            queryBuilder.selectColumns(Firmware.KEY_ID, Firmware.KEY_TYPE_ID, Firmware.KEY_VERSION_ID,
                    Firmware.KEY_TIMESTAMP);
            if (isType == null) {
                //Nothing to do, no filter, get all firmwares
            } else if (isType) {
                queryBuilder.where().eq(Firmware.KEY_TYPE_ID, id);
            } else {
                queryBuilder.where().eq(Firmware.KEY_VERSION_ID, id);
            }
            queryBuilder.orderBy(Firmware.KEY_TYPE_ID, true).orderBy(Firmware.KEY_VERSION_ID, true);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get selected type[isType:{},id:{}]", isType, id, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(Firmware.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<Firmware> getAll(List<Integer> ids) {
        return getAll(Firmware.KEY_ID, ids);
    }

}
