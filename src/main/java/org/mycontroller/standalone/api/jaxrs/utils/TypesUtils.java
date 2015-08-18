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
package org.mycontroller.standalone.api.jaxrs.utils;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.mapper.TypesIdNameMapper;
import org.mycontroller.standalone.db.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.AlarmUtils.TRIGGER;
import org.mycontroller.standalone.db.AlarmUtils.TYPE;
import org.mycontroller.standalone.db.TimerUtils.FREQUENCY;
import org.mycontroller.standalone.db.TimerUtils.WEEK_DAY;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class TypesUtils {
    private TypesUtils() {

    }

    public static ArrayList<TypesIdNameMapper> getSensorValueTypes() {
        MESSAGE_TYPE_SET_REQ[] types = MESSAGE_TYPE_SET_REQ.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_SET_REQ type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.name()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.name()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmTriggerTypes() {
        TRIGGER[] triggers = TRIGGER.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (TRIGGER trigger : triggers) {
            typesIdNameMappers.add(new TypesIdNameMapper(trigger.ordinal(), trigger.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmTypes() {
        TYPE[] types = TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (TYPE type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmDampeningTypes() {
        DAMPENING_TYPE[] types = DAMPENING_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (DAMPENING_TYPE type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerTypes() {
        org.mycontroller.standalone.db.TimerUtils.TYPE[] types = org.mycontroller.standalone.db.TimerUtils.TYPE
                .values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (org.mycontroller.standalone.db.TimerUtils.TYPE type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerFrequencies() {
        FREQUENCY[] types = FREQUENCY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (FREQUENCY type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerDays(boolean isAllDaysTicked) {
        WEEK_DAY[] types = WEEK_DAY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (WEEK_DAY type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value(), isAllDaysTicked));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodes() {
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Node node : nodes) {
            typesIdNameMappers.add(new TypesIdNameMapper(node.getId(), "[" + node.getId() + "] " + node.getName()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensors(int nodeId) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAll(nodeId);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Sensor sensor : sensors) {
            typesIdNameMappers.add(new TypesIdNameMapper(sensor.getId(), sensor.getSensorId(), "["
                    + sensor.getSensorId() + "] " + sensor.getName()));
        }
        return typesIdNameMappers;
    }
}
