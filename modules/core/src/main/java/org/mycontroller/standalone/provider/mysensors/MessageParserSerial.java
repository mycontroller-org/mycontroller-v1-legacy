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
package org.mycontroller.standalone.provider.mysensors;

import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.provider.IMessageParser;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public class MessageParserSerial extends MessageParserAbstract implements IMessageParser<byte[]> {

    @Override
    public IMessage getMessage(GatewayConfig config, byte[] gatewayData) throws MessageParserException {
        update(config.getId(), new String(gatewayData));
        return getMessage();
    }

    @Override
    public byte[] getGatewayData(IMessage message) throws MessageParserException {
        update(message);
        return getSerialString().getBytes();
    }

}
