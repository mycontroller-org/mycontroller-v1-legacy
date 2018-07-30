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

import org.mycontroller.standalone.db.tables.OperationTimerMap;

import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class OperationTimerMapDaoImpl extends BaseAbstractDaoImpl<OperationTimerMap, Object>
        implements OperationTimerMapDao {

    public OperationTimerMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, OperationTimerMap.class);
    }

    @Override
    public OperationTimerMap get(OperationTimerMap tdao) {
        // not supported
        return null;
    }

    @Override
    public List<OperationTimerMap> getAll(List<Object> ids) {
        // not supported
        return null;
    }

    @Override
    public List<OperationTimerMap> getByTimerId(Integer timerId) {
        return super.getAll(OperationTimerMap.KEY_TIMER_ID, timerId);
    }

    @Override
    public List<OperationTimerMap> getByOperationId(Integer operationId) {
        return super.getAll(OperationTimerMap.KEY_OPERATION_ID, operationId);
    }

    @Override
    public void deleteByOperationId(Integer operationId) {
        super.delete(OperationTimerMap.KEY_OPERATION_ID, operationId);
    }

    @Override
    public void deleteByTimerId(Integer timerId) {
        super.delete(OperationTimerMap.KEY_TIMER_ID, timerId);

    }

    @Override
    public List<Integer> getOperationIdsByTimerId(Integer timerId) {
        List<Integer> roleIds = new ArrayList<Integer>();
        try {
            if (timerId != null) {
                List<OperationTimerMap> timerOperationMaps = this.getDao().queryBuilder()
                        .where()
                        .eq(OperationTimerMap.KEY_TIMER_ID, timerId).query();
                for (OperationTimerMap timerOperationMap : timerOperationMaps) {
                    roleIds.add(timerOperationMap.getOperationTable().getId());
                }
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return roleIds;
    }

}
