/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.imperihome;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.imperihome.ImperiHomeSSIUtils.DEVICE_PARM_KEY;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private String id;
    private String name;
    private String room;
    private String type;
    private List<DeviceParam> params;

    public Device updateDeviceParm(SensorVariable sensorVariable) {
        if (params == null) {
            params = new ArrayList<DeviceParam>();
        }
        params.add(DeviceParam.builder()
                .key(DEVICE_PARM_KEY.valueOf(sensorVariable.getVariableType().name()).getText())
                .value(sensorVariable.getValue())
                .unit(sensorVariable.getUnit().length() == 0 ? null : sensorVariable.getUnit())
                //.graphable(sensorVariable.getMetricType() == METRIC_TYPE.NONE ? false : true)
                .graphable(false)
                .build());
        return this;
    }
}
