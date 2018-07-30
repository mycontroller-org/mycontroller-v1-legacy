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
package org.mycontroller.standalone.api.jaxrs.utils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class StatusOS extends StatusBase {
    public String getName() {
        return operatingSystemMXBean.getName();
    }

    public String getUserWorkingDir() {
        return System.getProperty("user.dir");
    }

    public String getJavaHome() {
        return System.getProperty("java.home");
    }

    public String getJavaVendorUrl() {
        return System.getProperty("java.vendor.url");
    }

    public String getArch() {
        return operatingSystemMXBean.getArch();
    }

    public String getVersion() {
        return operatingSystemMXBean.getVersion();
    }

    public int getAvailableProcess() {
        return operatingSystemMXBean.getAvailableProcessors();
    }

    public String getSystemLoadAverage() {
        if (operatingSystemMXBean.getSystemLoadAverage() >= 0) {
            return String.format("%.2f %%", operatingSystemMXBean.getSystemLoadAverage() * 100.0);
        } else {
            return "n/a";
        }
    }

    public long getCommittedVirtualMemorySize() {
        return operatingSystemMXBean.getCommittedVirtualMemorySize();
    }

    public long getFreePhysicalMemorySize() {
        return operatingSystemMXBean.getFreePhysicalMemorySize();
    }

    public long getFreeSwapSpaceSize() {
        return operatingSystemMXBean.getFreeSwapSpaceSize();
    }

    public String getProcessCpuLoad() {
        if (operatingSystemMXBean.getProcessCpuLoad() >= 0) {
            return String.format("%.2f %%", operatingSystemMXBean.getProcessCpuLoad() * 100.0);
        } else {
            return "n/a";
        }
    }

    public String getProcessCpuTime() {
        if (operatingSystemMXBean.getProcessCpuTime() >= 0) {
            return String.format("%d milliseconds", operatingSystemMXBean.getProcessCpuTime() / (1000 * 1000));
        } else {
            return "n/a";
        }
    }

    public String getSystemCpuLoad() {
        if (operatingSystemMXBean.getSystemCpuLoad() >= 0) {
            return String.format("%.2f %%", operatingSystemMXBean.getSystemCpuLoad() * 100.0);
        } else {
            return "n/a";
        }
    }

    public long getTotalPhysicalMemorySize() {
        return operatingSystemMXBean.getTotalPhysicalMemorySize();
    }

    public long getTotalSwapSpaceSize() {
        return operatingSystemMXBean.getTotalSwapSpaceSize();
    }

}
