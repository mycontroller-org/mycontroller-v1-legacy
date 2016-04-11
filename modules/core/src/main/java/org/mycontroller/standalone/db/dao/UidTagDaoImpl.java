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

import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.UidTag;

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
    public UidTag get(UidTag uidTag) {
        return super.getById(uidTag.getUid());
    }

    @Override
    public List<UidTag> getAll(List<Integer> ids) {
        return super.getAll(UidTag.KEY_UID, ids);
    }

    @Override
    public void deleteBySensorVariableIds(List<Integer> sVariableIds) {
        super.delete(UidTag.KEY_SENSOR_VARIABLE, sVariableIds);
    }

    @Override
    public void deleteBySensorId(Integer sId) {
        List<Integer> senosrVariableIds = DaoUtils.getSensorVariableDao().getSensorVariableIds(sId);
        deleteBySensorVariableIds(senosrVariableIds);
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            return this.getQueryResponse(query, UidTag.KEY_ID);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public UidTag getBySensorVariableId(Integer sVariableId) {
        List<UidTag> uidTags = super.getAll(UidTag.KEY_SENSOR_VARIABLE, sVariableId);
        if (uidTags != null && !uidTags.isEmpty()) {
            return uidTags.get(0);
        }
        return null;
    }

    @Override
    public UidTag getByUId(Integer uid) {
        List<UidTag> uidTags = super.getAll(UidTag.KEY_UID, uid);
        if (uidTags != null && !uidTags.isEmpty()) {
            return uidTags.get(0);
        }
        return null;
    }

    @Override
    public void deleteByUId(Integer uid) {
        super.delete(UidTag.KEY_SENSOR_VARIABLE, uid);
    }

}
