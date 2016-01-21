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
myControllerModule.controller('GatewaysController', function(alertService,
$scope, $filter, GatewaysFactory, $state, $uibModal, displayRestError, mchelper, CommonServices) {
    
  //GUI page settings
  $scope.headerStringList = "Gateways detail";
  $scope.noItemsSystemMsg = "No gateways set up.";
  $scope.noItemsSystemIcon = "fa fa-plug";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];
    
  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};
  
  //Get min number
  $scope.getMin = function(item1, item2){
    return CommonServices.getMin(item1, item2);
  };

  //get all Items
  $scope.getAllItems = function(){
    GatewaysFactory.getAll($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
    },function(error){
      displayRestError.display(error);
    });
  }

  //Hold all the selected item ids
  $scope.itemIds = [];

  $scope.selectAllItems = function(){
    CommonServices.selectAllItems($scope);
  };

  $scope.selectItem = function(item){
    CommonServices.selectItem($scope, item);
  };
  
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
        filterType: 'text',
      },
      {
        id: 'type',
        title:  'Type',
        placeholder: 'Filter by Type',
        filterType: 'select',
        filterValues: ['Serial','Ethernet','MQTT'],
      },
      {
        id: 'networkType',
        title:  'Network Type',
        placeholder: 'Filter by Network Type',
        filterType: 'select',
        filterValues: ['MySensors'],
      },
      {
        id: 'statusMessage',
        title:  'Status Message',
        placeholder: 'Filter by Status Message',
        filterType: 'text',
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
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
        sortType: 'text',
      },
      {
        id: 'state',
        title:  'Status',
        sortType: 'text',
      },
      {
        id: 'type',
        title:  'Type',
        sortType: 'text',
      },
      {
        id: 'networkType',
        title:  'Network type',
        sortType: 'text',
      },
      {
        id: 'statusMessage',
        title:  'Status message',
        sortType: 'text',
      },
      {
        id: 'statusSince',
        title:  'Status since',
        sortType: 'text',
      }
    ],
    onSortChange: sortChange
  };
  
  
  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("gatewaysAddEdit", {'id':$scope.itemIds[0]});
    }
  };
  
  //Enable items
  $scope.enable = function () {
    if($scope.itemIds.length > 0){
      GatewaysFactory.enable($scope.itemIds, function(response) {
        alertService.success('Enabled '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }
  };
  
  //Disable items
  $scope.disable = function () {
    if($scope.itemIds.length > 0){
      GatewaysFactory.disable($scope.itemIds, function(response) {
        alertService.success('Disabled '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }
  };
  
  //Discover items
  $scope.discover = function () {
    if($scope.itemIds.length > 0){
      GatewaysFactory.discover($scope.itemIds, function(response) {
        alertService.success('Discover started successfully for '+$scope.itemIds.length+' items(s).');
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }
  };
  
  //Reload items
  $scope.reload = function () {
    if($scope.itemIds.length > 0){
      GatewaysFactory.reload($scope.itemIds, function(response) {
        alertService.success('Reloaded '+$scope.itemIds.length+' items(s).');
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }
  };

  //Delete item(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      GatewaysFactory.delete($scope.itemIds, function(response) {
        alertService.success('Deleted '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
  
});


myControllerModule.controller('GatewaysControllerAddEdit', function ($scope, TypesFactory, GatewaysFactory, $stateParams, mchelper) {
  $scope.gateway = {};
  $scope.gateway.enabled = true;
  $scope.gatewayNetworkTypes = TypesFactory.getGatewayNetworkTypes();
  $scope.gatewayTypes = TypesFactory.getGatewayTypes();
  
  if($stateParams.id){
    $scope.gateway = GatewaysFactory.get({"gatewayId":$stateParams.id});
  }
  
  $scope.gatewaySerialDrivers = TypesFactory.getGatewaySerialDrivers();
  
  $scope.updateTypeChange = function (){
    $scope.gateway.variable1 = "";
    $scope.gateway.variable2 = "";
    $scope.gateway.variable3 = "";
    $scope.gateway.variable4 = "";
    $scope.gateway.variable5 = "";
    $scope.gateway.variable6 = "";
    $scope.gateway.variable7 = "";
    $scope.gateway.variable8 = "";
    $scope.gateway.variable9 = "";
    $scope.gateway.variable10 = "";
  };
  
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add gateway";
  $scope.headerStringUpdate = "Update gateway";
  $scope.cancelButtonState = "gatewaysList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  
  
  $scope.save = function(){
      $scope.saveProgress = true;
    if($stateParams.id){
      GatewaysFactory.update($scope.gateway,function(response) {
        alertService.success("Gateway Updated");
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      GatewaysFactory.create($scope.gateway,function(response) {
        alertService.success("Gateway Created");
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
  
});
