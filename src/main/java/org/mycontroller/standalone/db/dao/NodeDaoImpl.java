/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.db.tables.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class NodeDaoImpl extends BaseAbstractDao<Node, Integer> implements NodeDao {
    private static final Logger _logger = LoggerFactory.getLogger(NodeDaoImpl.class);

    public NodeDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Node.class);
    }

    @Override
    public void create(Node node) {
        try {
            if (node.getId() < 255 && node.getId() >= 0) {
                int count = this.getDao().create(node);
                _logger.debug("Created Node:[{}], Create count:{}", node, count);
            } else {
                _logger.warn("Node:[{}], Node Id should be in the range of 0~254", node);
            }
        } catch (SQLException ex) {
            _logger.error("unable to add Node:[{}]", node, ex);
        }
    }

    @Override
    public void delete(Node node) {
        try {
            int count = this.getDao().delete(node);
            _logger.debug("Node:[{}] deleted, Delete count:{}", node, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete node:[{}]", node, ex);
        }
    }

    @Override
    public void delete(int nodeId) {
        Node node = new Node(nodeId);
        this.delete(node);
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
    public Node get(Node node) {
        return this.get(node.getId());
    }

    @Override
    public Node get(int nodeId) {
        try {
            return this.getDao().queryForId(nodeId);
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
}
