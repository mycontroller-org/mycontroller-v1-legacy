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
package org.mycontroller.standalone.api;

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.RoomUtils;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class RoomApi {
    public Room getRoom(String... roomsName) {
        Integer parentId = null;
        Room roomTmp = null;
        for (String roomName : roomsName) {
            roomTmp = null;
            if (parentId != null) {
                List<Room> roomsList = DaoUtils.getRoomDao().getByParentId(parentId);
                if (roomsList == null) {
                    return null;
                }
                for (Room room : roomsList) {
                    if (room.getName().equals(roomName)) {
                        roomTmp = room;
                    }
                }
                if (roomTmp != null) {
                    parentId = roomTmp.getId();
                } else {
                    return null;
                }
            } else {
                roomTmp = DaoUtils.getRoomDao().getByName(roomName);
                if (roomTmp != null) {
                    parentId = roomTmp.getId();
                } else {
                    return null;
                }
            }
        }
        return roomTmp;
    }

    public QueryResponse getAll(HashMap<String, Object> filters) {
        return DaoUtils.getRoomDao().getAll(Query.get(filters));
    }

    public Room get(Integer id) {
        return DaoUtils.getRoomDao().getById(id);
    }

    public void delete(List<Integer> ids) {
        RoomUtils.delete(ids);
    }

    public void createOrUpdate(Room room) throws McDuplicateException, McBadRequestException {
        createOrUpdate(room, null);
    }

    public void createOrUpdate(Room room, List<Integer> sensorIds) throws McDuplicateException, McBadRequestException {
        Room availabilityCheck = DaoUtils.getRoomDao().getByNameAndParentId(room.getName(), room.getParentId());
        if (availabilityCheck != null && room.getId() != availabilityCheck.getId()) {
            throw new McDuplicateException("A room available with this parent with this name!");
        }
        if (room.getId() != null && room.getParentId() != null) {
            if (DaoUtils.getRoomDao().getChildrenIds(room.getId()).contains(room.getParentId())) {
                throw new McBadRequestException("This room children cannot be it is parent!");
            }
        }
        RoomUtils.createOrUpdate(room, sensorIds);
    }
}
