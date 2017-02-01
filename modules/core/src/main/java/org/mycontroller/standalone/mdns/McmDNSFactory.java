/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.mdns;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.SystemApi;
import org.mycontroller.standalone.api.jaxrs.model.McAbout;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Slf4j
public class McmDNSFactory {
    public static final int WEIGHT_DEFAULT = 10;
    public static final int PRIORITY_DEFAULT = 10;

    private static JmDNS jmDNSservice = null;
    private static final AtomicBoolean isServiceStopped = new AtomicBoolean(false);

    private static McmDNSServiceInfo mqttService = null;
    private static McmDNSServiceInfo restApiService = null;
    private static McmDNSServiceInfo httpService = null;

    private static JmDNS getJmDNSservice() {
        if (isServiceStopped.get()) {
            return null;
        }
        if (jmDNSservice == null) {
            try {
                InetAddress bindAddress = null;
                if (AppProperties.getInstance().getWebBindAddress().equalsIgnoreCase("0.0.0.0")) {
                    List<InetAddress> addrList = new ArrayList<InetAddress>();
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface ifc = interfaces.nextElement();
                        if (ifc.isUp() && !(ifc.isLoopback() || ifc.isPointToPoint() || ifc.isVirtual())
                                && ifc.supportsMulticast()) {
                            Enumeration<InetAddress> addressesOfAnInterface = ifc.getInetAddresses();
                            while (addressesOfAnInterface.hasMoreElements()) {
                                InetAddress addr = addressesOfAnInterface.nextElement();
                                //If we have IPv4 use it and exit from the loop.
                                if (Inet4Address.class == addr.getClass()) {
                                    addrList.add(addr);
                                    bindAddress = addr;
                                    break;
                                }
                            }
                        }
                    }
                    if (!addrList.isEmpty()) {
                        if (bindAddress == null) {
                            bindAddress = addrList.get(0);
                        }
                        _logger.debug(
                                "Web binding address set as '{}', hence taking random interface[{}] for mDNS service."
                                        + " Update web-binding address if you need mDNS on specific interface.",
                                AppProperties.getInstance().getWebBindAddress(), bindAddress.toString());
                    }
                } else {
                    bindAddress = InetAddress.getByName(AppProperties.getInstance().getWebBindAddress());
                }
                if (bindAddress == null) {
                    _logger.warn("Unable to start mDNS service. Please check your web bind address.");
                    isServiceStopped.set(true);
                    return null;
                }
                jmDNSservice = JmDNS.create(bindAddress, InetAddress.getLocalHost().getCanonicalHostName());
                _logger.debug("Started MDNS services...");
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        this.setName(AppProperties.APPLICATION_NAME + " mDNS-Shutdown-Hook");
                        unregisterAllServices();
                        try {
                            if (jmDNSservice != null && !isServiceStopped.get()) {
                                jmDNSservice.close();
                                isServiceStopped.set(true);
                                _logger.debug("Stopped mDNS services...");
                            }
                        } catch (IOException ex) {
                            _logger.error("Unable to close mDNS service!", ex);
                        }
                    }
                });
            } catch (Exception ex) {
                _logger.error("Unable to create mDNS registry service");
                isServiceStopped.set(true);
            }
        }
        return jmDNSservice;
    }

    private static ServiceInfo getServiceInfo(McmDNSServiceInfo serviceInfo) {
        return ServiceInfo.create(serviceInfo.getType(), serviceInfo.getName(), serviceInfo.getPort(),
                serviceInfo.getWeight(), serviceInfo.getPriority(), serviceInfo.getProperties());
    }

    public static void registerService(McmDNSServiceInfo serviceInfo) {
        if (isServiceStopped.get() || getJmDNSservice() == null) {
            return;
        }
        try {
            unregisterService(serviceInfo);
            getJmDNSservice().registerService(getServiceInfo(serviceInfo));
            _logger.info("Registered a mDNS service {}", serviceInfo);
        } catch (IOException ex) {
            _logger.error("Unable to register[{}] service", serviceInfo);
        }
    }

    public static void unregisterService(McmDNSServiceInfo serviceInfo) {
        if (isServiceStopped.get() || getJmDNSservice() == null) {
            return;
        }
        if (getJmDNSservice().getServiceInfo(serviceInfo.getType(), serviceInfo.getName()) != null) {
            getJmDNSservice().unregisterService(getServiceInfo(serviceInfo));
            _logger.info("Unregistered a mDNS service {}", serviceInfo);
        }
    }

    public static void unregisterAllServices() {
        if (isServiceStopped.get() || getJmDNSservice() == null) {
            return;
        }
        getJmDNSservice().unregisterAllServices();
    }

    public static void updateServices(boolean enable) {
        updateHttpService(enable);
        updateMqttService();
    }

    public static void updateMqttService() {
        if (AppProperties.getInstance().getMqttBrokerSettings().getEnabled()) {
            registerService(getMqttService());
        } else {
            unregisterService(getMqttService());
        }
    }

    public static void updateHttpService(boolean enable) {
        if (enable) {
            registerService(getHttpService());
            registerService(getRestApiService());
        } else {
            unregisterService(getHttpService());
            unregisterService(getRestApiService());
        }
    }

    private static McmDNSServiceInfo getMqttService() {
        if (mqttService == null) {
            mqttService = McmDNSServiceInfo.builder()
                    .type("_mc_mqtt._tcp.")
                    .name("MyController Mqtt broker service")
                    .port(AppProperties.getInstance().getMqttBrokerSettings().getHttpPort())
                    .weight(WEIGHT_DEFAULT)
                    .priority(PRIORITY_DEFAULT)
                    .build();
            mqttService.setProperty("feed", "device");
            loadVersionDetails(mqttService);
        }
        return mqttService;
    }

    private static McmDNSServiceInfo getRestApiService() {
        if (restApiService == null) {
            restApiService = McmDNSServiceInfo.builder()
                    .type("_mc_restapi._tcp.")
                    .name("MyController REST API service")
                    .port(AppProperties.getInstance().getWebHttpPort())
                    .weight(WEIGHT_DEFAULT)
                    .priority(PRIORITY_DEFAULT)
                    .build();
            restApiService.setProperty("path", "/mc/rest");
            loadVersionDetails(restApiService);
        }
        return restApiService;
    }

    private static McmDNSServiceInfo getHttpService() {
        if (httpService == null) {
            httpService = McmDNSServiceInfo.builder()
                    .type("_mc_http._tcp.")
                    .name("MyController HTTP service")
                    .port(AppProperties.getInstance().getWebHttpPort())
                    .weight(WEIGHT_DEFAULT)
                    .priority(PRIORITY_DEFAULT)
                    .build();
            httpService.setProperty("path", "/index.html");
            loadVersionDetails(httpService);
        }
        return httpService;
    }

    private static void loadVersionDetails(McmDNSServiceInfo dnsServiceInfo) {
        McAbout mcAbout = new SystemApi().getAbout();
        dnsServiceInfo.setProperty("appVersion", mcAbout.getApplicationVersion());
        dnsServiceInfo.setProperty("dbType", mcAbout.getDatabaseType());
        dnsServiceInfo.setProperty("builtOn", mcAbout.getGitBuiltOn());
        dnsServiceInfo.setProperty("osName", mcAbout.getOsName());
    }
}
