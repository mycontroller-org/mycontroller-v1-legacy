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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResourcePurgeConf {
    private Integer id;
    private String type;
    private String value;
    private Long start;
    private Long end;

    @JsonIgnore
    public String getOperator() {
        if (value == null) {
            return null;
        }
        if (value.startsWith("=")) {
            return "=";
        } else if (value.startsWith(">=")) {
            return ">=";
        } else if (value.startsWith("<=")) {
            return "<=";
        } else if (value.startsWith("!=")) {
            return "!=";
        } else if (value.startsWith(">")) {
            return ">";
        } else if (value.startsWith("<")) {
            return "<";
        } else {
            return "=";
        }
    }

    @JsonIgnore
    public String getRealValue() {
        if (value == null) {
            return null;
        }
        String operator = getOperator();
        if (operator != null) {
            return value.replaceFirst(operator, "").trim();
        }
        return value;
    }
}
