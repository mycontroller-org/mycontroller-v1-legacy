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
package org.mycontroller.standalone.auth;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public enum USER_ROLE {
    ADMIN,
    MQTT;
    public static USER_ROLE get(int id) {
        for (USER_ROLE type : values()) {
            if (type.ordinal() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.valueOf(id));
    }
}
