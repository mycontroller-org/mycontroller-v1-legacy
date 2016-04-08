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
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.tables.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class RoomDaoImpl extends BaseAbstractDaoImpl<Room, Integer> implements RoomDao {
    private static final Logger _logger = LoggerFactory.getLogger(RoomDaoImpl.class);

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
            return this.getQueryResponse(query, Room.KEY_ID);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
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
    public Room getByName(String name) {
        List<Room> rooms = super.getAll(Room.KEY_NAME, name);
        if (rooms != null && !rooms.isEmpty()) {
            return rooms.get(0);
        }
        return null;
    }

}
