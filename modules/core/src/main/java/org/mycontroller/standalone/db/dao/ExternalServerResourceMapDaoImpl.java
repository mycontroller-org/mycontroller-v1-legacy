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
import java.util.List;

import org.mycontroller.standalone.db.tables.ExternalServerResourceMap;

import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class ExternalServerResourceMapDaoImpl extends BaseAbstractDaoImpl<ExternalServerResourceMap, Object>
        implements ExternalServerResourceMapDao {
    public ExternalServerResourceMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ExternalServerResourceMap.class);
    }

    @Override
    public void deleteByResourceId(Integer resourceId) {
        super.delete(ExternalServerResourceMap.KEY_RESOURCE_ID, resourceId);
    }

    @Override
    public void deleteByExternalServerId(Integer externalServerId) {
        super.delete(ExternalServerResourceMap.KEY_EXTERNAL_SERVER_ID, externalServerId);

    }

    @Override
    public List<ExternalServerResourceMap> getAllByResourceId(Integer id) {
        return super.getAll(ExternalServerResourceMap.KEY_RESOURCE_ID, id);
    }

    @Override
    public List<ExternalServerResourceMap> getAllByExtServerId(Integer id) {
        return super.getAll(ExternalServerResourceMap.KEY_EXTERNAL_SERVER_ID, id);
    }

    @Override
    public ExternalServerResourceMap get(ExternalServerResourceMap tdao) {
        // Not supported
        return null;
    }

    @Override
    public List<ExternalServerResourceMap> getAll(List<Object> ids) {
        // Not supported
        return null;
    }

}
