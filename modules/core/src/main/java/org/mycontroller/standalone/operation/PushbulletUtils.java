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
package org.mycontroller.standalone.operation;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.restclient.pushbullet.PushbulletClient;
import org.mycontroller.restclient.pushbullet.model.Push;
import org.mycontroller.restclient.pushbullet.model.PushResponse;
import org.mycontroller.restclient.pushbullet.model.User;
import org.mycontroller.standalone.AppProperties;

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
    private static PushbulletClient client = null;

    public static synchronized void sendNote(String idens, String emails, String channelTags, String title,
            String body) {
        updateClient();
        if (client == null) {
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
            try {
                PushResponse response = client.createPush(push);
                _logger.debug("{}", response);
            } catch (Exception ex) {
                _logger.error("Exception: {}", push, ex);
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
            try {
                PushResponse response = client.createPush(push);
                _logger.debug("{}", response);
            } catch (Exception ex) {
                _logger.error("Exception: {}", push, ex);
            }
        }

    }

    public static User getCurrentUser() {
        updateClient();
        if (client == null) {
            _logger.warn("Looks like pushbullet configuration not updated. Could not send pushbullet notification!");
            throw new RuntimeException(
                    "Looks like pushbullet configuration not updated. Could not send pushbullet notification!");
        }
        User user = client.currentUser();
        _logger.debug("{}", user);
        return user;
    }

    private static void updateClient() {
        if (client == null) {
            try {
                _logger.debug("PushBullet:{}", AppProperties.getInstance().getPushbulletSettings());
                client = new PushbulletClient(
                        AppProperties.getInstance().getPushbulletSettings().getAccessToken(), TRUST_HOST_TYPE.DEFAULT);
            } catch (Exception ex) {
                _logger.error("Unable to create Pushbullet client, ", ex);
                client = null;
                throw new RuntimeException("Error: " + ex.getMessage());
            }
        }
    }

    public static void clearClient() {
        client = null;
    }

    private static String[] getTargetDevices(String targets) {
        if (targets != null && targets.trim().length() > 0) {
            return targets.trim().split(",");
        }
        return null;
    }

}
