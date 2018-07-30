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
package org.mycontroller.standalone.api.jaxrs.model;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
@Slf4j
public class McAboutBase {
    private String javaVmVendor;
    private String javaVmName;
    private String javaRuntimeVersion;
    private String javaVersion;
    private String javaHome;

    private String osArch;
    private String osName;
    private String osVersion;

    private String gitBranch;
    private String gitVersion;
    private String gitBuiltBy;
    private String gitCommit;
    private String gitCreatedBy;
    private String gitBuildJdk;
    private String gitBuiltOn;

    public McAboutBase() {
        javaVmVendor = System.getProperty("java.vm.vendor");
        javaVmName = System.getProperty("java.vm.name");
        javaRuntimeVersion = System.getProperty("java.runtime.version");
        javaHome = System.getProperty("java.home");

        osArch = System.getProperty("os.arch");
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");

        //Load git commit related details from MANIFEST.MF file
        String className = this.getClass().getSimpleName() + ".class";
        String classPath = this.getClass().getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return;
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        try {
            Manifest manifest;
            manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            gitBranch = attr.getValue("Built-From-Git-Branch");
            gitVersion = attr.getValue("Implementation-Version");
            gitBuiltBy = attr.getValue("Built-By");
            gitCommit = attr.getValue("Built-From-Git-SHA1");
            gitCreatedBy = attr.getValue("Created-By");
            gitBuildJdk = attr.getValue("Build-Jdk");
            gitBuiltOn = attr.getValue("Built-On");
        } catch (IOException ex) {
            _logger.error("Error, ", ex);
        }

    }
}
