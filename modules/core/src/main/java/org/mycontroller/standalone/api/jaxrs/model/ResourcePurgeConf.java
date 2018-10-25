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

    public static enum OPERATOR {
        EQ("="),
        GE(">="),
        LE("<="),
        NE("!="),
        GT(">"),
        LT("<");

        public static OPERATOR get(int id) {
            for (OPERATOR operator : values()) {
                if (operator.ordinal() == id) {
                    return operator;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private OPERATOR(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static OPERATOR fromString(String text) {
            if (text != null) {
                for (OPERATOR type : OPERATOR.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    @JsonIgnore
    public OPERATOR getOperator() {
        if (value == null) {
            return null;
        }
        if (value.startsWith("=")) {
            return OPERATOR.EQ;
        } else if (value.startsWith(">=")) {
            return OPERATOR.GE;
        } else if (value.startsWith("<=")) {
            return OPERATOR.LE;
        } else if (value.startsWith("!=")) {
            return OPERATOR.NE;
        } else if (value.startsWith(">")) {
            return OPERATOR.GT;
        } else if (value.startsWith("<")) {
            return OPERATOR.LT;
        } else {
            return OPERATOR.EQ;
        }
    }

    @JsonIgnore
    public String getRealValue() {
        if (value == null) {
            return null;
        }
        OPERATOR operator = getOperator();
        if (operator != null) {
            return value.replaceFirst(operator.getText(), "").trim();
        }
        return value;
    }
}
