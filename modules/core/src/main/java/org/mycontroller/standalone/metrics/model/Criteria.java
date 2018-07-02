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
package org.mycontroller.standalone.metrics.model;

import org.mycontroller.standalone.metrics.DATA_TYPE;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.utils.McUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Getter
@ToString
@Builder
public class Criteria {
    private ResourceModel resourceModel;
    private Long start;
    private Long end;
    private String bucketDuration;
    private DATA_TYPE dataType;

    public Long getBucketDurationLong() {
        if (bucketDuration == null) {
            return McUtils.ONE_MINUTE;
        } else if (bucketDuration.endsWith("mn")) {
            return McUtils.getLong(bucketDuration.replace("mn", "")) * McUtils.ONE_MINUTE;
        } else if (bucketDuration.endsWith("h")) {
            return McUtils.getLong(bucketDuration.replace("h", "")) * McUtils.ONE_HOUR;
        } else if (bucketDuration.endsWith("d")) {
            return McUtils.getLong(bucketDuration.replace("d", "")) * McUtils.ONE_DAY;
        } else if (bucketDuration.endsWith("m")) {
            return McUtils.getLong(bucketDuration.replace("m", "")) * McUtils.ONE_DAY * 30;
        } else if (bucketDuration.equalsIgnoreCase("raw")) {
            return -1L;
        }
        return -1L;
    }
}
