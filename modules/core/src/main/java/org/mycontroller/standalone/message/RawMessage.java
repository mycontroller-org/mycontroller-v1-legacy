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
package org.mycontroller.standalone.message;

import java.io.Serializable;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.gateway.GatewayUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RawMessage implements Serializable {
    /**  */
    private static final long serialVersionUID = 1L;
    private Integer gatewayId;
    private Object data;
    private String subData;
    private boolean isTxMessage = false;
    private NETWORK_TYPE networkType;
    private Long timestamp;

    public NETWORK_TYPE getNetworkType() {
        if (networkType == null) {
            networkType = GatewayUtils.getNetworkType(gatewayId);
        }
        return networkType;
    }

    //Return bytes
    public byte[] getGWBytes() {
        return ((String) this.data).getBytes();
    }

}
