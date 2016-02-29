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
package org.mycontroller.standalone.restclient.twilio.model;

import javax.ws.rs.core.MultivaluedHashMap;

import lombok.NonNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Data;
import lombok.Builder;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Builder
@Data
@ToString(includeFieldNames = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @NonNull
    private String to;
    @NonNull
    private String from;
    @NonNull
    private String body;

    public MultivaluedHashMap<String, String> getMultivaluedMap() {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
        map.add("From", from);
        map.add("To", to);
        map.add("Body", body);
        return map;
    }
}