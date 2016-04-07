/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.operation;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.pushbullet.PushbulletClient;
import org.mycontroller.standalone.restclient.pushbullet.PushbulletClientImpl;
import org.mycontroller.standalone.restclient.pushbullet.model.Push;
import org.mycontroller.standalone.restclient.pushbullet.model.PushResponse;
import org.mycontroller.standalone.restclient.pushbullet.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class PushbulletUtils {
    private static final Logger _logger = LoggerFactory.getLogger(PushbulletUtils.class);

    // Create a rest clients
    private static PushbulletClient pushbulletClient = null;

    private PushbulletUtils() {

    }

    public static synchronized void sendNote(String idens, String title, String body) {
        updateClient();

        Push push = Push.builder().type("note").title(title).body(body).build();
        _logger.debug("Pushbullet note: title:{}, idens:{}, body:{}", title, idens, body);
        ClientResponse<PushResponse> responsePushbullet = null;
        if (idens != null && idens.trim().length() > 0) {
            String[] idenArray = idens.split(",");
            for (String iden : idenArray) {
                _logger.debug("Sending note for devide iden:{}", iden);
                push.setDeviceIden(iden);
                responsePushbullet = pushbulletClient.sendPush(push);
                if (responsePushbullet.isSuccess()) {
                    _logger.debug("Note push sent successfully..., Response:{}", responsePushbullet.getEntity());
                } else {
                    _logger.warn("Note push send failed:{}]", responsePushbullet);
                }
            }
        } else {
            responsePushbullet = pushbulletClient.sendPush(push);
            if (responsePushbullet.isSuccess()) {
                _logger.debug("Note push sent successfully..., Response:{}", responsePushbullet.getEntity());
            } else {
                _logger.warn("Note push send failed:{}]", responsePushbullet);
            }
        }
    }

    public static synchronized User getCurrentUser() throws IllegalAccessException {
        updateClient();
        ClientResponse<User> responsePushbullet = pushbulletClient.getCurrentUser();
        _logger.debug("ClientResponse:{}", responsePushbullet);
        if (responsePushbullet.isSuccess()) {
            return responsePushbullet.getEntity();
        } else {
            _logger.error("Failed to get current user. Response:{}", responsePushbullet);
            throw new IllegalAccessException(responsePushbullet.getErrorMsg());
        }
    }

    private static void updateClient() {
        if (pushbulletClient == null) {
            try {
                _logger.debug("PushBullet:{}", AppProperties.getInstance().getPushbulletSettings());
                pushbulletClient = new PushbulletClientImpl(
                        AppProperties.getInstance().getPushbulletSettings().getAccessToken(), null);
            } catch (Exception ex) {
                _logger.error("Unable to create Pushbullet client, ", ex);
                throw new RuntimeException("Error: " + ex.getMessage());
            }
        }
    }

    public static void clearClient() {
        pushbulletClient = null;
    }

}
