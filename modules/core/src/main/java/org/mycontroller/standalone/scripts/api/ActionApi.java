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
package org.mycontroller.standalone.scripts.api;

import org.mycontroller.standalone.api.Gateways;
import org.mycontroller.standalone.api.Metrics;
import org.mycontroller.standalone.api.Nodes;
import org.mycontroller.standalone.api.Sensors;
import org.mycontroller.standalone.api.Timers;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class ActionApi {
    private Gateways gateways = new Gateways();
    private Nodes nodes = new Nodes();
    private Sensors sensors = new Sensors();
    private Timers timers = new Timers();
    private Metrics metrics = new Metrics();

    public Sensors sensors() {
        return sensors;
    }

    public Gateways gateways() {
        return gateways;
    }

    public Nodes nodes() {
        return nodes;
    }

    public Timers timers() {
        return timers;
    }

    public Metrics metrics() {
        return metrics;
    }

}
