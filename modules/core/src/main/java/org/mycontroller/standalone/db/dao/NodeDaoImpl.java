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
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class NodeDaoImpl extends BaseAbstractDaoImpl<Node, Integer> implements NodeDao {

    public NodeDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Node.class);
    }

    @Override
    public List<Node> getAllByGatewayId(Integer gatewayId) {
        return super.getAll(Node.KEY_GATEWAY_ID, gatewayId);
    }

    @Override
    public Node get(Node node) {
        return super.getById(node.getId());
    }

    @Override
    public Node get(Integer gatewayId, String nodeEui) {
        QueryBuilder<Node, Integer> queryBuilder = null;
        try {
            queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(Node.KEY_GATEWAY_ID, gatewayId).and().eq(Node.KEY_EUI, nodeEui);
            return queryBuilder.queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get Node. gatewayId:{}, nodeEui:{}", gatewayId, nodeEui, ex);
            try {
                _logger.error("PrepareStatement:[{}]", queryBuilder.prepareStatementString());
            } catch (SQLException qEx) {
                _logger.error("Exception on prepareStatement,", qEx);
            }
            return null;
        }
    }

    @Override
    public long countOf(Integer gatewayId) {
        return super.countOf(Node.KEY_GATEWAY_ID, gatewayId);
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(Node.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> getAllIds(Query query) {
        List<Integer> ids = new ArrayList<Integer>();
        try {
            query.setIdColumn(Node.KEY_ID);
            QueryResponse queryResponse = this.getQueryResponse(query);
            for (Node node : (List<Node>) queryResponse.getData()) {
                ids.add(node.getId());
            }
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
        return ids;
    }

    @Override
    public List<Node> getAll(List<Integer> ids) {
        return super.getAll(Node.KEY_ID, ids);
    }

    @Override
    public List<Integer> getNodeIdsByGatewayIds(List<Integer> ids) {
        List<Node> nodes = super.getAll(Node.KEY_GATEWAY_ID, ids);
        List<Integer> nodeIds = new ArrayList<Integer>();
        for (Node node : nodes) {
            nodeIds.add(node.getId());
        }
        return nodeIds;
    }

    public List<Node> getAll(Query query, String filter, AllowedResources allowedResources) {
        AuthUtils.updateQueryFilter(query.getFilters(), RESOURCE_TYPE.NODE, allowedResources);
        if (query.getFilters().get(Node.KEY_GATEWAY_ID) == null) {
            query.setAndQuery(false);
            if (filter != null) {
                query.getFilters().put(Node.KEY_EUI, filter);
                List<GatewayTable> gateways = DaoUtils.getGatewayDao().getAll(query, filter, null);
                if (gateways.size() > 0) {
                    ArrayList<Integer> gatewayIds = new ArrayList<Integer>();
                    for (GatewayTable gateway : gateways) {
                        gatewayIds.add(gateway.getId());
                    }
                    query.getFilters().put(Node.KEY_GATEWAY_ID, gatewayIds);
                    query.getFilters().put(Node.KEY_NAME, filter);
                }
                MESSAGE_TYPE_PRESENTATION type = MESSAGE_TYPE_PRESENTATION.fromString(filter);
                if (type != null) {
                    query.getFilters().put(Node.KEY_TYPE, type);
                }
            }
        }
        query.setIdColumn(Node.KEY_ID);
        query.setOrderBy(Node.KEY_EUI);
        query.setOrder(Query.ORDER_ASC);
        return super.getAllData(query);
    }

    @Override
    public void update(String key, Object value, Integer nodeId) {
        super.updateBulk(key, value, Node.KEY_ID, nodeId);
    }
}
