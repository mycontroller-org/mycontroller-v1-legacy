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
package org.mycontroller.standalone.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Data
@ToString
@Builder
@AllArgsConstructor
public class EngineStatistics implements Cloneable {
    private static final int MAXIMUM_SAMPLES = 10000;

    private double timeAverage;
    private double timeAverageLastMinute;
    private double timeAverageCurrentMinute;
    private long timeLastMessage;

    private long count;
    private long txCount;
    private long countLastMinute;
    private long countCurrentMinute;
    private long countFailure;

    private long sizeQueue;
    private long timestamp;
    private long timestampCurrentMinute;

    public EngineStatistics() {
        clear();
    }

    public void clear() {
        timeAverage = 0;
        timeAverageLastMinute = 0;
        timeAverageCurrentMinute = 0;
        timeLastMessage = 0;

        countFailure = 0;

        count = 0;
        txCount = 0;
        countLastMinute = 0;
        countCurrentMinute = 0;

        sizeQueue = 0;
        timestamp = 0;
        timestampCurrentMinute = 0;
    }

    public void incrementFailureCount() {
        countFailure++;
    }

    public void update(long timeTaken, boolean isTxMessage) {
        timeLastMessage = timeTaken;
        //if sample goes beyond MAXIMUM_SAMPLES, reset it to avoid big calculations.
        if (count > MAXIMUM_SAMPLES) {
            count = 1;
            txCount = 0;
            timeAverage = timeLastMessage;
            countFailure = 0;
            // if this is Tx message increment count
            if (isTxMessage) {
                txCount++;
            }
        } else {
            timeAverage = ((timeAverage * count) + timeLastMessage) / (count + 1);
            count++;
            if (isTxMessage) {
                txCount++;
            }
        }

        // Update current minute status
        timeAverageCurrentMinute = ((timeAverageCurrentMinute * countCurrentMinute) + timeLastMessage)
                / (countCurrentMinute + 1);
        countCurrentMinute++;
        timestamp = System.currentTimeMillis();
    }

    public double getPercentageFailure() {
        if (countFailure == 0) {
            return 0.0;
        }
        return (countFailure * 100.0) / txCount;
    }

    // update last minute status
    public void updateLastMinuteStatus() {
        if ((System.currentTimeMillis() - timestampCurrentMinute) >= 1000 * 60) {
            timestampCurrentMinute = System.currentTimeMillis();
            timeAverageLastMinute = timeAverageCurrentMinute;
            countLastMinute = countCurrentMinute;
            timeAverageCurrentMinute = 0;
            countCurrentMinute = 0;
        }
    }

    public EngineStatistics clone() {
        return EngineStatistics.builder()
                .timeAverage(timeAverage)
                .timeAverageCurrentMinute(timeAverageCurrentMinute)
                .timeAverageLastMinute(timeAverageLastMinute)
                .timeLastMessage(timeLastMessage)
                .count(count)
                .txCount(txCount)
                .countCurrentMinute(countCurrentMinute)
                .countLastMinute(countLastMinute)
                .countFailure(countFailure)
                .sizeQueue(sizeQueue)
                .timestamp(timestamp)
                .timestampCurrentMinute(timestampCurrentMinute)
                .build();
    }
}
