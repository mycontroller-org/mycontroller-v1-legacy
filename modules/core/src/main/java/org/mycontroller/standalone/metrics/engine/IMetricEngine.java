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
package org.mycontroller.standalone.metrics.engine;

import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.DataPointBase;
import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf;
import org.mycontroller.standalone.metrics.model.Criteria;
import org.mycontroller.standalone.metrics.model.DataPointer;
import org.mycontroller.standalone.metrics.model.Pong;
import org.mycontroller.standalone.model.ResourceModel;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public interface IMetricEngine {
    //post single data
    void post(DataPointer dataPointer);

    //Get single data
    DataPointBase get(Criteria criteria);

    //list data
    List<?> list(Criteria criteria);

    void purge(ResourceModel resourceModel, ResourcePurgeConf purgeConf);

    void purge(ResourceModel resourceModel);

    void purgeEverything();

    Pong ping();

    void close();
}
