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
import org.mycontroller.restclient.plivo.PlivoClient;
import org.mycontroller.restclient.plivo.model.Message;
import org.mycontroller.restclient.plivo.model.MessageResponse;
import org.mycontroller.restclient.twilio.TwilioClient;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.SMS_VENDOR;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SMSUtils {

    // Create a rest clients
    private static TwilioClient twilioClient = null;
    private static PlivoClient plivoClient = null;

    private static void sendSmsPlivo(String toPhoneNumbers, String message) {
        if (plivoClient == null) {
            try {
                plivoClient = new PlivoClient(
                        AppProperties.getInstance().getSmsSettings().getAuthSid(),
                        AppProperties.getInstance().getSmsSettings().getAuthToken(),
                        TRUST_HOST_TYPE.DEFAULT);
            } catch (Exception ex) {
                _logger.error("Unable to create plivo client, ", ex);
            }
        }
        Message messagePlivo = Message.builder()
                .src(AppProperties.getInstance().getSmsSettings().getFromNumber())
                .dst(toPhoneNumbers.replaceAll(",", "<").replace("+", ""))
                .text(message).build();
        if (message.length() > 160) {
            _logger.info("Message size bigger than 160 characters, Size:[{}], Message:[{}]",
                    message.length(), message);
        }

        try {
            MessageResponse responsePlivo = plivoClient.sendMessage(messagePlivo);
            _logger.debug("Response:{}", responsePlivo);
        } catch (Exception ex) {
            _logger.error("Exception, {}", messagePlivo, ex);
        }
    }

    //Refer: https://github.com/twilio/twilio-java
    private static void sendSmsTwilio(String toPhoneNumbers, String message) {
        if (twilioClient == null) {
            try {
                twilioClient = new TwilioClient(
                        AppProperties.getInstance().getSmsSettings().getAuthSid(),
                        AppProperties.getInstance().getSmsSettings().getAuthToken(),
                        TRUST_HOST_TYPE.DEFAULT);
            } catch (Exception ex) {
                _logger.error("Unable to create twilio client, ", ex);
            }
        }

        //https://www.twilio.com/docs/api/rest/sending-messages
        if (message.length() > 1600) {
            _logger.warn("Maximum allowed chars limit 1600, this message has {} chars. Dropping part of message",
                    message.length());
            message = message.substring(0, 1600);
        }

        org.mycontroller.restclient.twilio.model.Message messageTwilio =
                org.mycontroller.restclient.twilio.model.Message.builder()
                        .from(AppProperties.getInstance().getSmsSettings().getFromNumber())
                        .to("") //To should not be null
                        .body(message)
                        .build();

        org.mycontroller.restclient.twilio.model.MessageResponse responseTwilio = null;
        for (String toPhoneNumber : toPhoneNumbers.split(",")) {
            //Add to number
            messageTwilio.setTo(toPhoneNumber);
            //Send SMS
            try {
                responseTwilio = twilioClient.sendMessage(messageTwilio);
                _logger.debug("Response:{}", responseTwilio);
            } catch (Exception ex) {
                _logger.error("Exception, {}", messageTwilio, ex);
            }
        }
    }

    public static synchronized void sendSMS(String toPhoneNumbers, String message) {
        //Check weather you have sid and auth token
        if (AppProperties.getInstance().getSmsSettings().getAuthSid() == null ||
                AppProperties.getInstance().getSmsSettings().getAuthToken() == null ||
                AppProperties.getInstance().getSmsSettings().getFromNumber() == null ||
                AppProperties.getInstance().getSmsSettings().getVendor() == null ||
                toPhoneNumbers == null || message == null) {
            _logger.warn(
                    "Sms sending failed! Sms settings value should not be null. "
                            + "ToPhoneNumbers:{},Message:{},SmsSettings:{}",
                    toPhoneNumbers, message, AppProperties.getInstance().getSmsSettings());
            return;
        }
        switch (SMS_VENDOR.fromString(AppProperties.getInstance().getSmsSettings().getVendor())) {
            case PLIVO:
                sendSmsPlivo(toPhoneNumbers, message);
                break;
            case TWILIO:
                sendSmsTwilio(toPhoneNumbers, message);
                break;

            default:
                _logger.warn("This type of vendor not implemented yet! Vendor:{}",
                        AppProperties.getInstance().getSmsSettings().getVendor());
                break;
        }

    }

    public static void clearClients() {
        twilioClient = null;
        plivoClient = null;
    }

}
