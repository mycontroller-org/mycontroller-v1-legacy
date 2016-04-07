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
import org.mycontroller.standalone.AppProperties.SMS_VENDOR;
import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.plivo.PlivoClient;
import org.mycontroller.standalone.restclient.plivo.PlivoClientImpl;
import org.mycontroller.standalone.restclient.plivo.model.Message;
import org.mycontroller.standalone.restclient.twilio.TwilioClient;
import org.mycontroller.standalone.restclient.twilio.TwilioClientImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SMSUtils {

    // Create a rest clients
    private static TwilioClient twilioClient = null;
    private static PlivoClient plivoClient = null;

    private SMSUtils() {

    }

    public static synchronized void sendSmsPlivo(String toPhoneNumbers, String message) {
        if (plivoClient == null) {
            try {
                plivoClient = new PlivoClientImpl(
                        AppProperties.getInstance().getSmsSettings().getAuthSid(),
                        AppProperties.getInstance().getSmsSettings().getAuthToken());
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

        ClientResponse<org.mycontroller.standalone.restclient.plivo.model.MessageResponse> responsePlivo = plivoClient
                .sendMessage(messagePlivo);
        if (responsePlivo.isSuccess()) {
            _logger.debug("SMS sent successfully...");
            _logger.debug("Response:{}", responsePlivo.getEntity());
        } else {
            _logger.warn("SMS sending failed:{}]", responsePlivo);
        }

    }

    //Refer: https://github.com/twilio/twilio-java
    public static synchronized void sendSmsTwilio(String toPhoneNumbers, String message) {
        if (twilioClient == null) {
            try {
                twilioClient = new TwilioClientImpl(
                        AppProperties.getInstance().getSmsSettings().getAuthSid(),
                        AppProperties.getInstance().getSmsSettings().getAuthToken());
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

        org.mycontroller.standalone.restclient.twilio.model.Message messageTwilio =
                org.mycontroller.standalone.restclient.twilio.model.Message.builder()
                        .from(AppProperties.getInstance().getSmsSettings().getFromNumber())
                        .to("") //To should not be null
                        .body(message)
                        .build();

        ClientResponse<org.mycontroller.standalone.restclient.twilio.model.MessageResponse> responseTwilio = null;
        for (String toPhoneNumber : toPhoneNumbers.split(",")) {
            //Add to number
            messageTwilio.setTo(toPhoneNumber);
            //Send SMS
            responseTwilio = twilioClient.sendMessage(messageTwilio);
            if (responseTwilio.isSuccess()) {
                _logger.debug("SMS sent successfully...");
                _logger.debug("Response:{}", responseTwilio.getEntity());
            } else {
                _logger.warn("SMS sending failed:{}]", responseTwilio);
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
