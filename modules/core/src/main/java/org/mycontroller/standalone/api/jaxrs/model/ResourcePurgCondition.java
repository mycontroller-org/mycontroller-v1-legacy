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

import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf.OPERATOR;
import org.mycontroller.standalone.utils.McUtils;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.4.0
 */

@Getter
@ToString
public class ResourcePurgCondition {
    private OPERATOR operator;
    private String value;

    public ResourcePurgCondition(String rawValue) {
        if (rawValue != null) {
            // update operator
            if (rawValue.startsWith("=")) {
                operator = OPERATOR.EQ;
            } else if (rawValue.startsWith(">=")) {
                operator = OPERATOR.GE;
            } else if (rawValue.startsWith("<=")) {
                operator = OPERATOR.LE;
            } else if (rawValue.startsWith("!=")) {
                operator = OPERATOR.NE;
            } else if (rawValue.startsWith(">")) {
                operator = OPERATOR.GT;
            } else if (rawValue.startsWith("<")) {
                operator = OPERATOR.LT;
            } else {
                operator = OPERATOR.EQ;
            }

            // update value
            if (operator != null) {
                value = rawValue.replaceFirst(operator.getText(), "").trim();
            }
        }
    }

    public Integer getValueInteger() {
        return McUtils.getInteger(value);
    }

    public Long getValueLong() {
        return McUtils.getLong(value);
    }

    public Double getValueDouble() {
        return McUtils.getDouble(value);
    }

    public Boolean getValueBoolean() {
        return McUtils.getBoolean(value);
    }
}
