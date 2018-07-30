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
package org.mycontroller.standalone.metrics.export;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.api.MetricApi;
import org.mycontroller.standalone.api.jaxrs.model.DataPointBinary;
import org.mycontroller.standalone.api.jaxrs.model.DataPointCounter;
import org.mycontroller.standalone.api.jaxrs.model.DataPointDouble;
import org.mycontroller.standalone.api.jaxrs.model.DataPointGPS;
import org.mycontroller.standalone.api.jaxrs.model.MetricsCsv;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.model.ResourceModel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class CsvExportEngine {

    private static final SimpleDateFormat FILE_NAME_TIME_FORMAT = new SimpleDateFormat("yyyy-MMM-dd_hh-mm-ss");

    private MetricApi metricApi = new MetricApi();

    public String writeOnDisk(String uid, Long start, Long end, String duration, String bucketDuration,
            String writeOnDir) throws McBadRequestException, IOException {
        MetricsCsv metricsCsv = getMetric(uid, start, end, duration, bucketDuration);
        writeFileToDisk(writeOnDir, metricsCsv);
        return metricsCsv.getFileName();
    }

    public String writeOnDisk(Integer resourceId, String resourceType, Long start, Long end, String duration,
            String bucketDuration, String writeOnDir) throws McBadRequestException, IOException {
        MetricsCsv metricsCsv = getMetric(resourceId, resourceType, start, end, duration, bucketDuration);
        writeFileToDisk(writeOnDir, metricsCsv);
        return metricsCsv.getFileName();
    }

    private void writeFileToDisk(String writeOnDir, MetricsCsv metricsCsv) throws IOException {
        File targetFile = FileUtils.getFile(FileUtils.getFile(writeOnDir).getCanonicalPath() + File.separator
                + metricsCsv.getFileName());
        _logger.debug("FileName:{}", targetFile.getCanonicalPath());
        FileUtils.getFile(targetFile.getParentFile()).mkdirs();
        FileUtils.writeStringToFile(targetFile, metricsCsv.getData());
        metricsCsv.setFileName(targetFile.getCanonicalPath());
    }

    public MetricsCsv getMetric(String uid, Long start, Long end, String duration, String bucketDuration)
            throws McBadRequestException {
        return getMetric(metricApi.getResourceModel(null, null, uid), start, end, duration, bucketDuration);
    }

    public MetricsCsv getMetric(Integer resourceId, String resourceType, Long start, Long end,
            String duration, String bucketDuration) throws McBadRequestException {
        return getMetric(metricApi.getResourceModel(resourceId, resourceType, null), start, end, duration,
                bucketDuration);
    }

    private MetricsCsv getMetric(ResourceModel resourceModel, Long start, Long end, String duration,
            String bucketDuration) throws McBadRequestException {
        //Update dataType when required
        List<?> metrics = metricApi.getMetricData(resourceModel, start, end, duration, bucketDuration, null);
        return new MetricsCsv(getFileName(resourceModel), getCsvData(resourceModel, metrics));
    }

    private String getFileName(ResourceModel resourceModel) {
        StringBuilder builder = new StringBuilder();
        builder.append("mc_metric_csv_")
                .append("rType_").append(resourceModel.getResourceType().getText())
                .append("rId_").append(resourceModel.getResourceId())
                .append("_").append(FILE_NAME_TIME_FORMAT.format(new Date()))
                .append(".csv");
        return builder.toString().replaceAll(" ", "_");
    }

    @SuppressWarnings("unchecked")
    private String getCsvData(ResourceModel resourceModel, List<?> metrics) {
        _logger.debug("{}", metrics);
        StringBuilder builder = new StringBuilder();
        boolean isStartSet = false;
        METRIC_TYPE metrictType = null;
        switch (resourceModel.getResourceType()) {
            case NODE:
                metrictType = METRIC_TYPE.DOUBLE;
                break;
            case SENSOR_VARIABLE:
                metrictType = ((SensorVariable) resourceModel.getResource()).getMetricType();
                break;
            default:
                return null;
        }
        //Headers
        switch (metrictType) {
            case BINARY:
                builder.append("timestamp").append(",");
                builder.append("state");
                break;
            case DOUBLE:
                DataPointDouble doubleMetric = (DataPointDouble) metrics.get(0);
                if (doubleMetric.getStart() != null) {
                    isStartSet = true;
                    builder.append("start").append(",");
                    builder.append("end").append(",");
                } else {
                    builder.append("timestamp").append(",");
                }
                builder.append("empty").append(",");
                builder.append("samples").append(",");
                builder.append("minimum").append(",");
                builder.append("maximum").append(",");
                builder.append("average");
                break;
            case COUNTER:
                DataPointCounter counterMetric = (DataPointCounter) metrics.get(0);
                if (counterMetric.getStart() != null) {
                    isStartSet = true;
                    builder.append("start").append(",");
                    builder.append("end").append(",");
                } else {
                    builder.append("timestamp").append(",");
                }
                builder.append("empty").append(",");
                builder.append("samples").append(",");
                builder.append("value");
                break;
            case GPS:
                DataPointGPS gpsMetric = (DataPointGPS) metrics.get(0);
                if (gpsMetric.getStart() != null) {
                    isStartSet = true;
                    builder.append("start").append(",");
                    builder.append("end").append(",");
                } else {
                    builder.append("timestamp").append(",");
                }
                builder.append("empty").append(",");
                builder.append("samples").append(",");
                builder.append("lantitude").append(",");
                builder.append("longitude").append(",");
                builder.append("altitude");
                break;
            default:
                break;
        }

        if (metrics.isEmpty()) {
            return builder.toString();
        }

        //Update data
        switch (metrictType) {
            case BINARY:
                for (DataPointBinary metric : (List<DataPointBinary>) metrics) {
                    builder.append("\n");
                    builder.append(metric.getTimestamp()).append(",");
                    builder.append(getValue(metric.getState()));
                }
                break;
            case DOUBLE:
                for (DataPointDouble metric : (List<DataPointDouble>) metrics) {
                    builder.append("\n");
                    if (isStartSet) {
                        builder.append(metric.getStart()).append(",");
                        builder.append(metric.getEnd()).append(",");
                    } else {
                        builder.append(metric.getTimestamp()).append(",");
                    }
                    builder.append(metric.isEmpty()).append(",");
                    builder.append(getValue(metric.getSamples())).append(",");
                    builder.append(getValue(metric.getMin())).append(",");
                    builder.append(getValue(metric.getMax())).append(",");
                    builder.append(getValue(metric.getAvg()));
                }
                break;
            case COUNTER:
                for (DataPointCounter metric : (List<DataPointCounter>) metrics) {
                    builder.append("\n");
                    if (isStartSet) {
                        builder.append(metric.getStart()).append(",");
                        builder.append(metric.getEnd()).append(",");
                    } else {
                        builder.append(metric.getTimestamp()).append(",");
                    }
                    builder.append(metric.isEmpty()).append(",");
                    builder.append(getValue(metric.getSamples())).append(",");
                    builder.append(getValue(metric.getValue()));
                }
                break;
            case GPS:
                for (DataPointGPS metric : (List<DataPointGPS>) metrics) {
                    builder.append("\n");
                    if (isStartSet) {
                        builder.append(metric.getStart()).append(",");
                        builder.append(metric.getEnd()).append(",");
                    } else {
                        builder.append(metric.getTimestamp()).append(",");
                    }
                    builder.append(metric.isEmpty()).append(",");
                    builder.append(getValue(metric.getSamples())).append(",");
                    builder.append(getValue(metric.getLantitude())).append(",");
                    builder.append(getValue(metric.getLongitude())).append(",");
                    builder.append(getValue(metric.getAltitude()));
                }
                break;
            default:
                break;
        }
        return builder.toString();
    }

    private Object getValue(Object value) {
        if (value == null) {
            return "";
        }
        return value;
    }

}
