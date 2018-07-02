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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.settings.Variable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class VariableApi {

    public QueryResponse getAll(HashMap<String, Object> filters) {
        filters.put(Settings.KEY_SUB_KEY, filters.get(Variable.SKEY_KEY));
        filters.put(Settings.KEY_KEY, Variable.KEY_VARIABLES_REPOSITORY);
        Query query = Query.get(filters);
        if (query.getOrderBy().equals(Variable.SKEY_KEY)) {
            query.setOrderBy(Settings.KEY_SUB_KEY);
        }
        QueryResponse qResponse = DaoUtils.getSettingsDao().getAll(query, Settings.KEY_KEY);
        @SuppressWarnings("unchecked")
        List<Settings> settingsList = (List<Settings>) qResponse.getData();
        List<Variable> variables = new ArrayList<Variable>();
        for (Settings settings : settingsList) {
            variables.add(Variable.get(settings));
        }
        qResponse.setData(variables);
        if (qResponse.getQuery().getOrderBy().equals(Settings.KEY_SUB_KEY)) {
            qResponse.getQuery().setOrderBy(Variable.SKEY_KEY);
        }
        filters.put(Variable.SKEY_KEY, filters.get(Settings.KEY_SUB_KEY));
        filters.remove(Settings.KEY_SUB_KEY);
        return qResponse;
    }

    public Variable get(String key) {
        return Variable.get(key);
    }

    public Variable get(Integer id) {
        return Variable.get(id);
    }

    public void update(Variable variable) {
        if (variable != null) {
            variable.save();
        }
    }

    public void delete(List<Integer> ids) {
        for (Integer id : ids) {
            Settings settings = DaoUtils.getSettingsDao().getById(id);
            if (settings != null && settings.getKey().equals(Variable.KEY_VARIABLES_REPOSITORY)) {
                DaoUtils.getSettingsDao().deleteById(settings.getId());
            }
        }
    }

}
