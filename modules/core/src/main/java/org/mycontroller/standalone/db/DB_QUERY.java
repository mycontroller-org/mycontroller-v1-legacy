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
package org.mycontroller.standalone.db;

import java.text.MessageFormat;

import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class DB_QUERY {
    public static final String ORDER_BY_NODE_EUI = MessageFormat
            .format("SELECT {0} FROM {1} WHERE id={2} ", Node.KEY_EUI, DB_TABLES.NODE, Sensor.KEY_NODE_ID);
    public static final String ORDER_BY_NODE_NAME = MessageFormat
            .format("SELECT {0} FROM {1} WHERE id={2} ", Node.KEY_NAME, DB_TABLES.NODE, Sensor.KEY_NODE_ID);
}
