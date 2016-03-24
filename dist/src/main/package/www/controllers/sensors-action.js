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
myControllerModule.controller('SensorsActionControllerList', function(
  alertService, $scope, SensorsFactory, TypesFactory, NodesFactory, SettingsFactory, $uibModal, displayRestError, mchelper, CommonServices, pfViewUtils, $filter, $window, $interval) {

  //GUI page settings
  //$scope.headerStringList = "Sesnors detail";
  $scope.noItemsSystemMsg = $filter('translate')('NO_SENSORS_SETUP');
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

  //Stop if an request sent already
  var updateInprogress = false;
  //get all items
  $scope.getAllItems = function(hideLoading){
    if(updateInprogress){
      return;
    }
    updateInprogress = true;
    if(!hideLoading){
      $scope.dataLoading = true;
    }
    SensorsFactory.getAll($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
      $scope.dataLoading = false;
      updateInprogress = false;
    },function(error){
      displayRestError.display(error);
      $scope.dataLoading = false;
      updateInprogress = false;
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
        title:  $filter('translate')('SENSOR_NAME'),
        placeholder: $filter('translate')('FILTER_BY_SENSOR_NAME'),
        filterType: 'text'
      },
      {
        id: 'nodeId',
        title:  $filter('translate')('NODE_ID'),
        placeholder: $filter('translate')('FILTER_BY_NODE_ID'),
        filterType: 'text'
      },
      {
        id: 'sensorId',
        title:  $filter('translate')('SENSOR_ID'),
        placeholder: $filter('translate')('FILTER_BY_SENSOR_ID'),
        filterType: 'text'
      },
      {
        id: 'type',
        title:  $filter('translate')('TYPE'),
        placeholder: $filter('translate')('FILTER_BY_TYPE'),
        filterType: 'text',
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };


  //View selection
  var viewSelected = function(viewId) {
    mchelper.userSettings.actionBoardView = viewId;
    SettingsFactory.saveUserSettings(mchelper.userSettings);
    CommonServices.saveMchelper(mchelper);
    if(viewId === 'cardView'){
      $scope.query.pageLimit = 12;
      $scope.tooltipPlacement = 'top';
    }else if(viewId === 'listView'){
      $scope.query.pageLimit = 10;
      $scope.tooltipPlacement = 'left';
    }
    $scope.currentPage = 1;
    $scope.query.page=1;
    $scope.getAllItems();
    $scope.viewType = viewId;

  };

  //View configuration
  $scope.viewsConfig = {
      views: [pfViewUtils.getListView(), pfViewUtils.getCardView()],
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
        id: 'lastSeen',
        title:  $filter('translate')('LAST_SEEN'),
        sortType: 'text'
      },{
        id: 'name',
        title:  $filter('translate')('SENSOR_NAME'),
        sortType: 'text'
      },
      {
        id: 'nodeId',
        title:  $filter('translate')('NODE_ID'),
        sortType: 'text'
      },
      {
        id: 'sensorId',
        title:  $filter('translate')('SENSOR_ID'),
        sortType: 'number'
      },
      {
        id: 'type',
        title:  $filter('translate')('TYPE'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange,
    isAscending: false,
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
  $scope.viewsConfig.currentView = mchelper.userSettings.actionBoardView;
  $scope.tooltipPlacement = 'left';
  $scope.tooltipEnabled = true;
  $scope.viewType = $scope.viewsConfig.currentView;
  //Update list table
  //getAllItems();


  //fix for layout tiles
  var isInnterWidth = function(minWidth, maxWidth, value){
    return (minWidth <= value) && (maxWidth >= value);
  };
  $scope.$watch(function(){
       return $window.innerWidth;
    }, function(value) {
      $scope.colLg3 = isInnterWidth(1200,1600, value);
  });

  function updatePage(){
    $scope.getAllItems(true);
  };

  // global page refresh
  var promise = $interval(updatePage, mchelper.cfg.globalPageRefreshTime);

  // cancel interval on scope destroy
  $scope.$on('$destroy', function(){
    $interval.cancel(promise);
  });

});
