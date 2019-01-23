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

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.exceptions.McDatabaseException;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class RoomDaoImpl extends BaseAbstractDaoImpl<Room, Integer> implements RoomDao {

    public RoomDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Room.class);
    }

    @Override
    public Room get(Room node) {
        return super.getById(node.getId());
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(Room.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<Room> getAll(List<Integer> ids) {
        return super.getAll(Room.KEY_ID, ids);
    }

    @Override
    public void deleteIds(List<Integer> ids) {
        super.deleteByIds(ids);

    }

    @Override
    public List<Room> getByParentId(Integer parentId) {
        if (parentId == null) {
            try {
                return this.getDao().queryBuilder().where().isNull(Room.KEY_PARENT_ID).query();
            } catch (SQLException ex) {
                _logger.error("unable to get parent", ex);
                throw new McDatabaseException(ex);
            }
        } else {
            return super.getAll(Room.KEY_PARENT_ID, parentId);
        }
    }

    @Override
    public Room getByName(String name) {
        List<Room> rooms = super.getAll(Room.KEY_NAME, name);
        if (rooms != null && !rooms.isEmpty()) {
            return rooms.get(0);
        }
        return null;
    }

    @Override
    public Room getByNameAndParentId(String name, Integer parentId) {
        try {
            QueryBuilder<Room, Integer> queryBuilder = this.getDao().queryBuilder();
            Where<Room, Integer> where = queryBuilder.where();
            int whereCount = 0;
            where.eq(Room.KEY_NAME, name);
            whereCount++;
            if (parentId == null) {
                where.isNull(Room.KEY_PARENT_ID);
            } else {
                where.eq(Room.KEY_PARENT_ID, parentId);
            }
            whereCount++;

            where.and(whereCount);

            queryBuilder.setWhere(where);

            List<Room> rooms = queryBuilder.query();
            if (rooms != null && !rooms.isEmpty()) {
                return rooms.get(0);
            }
        } catch (SQLException ex) {
            _logger.error("unable to get room", ex);
            throw new McDatabaseException(ex);
        }
        return null;
    }

    private void updateParentId(List<Integer> parentIds, Room room) {
        if (room == null || room.getParentId() == null) {
            return;
        } else {
            parentIds.add(room.getParentId());
            updateParentId(parentIds, this.getById(room.getParentId()));
        }
    }

    @Override
    public List<Integer> getParentIds(Integer id) {
        List<Integer> parentIds = new ArrayList<Integer>();
        updateParentId(parentIds, this.getById(id));
        return parentIds;
    }

    @Override
    public List<Integer> getChildrenIds(Integer id) {
        List<Integer> childrenIds = new ArrayList<Integer>();
        List<Room> children = this.getByParentId(id);
        if (children != null && !children.isEmpty()) {
            for (Room room : children) {
                childrenIds.add(room.getId());
            }
        }
        return childrenIds;
    }

}
