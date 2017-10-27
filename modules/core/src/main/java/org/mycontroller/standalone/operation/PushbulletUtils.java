/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PushbulletUtils {

    // Create a rest clients
    private static PushbulletClient pushbulletClient = null;

    public static synchronized void sendNote(String idens, String emails, String channelTags, String title,
            String body) {
        updateClient();
        if (pushbulletClient == null) {
            _logger.warn("Looks like pushbullet configuration not updated. Could not send pushbullet notification!");
        }
        Push push = Push.builder().type("note").title(title).body(body).build();
        _logger.debug("Pushbullet note: title:{}, body:{}, idens:{}, emails:{}, channelTags:{}", title, body, idens,
                emails, channelTags);
        String[] idensArray = getTargetDevices(idens);
        String[] emailsArray = getTargetDevices(emails);
        String[] channelTagsArray = getTargetDevices(channelTags);

        if (idensArray != null || emailsArray != null || channelTagsArray != null) {
            sendNote("iden", idensArray, push);
            sendNote("email", emailsArray, push);
            sendNote("channel_tag", channelTagsArray, push);
        } else {
            push.clearTargets();
            ClientResponse<PushResponse> responsePushbullet = pushbulletClient.sendPush(push);
            if (responsePushbullet.isSuccess()) {
                _logger.debug("Note push sent successfully..., WUResponse:{}", responsePushbullet.getEntity());
            } else {
                _logger.warn("Note push sending failed:{}]", responsePushbullet);
            }
        }
    }

    private static void sendNote(String targetType, String[] targetArray, Push push) {
        if (targetArray == null || targetArray == null) {
            _logger.debug("Nothing to do with null values[targetType:{}, targetArray:{}]", targetType, targetArray);
            return;
        }
        for (String target : targetArray) {
            _logger.debug("Sending note for devide:[type:{}, target:{}]", targetType, target);
            push.clearTargets();
            switch (targetType.toLowerCase()) {
                case "iden":
                    push.setDeviceIden(target);
                    break;
                case "email":
                    push.setEmail(target);
                    break;
                case "channel_tag":
                    push.setChannelTag(target);
                    break;
                default:
                    _logger.warn("Unknown target type:[{}]", targetType);
                    return;

            }
            ClientResponse<PushResponse> responsePushbullet = pushbulletClient.sendPush(push);
            if (responsePushbullet.isSuccess()) {
                _logger.debug("Note push sent successfully..., WUResponse:{}", responsePushbullet.getEntity());
            } else {
                _logger.warn("Note push sending failed:{}]", responsePushbullet);
            }
        }

    }

    public static User getCurrentUser() throws IllegalAccessException {
        updateClient();
        if (pushbulletClient == null) {
            _logger.warn("Looks like pushbullet configuration not updated. Could not send pushbullet notification!");
            throw new RuntimeException(
                    "Looks like pushbullet configuration not updated. Could not send pushbullet notification!");
        }
        ClientResponse<User> responsePushbullet = pushbulletClient.getCurrentUser();
        _logger.debug("ClientResponse:{}", responsePushbullet);
        if (responsePushbullet.isSuccess()) {
            return responsePushbullet.getEntity();
        } else {
            _logger.error("Failed to get current user. WUResponse:{}", responsePushbullet);
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
                pushbulletClient = null;
                throw new RuntimeException("Error: " + ex.getMessage());
            }
        }
    }

    public static void clearClient() {
        pushbulletClient = null;
    }

    private static String[] getTargetDevices(String targets) {
        if (targets != null && targets.trim().length() > 0) {
            return targets.trim().split(",");
        }
        return null;
    }

}
