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
package org.mycontroller.standalone.api.jaxrs.json;

import org.mycontroller.standalone.AppProperties;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
public class McAbout {
    private String applicationVersion;
    private String applicationDbVersion;
    private String applicationLocation;

    private String javaVmVendor;
    private String javaVmName;
    private String javaRuntimeVersion;
    private String javaVersion;
    private String javaHome;

    private String osArch;
    private String osName;
    private String osVersion;

    public McAbout() {
        applicationVersion = AppProperties.getInstance().getControllerSettings().getVersion();
        applicationDbVersion = AppProperties.getInstance().getControllerSettings().getDbVersion();
        applicationLocation = AppProperties.getInstance().getAppDirectory();

        javaVmVendor = System.getProperty("java.vm.vendor");
        javaVmName = System.getProperty("java.vm.name");
        javaRuntimeVersion = System.getProperty("java.runtime.version");
        javaHome = System.getProperty("java.home");

        osArch = AppProperties.getOsArch();
        osName = AppProperties.getOsName();
        osVersion = AppProperties.getOsVersion();
    }
}
