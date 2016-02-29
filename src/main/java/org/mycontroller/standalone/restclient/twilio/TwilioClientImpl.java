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
package org.mycontroller.standalone.restclient.twilio;

import java.net.URI;

import org.mycontroller.standalone.restclient.ClientBase;
import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.RestFactory;
import org.mycontroller.standalone.restclient.twilio.model.Message;
import org.mycontroller.standalone.restclient.twilio.model.MessageResponse;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class TwilioClientImpl extends ClientBase<TwilioRestAPI> implements TwilioClient {
    public TwilioClientImpl(String authSid, String authToken) throws Exception {
        super(new URI(String.format("%s/Accounts/%s", TWILIO_URL, authSid)),
                authSid,
                authToken,
                new RestFactory<TwilioRestAPI>(TwilioRestAPI.class));
    }

    @Override
    public ClientResponse<MessageResponse> sendMessage(Message message) {
        return new ClientResponse<MessageResponse>(MessageResponse.class,
                restApi().sendMessage(message.getMultivaluedMap()),
                201);
    }
}