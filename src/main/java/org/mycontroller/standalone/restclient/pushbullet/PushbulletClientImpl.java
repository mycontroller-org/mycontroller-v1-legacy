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
package org.mycontroller.standalone.restclient.pushbullet;

import java.net.URI;

import org.mycontroller.standalone.restclient.ClientBase;
import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.RestFactory;
import org.mycontroller.standalone.restclient.pushbullet.model.Devices;
import org.mycontroller.standalone.restclient.pushbullet.model.Push;
import org.mycontroller.standalone.restclient.pushbullet.model.PushResponse;
import org.mycontroller.standalone.restclient.pushbullet.model.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class PushbulletClientImpl extends ClientBase<PushbulletRestAPI> implements PushbulletClient {
    public PushbulletClientImpl(String authId, String authToken) throws Exception {
        super(new URI(String.format("%s/%s", PUSHBULLET_URL, PUSHBULLET_VERSION)),
                authId,
                authToken,
                new RestFactory<PushbulletRestAPI>(PushbulletRestAPI.class));
    }

    @Override
    public ClientResponse<Devices> getDevices() {
        return new ClientResponse<Devices>(Devices.class, restApi().getDevices(), 200);
    }

    @Override
    public ClientResponse<PushResponse> sendPush(Push push) {
        return new ClientResponse<PushResponse>(PushResponse.class, restApi().sendPush(push), 200);
    }

    @Override
    public ClientResponse<User> getCurrentUser() {
        return new ClientResponse<User>(User.class, restApi().getCurrentUser(), 200);
    }
}
