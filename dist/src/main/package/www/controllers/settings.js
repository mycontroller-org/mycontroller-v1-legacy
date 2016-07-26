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
myControllerModule.controller('SettingsSystemController', function(alertService, $scope, $filter, SettingsFactory,
  StatusFactory, TypesFactory, displayRestError, mchelper, $translate, $cookieStore, CommonServices, NavigatorGeolocation) {

  //config, language, user, etc.,
  $scope.mchelper = mchelper;
  $scope.cs = CommonServices;

  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};

  //settings location details, sunrise, sunset
  $scope.updateSettingsLocation = function(){
    $scope.locationSettings = SettingsFactory.getLocation();
  };

  //settings MyController
  $scope.updateSettingsController = function(){
    SettingsFactory.getController(function(resource){
      $scope.controllerSettings = resource;
      $scope.aliveCheckMinutes = $scope.controllerSettings.aliveCheckInterval / 60000;
      $scope.globalPageRefreshTime = $scope.controllerSettings.globalPageRefreshTime / 1000;
      $scope.executeDiscoverMinutes = $scope.controllerSettings.executeDiscoverInterval / 60000;
    });
  };

  //Pre-load
  $scope.locationSettings = {};
  $scope.controllerSettings = {};
  //get log levels
  $scope.logLevels = TypesFactory.getResourceLogsLogLevels();
  //get languages
  $scope.languages = TypesFactory.getLanguages();
  $scope.updateSettingsLocation();
  $scope.updateSettingsController();
  $scope.aliveCheckMinutes = null;
  $scope.globalPageRefreshTime = null;

  //Get current location
  $scope.updateGeoLocation = function(){
    NavigatorGeolocation.getCurrentPosition()
    .then(function(position) {
      $scope.locationSettings.latitude = $filter('number')(position.coords.latitude, 4);
      $scope.locationSettings.longitude = $filter('number')(position.coords.longitude, 4);
    });
  };

  //Save location
  $scope.saveLocation = function(){
    $scope.saveProgress.location = true;
    SettingsFactory.saveLocation($scope.locationSettings,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.location = false;
        $scope.updateSettingsLocation();
        $scope.editEnable.location = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.location = false;
      });
  };

  //Save controller
  $scope.saveController = function(){
    $scope.saveProgress.controller = true;
    $scope.controllerSettings.aliveCheckInterval = $scope.aliveCheckMinutes * 60000;
    $scope.controllerSettings.executeDiscoverInterval = $scope.executeDiscoverMinutes * 60000;
    $scope.controllerSettings.globalPageRefreshTime = $scope.globalPageRefreshTime * 1000;
    SettingsFactory.saveController($scope.controllerSettings,function(response) {
          StatusFactory.getConfig(function(response) {
            mchelper.cfg = response;//Update config
            //Update language
            $translate.use(mchelper.cfg.languageId);
            //Store all the configurations locally
            $cookieStore.put('mchelper', mchelper);
          });
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.controller = false;
        $scope.updateSettingsController();
        $scope.editEnable.controller = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.controller = false;
      });
  };

});

