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

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.AppProperties.SMS_VENDOR;
import org.mycontroller.standalone.restclient.ClientResponse;
import org.mycontroller.standalone.restclient.plivo.PlivoClient;
import org.mycontroller.standalone.restclient.plivo.PlivoClientImpl;
import org.mycontroller.standalone.restclient.plivo.model.Message;
import org.mycontroller.standalone.restclient.twilio.TwilioClient;
import org.mycontroller.standalone.restclient.twilio.TwilioClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SMSUtils {
    private static final Logger _logger = LoggerFactory.getLogger(SMSUtils.class);

    // Create a rest clients
    private static TwilioClient twilioClient = null;
    private static PlivoClient plivoClient = null;

    private SMSUtils() {

    }

    public static synchronized void sendSmsPlivo(String toPhoneNumbers, String message) {
        if (plivoClient == null) {
            try {
                plivoClient = new PlivoClientImpl(
                        ObjectFactory.getAppProperties().getSmsSettings().getAuthSid(),
                        ObjectFactory.getAppProperties().getSmsSettings().getAuthToken());
            } catch (Exception ex) {
                _logger.error("Unable to create plivo client, ", ex);
            }
        }
        Message messagePlivo = Message.builder()
                .src(ObjectFactory.getAppProperties().getSmsSettings().getFromNumber())
                .dst(toPhoneNumbers.replaceAll(",", "<").replace("+", ""))
                .text(message).build();
        if (message.length() > 160) {
            _logger.info("Message size bigger than 160 characters, Size:[{}], Message:[{}]", message.length(), message);
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
                        ObjectFactory.getAppProperties().getSmsSettings().getAuthSid(),
                        ObjectFactory.getAppProperties().getSmsSettings().getAuthToken());
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
                        .from(ObjectFactory.getAppProperties().getSmsSettings().getFromNumber())
                        .to("") //To should not be null
                        .body(message)
                        .build();

        for (String toPhoneNumber : toPhoneNumbers.split(",")) {
            //Add to number
            messageTwilio.setTo(toPhoneNumber);
            //Send SMS
            ClientResponse<org.mycontroller.standalone.restclient.twilio.model.MessageResponse> responseTwilio = twilioClient
                    .sendMessage(messageTwilio);
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
        if (ObjectFactory.getAppProperties().getSmsSettings().getAuthSid() == null ||
                ObjectFactory.getAppProperties().getSmsSettings().getAuthToken() == null ||
                ObjectFactory.getAppProperties().getSmsSettings().getFromNumber() == null ||
                ObjectFactory.getAppProperties().getSmsSettings().getVendor() == null ||
                toPhoneNumbers == null || message == null) {
            _logger.warn(
                    "Sms sending failed! Sms settings value should not be null. ToPhoneNumbers:{},Message:{},SmsSettings:{}",
                    toPhoneNumbers, message, ObjectFactory.getAppProperties().getSmsSettings());
            return;
        }
        switch (SMS_VENDOR.fromString(ObjectFactory.getAppProperties().getSmsSettings().getVendor())) {
            case PLIVO:
                sendSmsPlivo(toPhoneNumbers, message);
                break;
            case TWILIO:
                sendSmsTwilio(toPhoneNumbers, message);
                break;

            default:
                _logger.warn("This type of vendor not implemented yet! Vendor:{}",
                        ObjectFactory.getAppProperties().getSmsSettings().getVendor());
                break;
        }

    }

    public static void clearClients() {
        twilioClient = null;
        plivoClient = null;
    }

}
