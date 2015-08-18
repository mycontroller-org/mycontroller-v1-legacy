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
package org.mycontroller.standalone.scheduler.jobs;

import java.util.List;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.db.AGGREGATION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.TIME_REF;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsOnOffTypeDevice;
import org.mycontroller.standalone.db.tables.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsAggregationBase {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsAggregationBase.class.getName());
    private AGGREGATION_TYPE aggregationType = null;
    private Long referanceTime = null;
    private AGGREGATION_TYPE retriveDataAggregationType = null;
    private Long entryTimestamp = null;

    public MetricsAggregationBase(AGGREGATION_TYPE aggregationType) {
        this.aggregationType = aggregationType;
    }

    public MetricsAggregationBase() {
    }

    public void runAggregate() {
        if (this.aggregationType == null) {
            _logger.warn("Should create object with valid aggregation type!");
            return;
        } else {
            setValuesBasedOnType(this.aggregationType);
        }

        _logger.debug("type:{},referenceTime:{}, retriveDataType:{}", this.aggregationType, this.referanceTime,
                this.retriveDataAggregationType);

        List<Sensor> sensors = DaoUtils.getSensorDao().getAll();
        _logger.debug("Sensors List:{}", sensors);

        for (Sensor sensor : sensors) {
            List<MetricsDoubleTypeDevice> metrics = this.getMetricsDoubleTypeAllAfter(sensor);
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
                metric.setSensor(sensor);
                metric.setMin(NumericUtils.round(min, NumericUtils.DOUBLE_ROUND));
                metric.setMax(NumericUtils.round(max, NumericUtils.DOUBLE_ROUND));
                metric.setAvg(NumericUtils.round(avg, NumericUtils.DOUBLE_ROUND));
                metric.setSamples(samples);
                metric.setTimestamp(entryTimestamp);
                DaoUtils.getMetricsDoubleTypeDeviceDao().create(metric);
            }
        }
        this.purgeDB();
    }

    private void setValuesBasedOnType(AGGREGATION_TYPE aggregationType) {
        switch (aggregationType) {
            case ONE_MINUTE:
                referanceTime = TIME_REF.ONE_MINUTE;
                retriveDataAggregationType = AGGREGATION_TYPE.RAW;
                entryTimestamp = System.currentTimeMillis();
                break;
            case FIVE_MINUTES:
                referanceTime = TIME_REF.FIVE_MINUTES;
                retriveDataAggregationType = AGGREGATION_TYPE.ONE_MINUTE;
                entryTimestamp = System.currentTimeMillis() - TIME_REF.ONE_SECOND;
                break;
            case ONE_HOUR:
                referanceTime = TIME_REF.ONE_HOUR;
                retriveDataAggregationType = AGGREGATION_TYPE.ONE_MINUTE;
                entryTimestamp = System.currentTimeMillis() - TIME_REF.ONE_SECOND;
                break;
            case ONE_DAY:
                referanceTime = TIME_REF.ONE_DAY;
                retriveDataAggregationType = AGGREGATION_TYPE.FIVE_MINUTES;
                entryTimestamp = System.currentTimeMillis() - TIME_REF.ONE_SECOND;
                break;
            case THIRTY_DAYS:
                referanceTime = TIME_REF.ONE_HOUR_MAX_RETAIN_TIME;
                retriveDataAggregationType = AGGREGATION_TYPE.ONE_HOUR;
                break;
            case ONE_YEAR:
                referanceTime = TIME_REF.ONE_YEAR;
                retriveDataAggregationType = AGGREGATION_TYPE.ONE_DAY;
                break;
            case ALL_DAYS:
                referanceTime = TIME_REF.MILLISECONDS_2015;
                retriveDataAggregationType = AGGREGATION_TYPE.ONE_DAY;
                break;
            default:
                _logger.warn("Invalid type! nothing to do, type:{}", this.aggregationType);
                return;
        }
    }

    public List<MetricsDoubleTypeDevice> getMetricsDoubleTypeAllAfter(AGGREGATION_TYPE aggregationType, Sensor sensor) {
        this.setValuesBasedOnType(aggregationType);
        return this.getMetricsDoubleTypeAllAfter(sensor);
    }

    private List<MetricsDoubleTypeDevice> getMetricsDoubleTypeAllAfter(Sensor sensor) {
        List<MetricsDoubleTypeDevice> metrics = DaoUtils
                .getMetricsDoubleTypeDeviceDao()
                .getAllAfter(new MetricsDoubleTypeDevice(
                        sensor,
                        this.retriveDataAggregationType.ordinal(),
                        System.currentTimeMillis() - this.referanceTime));
        return metrics;
    }
    
    /** Get metric data for boolean type */

    public List<MetricsOnOffTypeDevice> getMetricsBooleanTypeAllAfter(AGGREGATION_TYPE aggregationType, Sensor sensor) {
        this.setValuesBasedOnType(aggregationType);
        return this.getMetricsOnOffTypeAllAfter(sensor);
    }

    private List<MetricsOnOffTypeDevice> getMetricsOnOffTypeAllAfter(Sensor sensor) {
        List<MetricsOnOffTypeDevice> metrics = DaoUtils
                .getMetricsOnOffTypeDeviceDao()
                .getAllAfter(new MetricsOnOffTypeDevice(
                        sensor,
                        System.currentTimeMillis() - this.referanceTime));
        return metrics;
    }

    private void purgeDB() {
        switch (aggregationType) {
            case ONE_MINUTE:
                MetricsAggregationUtils.purgeRawData();
                MetricsAggregationUtils.purgeOneMinuteData();
                break;
            case FIVE_MINUTES:
                MetricsAggregationUtils.purgeFiveMinutesData();
                break;
            case ONE_HOUR:
                /*Settings lastDayAggregation = DaoUtils.getSettingsDao().get(Settings.LAST_ONE_DAY_AGGREGATION);
                long yesterday = (System.currentTimeMillis() / TIME_REF.ONE_DAY) - TIME_REF.ONE_DAY;
                if (lastDayAggregation == null) {
                    long lastDay = System.currentTimeMillis() / TIME_REF.ONE_DAY;
                    lastDayAggregation = new Settings(Settings.LAST_ONE_DAY_AGGREGATION, lastDay);
                    DaoUtils.getSettingsDao().create(lastDayAggregation);
                } else if (yesterday == lastDayAggregation.getLongValue()) {
                    MetricsAggregationUtils.purgeOneHourData();
                }*/
                MetricsAggregationUtils.purgeOneHourData();
                break;
            default:
                _logger.debug("Invalid type or nothing to do, type:{}", aggregationType);
                break;
        }
    }
}