myControllerModule.controller('SettingsNotificationsController', function(alertService, $scope, $filter, SettingsFactory, displayRestError, mchelper, CommonServices) {

  //config, language, user, etc.,
  $scope.mchelper = mchelper;
  $scope.cs = CommonServices;

  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};
  $scope.testing = {};

  //settings Email
  $scope.updateSettingsEmail = function(){
    $scope.emailSettings = SettingsFactory.getEmail();
  };

  //settings SMS
  $scope.updateSettingsSms = function(){
    $scope.smsSettings = SettingsFactory.getSms();
  };

  //settings Pushbullet
  $scope.updateSettingsPushbullet = function(){
    $scope.pushbulletSettings = SettingsFactory.getPushbullet();
  };




  //Pre-load
  $scope.emailSettings = {};
  $scope.smsSettings = {};
  $scope.updateSettingsEmail();
  $scope.updateSettingsSms();
  $scope.updateSettingsPushbullet();

  //Save email
  $scope.saveEmail = function(){
    $scope.saveProgress.email = true;
    SettingsFactory.saveEmail($scope.emailSettings,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.email = false;
        $scope.updateSettingsEmail();
        $scope.editEnable.email = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.email = false;
      });
  };

  //Send test email
  $scope.sendTestEmail = function(){
    $scope.testing.email = true;
    SettingsFactory.saveEmail({'testOnly': true}, $scope.emailSettings, function(response) {
        alertService.success(response.message);
        $scope.testing.email = false;
      },function(error){
        displayRestError.display(error);
        $scope.testing.email = false;
      });
  };

  //Save sms
  $scope.saveSms = function(){
    $scope.saveProgress.sms = true;
    SettingsFactory.saveSms($scope.smsSettings,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.sms = false;
        $scope.updateSettingsSms();
        $scope.editEnable.sms = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.sms = false;
      });
  };

  //Save pushbullet
  $scope.savePushbullet = function(){
    $scope.saveProgress.pushbullet = true;
    SettingsFactory.savePushbullet($scope.pushbulletSettings,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.pushbullet = false;
        $scope.updateSettingsPushbullet();
        $scope.editEnable.pushbullet = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.pushbullet = false;
      });
  };

});

myControllerModule.controller('SettingsSystemMySensors', function(alertService, $scope, $filter, SettingsFactory, TypesFactory, FirmwaresFactory, displayRestError, mchelper) {

  //config, language, user, etc.,
  $scope.mchelper = mchelper;

  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};


  //settings MySensors
  $scope.updateSettingsMySensors = function(){
    SettingsFactory.getMySensors(function(response){
      $scope.mySensorsSettings = response;
      if(response.defaultFirmware){
        FirmwaresFactory.getFirmware({"refId": response.defaultFirmware},function(response){
          $scope.defaultFirmware = response.firmwareName;
        });
      }
    });
  };

  //Pre-load
  $scope.mySensorsSettings = {};
  //Get firmwares list
  $scope.firmwares = TypesFactory.getFirmwares();
  $scope.updateSettingsMySensors();
  $scope.defaultFirmware = null;

  //Save mySensors
  $scope.saveMySensors = function(){
    $scope.saveProgress.mySensors = true;
    SettingsFactory.saveMySensors($scope.mySensorsSettings,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.mySensors = false;
        $scope.updateSettingsMySensors();
        $scope.editEnable.mySensors = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.mySensors = false;
      });
  };

});

