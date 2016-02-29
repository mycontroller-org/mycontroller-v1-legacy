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
package org.mycontroller.standalone.restclient.plivo;

import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.plivo.model.Message;
import org.mycontroller.standalone.restclient.plivo.model.MessageResponse;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public interface PlivoClient {
    public static final String PLIVO_URL = "https://api.plivo.com";
    public static final String PLIVO_VERSION = "v1";

    ClientResponse<MessageResponse> sendMessage(Message message);
}
