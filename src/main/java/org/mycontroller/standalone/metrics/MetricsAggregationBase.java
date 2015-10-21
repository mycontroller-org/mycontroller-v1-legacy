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
package org.mycontroller.standalone.metrics;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.db.AGGREGATION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.TIME_REF;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsAggregationBase {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsAggregationBase.class.getName());
    private AGGREGATION_TYPE aggregationType = null;

    public MetricsAggregationBase(AGGREGATION_TYPE aggregationType) {
        this.aggregationType = aggregationType;
    }

    public MetricsAggregationBase() {
    }

    public void runAggregate() {
        if (this.aggregationType == null) {
            _logger.warn("Should create object with valid aggregation type!");
            return;
        }

        _logger.debug("Run Aggregation, type:{}", this.aggregationType);

        List<Sensor> sensors = DaoUtils.getSensorDao().getAll();
        _logger.debug("Sensors List:{}", sensors);

        for (Sensor sensor : sensors) {
            List<SensorValue> sensorValues = DaoUtils.getSensorValueDao().getAllDoubleMetric(sensor.getId());

            for (SensorValue sensorValue : sensorValues) {
                List<MetricsDoubleTypeDevice> metrics = this.getLowLevelData(sensorValue, aggregationType);
                //Calculate Metrics
                if (metrics.size() > 0) {
                    int samples = 0;
                    if (this.aggregationType.ordinal() == AGGREGATION_TYPE.ONE_MINUTE.ordinal()) {
                        samples = metrics.size();
                    }
                    _logger.debug("Number of records:{}", metrics.size());
                    Double min = Double.MAX_VALUE;
                    Double max = Double.MIN_VALUE;
                    Double sum = 0D;
                    for (MetricsDoubleTypeDevice metric : metrics) {
                        if (this.aggregationType.ordinal() == AGGREGATION_TYPE.ONE_MINUTE.ordinal()) {
                            if (metric.getAvg() > max) {
                                max = metric.getAvg();
                            }

                            if (metric.getAvg() < min) {
                                min = metric.getAvg();
                            }
                            sum = sum + metric.getAvg();
                        } else {
                            if (metric.getMax() > max) {
                                max = metric.getMax();
                            }

                            if (metric.getMin() < min) {
                                min = metric.getMin();
                            }
                            sum = sum + (metric.getAvg() * metric.getSamples());
                            samples = samples + metric.getSamples();
                        }
                    }
                    Double avg = sum / samples;
                    MetricsDoubleTypeDevice metric = new MetricsDoubleTypeDevice();
                    metric.setAggregationType(this.aggregationType.ordinal());
                    metric.setSensorValue(sensorValue);
                    metric.setMin(NumericUtils.round(min, NumericUtils.DOUBLE_ROUND));
                    metric.setMax(NumericUtils.round(max, NumericUtils.DOUBLE_ROUND));
                    metric.setAvg(NumericUtils.round(avg, NumericUtils.DOUBLE_ROUND));
                    metric.setSamples(samples);
                    metric.setTimestamp(System.currentTimeMillis() - TIME_REF.ONE_SECOND);
                    DaoUtils.getMetricsDoubleTypeDeviceDao().create(metric);
                }
            }
        }
        this.purgeDB();
    }

    private List<MetricsDoubleTypeDevice> getLowLevelData(SensorValue sensorValue, AGGREGATION_TYPE aggregationType) {
        switch (aggregationType) {
            case ONE_MINUTE:
                return this.getMetricsDoubleData(sensorValue, AGGREGATION_TYPE.RAW,
                        this.getFromTime(aggregationType));
            case FIVE_MINUTES:
                return this.getMetricsDoubleData(sensorValue, AGGREGATION_TYPE.ONE_MINUTE,
                        this.getFromTime(aggregationType));
            case ONE_HOUR:
                return this.getMetricsDoubleData(sensorValue, AGGREGATION_TYPE.FIVE_MINUTES,
                        this.getFromTime(aggregationType));
            case ONE_DAY:
                return this.getMetricsDoubleData(sensorValue, AGGREGATION_TYPE.ONE_HOUR,
                        this.getFromTime(aggregationType));
            default:
                return new ArrayList<MetricsDoubleTypeDevice>();
        }

    }

    public Long getFromTime(AGGREGATION_TYPE aggregationType) {
        switch (aggregationType) {
            case RAW:
                return null;
            case ONE_MINUTE:
                return System.currentTimeMillis() - TIME_REF.ONE_MINUTE;
            case FIVE_MINUTES:
                return System.currentTimeMillis() - TIME_REF.FIVE_MINUTES;
            case ONE_HOUR:
                return System.currentTimeMillis() - TIME_REF.ONE_HOUR;
            case ONE_DAY:
                return System.currentTimeMillis() - TIME_REF.ONE_DAY;
            default:
                return null;
        }
    }

    public List<MetricsDoubleTypeDevice> getMetricsDoubleData(SensorValue sensorValue, AGGREGATION_TYPE aggrType,
            Long fromTimestamp) {
        MetricsDoubleTypeDevice metricsDoubleType = new MetricsDoubleTypeDevice(sensorValue, aggrType.ordinal());
        if (fromTimestamp != null) {
            metricsDoubleType.setTimestampFrom(fromTimestamp);
        }
        return DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(metricsDoubleType);
    }

    /** Get metric data for boolean type */

    public List<MetricsBinaryTypeDevice> getMetricsBinaryData(SensorValue sensorValue, Long fromTimestamp) {
        MetricsBinaryTypeDevice binaryTypeDevice = new MetricsBinaryTypeDevice(sensorValue);
        if (fromTimestamp != null) {
            binaryTypeDevice.setTimestampFrom(fromTimestamp);
        }
        return DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(binaryTypeDevice);
    }

    private void purgeDB() {
        switch (aggregationType) {
            case RAW:
            case ONE_MINUTE:
                MetricsAggregationUtils.purgeRawData();
                MetricsAggregationUtils.purgeOneMinuteData();
                break;
            case FIVE_MINUTES:
                MetricsAggregationUtils.purgeFiveMinutesData();
                break;
            case ONE_HOUR:
                MetricsAggregationUtils.purgeOneHourData();
                break;
            default:
                _logger.debug("Invalid type or nothing to do, type:{}", aggregationType);
                break;
        }
    }
}
