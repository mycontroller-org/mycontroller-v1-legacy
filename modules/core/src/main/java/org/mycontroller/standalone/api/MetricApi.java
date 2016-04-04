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
package org.mycontroller.standalone.api;

import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricDouble;
import org.mycontroller.standalone.model.ResourceCountModel;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class MetricApi {
    //Get count of resources
    public ResourceCountModel getResourceCount(RESOURCE_TYPE resourceType, Integer resourceId) {
        return new ResourceCountModel(resourceType, resourceId);
    }

    public List<MetricsDoubleTypeDevice> getSensorVariableMetricsDouble(Integer sensorVariableId,
            Long timestampFrom, Long timestampTo) {
        return DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(MetricsDoubleTypeDevice.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .sensorVariable(SensorVariable.builder().id(sensorVariableId).build())
                .build());
    }

    public MetricDouble getSensorVariableMetricDouble(SensorVariable sensorVariable, Long timestampFrom,
            Long timestampTo) {
        MetricsDoubleTypeDevice queryInput = MetricsDoubleTypeDevice.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .sensorVariable(sensorVariable)
                .build();

        MetricsDoubleTypeDevice metric = DaoUtils.getMetricsDoubleTypeDeviceDao().getMinMaxAvg(queryInput);

        MetricDouble metricDouble = null;
        //If current value not available, do not allow any value
        if (sensorVariable.getValue() == null || metric.getMin() == null) {
            metricDouble = MetricDouble.builder().variable(sensorVariable).build();
        } else {
            metricDouble = MetricDouble.builder()
                    .variable(sensorVariable)
                    .minimum(metric.getMin())
                    .average(metric.getAvg())
                    .maximum(metric.getMax())
                    .current(McUtils.getDouble(sensorVariable.getValue()))
                    .previous(McUtils.getDouble(sensorVariable.getPreviousValue()))
                    .build();
        }
        return metricDouble;
    }

    public List<MetricsBinaryTypeDevice> getSensorVariableMetricsBinary(Integer sensorVariableId,
            Long timestampFrom, Long timestampTo) {
        return DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(MetricsBinaryTypeDevice.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .sensorVariable(SensorVariable.builder().id(sensorVariableId).build())
                .build());
    }

    public List<MetricsBatteryUsage> getMetricsBattery(Integer nodeId, Long timestampFrom, Long timestampTo,
            Boolean withMinMax) {
        MetricsBatteryUsage metricQueryBattery = MetricsBatteryUsage.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .node(Node.builder().id(nodeId).build())
                .build();
        return DaoUtils.getMetricsBatteryUsageDao().getAll(metricQueryBattery);
    }
}
