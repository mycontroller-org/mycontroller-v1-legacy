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
package org.mycontroller.standalone.units;

import org.mycontroller.standalone.units.UnitUtils.UNIT_TYPE;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Builder
@Getter
@ToString
public class Unit {
    private UNIT_TYPE type;
    private String unit;
    private String unitLow;
    private String unitHigh;
    private Double limitLow;
    private Double limitHigh;
    private Double mtplLow; // Multiplier low
    private Double divHigh; //Divider high

    public static Unit get(Object... args) {
        return Unit.builder()
                .type((UNIT_TYPE) args[0])
                .unit((String) args[1])
                .unitLow((String) args[2])
                .unitHigh((String) args[3])
                .limitLow((Double) args[4])
                .limitHigh((Double) args[5])
                .mtplLow((Double) args[6])
                .divHigh((Double) args[7])
                .build();
    }
}
