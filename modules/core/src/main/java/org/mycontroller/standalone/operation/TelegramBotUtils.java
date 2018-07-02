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

import java.util.Map;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.restclient.telegrambot.TelegramBotClient;
import org.mycontroller.restclient.telegrambot.model.Message;
import org.mycontroller.restclient.telegrambot.model.User;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.settings.TelegramBotSettings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.3.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TelegramBotUtils {

    // Create a rest clients
    private static TelegramBotClient client = null;

    public static synchronized void sendMessage(String chatId, String parseMode, String text) {
        updateClient();
        if (client == null) {
            _logger.warn("Looks like telegram boot configuration not updated. Could not send pushbullet notification!");
        }
        Message message = Message.builder()
                .chatId(chatId)
                .parseMode(parseMode)
                .text(text)
                .build();
        _logger.debug("TelegramBot sendMessage:{}", message);

        try {
            Map<String, Object> response = client.sendMessage(message);
            _logger.debug("{}", response);
        } catch (Exception ex) {
            _logger.error("Exception: {}", message, ex);
        }
    }

    public static User getMe() {
        updateClient();
        if (client == null) {
            _logger.warn("Looks like telegramBot configuration not updated. Could not send telegramBot message!");
            throw new RuntimeException(
                    "Looks like telegramBot configuration not updated. Could not send telegramBot message!");
        }
        User user = client.getMe();
        _logger.debug("{}", user);
        return user;
    }

    private static void updateClient() {
        if (client == null) {
            try {
                TelegramBotSettings settings = AppProperties.getInstance().getTelegramBotSettings();
                _logger.debug("TelegramBot:{}", settings);
                client = new TelegramBotClient(settings.getToken(), TRUST_HOST_TYPE.DEFAULT);
            } catch (Exception ex) {
                _logger.error("Unable to create TelegramBot client, ", ex);
                client = null;
                throw new RuntimeException("Error: " + ex.getMessage());
            }
        }
    }

    public static void clearClient() {
        client = null;
    }

}
