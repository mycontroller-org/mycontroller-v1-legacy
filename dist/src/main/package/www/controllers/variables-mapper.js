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
myControllerModule.controller('VariablesMapperListController', function(alertService, $scope, $filter, displayRestError, TypesFactory, $filter, mchelper, CommonServices, $state) {


  //GUI page settings
  $scope.headerStringList = $filter('translate')('SENSORS_AND_VARIABLES_MAPPING');

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];

  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};


  //get all items
  $scope.getAllItems = function(){
    TypesFactory.getSensorVariableMapper(function(response) {
      $scope.orgList = response;
      $scope.filteredList = $scope.orgList;
      $scope.filterConfig.resultsCount = $scope.filteredList.length;
    },function(error){
      displayRestError.display(error);
    });
  }

  //Pre load
  $scope.getAllItems();
  $scope.itemName = null;

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.filterChangeLocal(filters, $scope);
    $scope.itemName = null;
  };

  $scope.filterConfig = {
    fields: [
      {
        id: 'displayName',
        title:  $filter('translate')('SENSOR_TYPE'),
        placeholder:  $filter('translate')('FILTER_BY_SENSOR_TYPE'),
        filterType: 'text'
      },{
        id: 'value',
        title:  $filter('translate')('SENSOR_VARIABLES'),
        placeholder:  $filter('translate')('FILTER_BY_SENSOR_VARIABLES'),
        filterType: 'array'
      },
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };

   //Select item
  $scope.selectItem = function (item) {
    if($scope.itemName === item.displayName){
      $scope.itemName = null;
    }else{
      $scope.itemName = item.displayName;
    }
  };

  //Edit item
  $scope.editItem = function () {
    $state.go("settingsVariablesMapperEdit", {'sensorType':$scope.itemName});
  };

});

//Edit mapping
myControllerModule.controller('VariablesMapperEditController', function ($scope, TypesFactory, $filter, $stateParams, mchelper, alertService) {

  //GUI page settings
  $scope.headerStringAdd = $filter('translate')('MODIFIY_SENSOR_VARIABLES_MAPPING');
  $scope.cancelButtonState = "settingsVariablesMapperList"; //Cancel button state
  $scope.saveProgress = false;


  $scope.item = {};
  $scope.sensorVariableTypes = {};
  $scope.item.displayName = $stateParams.sensorType;
  $scope.item.value = [];

  $scope.getSensorVariables = function(){
    TypesFactory.getSensorVariableMapperByType({"sensorType": $scope.item.displayName}, function(resource){
      $scope.sensorVariableTypes = resource;
      angular.forEach($scope.sensorVariableTypes, function(value, key) {
        if(value.ticked){
          $scope.item.value.push(value.displayName);
        }
      });
    });
  };


  //pre load
  $scope.getSensorVariables();

  $scope.save = function(){
    $scope.saveProgress = true;
    TypesFactory.updateSensorVariableMapper($scope.item,function(response) {
      $scope.saveProgress = false;
      alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
    },function(error){
      $scope.saveProgress = false;
      displayRestError.display(error);
    });
  }

});
