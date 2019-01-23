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
package org.mycontroller.standalone;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.mycontroller.standalone.api.GoogleAnalyticsApi;
import org.mycontroller.standalone.api.jaxrs.AuthenticationHandler;
import org.mycontroller.standalone.api.jaxrs.BackupHandler;
import org.mycontroller.standalone.api.jaxrs.DashboardHandler;
import org.mycontroller.standalone.api.jaxrs.ExternalServerHandler;
import org.mycontroller.standalone.api.jaxrs.FirmwareHandler;
import org.mycontroller.standalone.api.jaxrs.ForwardPayloadHandler;
import org.mycontroller.standalone.api.jaxrs.GatewayHandler;
import org.mycontroller.standalone.api.jaxrs.MetricsHandler;
import org.mycontroller.standalone.api.jaxrs.MyControllerHandler;
import org.mycontroller.standalone.api.jaxrs.NodeHandler;
import org.mycontroller.standalone.api.jaxrs.OSCommandExecuterHandler;
import org.mycontroller.standalone.api.jaxrs.OperationHandler;
import org.mycontroller.standalone.api.jaxrs.OptionsHandler;
import org.mycontroller.standalone.api.jaxrs.ResourcesDataHandler;
import org.mycontroller.standalone.api.jaxrs.ResourcesGroupHandler;
import org.mycontroller.standalone.api.jaxrs.ResourcesLogsHandler;
import org.mycontroller.standalone.api.jaxrs.RoomHandler;
import org.mycontroller.standalone.api.jaxrs.RuleHandler;
import org.mycontroller.standalone.api.jaxrs.ScriptsHandler;
import org.mycontroller.standalone.api.jaxrs.SecurityHandler;
import org.mycontroller.standalone.api.jaxrs.SensorHandler;
import org.mycontroller.standalone.api.jaxrs.SettingsHandler;
import org.mycontroller.standalone.api.jaxrs.TemplatesHandler;
import org.mycontroller.standalone.api.jaxrs.TimerHandler;
import org.mycontroller.standalone.api.jaxrs.TypesHandler;
import org.mycontroller.standalone.api.jaxrs.UidTagHandler;
import org.mycontroller.standalone.api.jaxrs.VariablesHandler;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.ApplicationExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.BadRequestExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.DefaultOptionsMethodExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.ForbiddenExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.NotAcceptableExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.NotAllowedExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.NotAuthorizedExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.NotFoundExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.exception.mappers.NotSupportedExceptionMapper;
import org.mycontroller.standalone.api.jaxrs.mixins.McJacksonJson2Provider;
import org.mycontroller.standalone.auth.BasicAthenticationSecurityDomain;
import org.mycontroller.standalone.auth.McContainerRequestFilter;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.mycontroller.standalone.externalserver.ExternalServerFactory;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.mdns.McmDNSFactory;
import org.mycontroller.standalone.metrics.MetricsUtils;
import org.mycontroller.standalone.mqttbroker.MoquetteMqttBroker;
import org.mycontroller.standalone.offheap.OffHeapFactory;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.scripts.McScriptEngineUtils;
import org.mycontroller.standalone.settings.SettingsUtils;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.utils.McServerFileUtils;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class StartApp {

    //TJWS Server instance
    private static final TJWSEmbeddedJaxrsServer server = new TJWSEmbeddedJaxrsServer();
    static ResteasyDeployment deployment;
    static long start;

    public static void main(String[] args) {
        try {
            _logger.info(
                    "\n\n"
                            + "****************************************** Data Processing Agreement *****************"
                            + "**************************\n"
                            + "By using this software you agree that the following non-PII (non personally"
                            + " identifiable information data       \n"
                            + "will be collected, processed and used by MyController.org for the purpose of"
                            + " improving quality of this software.\n"
                            + "----------------------------------------------------------------------------"
                            + "------------------------------------\n"
                            + "If you do not like to share anonymous data(non-PII), disable it in "
                            + "'mycontroller/conf/mycontroller.properties'  \n"
                            + "by setting 'mcc.collect.anonymous.data=false' and **restart** this server and **logout"
                            + " and login** in the UI.   \n"
                            + "                                          ------ Thank you! ------                     "
                            + "                         \n"
                            + "***************************************************************************************"
                            + "*************************\n\n");
            startMycontroller();
        } catch (Exception ex) {
            _logger.error("Unable to start application, refer error log,", ex);
            System.exit(1);//Terminate jvm, with non zero
        }
    }

    public static synchronized void startMycontroller() throws ClassNotFoundException, SQLException,
            URISyntaxException {
        start = System.currentTimeMillis();
        loadInitialProperties(System.getProperty("mc.conf.file"));
        _logger.debug("App Properties: {}", AppProperties.getInstance().toString());
        _logger.debug("Operating System detail:[os:{},arch:{},version:{}]",
                AppProperties.getOsName(), AppProperties.getOsArch(), AppProperties.getOsVersion());
        startServices();
        long duration = System.currentTimeMillis() - start;
        GoogleAnalyticsApi.instance().trackStartTime(duration);
        _logger.info("MyController.org server started in [{}] ms", duration);
    }

    private static void loadStartingValues() {
        try {
            // update log file location
            McServerFileUtils.updateApplicationLogLocation();
            // update sunrise/sunset time
            TimerUtils.updateSunriseSunset();
            _logger.debug("Sunrise[{}], Sunset[{}] time updated", TimerUtils.getSunriseTime(),
                    TimerUtils.getSunsetTime());
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
        resources.add(AuthenticationHandler.class.getName());
        resources.add(BackupHandler.class.getName());
        resources.add(DashboardHandler.class.getName());
        resources.add(ExternalServerHandler.class.getName());
        resources.add(FirmwareHandler.class.getName());
        resources.add(ForwardPayloadHandler.class.getName());
        resources.add(GatewayHandler.class.getName());
        resources.add(MetricsHandler.class.getName());
        resources.add(MyControllerHandler.class.getName());
        resources.add(NodeHandler.class.getName());
        resources.add(OperationHandler.class.getName());
        resources.add(OSCommandExecuterHandler.class.getName());
        resources.add(ResourcesDataHandler.class.getName());
        resources.add(ResourcesGroupHandler.class.getName());
        resources.add(ResourcesLogsHandler.class.getName());
        resources.add(RoomHandler.class.getName());
        resources.add(RuleHandler.class.getName());
        resources.add(ScriptsHandler.class.getName());
        resources.add(SecurityHandler.class.getName());
        resources.add(SensorHandler.class.getName());
        resources.add(SettingsHandler.class.getName());
        resources.add(TemplatesHandler.class.getName());
        resources.add(TimerHandler.class.getName());
        resources.add(TypesHandler.class.getName());
        resources.add(UidTagHandler.class.getName());
        resources.add(VariablesHandler.class.getName());

        //Add PreFlight handler
        resources.add(OptionsHandler.class.getName());

        //Add Exception mapper(providers)
        ArrayList<Object> providers = new ArrayList<Object>();
        providers.add(new ApplicationExceptionMapper());
        providers.add(new BadRequestExceptionMapper());
        providers.add(new DefaultOptionsMethodExceptionMapper());
        providers.add(new ForbiddenExceptionMapper());
        providers.add(new McContainerRequestFilter());//SecurityInterceptor
        providers.add(new McJacksonJson2Provider()); //Mixin provider
        providers.add(new NotAcceptableExceptionMapper());
        providers.add(new NotAllowedExceptionMapper());
        providers.add(new NotAuthorizedExceptionMapper());
        providers.add(new NotFoundExceptionMapper());
        providers.add(new NotSupportedExceptionMapper());

        //Add all resourceClasses
        deployment.setResourceClasses(resources);
        //Add all providers
        deployment.setProviders(providers);
        return deployment;
    }

    private static void startHTTPWebServer() {
        //Check HTTPS enabled?
        if (AppProperties.getInstance().isWebHttpsEnabled()) {
            // Set up SSL connections on server
            server.setSSLPort(AppProperties.getInstance().getWebHttpPort());
            server.setSSLKeyStoreFile(AppProperties.getInstance().getWebSslKeystoreFile());
            server.setSSLKeyStorePass(AppProperties.getInstance().getWebSslKeystorePassword());
            server.setSSLKeyStoreType(AppProperties.getInstance().getWebSslKeystoreType());
        } else {
            //Set http communication port
            server.setPort(AppProperties.getInstance().getWebHttpPort());
        }

        if (AppProperties.getInstance().getWebBindAddress() != null) {
            server.setBindAddress(AppProperties.getInstance().getWebBindAddress());
        }

        //Deploy RestEasy with TJWS
        server.setDeployment(getResteasyDeployment());
        server.addFileMapping("/", new File(AppProperties.getInstance().getWebFileLocation()));

        //Enable Authentication
        server.setSecurityDomain(new BasicAthenticationSecurityDomain());
        server.getDeployment().setSecurityEnabled(true);

        server.setRootResourcePath("/mc");
        // Start TJWS server
        server.start();

        _logger.info("TJWS server started successfully, HTTPS Enabled?:{}, HTTP(S) Port: [{}]",
                AppProperties.getInstance().isWebHttpsEnabled(),
                AppProperties.getInstance().getWebHttpPort());

        if (AppProperties.getInstance().isMDNSserviceEnabled()) {
            McmDNSFactory.updateHttpService(true);
        }
    }

    private static void stopHTTPWebServer() {
        if (server != null) {
            server.stop();
            _logger.debug("Web server stopped.");
        } else {
            _logger.debug("Web server is not running.");
        }
    }

    private static boolean startServices() throws ClassNotFoundException, SQLException, URISyntaxException {
        //Start order..
        // - set to default locale
        // - Add Shutdown hook
        // - Start DB service
        // - Cleanup services
        // - Initialize MapDB store
        // - Set to locale actual
        // - Check password reset file
        // - Start message Monitor Thread
        // - Load starting values
        // - Start MQTT Broker
        // - Start gateway listener
        // - Start scheduler
        // - Start Web Server

        //Set to default locale
        McUtils.updateLocale(MC_LANGUAGE.EN_US);

        //Add Shutdown hook
        new AppShutdownHook().attachShutDownHook();

        //Start DB migration service
        DataBaseUtils.runDatabaseMigration();

        //cleanup services
        cleanUpServices();

        //Load Metric engine factory
        MetricsUtils.loadEngine();

        //Initialize MapDB store
        //MapDbFactory.init();
        OffHeapFactory.init();

        //Create or update static json file used for GUI before login
        SettingsUtils.updateStaticJsonInformationFile();

        //Set to locale actual
        McUtils.updateLocale(MC_LANGUAGE.fromString(AppProperties.getInstance().getControllerSettings()
                .getLanguage()));

        //List available script engines information
        McScriptEngineUtils.listAvailableEngines();

        //Check password reset file
        ResetPassword.executeResetPassword();

        //Start message Monitor Thread
        //Create new thread to monitor received logs
        //MessageMonitorThread messageMonitorThread = new MessageMonitorThread();
        //Thread thread = new Thread(messageMonitorThread);
        //thread.start();

        // - Load starting values
        loadStartingValues();

        // - Start MQTT Broker
        MoquetteMqttBroker.start();

        //Start all the gateways
        GatewayUtils.loadEngineAll();

        // - Start scheduler
        SchedulerUtils.startScheduler();

        // - Start Web Server
        startHTTPWebServer();

        return true;
    }

    private static void cleanUpServices() {
        // clean the services
        // - MQTT client location
        // - MQTT broker location

        try {
            File mqttClientDir = new File(AppProperties.getInstance().getMqttClientPersistentStoresLocation());
            if (mqttClientDir.exists()) {
                FileUtils.cleanDirectory(mqttClientDir);
                _logger.debug("MQTT Client persistent store cleared. [{}]", mqttClientDir.getCanonicalFile());
            }
            File mqttBrokerDir = new File(AppProperties.getInstance().getMqttBrokerPersistentStore()).getParentFile();
            if (mqttBrokerDir.exists()) {
                FileUtils.cleanDirectory(mqttBrokerDir);
                _logger.debug("MQTT broker persistent store cleared. [{}]", mqttBrokerDir.getCanonicalFile());
            }
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
        }
    }

    public static synchronized void stopServices() {
        //Stop order..
        // - stop web server
        // - clear external servers
        // - Stop scheduler
        // - Stop GatewayTable Listener
        // - Stop MQTT broker
        // - Stop message Monitor Thread
        // - Clear Raw Message Queue (Optional)
        // - Stop DB service
        // - Stop metric engine
        stopHTTPWebServer();
        ExternalServerFactory.clearDrivers();
        SchedulerUtils.stop();
        GatewayUtils.unloadEngineAll();
        MoquetteMqttBroker.stop();
        DataBaseUtils.stop();
        MetricsUtils.shutdownEngine();
        OffHeapFactory.close();
        McThreadPoolFactory.shutdownNow();
        _logger.debug("All services stopped.");
        //Remove references
        McObjectManager.clearAllReferences();
    }

    public static boolean loadInitialProperties(String propertiesFile) {
        try {
            Properties properties = new Properties();
            if (propertiesFile == null) {
                properties
                        .load(ClassLoader.getSystemClassLoader().getResourceAsStream("mycontroller.properties"));

            } else {
                FileReader fileReader = new FileReader(propertiesFile);
                properties.load(fileReader);
                fileReader.close();
            }
            AppProperties.getInstance().loadProperties(properties);
            _logger.debug("Properties are loaded successfuly...");
            return true;
        } catch (IOException ex) {
            _logger.error("Exception while loading properties file, ", ex);
            return false;
        }
    }
}
