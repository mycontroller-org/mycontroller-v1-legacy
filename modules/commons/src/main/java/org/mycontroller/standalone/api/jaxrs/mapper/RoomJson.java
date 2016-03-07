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
package org.mycontroller.standalone.api.jaxrs.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.db.tables.Sensor;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@NoArgsConstructor
@ToString
@Data
public class RoomJson {

    private Room room;
    private List<Integer> sensorIds;

    public RoomJson(Room room) {
        this.room = room;
    }

    @JsonIgnore
    public RoomJson mapResources() {
        if (room.getId() != null) {
            sensorIds = new ArrayList<Integer>();
            List<Sensor> sensors = DaoUtils.getSensorDao().getAllByRoomId(room.getId());
            for (Sensor sensor : sensors) {
                sensorIds.add(sensor.getId());
            }
        }
        return this;
    }

    @JsonIgnore
    public void createOrUpdate() {
        if (room.getId() != null) {
            //Update room
            DaoUtils.getRoomDao().update(room);
            //clear all old mapping
            removeMapping(room.getId());
        } else {
            //Create room
            DaoUtils.getRoomDao().create(room);
            //update created room id
            room = DaoUtils.getRoomDao().getByName(room.getName());
        }

        //Update sensors
        if (sensorIds != null && !sensorIds.isEmpty()) {
            List<Sensor> sensors = DaoUtils.getSensorDao().getAll(sensorIds);
            for (Sensor sensor : sensors) {
                sensor.setRoom(room);
                DaoUtils.getSensorDao().update(sensor);
            }
        }
    }

    @JsonIgnore
    public void delete(List<Integer> roomIds) {
        for (Integer id : roomIds) {
            removeMapping(id);
        }
        DaoUtils.getRoomDao().deleteByIds(roomIds);
    }

    @JsonIgnore
    private void removeMapping(Integer id) {
        //clear all old mapping
        List<Sensor> sensors = DaoUtils.getSensorDao().getAllByRoomId(id);
        for (Sensor sensor : sensors) {
            sensor.setRoom(null);
            DaoUtils.getSensorDao().update(sensor);
        }
    }
}
