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

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class GatewayDaoImpl extends BaseAbstractDaoImpl<GatewayTable, Integer> implements GatewayDao {
    public GatewayDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, GatewayTable.class);
    }

    @Override
    public List<GatewayTable> getAllEnabled() {
        return getAll(null, null, true);
    }

    @Override
    public List<GatewayTable> getAll(GATEWAY_TYPE gatewayType, NETWORK_TYPE networkType) {
        return getAll(gatewayType, networkType, null);
    }

    @Override
    public List<GatewayTable> getAll(GATEWAY_TYPE gatewayType, NETWORK_TYPE networkType, Boolean enabled) {
        try {
            if (gatewayType == null && networkType == null && enabled == null) {
                return this.getDao().queryForAll();
            }
            QueryBuilder<GatewayTable, Integer> queryBuilder = this.getDao().queryBuilder();
            Where<GatewayTable, Integer> where = queryBuilder.where();
            where.gt("id", 0);//This line is used to add and() for above inputs
            if (enabled != null) {
                where.and().eq(GatewayTable.KEY_ENABLED, enabled);
            }
            if (gatewayType != null) {
                where.and().eq(GatewayTable.KEY_TYPE, gatewayType);
            }
            if (networkType != null) {
                where.and().eq(GatewayTable.KEY_NETWORK_TYPE, networkType);
            }
            queryBuilder.setWhere(where);
            List<GatewayTable> gateways = this.getDao().query(queryBuilder.prepare());
            return gateways;
        } catch (SQLException ex) {
            _logger.error("unable to get all gateways:[type:{}, NetworkType:{}, Enabled:{}]",
                    gatewayType != null ? gatewayType.getText() : null, networkType, enabled, ex);
            return null;
        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(GatewayTable.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public GatewayTable get(GatewayTable tdao) {
        return this.getById(tdao.getId());
    }

    @Override
    public List<GatewayTable> getAll(List<Integer> ids) {
        return getAll(GatewayTable.KEY_ID, ids);
    }

    public List<GatewayTable> getAll(Query query, String filter, AllowedResources allowedResources) {
        AuthUtils.updateQueryFilter(query.getFilters(), RESOURCE_TYPE.GATEWAY, allowedResources);
        query.getFilters().put(GatewayTable.KEY_NAME, filter);
        query.setAndQuery(false);
        return super.getAllData(query);
    }
}
