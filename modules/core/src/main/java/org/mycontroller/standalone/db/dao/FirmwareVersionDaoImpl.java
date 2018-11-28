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
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.exceptions.McDatabaseException;

import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class FirmwareVersionDaoImpl extends BaseAbstractDaoImpl<FirmwareVersion, Integer> implements
        FirmwareVersionDao {

    public FirmwareVersionDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, FirmwareVersion.class);
    }

    @Override
    public FirmwareVersion get(FirmwareVersion tdao) {
        return this.getById(tdao.getId());
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(FirmwareVersion.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<FirmwareVersion> getAll(List<Integer> ids) {
        return getAll(FirmwareVersion.KEY_ID, ids);
    }

}
