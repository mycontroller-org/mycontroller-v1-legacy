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
package org.mycontroller.standalone.db;

public class TypeUtils {
    private TypeUtils() {

    }

    public enum METRIC_TYPE {
        NONE,
        DOUBLE,
        BINARY;

        public static METRIC_TYPE get(int id) {
            for (METRIC_TYPE metric_type : values()) {
                if (metric_type.ordinal() == id) {
                    return metric_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }
}
