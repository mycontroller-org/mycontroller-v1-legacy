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
package org.mycontroller.standalone.db.dao;

import java.util.List;

import org.mycontroller.standalone.db.tables.Firmware;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public interface FirmwareDao {
    void create(Firmware firmware);
    void createOrUpdate(Firmware firmware);
    void delete(Firmware firmware);
    void delete(int id);
    void delete(Integer typeId, Integer versionId);
    void update(Firmware firmware);
    List<Firmware> getAll();
    Firmware get(Firmware firmware);
    Firmware get(int id);
    Firmware get(Integer typeId, Integer versionId);
}
