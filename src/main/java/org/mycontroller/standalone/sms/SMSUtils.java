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
package org.mycontroller.standalone.sms;

import java.util.LinkedHashMap;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.settings.SmsSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.message.MessageResponse;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SMSUtils {
    private static final Logger _logger = LoggerFactory.getLogger(SMSUtils.class);

    private static RestAPI plivoApi = null;
    private static LinkedHashMap<String, String> smsParameters = new LinkedHashMap<String, String>();

    private SMSUtils() {

    }

    public static void sendSMS(String phoneNumber, String message) throws Exception {
        initialize(true);
        smsParameters.put("dst", phoneNumber.replaceAll(",", "<").replace("+", ""));
        smsParameters.put("text", message);
        if (message.length() > 160) {
            _logger.info("Message size bigger than 160 characters, Size:[{}], Message:[{}]", message.length(), message);
        }
        MessageResponse msgResponse = plivoApi.sendMessage(smsParameters);
        if (msgResponse.serverCode == 202) {
            _logger.debug("SMS sent successfully...");
        } else {
            _logger.warn("Error SMS:[apiId:{}, error:{}, serverCode:{}, message:{}, messageUuids:{}]",
                    msgResponse.apiId,
                    msgResponse.error, msgResponse.serverCode,
                    msgResponse.message, msgResponse.messageUuids);
            throw new Exception(msgResponse.error);
        }
    }

    public static void initialize(boolean reinitialize) {
        if (plivoApi == null || reinitialize) {
            SmsSettings smsSettings = ObjectFactory.getAppProperties().getSmsSettings();
            if (smsSettings.getAuthId() != null && smsSettings.getAuthToken() != null
                    && smsSettings.getFromNumber() != null) {
                plivoApi = new RestAPI(smsSettings.getAuthId(), smsSettings.getAuthToken(), "v1");
                smsParameters.put("src", smsSettings.getFromNumber());
            } else {
                throw new IllegalArgumentException("Set SMS settings[authId, authToken, FromPhoneNumber]");
            }
        }
    }

}
