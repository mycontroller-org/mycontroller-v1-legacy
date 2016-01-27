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
myControllerModule.controller('SensorsActionControllerList', function(
  alertService, $scope, SensorsFactory, TypesFactory, NodesFactory, $uibModal, displayRestError, mchelper, CommonServices, pfViewUtils, $filter) {
  
  //GUI page settings
  //$scope.headerStringList = "Sesnors detail";
  $scope.noItemsSystemMsg = "No sensors set up.";
  $scope.noItemsSystemIcon = "fa fa-eye";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];
  $scope.cs = CommonServices;
    
  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};
  
  //Get min number
  $scope.getMin = function(item1, item2){
    return CommonServices.getMin(item1, item2);
  };
  
  //get all Sensors
  $scope.getAllItems = function(){
    SensorsFactory.getAll($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
    },function(error){
      displayRestError.display(error);
    });
  }

  //On page change
  $scope.pageChanged = function(newPage){
    CommonServices.updatePageChange($scope, newPage);
  };

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.updateFiltersChange($scope, filters);
  };
  
  $scope.filterConfig = {
    fields: [
      {
        id: 'name',
        title:  'Name',
        placeholder: 'Filter by Name',
        filterType: 'text'
      },
      {
        id: 'node.gateway.name',
        title:  'Gateway',
        placeholder: 'Filter by Gateway',
        filterType: 'text'
      },
      {
        id: 'node.eui',
        title:  'Node EUI',
        placeholder: 'Filter by Type',
        filterType: 'text',
      },
      {
        id: 'sensorId',
        title:  'Id',
        placeholder: 'Filter by Id',
        filterType: 'integer',
      },
      {
        id: 'type',
        title:  'Type',
        placeholder: 'Filter by Type',
        filterType: 'text',
      },
      {
        id: 'variableTypes',
        title:  'Variable Types',
        placeholder: 'Filter by Variable Types',
        filterType: 'text',
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };
  
  
  //View selection
  var viewSelected = function(viewId) {
    $scope.viewType = viewId
  };
  
  //View configuration
  $scope.viewsConfig = {
      views: [pfViewUtils.getListView(), pfViewUtils.getTilesView()],
      onViewSelect: viewSelected,
    };
  
  //Sort columns
  var sortChange = function (sortId, isAscending) {
    //Reset sort type and update items
    CommonServices.updateSortChange($scope, sortId, isAscending);
  };

 
  $scope.sortConfig = {
    fields: [
      {
        id: 'name',
        title:  'Name',
        sortType: 'text'
      },
      {
        id: 'nodeId',
        title:  'Node Id',
        sortType: 'text'
      },
      {
        id: 'sensorId',
        title:  'Sensor Id',
        sortType: 'number'
      },
      {
        id: 'type',
        title:  'Type',
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };
  
  
  // Item tool bar config
  $scope.sensorsToolbarConfig = {
      viewsConfig: $scope.viewsConfig,
      filterConfig: $scope.filterConfig,
      sortConfig: $scope.sortConfig,
    };
  
  
  //refresh sensor
  $scope.refreshSensor = function(sensor){
    SensorsFactory.get({"id":sensor.id}, function(response) {
      var newSensor = response;
      sensor.lastSeen = newSensor.lastSeen;
      sensor.variables = newSensor.variables;
    },function(error){
      displayRestError.display(error);
    });
  };
  
  
  //Update Variable / Send Payload
  $scope.updateVariable = function(variable){
    SensorsFactory.updateVariable(variable, function(){
      //update Success
    },function(error){
      displayRestError.display(error);
    });
  };
  
  //HVAC heater options - HVAC flow state
  $scope.hvacOptionsFlowState = TypesFactory.getHvacOptionsFlowState();  
  //HVAC heater options - HVAC flow mode
  $scope.hvacOptionsFlowMode = TypesFactory.getHvacOptionsFlowMode();  
  //HVAC heater options - HVAC fan speed
  $scope.hvacOptionsFanSpeed = TypesFactory.getHvacOptionsFanSpeed();  
  
  //Defined variable types list
  $scope.definedVariableTypes = CommonServices.getSensorVariablesKnownList();
  

  
  //update rgba color
  $scope.updateRgba = function(variable){
    variable.value = CommonServices.rgba2hex(variable.rgba);
    $scope.updateVariable(variable);
  };
  
  
  //Pre load
  $scope.viewsConfig.currentView = $scope.viewsConfig.views[0].id;
  $scope.viewType = $scope.viewsConfig.currentView;
  //Update list table
  //getAllItems();

});
