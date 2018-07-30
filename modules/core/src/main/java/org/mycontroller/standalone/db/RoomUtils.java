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
package org.mycontroller.standalone.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.db.tables.Sensor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomUtils {
    public static String getFullPath(Room room) {
        ArrayList<String> names = new ArrayList<String>();
        addNamePath(names, room);
        StringBuilder builder = new StringBuilder();
        Collections.reverse(names);//fix name order in correct order
        for (String name : names) {
            if (builder.length() > 0) {
                builder.append(" >> ");
            }
            builder.append(name);
        }
        return builder.toString();
    }

    private static void addNamePath(ArrayList<String> names, Room room) {
        names.add(room.getName());
        if (room.getParentId() != null) {
            room = DaoUtils.getRoomDao().getById(room.getParentId());
            if (room != null) {
                addNamePath(names, room);
            }
        }
    }

    public static void createOrUpdate(Room room, List<Integer> sensorIds) {
        if (room.getId() != null) {
            //Update room
            DaoUtils.getRoomDao().update(room);
        } else {
            //Create room
            DaoUtils.getRoomDao().create(room);
            //update created room id
            room = DaoUtils.getRoomDao().getByName(room.getName());
        }

        //Update sensors
        if (sensorIds != null && !sensorIds.isEmpty()) {
            //clear all old mapping
            removeSensorMapping(room.getId());
            List<Sensor> sensors = DaoUtils.getSensorDao().getAll(sensorIds);
            for (Sensor sensor : sensors) {
                sensor.setRoom(room);
                DaoUtils.getSensorDao().update(sensor);
            }
        }

    }

    public static void delete(List<Integer> ids) {
        for (Integer id : ids) {
            removeSensorMapping(id);
            removeParentMapping(id);
        }
        DaoUtils.getRoomDao().deleteByIds(ids);
    }

    private static void removeSensorMapping(Integer id) {
        //clear all old mapping
        List<Sensor> sensors = DaoUtils.getSensorDao().getAllByRoomId(id);
        for (Sensor sensor : sensors) {
            sensor.setRoom(null);
            DaoUtils.getSensorDao().update(sensor);
        }
    }

    private static void removeParentMapping(Integer id) {
        //clear all old mapping
        List<Room> rooms = DaoUtils.getRoomDao().getByParentId(id);
        for (Room room : rooms) {
            room.setParentId(null);
            DaoUtils.getRoomDao().update(room);
        }
    }
}
