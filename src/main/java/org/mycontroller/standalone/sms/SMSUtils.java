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
import org.mycontroller.standalone.AppProperties.SMS_VENDOR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.message.MessageResponse;
import com.plivo.helper.exception.PlivoException;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Sms;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SMSUtils {
    private static final Logger _logger = LoggerFactory.getLogger(SMSUtils.class);

    // Create a rest clients
    private static TwilioRestClient clientTwilio = null;
    private static RestAPI clientPlivo = null;
    //Store sms parameters
    private static LinkedHashMap<String, String> smsParameters = new LinkedHashMap<String, String>();

    private SMSUtils() {

    }

    public static synchronized void sendSmsPlivo(String toPhoneNumbers, String message) {
        //Needs to create brand new instance of RestAPI before each call
        clientPlivo = new RestAPI(ObjectFactory.getAppProperties().getSmsSettings().getAuthSid(), ObjectFactory
                .getAppProperties().getSmsSettings().getAuthToken(), "v1");
        smsParameters.clear();
        smsParameters.put("src", ObjectFactory.getAppProperties().getSmsSettings().getFromNumber());
        smsParameters.put("dst", toPhoneNumbers.replaceAll(",", "<").replace("+", ""));
        smsParameters.put("text", message);
        if (message.length() > 160) {
            _logger.info("Message size bigger than 160 characters, Size:[{}], Message:[{}]", message.length(), message);
        }

        try {
            MessageResponse msgResponse = clientPlivo.sendMessage(smsParameters);
            if (msgResponse.serverCode == 202) {
                _logger.debug("SMS sent successfully...");
            } else {
                _logger.warn("Error SMS:[apiId:{}, error:{}, serverCode:{}, message:{}, messageUuids:{}]",
                        msgResponse.apiId,
                        msgResponse.error, msgResponse.serverCode,
                        msgResponse.message, msgResponse.messageUuids);
            }
        } catch (PlivoException ex) {
            _logger.error("Error, ", ex);
        }

    }

    //Refer: https://github.com/twilio/twilio-java
    public static synchronized void sendSmsTwilio(String toPhoneNumbers, String message) {
        if (clientTwilio == null) {
            clientTwilio = new TwilioRestClient(ObjectFactory.getAppProperties().getSmsSettings().getAuthSid(),
                    ObjectFactory.getAppProperties().getSmsSettings().getAuthToken());
            clientTwilio = new TwilioRestClient(ObjectFactory.getAppProperties().getSmsSettings().getAuthSid(),
                    ObjectFactory.getAppProperties().getSmsSettings().getAuthToken());
        }
        //Clear previous messages
        smsParameters.clear();
        //Add from number
        smsParameters.put("From", ObjectFactory.getAppProperties().getSmsSettings().getFromNumber());
        //https://www.twilio.com/docs/api/rest/sending-messages
        if (message.length() > 1600) {
            _logger.warn("Maximum allowed chars limit 1600, this message has {} chars. Dropping part of message",
                    message.length());
            message = message.substring(0, 1600);
        }
        //Add SMS
        smsParameters.put("Body", message);
        for (String toPhoneNumber : toPhoneNumbers.split(",")) {
            //Add to number
            smsParameters.put("To", toPhoneNumber);
            //Send SMS
            try {
                Sms sms = clientTwilio.getAccount().getSmsFactory().create(smsParameters);
                _logger.info("SMS json string", sms.toJSON());
                if (sms.getProperty("ErrorCode") != null) {
                    _logger.warn("Message sending failed! Erro code:{}, Error message:{}",
                            sms.getProperty("ErrorCode"), sms.getProperty("ErrorMessage"));
                }
            } catch (TwilioRestException ex) {
                _logger.error("Error while sending SMS, ", ex);
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
        clientTwilio = null;
        clientPlivo = null;
    }

}
