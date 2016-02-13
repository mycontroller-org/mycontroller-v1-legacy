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
package org.mycontroller.standalone.restclient.twilio.model;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Getter
@ToString(includeFieldNames = true)
@NoArgsConstructor
public class MessageResponse {
    @JsonProperty("sid")
    private String sid;

    @JsonProperty("date_created")
    private Date dateCreated;

    @JsonProperty("date_updated")
    private Date dateUpdated;

    @JsonProperty("date_sent")
    private Date dateSent;

    @JsonProperty("account_sid")
    private String accountSid;

    @JsonProperty("to")
    private String to;

    @JsonProperty("from")
    private String from;

    @JsonProperty("messaging_service_sid")
    private String messagingServiceSid;

    @JsonProperty("body")
    private String body;

    @JsonProperty("status")
    private String status;

    @JsonProperty("num_segments")
    private String numSegments;

    @JsonProperty("num_media")
    private String numMedia;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("api_version")
    private String apiVersion;

    @JsonProperty("price")
    private String price;

    @JsonProperty("price_unit")
    private String priceUnit;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("subresource_uris")
    private Map<String, Object> subresourceUris;

}