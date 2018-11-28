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
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.exceptions.McDatabaseException;

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
    public UidTag get(UidTag uidTag) {
        return super.getById(uidTag.getId());
    }

    @Override
    public List<UidTag> getAllByUid(List<String> uids) {
        return super.getAll(UidTag.KEY_UID, uids);
    }

    @Override
    public void delete(RESOURCE_TYPE resourceType, Integer resourceId) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(UidTag.KEY_RESOURCE_TYPE, resourceType);
        map.put(UidTag.KEY_RESOURCE_ID, resourceId);
        super.delete(map);
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(UidTag.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public UidTag get(RESOURCE_TYPE resourceType, Integer resourceId) {
        QueryBuilder<UidTag, Integer> queryBuilder = getDao().queryBuilder();
        try {
            queryBuilder.where().eq(UidTag.KEY_RESOURCE_TYPE, resourceType).and()
                    .eq(UidTag.KEY_RESOURCE_ID, resourceId);
            List<UidTag> uidTags = queryBuilder.query();
            if (uidTags != null && !uidTags.isEmpty()) {
                return uidTags.get(0);
            }
        } catch (SQLException ex) {
            _logger.error("Error, ", ex);
            throw new McDatabaseException(ex);
        }
        return null;
    }

    @Override
    public UidTag getByUid(String uid) {
        List<UidTag> uidTags = super.getAll(UidTag.KEY_UID, uid);
        if (uidTags != null && !uidTags.isEmpty()) {
            return uidTags.get(0);
        }
        return null;
    }

    @Override
    public void deleteByUid(String uid) {
        super.delete(UidTag.KEY_UID, uid);
    }

    @Override
    public List<UidTag> getAll(List<Integer> ids) {
        return super.getAll(UidTag.KEY_ID, ids);
    }

}
