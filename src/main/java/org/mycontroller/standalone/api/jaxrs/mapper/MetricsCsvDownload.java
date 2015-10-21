package org.mycontroller.standalone.api.jaxrs.mapper;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MetricsCsvDownload {
    private String fileName;
    private String data;

    public MetricsCsvDownload() {

    }

    public MetricsCsvDownload(String fileName) {
        this(fileName, null);
    }

    public MetricsCsvDownload(String fileName, String data) {
        this.fileName = fileName;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
