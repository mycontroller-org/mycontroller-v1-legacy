/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.db.alarm;

import org.mycontroller.standalone.db.AlarmUtils.DAMPENING_TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class DampeningLastNEvaluations {
    private int occurrences;
    private int evaluations;

    public DampeningLastNEvaluations() {

    }

    public DampeningLastNEvaluations(int occurrences, int evaluations) {
        this.occurrences = occurrences;
        this.evaluations = evaluations;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public int getEvaluations() {
        return evaluations;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public void setEvaluations(int evaluations) {
        this.evaluations = evaluations;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DAMPENING_TYPE.LAST_N_EVALUATIONS.value());
        builder.append(": ").append(occurrences);
        builder.append(" out of ").append(evaluations);
        return builder.toString();
    }
}
