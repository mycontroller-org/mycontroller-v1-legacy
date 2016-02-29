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

import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.pushbullet.model.Devices;
import org.mycontroller.standalone.restclient.pushbullet.model.Push;
import org.mycontroller.standalone.restclient.pushbullet.model.PushResponse;
import org.mycontroller.standalone.restclient.pushbullet.model.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public interface PushbulletClient {
    public static final String PUSHBULLET_URL = "https://api.pushbullet.com";
    public static final String PUSHBULLET_VERSION = "v2";

    ClientResponse<Devices> getDevices();
    
    ClientResponse<User> getCurrentUser();

    ClientResponse<PushResponse> sendPush(Push push);
}
