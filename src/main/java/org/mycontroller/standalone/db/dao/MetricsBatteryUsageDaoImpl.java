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

import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsBatteryUsageDaoImpl extends BaseAbstractDaoImpl<MetricsBatteryUsage, Integer> implements
        MetricsBatteryUsageDao {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsBatteryUsageDaoImpl.class);

    public MetricsBatteryUsageDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsBatteryUsage.class);
    }

    @Override
    public void deletePrevious(MetricsBatteryUsage metric) {
        try {
            DeleteBuilder<MetricsBatteryUsage, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBatteryUsage.KEY_NODE_ID, metric.getNode().getEui())
                    .and().lt(MetricsBatteryUsage.KEY_TIMESTAMP, metric.getTimestamp());
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deleteByNodeRefId(int nodeId) {
        try {
            DeleteBuilder<MetricsBatteryUsage, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBatteryUsage.KEY_NODE_ID, nodeId);
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric-nodeEui:[{}] deleted, Delete count:{}", nodeId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-nodeEui:[{}]", nodeId, ex);
        }
    }

    @Override
    public List<MetricsBatteryUsage> getAllAfter(MetricsBatteryUsage metric) {
        try {
            return this.getDao().query(
                    this.getDao()
                            .queryBuilder()
                            .where().eq(MetricsBatteryUsage.KEY_NODE_ID, metric.getNode().getEui())
                            .and().ge(MetricsBatteryUsage.KEY_TIMESTAMP, metric.getTimestamp())
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public List<MetricsBatteryUsage> getAll(int nodeId) {
        try {
            return this.getDao().query(
                    this.getDao()
                            .queryBuilder()
                            .where().eq(MetricsBatteryUsage.KEY_NODE_ID, nodeId)
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to getAll, nodeEui:{}", nodeId, ex);
        }
        return null;
    }

    @Override
    public MetricsBatteryUsage get(MetricsBatteryUsage metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(MetricsBatteryUsage.KEY_NODE_ID, metric.getNode().getEui())
                            .and().eq(MetricsBatteryUsage.KEY_TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public MetricsBatteryUsage getLast(int nodeId) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder().limit(1l).orderBy("id", false)
                            .where().eq(MetricsBatteryUsage.KEY_NODE_ID, nodeId)
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to getLast, nodeEui:{}", nodeId, ex);
        }
        return null;
    }

    @Override
    public List<MetricsBatteryUsage> getAll(List<Integer> ids) {
        return getAll(MetricsBatteryUsage.KEY_ID, ids);
    }
}
