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
package org.mycontroller.standalone.provider.mysensors;

import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils.MYS_MESSAGE_TYPE_SET_REQ;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class MySensorsEngine {
    public static void updateMessage(MySensorsRawMessage mysRawMessage) {
        switch (MYS_MESSAGE_TYPE.get(mysRawMessage.getMessageType())) {
            case C_INTERNAL:
                updateInternal(mysRawMessage);
                break;
            case C_PRESENTATION:
                updatePresentation(mysRawMessage);
                break;
            case C_SET:
            case C_REQ:
                updateSetReq(mysRawMessage);
                break;
            case C_STREAM:
                updateStream(mysRawMessage);
                break;
            default:
                break;
        }
    }

    private static void updateInternal(MySensorsRawMessage mysRawMessage) {

    }

    private static void updatePresentation(MySensorsRawMessage mysRawMessage) {

    }

    private static void updateSetReq(MySensorsRawMessage mysRawMessage) {
        switch (MYS_MESSAGE_TYPE_SET_REQ.get(mysRawMessage.getSubType())) {
            case V_RGB:
            case V_RGBW:
                //Change RGB and RGBW values
                if (mysRawMessage.isTxMessage()) {
                    if (mysRawMessage.getPayload().startsWith("#")) {
                        mysRawMessage.setPayload(mysRawMessage.getPayload().replaceAll("#", ""));
                    }
                } else if (!mysRawMessage.getPayload().startsWith("#")) {
                    mysRawMessage.setPayload("#" + mysRawMessage.getPayload());
                }

                break;

            default:
                break;
        }
    }

    private static void updateStream(MySensorsRawMessage mysRawMessage) {

    }
}
