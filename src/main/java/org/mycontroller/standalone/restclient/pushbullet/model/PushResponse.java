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
package org.mycontroller.standalone.restclient.pushbullet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushResponse {

    private String active;
    private String body;
    private String created;
    private String direction;
    private String dismissed;
    private String iden;
    private String modified;

    @JsonProperty("receiver_email")
    private String receiverEmail;

    @JsonProperty("receiver_email_normalized")
    private String receiverEmailNormalized;

    @JsonProperty("receiver_iden")
    private String receiverIden;

    @JsonProperty("sender_email")
    private String senderEmail;

    @JsonProperty("sender_email_normalized")
    private String senderEmailEormalized;

    @JsonProperty("sender_iden")
    private String senderIden;

    @JsonProperty("sender_name")
    private String senderName;

    private String title;
    private String type;

}
