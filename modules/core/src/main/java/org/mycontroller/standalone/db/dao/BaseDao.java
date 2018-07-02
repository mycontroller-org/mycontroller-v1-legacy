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

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableInfo;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface BaseDao<Tdao, Tid> {
    void create(Tdao tdao);

    void createOrUpdate(Tdao tdao);

    void delete(Tdao tdao);

    void deleteById(Tid id);

    void delete(String key, Object value);

    void delete(String key, List<?> values);

    void update(Tdao tdao);

    void updateId(Tdao tdao, Tid tid);

    void updateBulk(String setColName, Object setColValue);

    void updateBulk(String setColName, Object setColValue, String whereColName, Object whereColValue);

    List<Tdao> getAll();

    Tdao get(Tdao tdao);

    Tdao getById(Tid id);

    void deleteByIds(List<Tid> ids);

    List<Tdao> getAll(List<Tid> ids);

    Long countOf();

    List<Tdao> getAll(String key, Object value);

    List<Tdao> getAllData(Query query);

    Tdao get(String key, Object value);

    long countOf(HashMap<String, Object> columnValues);

    long countOf(String key, Object data);

    Dao<Tdao, Tid> getDao();

    TableInfo<Tdao, Tid> getTableInfo();

    void delete(HashMap<String, Object> map);

}
