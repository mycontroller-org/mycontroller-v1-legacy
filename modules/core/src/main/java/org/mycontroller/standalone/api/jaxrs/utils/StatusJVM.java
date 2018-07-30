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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sun.management.GcInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class StatusJVM extends StatusBase {
    private static final String GET_LAST_GC_INFO = "getLastGcInfo";

    public List<HashMap<String, Object>> getGarbageCollectors() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        for (GarbageCollectorMXBean bean : garbageCollectorMXBeans) {
            list.add(getGarbageCollector(bean));
        }
        return list;
    }

    private HashMap<String, Object> getGarbageCollector(GarbageCollectorMXBean bean) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        GcInfo gcInfo = getGcInfo(bean);
        hashMap.put("name", bean.getName());
        hashMap.put("collectionCount", bean.getCollectionCount());
        hashMap.put("collectionTime", bean.getCollectionTime());
        hashMap.put("lastGcStartTime", gcInfo.getStartTime());
        hashMap.put("lastGcEndTime", gcInfo.getEndTime());
        hashMap.put("lastGcDuration", gcInfo.getDuration());
        hashMap.put("memoryUsageBeforeGc", gcInfo.getMemoryUsageBeforeGc());
        hashMap.put("memoryUsageAfterGc", gcInfo.getMemoryUsageAfterGc());
        return hashMap;

    }

    private GcInfo getGcInfo(GarbageCollectorMXBean bean) {
        try {
            Method method = bean.getClass().getMethod(GET_LAST_GC_INFO, new Class[] {});
            method.setAccessible(true);
            return (GcInfo) method.invoke(bean, new Object[] {});
        } catch (Exception e) {
            _logger.error("Error, ", e);
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Object> getClassLoadingDetail() {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("loadedClassCount", classLoadingMXBean.getLoadedClassCount());
        hashMap.put("unloadedClassCount", classLoadingMXBean.getUnloadedClassCount());
        hashMap.put("totalLoadedClassCount", classLoadingMXBean.getTotalLoadedClassCount());
        return hashMap;
    }

    public HashMap<String, Object> getMemory() {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("heapMemoryUsage", getMemoryUsage(memoryMXBean.getHeapMemoryUsage()));
        hashMap.put("nonHeapMemoryUsage", getMemoryUsage(memoryMXBean.getNonHeapMemoryUsage()));
        return hashMap;
    }

    private HashMap<String, Object> getMemoryUsage(MemoryUsage memoryUsage) {
        HashMap<String, Object> usage = new HashMap<String, Object>();
        usage.put("init", memoryUsage.getInit());
        usage.put("committed", memoryUsage.getCommitted());
        usage.put("used", memoryUsage.getUsed());
        usage.put("max", memoryUsage.getMax());
        return usage;
    }

    public HashMap<String, Object> getThread() {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        hashMap.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        hashMap.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());
        hashMap.put("threadCount", threadMXBean.getThreadCount());
        return hashMap;
    }

    public HashMap<String, Object> getJvmInfo() {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("startTime", runtimeMXBean.getStartTime());
        hashMap.put("uptime", runtimeMXBean.getUptime());
        hashMap.put("name", runtimeMXBean.getName());
        hashMap.put("managementSpecVersion", runtimeMXBean.getManagementSpecVersion());
        hashMap.put("specName", runtimeMXBean.getSpecName());
        hashMap.put("specVendor", runtimeMXBean.getSpecVendor());
        hashMap.put("specVersion", runtimeMXBean.getSpecVersion());
        hashMap.put("vmName", runtimeMXBean.getVmName());
        hashMap.put("vmVendor", runtimeMXBean.getVmVendor());
        hashMap.put("vmVersion", runtimeMXBean.getVmVersion());
        return hashMap;
    }

}
