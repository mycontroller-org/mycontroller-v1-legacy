/*
 * Copyright 2015-2019 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.onetime;

import lombok.ToString;

import lombok.Getter;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */

@Getter
@ToString
public class OnetimeResetCommand {
    private String command;
    private String data;

    public OnetimeResetCommand(String rawData) {
        String[] raw = rawData.split(":", 2);
        command = raw[0].trim().toLowerCase();
        if (raw.length > 1) {
            data = raw[1].trim();
        }
    }

    public boolean isValid() {
        if (data != null && data.length() > 0) {
            return true;
        }
        return false;
    }

}
