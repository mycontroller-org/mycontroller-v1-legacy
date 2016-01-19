/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
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
    public void create(Node node) {
        try {

            int count = this.getDao().create(node);
            _logger.debug("Created Node:[{}], Create count:{}", node, count);

        } catch (SQLException ex) {
            _logger.error("unable to add Node:[{}]", node, ex);
        }
    }

    @Override
    public void delete(Node node) {
        try {
            int count = 0;
            if (node.getId() != null) {
                count = this.getDao().delete(node);
            } else {
                DeleteBuilder<Node, Integer> deleteBuilder = this.getDao().deleteBuilder();
                deleteBuilder.where().eq(Node.KEY_GATEWAY_ID, node.getGateway());
                if (node.getEui() != null) {
                    deleteBuilder.where().and().eq("eui", node.getEui());
                }
                count = deleteBuilder.delete();
            }
            _logger.debug("Node:[{}] deleted, Delete count:{}", node, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete node:[{}]", node, ex);
        }
    }

    @Override
    public void delete(Integer gatewayId, String nodeEui) {
        this.delete(new Node(nodeEui, new Gateway(gatewayId)));
    }

    @Override
    public void delete(Integer id) {
        this.delete(new Node(id));
    }

    @Override
    public void update(Node node) {
        try {
            int count = this.getDao().update(node);
            _logger.debug("Updated Node:[{}], Update count:{}", node, count);
        } catch (SQLException ex) {
            _logger.error("unable to update node:[{}]", node, ex);
        }
    }

    @Override
    public List<Node> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all Nodes", ex);
            return null;
        }
    }

    @Override
    public List<Node> getAll(Integer gatewayId) {
        try {
            if (gatewayId != null) {
                return this.getDao().queryForEq(Node.KEY_GATEWAY_ID, gatewayId);
            } else {
                return this.getDao().queryForAll();
            }
        } catch (SQLException ex) {
            _logger.error("unable to get all Nodes", ex);
            return null;
        }
    }

    @Override
    public Node get(Node node) {
        if (node.getId() != null) {
            return this.get(node.getId());
        } else {
            try {
                QueryBuilder<Node, Integer> queryBuilder = this.getDao().queryBuilder();
                queryBuilder.where().eq(Node.KEY_GATEWAY_ID, node.getGateway()).and().eq("eui", node.getEui());
                return queryBuilder.queryForFirst();
            } catch (SQLException ex) {
                _logger.error("unable to get Node", ex);
                return null;
            }
        }
    }

    @Override
    public Node get(Integer gatewayId, String nodeEui) {
        return get(new Node(nodeEui, new Gateway(gatewayId)));
    }

    @Override
    public Node get(Integer id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get Node", ex);
            return null;
        }
    }

    @Override
    public List<Node> getByName(String nodeName) {
        try {

            return this.getDao().queryForEq("name", nodeName);
        } catch (SQLException ex) {
            _logger.error("unable to get node by name", ex);
            return null;
        }
    }

    @Override
    public void createOrUpdate(Node node) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(node);
            _logger.debug("CreateOrUpdate Node:[{}],Create:{},Update:{},Lines Changed:{}", node, status.isCreated(),
                    status.isUpdated(), status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate Node:[{}]", node, ex);
        }
    }

    @Override
    public long countOf(Integer gatewayId) {
        try {
            QueryBuilder<Node, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(Node.KEY_GATEWAY_ID, gatewayId);
            return queryBuilder.countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get Node count:[GatewayId:{}]", gatewayId, ex);
        }
        return 0;
    }

    @Override
    public void delete(List<Integer> nodeIds) {
        try {
            int count = 0;
            if (nodeIds != null) {
                DeleteBuilder<Node, Integer> deleteBuilder = this.getDao().deleteBuilder();
                deleteBuilder.where().in(Node.KEY_ID, nodeIds);
                count = deleteBuilder.delete();
            }
            _logger.debug("NodeIds:[{}] deleted, Delete count:{}", nodeIds, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete nodeIds:[{}]", nodeIds, ex);
        }
    }

    @Override
    public List<Node> get(List<Integer> ids) {
        try {
            if (ids != null) {
                QueryBuilder<Node, Integer> queryBuilder = this.getDao().queryBuilder();
                queryBuilder.where().in(Node.KEY_ID, ids);
                return queryBuilder.query();
            }
        } catch (SQLException ex) {
            _logger.error("unable to get node for Ids:[{}]", ids, ex);
        }
        return null;
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
}