myControllerModule.controller('SettingsMetricsController', function(alertService, $scope, $filter, SettingsFactory, displayRestError, mchelper, CommonServices,  $uibModal) {

  //config, language, user, etc.,
  $scope.mchelper = mchelper;
  $scope.cs = CommonServices;

  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};

  //settings Units
  $scope.updateSettingsMetrics = function(){
    SettingsFactory.getMetrics(function(response){
      $scope.metricsSettings = response;
      $scope.metricsSettings.defaultTimeRange = $scope.metricsSettings.defaultTimeRange.toString();
    });
  };

  //Update Retention settings
  $scope.updateSettingsMetricsRetention = function(){
    SettingsFactory.getMetricsRetention(function(response) {
        $scope.metricsRetention = response;
        $scope.rRawData = CommonServices.getTimestampJson(response.retentionRawData);
        $scope.rOneMinute = CommonServices.getTimestampJson(response.retentionOneMinute);
        $scope.rFiveMinutes = CommonServices.getTimestampJson(response.retentionFiveMinutes);
        $scope.rOneHour = CommonServices.getTimestampJson(response.retentionOneHour);
        $scope.rSixHours = CommonServices.getTimestampJson(response.retentionSixHours);
        $scope.rTwelveHours = CommonServices.getTimestampJson(response.retentionTwelveHours);
        $scope.rOneDay = CommonServices.getTimestampJson(response.retentionOneDay);
      },function(error){
        displayRestError.display(error);
      });
  }


  //Pre-load
  $scope.metricsSettings = {};
  $scope.updateSettingsMetricsRetention();
  $scope.updateSettingsMetrics();

  //Save garphs settings
  $scope.saveMetrics = function(){
    $scope.saveProgress.metrics = true;
    SettingsFactory.saveMetrics($scope.metricsSettings,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.metrics = false;
        $scope.updateSettingsMetrics();
        $scope.editEnable.metrics = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.metrics = false;
      });
  };

  //Save retention settings
  $scope.saveMetricsRetention = function(){
    $scope.saveProgress.metricsRetention = true;
    //Update on timestamp format
    $scope.metricsRetention.retentionRawData = CommonServices.getTimestamp($scope.rRawData);
    $scope.metricsRetention.retentionOneMinute = CommonServices.getTimestamp($scope.rOneMinute);
    $scope.metricsRetention.retentionFiveMinutes = CommonServices.getTimestamp($scope.rFiveMinutes);
    $scope.metricsRetention.retentionOneHour = CommonServices.getTimestamp($scope.rOneHour);
    $scope.metricsRetention.retentionSixHours = CommonServices.getTimestamp($scope.rSixHours);
    $scope.metricsRetention.retentionTwelveHours = CommonServices.getTimestamp($scope.rTwelveHours);
    $scope.metricsRetention.retentionOneDay = CommonServices.getTimestamp($scope.rOneDay);

    SettingsFactory.saveMetricsRetention($scope.metricsRetention,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.metricsRetention = false;
        $scope.updateSettingsMetricsRetention();
        $scope.editEnable.metricsDataRetention = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.metricsRetention = false;
      });
  };

  //edit confirmation
  $scope.retentionWarning = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/edit-confirmation-modal.html',
    controller: 'MetricsRetentionWarnController',
    size: size,
    resolve: {}
    });
    addModalInstance.result.then(function () {
      $scope.editEnable.metricsDataRetention = true;
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

});

//retention change Modal
myControllerModule.controller('MetricsRetentionWarnController', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('RETENTION_DIALOG_TITLE');
  $scope.message = $filter('translate')('RETENTION_DIALOG_CONFIRMATION_MSG');
  $scope.continute = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});


myControllerModule.controller('SettingsMqttBrokerController', function(alertService, $scope, $filter, SettingsFactory, CommonServices, displayRestError, $uibModal) {

  //config, language, user, etc.,
  $scope.cs = CommonServices;

  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};


  //settings mqtt broker
  $scope.updateSettingsMqttBroker = function(){
    SettingsFactory.getMqttBroker(function(response){
      $scope.mqttBrokerSettings = response;
      if(response.defaultFirmware){
        FirmwaresFactory.getFirmware({"refId": response.defaultFirmware},function(response){
          $scope.defaultFirmware = response.firmwareName;
        });
      }
    });
  };

  //Pre-load
  $scope.mqttBrokerSettings = {};
  $scope.updateSettingsMqttBroker();

  //Save settings
  $scope.saveMqttBroker = function(){
    $scope.saveProgress.mqttBroker = true;
    SettingsFactory.saveMqttBroker($scope.mqttBrokerSettings,function(response) {
        alertService.success($filter('translate')('UPDATED_SUCCESSFULLY'));
        $scope.saveProgress.mqttBroker = false;
        $scope.updateSettingsMqttBroker();
        $scope.editEnable.mqttBroker = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.mqttBroker = false;
      });
  };


  //edit confirmation
  $scope.saveSettings = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/edit-confirmation-modal.html',
    controller: 'MqttBrokerChangeController',
    size: size,
    resolve: {}
    });
    addModalInstance.result.then(function () {
      $scope.saveMqttBroker();
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

});

//Mqtt broker change warning
myControllerModule.controller('MqttBrokerChangeController', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('MQTT_BROKER_EDIT_DIALOG');
  $scope.message = $filter('translate')('MQTT_BROKER_EDIT_CONFIRMATION_MSG');
  $scope.continute = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});
