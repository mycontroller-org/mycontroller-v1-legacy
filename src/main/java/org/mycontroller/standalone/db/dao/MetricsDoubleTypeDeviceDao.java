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

import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public interface MetricsDoubleTypeDeviceDao {
    void create(MetricsDoubleTypeDevice metric);
    void createOrUpdate(MetricsDoubleTypeDevice metric);
    void delete(MetricsDoubleTypeDevice metric);
    void deletePrevious(MetricsDoubleTypeDevice metric);
    void deleteBySensorRefId(int sensorRefId);
    void update(MetricsDoubleTypeDevice metric);
    List<MetricsDoubleTypeDevice> getAll(MetricsDoubleTypeDevice metric);
    List<MetricsDoubleTypeDevice> getAllAfter(MetricsDoubleTypeDevice metric);
    List<MetricsDoubleTypeDevice> getAll();
    MetricsDoubleTypeDevice get(MetricsDoubleTypeDevice metric);
}
