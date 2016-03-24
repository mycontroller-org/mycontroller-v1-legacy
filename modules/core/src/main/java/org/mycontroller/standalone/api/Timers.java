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
package org.mycontroller.standalone.api;

import java.util.List;

import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.timer.TimerUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class Timers {

    public Timer get(int id) {
        return DaoUtils.getTimerDao().getById(id);
    }

    public QueryResponse getAll(Query query) {
        return DaoUtils.getTimerDao().getAll(query);
    }

    public void update(Timer timer) {
        TimerUtils.updateTimer(timer);
    }

    public void add(Timer timer) {
        TimerUtils.addTimer(timer);
    }

    public void delete(List<Integer> ids) {
        TimerUtils.deleteTimers(ids);
    }

    public void enable(List<Integer> ids) {
        TimerUtils.enableTimers(ids);
    }

    public void disable(List<Integer> ids) {
        TimerUtils.disableTimers(ids);
    }

}
