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
package org.mycontroller.standalone.api.jaxrs.model;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Data
@Builder
@AllArgsConstructor
public class TopologyKinds {
    private Boolean gateway = false;
    private Boolean node = false;
    private Boolean sensor = false;
    private Boolean sensorVariable = false;

    public void update(RESOURCE_TYPE type) {
        switch (type) {
            case GATEWAY:
                gateway = true;
            case NODE:
                node = true;
            case SENSOR:
                sensor = true;
            case SENSOR_VARIABLE:
                sensorVariable = true;
                break;
            default:
                break;
        }
    }
}
