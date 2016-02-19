/*
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
myControllerModule.controller('SettingsSystemController', function(alertService, $scope, $filter, SettingsFactory,
  StatusFactory, TypesFactory, displayRestError, mchelper, $translate, $cookieStore, CommonServices) {
  
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
   
  //Save functions
  
  //Save location
  $scope.saveLocation = function(){
    $scope.saveProgress.location = true;
    SettingsFactory.saveLocation($scope.locationSettings,function(response) {
        alertService.success('Update success...');
        $scope.saveProgress.location = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.location = false;
      });
  };
  
  //Save controller
  $scope.saveController = function(){
    $scope.saveProgress.controller = true;
    $scope.controllerSettings.aliveCheckInterval = $scope.aliveCheckMinutes * 60000;
    $scope.controllerSettings.globalPageRefreshTime = $scope.globalPageRefreshTime * 1000;
    SettingsFactory.saveController($scope.controllerSettings,function(response) {
          StatusFactory.getConfig(function(response) {
            mchelper.cfg = response;//Update config
            //Update language
            $translate.use(mchelper.cfg.languageId);
            //Store all the configurations locally
            $cookieStore.put('mchelper', mchelper);
          });
        alertService.success('Update success...');
        $scope.saveProgress.controller = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.controller = false;
      });
  }; 

});

myControllerModule.controller('SettingsUnitsController', function(alertService, $scope, $filter, SettingsFactory, displayRestError, mchelper) {
  
  //config, language, user, etc.,
  $scope.mchelper = mchelper;
  
  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};
  
  //settings Units
  $scope.updateSettingsUnits = function(){
    $scope.unitsSettings = SettingsFactory.getUnits();
  };
  
  
  //Pre-load
  $scope.unitsSettings = {};
  $scope.updateSettingsUnits();

  //Save units
  $scope.saveUnits = function(){
    $scope.saveProgress.units = true;
    SettingsFactory.saveUnits($scope.unitsSettings,function(response) {
        alertService.success('Update success...');
        $scope.saveProgress.units = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.units = false;
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
  
  //settings Email
  $scope.updateSettingsEmail = function(){
    $scope.emailSettings = SettingsFactory.getEmail();
  };
  
  //settings SMS
  $scope.updateSettingsSms = function(){
    $scope.smsSettings = SettingsFactory.getSms();
  };
  
  
  
  
  //Pre-load
  $scope.emailSettings = {};
  $scope.smsSettings = {};
  $scope.updateSettingsEmail();
  $scope.updateSettingsSms();
   
  //Save email
  $scope.saveEmail = function(){
    $scope.saveProgress.email = true;
    SettingsFactory.saveEmail($scope.emailSettings,function(response) {
        alertService.success('Update success...');
        $scope.saveProgress.email = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.email = false;
      });
  }; 
   
   
  //Save sms
  $scope.saveSms = function(){
    $scope.saveProgress.sms = true;
    SettingsFactory.saveSms($scope.smsSettings,function(response) {
        alertService.success('Update success...');
        $scope.saveProgress.sms = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.sms = false;
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
        alertService.success('Update success...');
        $scope.saveProgress.mySensors = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.mySensors = false;
      });
  };

});

myControllerModule.controller('SettingsMetricsController', function(alertService, $scope, $filter, SettingsFactory, displayRestError, mchelper) {
  
  //config, language, user, etc.,
  $scope.mchelper = mchelper;
  
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
  
  
  //Pre-load
  $scope.metricsSettings = {};
  $scope.updateSettingsMetrics();

  //Save units
  $scope.saveMetrics = function(){
    $scope.saveProgress.metrics = true;
    SettingsFactory.saveMetrics($scope.metricsSettings,function(response) {
        alertService.success('Update success...');
        $scope.saveProgress.metrics = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.metrics = false;
      });
  }; 

});
