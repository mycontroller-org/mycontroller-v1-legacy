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

import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsBatteryUsageDaoImpl extends BaseAbstractDao<MetricsBatteryUsage, Integer> implements
        MetricsBatteryUsageDao {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsBatteryUsageDaoImpl.class);

    public MetricsBatteryUsageDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsBatteryUsage.class);
    }

    @Override
    public void create(MetricsBatteryUsage metric) {
        try {
            MetricsBatteryUsage lastMetric = this.getLast(metric.getNode().getId());
            if (lastMetric != null && (lastMetric.getValue() == metric.getValue())) {
                _logger.info("There is no change with last value, nothing to update. Last:[{}], New:[{}]",
                        lastMetric, metric);
            } else {
                int count = this.getDao().create(metric);
                _logger.debug("Created Metric:[{}], Create count:{}", metric, count);
            }
        } catch (SQLException ex) {
            _logger.error("unable to add Metric:[{}]", metric, ex);
        }
    }

    @Override
    public void createOrUpdate(MetricsBatteryUsage metric) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(metric);
            _logger.debug("CreateOrUpdate Metric:[{}],Create:{},Update:{},Lines Changed:{}",
                    metric, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to createOrUpdate Metric:[{}]", metric, ex);
        }
    }

    @Override
    public void delete(MetricsBatteryUsage metric) {
        try {
            int count = this.getDao().delete(metric);
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deletePrevious(MetricsBatteryUsage metric) {
        try {
            DeleteBuilder<MetricsBatteryUsage, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBatteryUsage.NODE_REF_ID, metric.getNode().getId())
                    .and().lt(MetricsBatteryUsage.TIMESTAMP, metric.getTimestamp());
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deleteByNodeRefId(int nodeRefId) {
        try {
            DeleteBuilder<MetricsBatteryUsage, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBatteryUsage.NODE_REF_ID, nodeRefId);
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric-nodeRefId:[{}] deleted, Delete count:{}", nodeRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-nodeRefId:[{}]", nodeRefId, ex);
        }
    }

    @Override
    public void update(MetricsBatteryUsage metric) {
        try {
            int count = this.getDao().update(metric);
            _logger.debug("Metric:[{}] updated, Update count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to update metric:[{}]", metric, ex);
        }
    }

    @Override
    public List<MetricsBatteryUsage> getAllAfter(MetricsBatteryUsage metric) {
        try {
            return this.getDao().query(
                    this.getDao()
                            .queryBuilder()
                            .where().eq(MetricsBatteryUsage.NODE_REF_ID, metric.getNode().getId())
                            .and().ge(MetricsBatteryUsage.TIMESTAMP, metric.getTimestamp())
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public List<MetricsBatteryUsage> getAll(int nodeRefId) {
        try {
            return this.getDao().query(
                    this.getDao()
                            .queryBuilder()
                            .where().eq(MetricsBatteryUsage.NODE_REF_ID, nodeRefId)
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to getAll, nodeRefId:{}", nodeRefId, ex);
        }
        return null;
    }

    @Override
    public MetricsBatteryUsage get(MetricsBatteryUsage metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(MetricsBatteryUsage.NODE_REF_ID, metric.getNode().getId())
                            .and().eq(MetricsBatteryUsage.TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public MetricsBatteryUsage getLast(int nodeRefId) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder().limit(1l).orderBy("id", false)
                            .where().eq(MetricsBatteryUsage.NODE_REF_ID, nodeRefId)
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to getLast, nodeRefId:{}", nodeRefId, ex);
        }
        return null;
    }
}
