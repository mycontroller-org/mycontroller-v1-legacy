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

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewayUtils.TYPE;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class GatewayDaoImpl extends BaseAbstractDaoImpl<Gateway, Integer> implements GatewayDao {
    public GatewayDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Gateway.class);
    }

    @Override
    public List<Gateway> getAllEnabled() {
        return getAll(null, null, true);
    }

    @Override
    public List<Gateway> getAll(TYPE type, NETWORK_TYPE networkType) {
        return getAll(type, networkType, null);
    }

    @Override
    public List<Gateway> getAll(TYPE type, NETWORK_TYPE networkType, Boolean enabled) {
        try {
            if (type == null && networkType == null && enabled == null) {
                return this.getDao().queryForAll();
            }
            QueryBuilder<Gateway, Integer> queryBuilder = this.getDao().queryBuilder();
            Where<Gateway, Integer> where = queryBuilder.where();
            where.gt("id", 0);//This line is used to add and() for above inputs
            if (enabled != null) {
                where.and().eq(Gateway.KEY_ENABLED, enabled);
            }
            if (type != null) {
                where.and().eq(Gateway.KEY_TYPE, type);
            }
            if (networkType != null) {
                where.and().eq(Gateway.KEY_NETWORK_TYPE, networkType);
            }
            queryBuilder.setWhere(where);
            List<Gateway> gateways = this.getDao().query(queryBuilder.prepare());
            return gateways;
        } catch (SQLException ex) {
            _logger.error("unable to get all gateways:[type:{}, NetworkType:{}, Enabled:{}]",
                    type.getText(), networkType, enabled, ex);
            return null;
        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            return this.getQueryResponse(query, Gateway.KEY_ID);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public Gateway get(Gateway tdao) {
        return this.getById(tdao.getId());
    }

    @Override
    public List<Gateway> getAll(List<Integer> ids) {
        return getAll(Gateway.KEY_ID, ids);
    }

}
