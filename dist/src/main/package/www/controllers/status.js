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
myControllerModule.controller('StatusSystemController', function(alertService,
$scope, $filter, StatusFactory, $uibModal, $stateParams, displayRestError) {

  //GUI page settings
  $scope.headerStringList = "System status";

  //OS Status
  $scope.osStatus = StatusFactory.getOsStatus();
  //JVM Status
  $scope.jvmStatus = StatusFactory.getJvmStatus();
  //Script engines
  $scope.scriptEngines = StatusFactory.getScriptEngines();

  //Run Garbage Collection
  $scope.runGC = function(){
    StatusFactory.runGarbageCollection(function(response) {
      //Update display data
      $scope.jvmStatus = response;
    },function(error){
      displayRestError.display(error);
    });
  }

});

myControllerModule.controller('McAboutController', function(alertService,
$scope, $filter, StatusFactory, $uibModal, $stateParams, displayRestError) {
  //GUI page settings
  $scope.headerStringList = "About";

  //About MC
   $scope.mcAbout = StatusFactory.getMcAbout();
});

myControllerModule.controller('StatusMcLogController', function(alertService,
$scope, $filter, StatusFactory, $uibModal, $stateParams, displayRestError) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('MYCONTROLLER_SERVER_LOG');
  $scope.noItemsSystemMsg = $filter('translate')('NO_LOGS_AVAILABLE');
  $scope.noItemsSystemIcon = "fa fa-list";

  $scope.initialLog = {};
  $scope.logData = [];
  $scope.logLevel = null;

  //Refresh
  $scope.refreshLogs = function(){
    $scope.initialLog = StatusFactory.getMcServerLog(function(response){
      $scope.logData = response.data;
    });
  };

  //Get log level string
  /*
  $scope.getLogLevel = function(log){
    if(log.indexOf(' ERROR ') > -1 ){
      $scope.logLevel = "danger";
    }else if(log.indexOf(' INFO ') > -1 ){
      $scope.logLevel = "info";
    }else if(log.indexOf(' WARN ') > -1 ){
      $scope.logLevel = "warning";
    }else if(log.indexOf(' DEBUG ') > -1 ){
      $scope.logLevel = "default";
    }

    if(!$scope.logLevel){
      $scope.logLevel = "default";
    }
    return $scope.logLevel;
  }
  */
  //Pre load
  $scope.initialLog = StatusFactory.getMcServerLog(function(response){
    $scope.logData = response.data;
    /*
    if(response.data && response.data.length > 0){
      $scope.logData = response.data.split('\n');
    }
    */
  });


});

