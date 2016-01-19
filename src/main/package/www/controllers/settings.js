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
myControllerModule.controller('SettingsSystemController', function(alertService, $scope, $filter, SettingsFactory, displayRestError, about) {
  
  //about, Timezone, etc.,
  $scope.about = about;
  
  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};

  //settings location details, sunrise, sunset
  $scope.updateSettingsLocation = function(){
    $scope.locationSettings = SettingsFactory.getLocation();
  };
  
  //settings MyController
  $scope.updateSettingsController = function(){
    $scope.controllerSettings = SettingsFactory.getController();
  };
  
  //Pre-load
  $scope.locationSettings = {};
  $scope.controllerSettings = {};
  $scope.updateSettingsLocation();
  $scope.updateSettingsController();
   
   
   
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
    SettingsFactory.saveController($scope.controllerSettings,function(response) {
        alertService.success('Update success...');
        $scope.saveProgress.controller = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress.controller = false;
      });
  }; 

});

myControllerModule.controller('SettingsUnitsController', function(alertService, $scope, $filter, SettingsFactory, displayRestError, about) {
  
  //about, Timezone, etc.,
  $scope.about = about;
  
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

myControllerModule.controller('SettingsNotificationsController', function(alertService, $scope, $filter, SettingsFactory, displayRestError, about) {
  
  //about, Timezone, etc.,
  $scope.about = about;
  
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

myControllerModule.controller('SettingsSystemMySensors', function(alertService, $scope, $filter, SettingsFactory, displayRestError, about) {
  
  //about, Timezone, etc.,
  $scope.about = about;
  
  //editable settings
  $scope.editEnable = {};
  $scope.saveProgress = {};

 
  //settings MySensors
  $scope.updateSettingsMySensors = function(){
    $scope.mySensorsSettings = SettingsFactory.getMySensors();
  };
 
  
  //Pre-load
  $scope.mySensorsSettings = {};
  $scope.updateSettingsMySensors();

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
