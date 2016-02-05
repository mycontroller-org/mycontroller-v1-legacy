/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.api.jaxrs.AlarmHandler;
import org.mycontroller.standalone.api.jaxrs.AuthenticationHandler;
import org.mycontroller.standalone.api.jaxrs.DashboardHandler;
import org.mycontroller.standalone.api.jaxrs.FirmwareHandler;
import org.mycontroller.standalone.api.jaxrs.GatewayHandler;
import org.mycontroller.standalone.api.jaxrs.ResourcesGroupHandler;
import org.mycontroller.standalone.api.jaxrs.MyControllerHandler;
import org.mycontroller.standalone.api.jaxrs.MetricsHandler;
import org.mycontroller.standalone.api.jaxrs.NodeHandler;
import org.mycontroller.standalone.api.jaxrs.ForwardPayloadHandler;
import org.mycontroller.standalone.api.jaxrs.SensorHandler;
import org.mycontroller.standalone.api.jaxrs.ResourcesLogsHandler;
import org.mycontroller.standalone.api.jaxrs.SettingsHandler;
import org.mycontroller.standalone.api.jaxrs.TimerHandler;
import org.mycontroller.standalone.api.jaxrs.TypesHandler;
import org.mycontroller.standalone.api.jaxrs.UidTagHandler;
import org.mycontroller.standalone.api.jaxrs.SecurityHandler;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.*;
import org.mycontroller.standalone.api.jaxrs.mixins.McJacksonJson2Provider;
import org.mycontroller.standalone.auth.BasicAthenticationSecurityDomain;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.mycontroller.standalone.gateway.GatewaySerial;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.message.MessageMonitorThread;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.mqttbroker.MoquetteMqttBroker;
import org.mycontroller.standalone.mysensors.MySensorsIActionEngine;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class StartApp {
    private static final Logger _logger = LoggerFactory.getLogger(StartApp.class.getName());

    //TJWS Server instance
    final static TJWSEmbeddedJaxrsServer server = new TJWSEmbeddedJaxrsServer();
    static ResteasyDeployment deployment;
    static long start;

    public static void main(String[] args) {
        try {
            start = System.currentTimeMillis();
            loadInitialProperties();
            _logger.debug("App Properties: {}", ObjectFactory.getAppProperties().toString());
            _logger.debug("Operating System detail:[os:{},arch:{},version:{}]",
                    AppProperties.getOsName(), AppProperties.getOsArch(), AppProperties.getOsVersion());
            startServices();
            _logger.info("MyController.org server started in [{}] ms", System.currentTimeMillis() - start);
        } catch (Exception ex) {
            _logger.error("Unable to start application, refer error log,", ex);
            System.exit(1);//Terminate jvm, with non zero
        }
    }

    private static void addGateway() {
        if (DaoUtils.getGatewayDao().getAll().isEmpty()) {
            GatewaySerial gateway = new GatewaySerial();
            gateway.setEnabled(true);
            gateway.setType(GatewayUtils.TYPE.SERIAL);
            gateway.setNetworkType(NETWORK_TYPE.MY_SENSORS);
            gateway.setName("Serial-gateway");
            gateway.setPortName("/dev/ttyUSB0");
            gateway.setBaudRate(115200);
            gateway.setDriver(GatewayUtils.SERIAL_PORT_DRIVER.AUTO);
            gateway.setRetryFrequency(60);
            DaoUtils.getGatewayDao().create(gateway.getGateway());
        }

    }

    private static void loadStartingValues() {
        //Update sunrise/sunset time
        try {
            TimerUtils.updateSunriseSunset();
            _logger.debug("Sunrise[{}], Sunset[{}] time updated", TimerUtils.getSunriseTime(),
                    TimerUtils.getSunsetTime());
            //Disable all alram triggeres
            //DaoUtils.getAlarmDao().disableAllTriggered();

            //Load IActionEngines
            ObjectFactory.addIActionEngine(NETWORK_TYPE.MY_SENSORS, new MySensorsIActionEngine());

        } catch (Exception ex) {
            _logger.error("Failed to update sunrise/sunset time", ex);
        }
    }

    //ResteasyDeployment for TJWS server.
    private static ResteasyDeployment getResteasyDeployment() {
        if (deployment == null) {
            deployment = new ResteasyDeployment();
        }
        ArrayList<String> resources = new ArrayList<String>();
        resources.add(MyControllerHandler.class.getName());
        resources.add(NodeHandler.class.getName());
        resources.add(SensorHandler.class.getName());
        resources.add(TypesHandler.class.getName());
        resources.add(MetricsHandler.class.getName());
        resources.add(AuthenticationHandler.class.getName());
        resources.add(SecurityHandler.class.getName());
        resources.add(AlarmHandler.class.getName());
        resources.add(ResourcesLogsHandler.class.getName());
        resources.add(TimerHandler.class.getName());
        resources.add(ForwardPayloadHandler.class.getName());
        resources.add(UidTagHandler.class.getName());
        resources.add(FirmwareHandler.class.getName());
        resources.add(SettingsHandler.class.getName());
        resources.add(GatewayHandler.class.getName());
        resources.add(ResourcesGroupHandler.class.getName());
        resources.add(DashboardHandler.class.getName());

        //Add PreFlight handler
        //resources.add(OptionsHandler.class.getName());

        //Add Exception mapper(providers)
        ArrayList<Object> providers = new ArrayList<Object>();
        providers.add(new BadRequestExceptionMapper());
        providers.add(new NotAcceptableExceptionMapper());
        providers.add(new NotAllowedExceptionMapper());
        providers.add(new NotFoundExceptionMapper());
        providers.add(new NotSupportedExceptionMapper());
        providers.add(new DefaultOptionsMethodExceptionMapper());
        providers.add(new ForbiddenExceptionMapper());
        providers.add(new ApplicationExceptionMapper());
        providers.add(new McJacksonJson2Provider()); //Mixin provider

        //Add all resourceClasses
        deployment.setResourceClasses(resources);
        //Add all providers
        deployment.setProviders(providers);
        return deployment;
    }

    private static void startHTTPWebServer() {
        //Check HTTPS enabled?
        if (ObjectFactory.getAppProperties().isWebHttpsEnabled()) {
            // Set up SSL connections on server
            server.setSSLPort(ObjectFactory.getAppProperties().getWebHttpPort());
            server.setSSLKeyStoreFile(ObjectFactory.getAppProperties().getWebSslKeystoreFile());
            server.setSSLKeyStorePass(ObjectFactory.getAppProperties().getWebSslKeystorePassword());
            server.setSSLKeyStoreType(ObjectFactory.getAppProperties().getWebSslKeystoreType());
        } else {
            //Set http communication port
            server.setPort(ObjectFactory.getAppProperties().getWebHttpPort());
        }

        if (ObjectFactory.getAppProperties().getWebBindAddress() != null) {
            server.setBindAddress(ObjectFactory.getAppProperties().getWebBindAddress());
        }

        //Deploy RestEasy with TJWS
        server.setDeployment(getResteasyDeployment());
        server.addFileMapping("/", new File(ObjectFactory.getAppProperties().getWebFileLocation()));

        //Enable Authentication
        server.setSecurityDomain(new BasicAthenticationSecurityDomain());
        server.getDeployment().setSecurityEnabled(true);

        // Start TJWS server
        server.setRootResourcePath("/mc");

        server.start();
        _logger.info("TJWS server started successfully, HTTPS Enabled?:{}, HTTP(S) Port: [{}]",
                ObjectFactory.getAppProperties().isWebHttpsEnabled(),
                ObjectFactory.getAppProperties().getWebHttpPort());
    }

    private static boolean startServices() throws ClassNotFoundException, SQLException {
        //Start order..
        // - Add Shutdown hook
        // - Start DB service
        // - Start message Monitor Thread
        // - Load starting values
        // - Start MQTT Broker
        // - Start gateway listener
        // - Start scheduler
        // - Start Web Server

        //Add Shutdown hook
        new AppShutdownHook().attachShutDownHook();

        //Start DB service
        DataBaseUtils.loadDatabase();

        //Start message Monitor Thread
        //Create RawMessageQueue, which is required for MessageMonitorThread
        ObjectFactory.setRawMessageQueue(new RawMessageQueue());
        //Create new thread to monitor received logs
        MessageMonitorThread messageMonitorThread = new MessageMonitorThread();
        Thread thread = new Thread(messageMonitorThread);
        thread.start();

        // - Load starting values
        loadStartingValues();

        // - Start MQTT Broker
        MoquetteMqttBroker.start();

        //TODO: ------------------REMOVE BELOW LINE----------------------
        addGateway();
        //------------------------REMOVE ABOVE LINE----------------------

        //Start all the gateways
        GatewayUtils.loadAllGateways();

        // - Start scheduler
        SchedulerUtils.startScheduler();

        // - Start Web Server
        startHTTPWebServer();

        return true;
    }

    public static synchronized void stopServices() {
        //Stop order..
        // - Stop scheduler
        // - Stop Gateway Listener
        // - Stop MQTT broker
        // - Stop message Monitor Thread
        // - Clear Raw Message Queue (Optional)
        // - Stop DB service
        SchedulerUtils.stop();
        GatewayUtils.unloadAllGateways();
        MoquetteMqttBroker.stop();
        MessageMonitorThread.setTerminationIssued(true);
        DataBaseUtils.stop();
        _logger.debug("All services stopped. Shutting down...");
        _logger.info("Bye, Have a nice day! See you soon");
    }

    private static boolean loadInitialProperties() {
        String propertiesFile = System.getProperty("mc.conf.file");
        try {
            Properties properties = new Properties();
            if (propertiesFile == null) {
                properties
                        .load(ClassLoader.getSystemClassLoader().getResourceAsStream("mycontroller.properties"));

            } else {
                properties.load(new FileReader(propertiesFile));
            }
            AppProperties appProperties = new AppProperties(properties);
            ObjectFactory.setAppProperties(appProperties);
            _logger.debug("Properties are loaded successfuly...");
            return true;
        } catch (IOException ex) {
            _logger.error("Exception while loading properties file, ", ex);
            return false;
        }
    }
}
