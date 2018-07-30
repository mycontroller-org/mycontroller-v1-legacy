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
package org.mycontroller.standalone.db;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeUtils {
    public enum NODE_REGISTRATION_STATE {
        NEW("New"),
        BLOCKED("Blocked"),
        REGISTERED("Registered");

        private final String type;

        private NODE_REGISTRATION_STATE(String type) {
            this.type = type;
        }

        public String getText() {
            return this.type;
        }

        public static NODE_REGISTRATION_STATE get(int id) {
            for (NODE_REGISTRATION_STATE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static NODE_REGISTRATION_STATE fromString(String text) {
            if (text != null) {
                for (NODE_REGISTRATION_STATE type : NODE_REGISTRATION_STATE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }
}
