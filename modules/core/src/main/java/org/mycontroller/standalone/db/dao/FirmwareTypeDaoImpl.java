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
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class FirmwareTypeDaoImpl extends BaseAbstractDaoImpl<FirmwareType, Integer> implements FirmwareTypeDao {
    private static final Logger _logger = LoggerFactory.getLogger(FirmwareTypeDaoImpl.class);

    public FirmwareTypeDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, FirmwareType.class);
    }

    @Override
    public FirmwareType get(FirmwareType firmwareType) {
        return this.getById(firmwareType.getId());
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            return this.getQueryResponse(query, FirmwareType.KEY_ID);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public List<FirmwareType> getAll(List<Integer> ids) {
        return getAll(FirmwareType.KEY_ID, ids);
    }

}
