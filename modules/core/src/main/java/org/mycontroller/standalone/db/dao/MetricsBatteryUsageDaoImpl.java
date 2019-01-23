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

import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.exceptions.McDatabaseException;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class MetricsBatteryUsageDaoImpl extends BaseAbstractDaoImpl<MetricsBatteryUsage, Object> implements
        MetricsBatteryUsageDao {

    public MetricsBatteryUsageDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsBatteryUsage.class);
    }

    @Override
    public void deletePrevious(MetricsBatteryUsage metric) {
        try {
            DeleteBuilder<MetricsBatteryUsage, Object> deleteBuilder = this.getDao().deleteBuilder();
            Where<MetricsBatteryUsage, Object> where = deleteBuilder.where();
            int whereCount = 0;
            if (metric.getTimestamp() != null) {
                where.le(MetricsBatteryUsage.KEY_TIMESTAMP, metric.getTimestamp());
                whereCount++;
            }
            if (metric.getAggregationType() != null) {
                where.le(MetricsBatteryUsage.KEY_AGGREGATION_TYPE, metric.getAggregationType());
                whereCount++;
            }
            if (whereCount > 0) {
                where.and(whereCount);
                deleteBuilder.setWhere(where);
            }

            int count = deleteBuilder.delete();
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void deleteByNodeId(int nodeId) {
        try {
            DeleteBuilder<MetricsBatteryUsage, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBatteryUsage.KEY_NODE_ID, nodeId);
            int count = deleteBuilder.delete();
            _logger.debug("Metric-nodeId:[{}] deleted, Delete count:{}", nodeId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-nodeId:[{}]", nodeId, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<MetricsBatteryUsage> getAll(MetricsBatteryUsage metric) {
        try {
            QueryBuilder<MetricsBatteryUsage, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsBatteryUsage, Object> whereBuilder = queryBuilder.where();
            whereBuilder.eq(MetricsBatteryUsage.KEY_NODE_ID,
                    metric.getNode().getId());
            if (metric.getAggregationType() != null) {
                whereBuilder.and().eq(MetricsBatteryUsage.KEY_AGGREGATION_TYPE,
                        metric.getAggregationType());
            }
            if (metric.getStart() != null) {
                whereBuilder.and().gt(MetricsBatteryUsage.KEY_TIMESTAMP,
                        metric.getStart());
            }
            if (metric.getEnd() != null) {
                whereBuilder.and().le(MetricsBatteryUsage.KEY_TIMESTAMP,
                        metric.getEnd());
            }
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public MetricsBatteryUsage get(MetricsBatteryUsage metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where()
                            .eq(MetricsBatteryUsage.KEY_NODE_ID, metric.getNode().getId())
                            .and().eq(MetricsBatteryUsage.KEY_AGGREGATION_TYPE, metric.getAggregationType())
                            .and().eq(MetricsBatteryUsage.KEY_TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<MetricsBatteryUsage> getAll(List<Object> ids) {
        return null;
    }

    @Override
    public List<MetricsBatteryUsage> getAggregationRequiredNodeIds(AGGREGATION_TYPE aggregationType,
            Long fromTimestamp, Long toTimestamp) {
        QueryBuilder<MetricsBatteryUsage, Object> queryBuilder = this.getDao().queryBuilder();
        try {
            return queryBuilder.distinct().selectColumns(MetricsBatteryUsage.KEY_NODE_ID)
                    .where().eq(MetricsBatteryUsage.KEY_AGGREGATION_TYPE, aggregationType).and()
                    .gt(MetricsBatteryUsage.KEY_TIMESTAMP, fromTimestamp).and()
                    .le(MetricsBatteryUsage.KEY_TIMESTAMP, toTimestamp)
                    .query();
        } catch (SQLException ex) {
            _logger.error("Exception,", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public long countOf(AGGREGATION_TYPE aggregationType, long start, long end) {
        QueryBuilder<MetricsBatteryUsage, Object> queryBuilder = getDao().queryBuilder();
        try {
            return queryBuilder.where().gt(MetricsBatteryUsage.KEY_TIMESTAMP, start).and()
                    .le(MetricsBatteryUsage.KEY_TIMESTAMP, end).and()
                    .eq(MetricsBatteryUsage.KEY_AGGREGATION_TYPE, aggregationType).countOf();
        } catch (Exception ex) {
            _logger.error("Unable to execute countOf query", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public boolean isRecordFound(AGGREGATION_TYPE aggregationType, long start, long end) {
        QueryBuilder<MetricsBatteryUsage, Object> queryBuilder = getDao().queryBuilder();
        try {
            return queryBuilder.where().gt(MetricsBatteryUsage.KEY_TIMESTAMP, start).and()
                    .le(MetricsBatteryUsage.KEY_TIMESTAMP, end).and()
                    .eq(MetricsBatteryUsage.KEY_AGGREGATION_TYPE, aggregationType).queryForFirst() != null;
        } catch (Exception ex) {
            _logger.error("Unable to execute countOf query", ex);
            throw new McDatabaseException(ex);
        }
    }
}
