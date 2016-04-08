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
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.tables.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class NodeDaoImpl extends BaseAbstractDaoImpl<Node, Integer> implements NodeDao {
    private static final Logger _logger = LoggerFactory.getLogger(NodeDaoImpl.class);

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
        try {
            QueryBuilder<Node, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(Node.KEY_GATEWAY_ID, gatewayId).and().eq(Node.KEY_EUI, nodeEui);
            return queryBuilder.queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get Node", ex);
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
            return this.getQueryResponse(query, Node.KEY_ID);
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
            QueryResponse queryResponse = this.getQueryResponse(query, Node.KEY_ID);
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

}
