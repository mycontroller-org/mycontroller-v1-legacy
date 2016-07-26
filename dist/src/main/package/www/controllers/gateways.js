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
myControllerModule.controller('GatewaysController', function(alertService,
$scope, $filter, GatewaysFactory, $state, $uibModal, displayRestError, mchelper, CommonServices) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('GATEWAYS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_GATEWAYS_SETUP');
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
        title: $filter('translate')('NAME'),
        placeholder: $filter('translate')('FILTER_BY_NAME'),
        filterType: 'text',
      },
      {
        id: 'type',
        title:  $filter('translate')('TYPE'),
        placeholder: $filter('translate')('FILTER_BY_TYPE'),
        filterType: 'select',
        filterValues: ['Serial','Ethernet','MQTT'],
      },
      {
        id: 'networkType',
        title:  $filter('translate')('NETWORK_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_NETWORK_TYPE'),
        filterType: 'select',
        filterValues: ['MySensors'],
      },
      {
        id: 'statusMessage',
        title:  $filter('translate')('STATUS_MESSAGE'),
        placeholder: $filter('translate')('FILTER_BY_STATUS_MESSAGE'),
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
        title:  $filter('translate')('NAME'),
        sortType: 'text',
      },
      {
        id: 'state',
        title:  $filter('translate')('STATUS'),
        sortType: 'text',
      },
      {
        id: 'type',
        title:  $filter('translate')('TYPE'),
        sortType: 'text',
      },
      {
        id: 'networkType',
        title:  $filter('translate')('NETWORK_TYPE'),
        sortType: 'text',
      },
      {
        id: 'statusMessage',
        title:  $filter('translate')('STATUS_MESSAGE'),
        sortType: 'text',
      },
      {
        id: 'statusSince',
        title:  $filter('translate')('STATUS_SINCE'),
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
        alertService.success($filter('translate')('ITEMS_ENABLED_SUCCESSFULLY'));
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
        alertService.success($filter('translate')('ITEMS_DISABLED_SUCCESSFULLY'));
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
        alertService.success($filter('translate')('DISCOVER_INITIATED_SUCCESSFULLY'));
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }
  };

  //Update noe informations
  $scope.refreshNodesInfo = function () {
    if($scope.itemIds.length > 0){
      GatewaysFactory.executeNodeInfoUpdate($scope.itemIds, function(response) {
        alertService.success($filter('translate')('REFRESH_NODES_INFO_INITIATED_SUCCESSFULLY'));
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
        alertService.success($filter('translate')('RELOAD_INITIATED_SUCCESSFULLY'));
        $scope.itemIds = [];
        //Update display table
        $scope.getAllItems();
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
        alertService.success($filter('translate')('ITEMS_DELETED_SUCCESSFULLY'));
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


myControllerModule.controller('GatewaysControllerAddEdit', function ($scope, TypesFactory, GatewaysFactory, $stateParams, mchelper, $state, alertService, $filter, CommonServices, displayRestError) {
  $scope.gateway = {};
  $scope.gateway.enabled = true;
  $scope.gatewayTypes = {};
  $scope.trustHostTypes = TypesFactory.getTrustHostTypes();
  $scope.gatewayNetworkTypes = TypesFactory.getGatewayNetworkTypes();
  $scope.cs = CommonServices;

  if($stateParams.id){
    GatewaysFactory.get({"gatewayId":$stateParams.id}, function(response){
      $scope.gateway = response;
      $scope.updateGatewayTypes();
    },function(error){
        displayRestError.display(error);
    });
  }

  $scope.gatewaySerialDrivers = TypesFactory.getGatewaySerialDrivers();

  $scope.updateTypeChange = function (){
    if($scope.gateway.type === 'Serial'){
      $scope.gateway.driver='';
      $scope.gateway.portName='';
      $scope.gateway.baudRate='';
      $scope.gateway.retryFrequency='';
    }else if($scope.gateway.type === 'Ethernet'){
      $scope.gateway.host='';
      $scope.gateway.port='';
      $scope.gateway.aliveFrequency='';
    }else if($scope.gateway.type === 'MQTT'){
      $scope.gateway.brokerHost='';
      $scope.gateway.clientId='';
      $scope.gateway.topicsPublish='';
      $scope.gateway.topicsSubscribe='';
      $scope.gateway.username='';
      $scope.gateway.password='';
    }else if($scope.gateway.type === 'Sparkfun [phant.io]'){
      $scope.gateway.url='https://data.sparkfun.com';
      $scope.gateway.publicKey='';
      $scope.gateway.privateKey='';
      $scope.gateway.pollFrequency='1';
      $scope.gateway.recordsLimit='10';
      $scope.gateway.trustHostType='';
    }
  };

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_GATEWAY');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_GATEWAY');
  $scope.cancelButtonState = "gatewaysList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  // Update gateway types
  $scope.updateGatewayTypes = function(){
    $scope.gatewayTypes = TypesFactory.getGatewayTypes({"networkType": $scope.gateway.networkType});
  }

  $scope.save = function(){
      $scope.saveProgress = true;
    if($stateParams.id){
      GatewaysFactory.update($scope.gateway,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("gatewaysList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      GatewaysFactory.create($scope.gateway,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("gatewaysList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }

});


//item Detail
myControllerModule.controller('GatewaysControllerDetail', function ($scope, $stateParams, mchelper, GatewaysFactory, MetricsFactory, $filter) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.node = {};
  $scope.headerStringList = $filter('translate')('GATEWAY_DETAILS');

  $scope.item = GatewaysFactory.get({"gatewayId":$stateParams.id});
  $scope.resourceCount = MetricsFactory.getResourceCount({"resourceType":"Gateway", "resourceId":$stateParams.id});
});
