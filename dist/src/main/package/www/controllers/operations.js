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
myControllerModule.controller('OperationsController', function(alertService,
$scope, OperationsFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('OPERATIONS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_OPERATIONS_SETUP');
  $scope.noItemsSystemIcon = "fa fa-tasks";

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


  if($stateParams.resourceType){
    $scope.query.resourceType = $stateParams.resourceType;
    $scope.query.resourceId = $stateParams.resourceId;
  }

  //get all Sensors
  $scope.getAllItems = function(){
    OperationsFactory.getAll($scope.query, function(response) {
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
        title:  $filter('translate')('NAME'),
        placeholder: $filter('translate')('FILTER_BY_NAME'),
        filterType: 'text'
      },
      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        placeholder: $filter('translate')('FILTER_BY_ENABLED'),
        filterType: 'select',
        filterValues: ['True','False'],
      },
      {
        id: 'type',
        title:  $filter('translate')('OPERATION_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_OPERATION_TYPE'),
        filterType: 'select',
        filterValues: ['Send payload','Send SMS','Send email','Send pushbullet note','Execute script'],
      },
      {
        id: 'publicAccess',
        title: $filter('translate')('PUBLIC_ACCESS'),
        placeholder: $filter('translate')('FILTER_BY_PUBLIC_ACCESS'),
        filterType: 'select',
        filterValues: ['True','False'],
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
        sortType: 'text'
      },
      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        sortType: 'text'
      },
      {
        id: 'type',
        title:  $filter('translate')('OPERATION_TYPE'),
        sortType: 'text'
      },
      {
        id: 'publicAccess',
        title:  $filter('translate')('PUBLIC_ACCESS'),
        sortType: 'text'
      },
      {
        id: 'lastExecution',
        title: $filter('translate')('LAST_EXECUTION'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
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
      OperationsFactory.deleteIds($scope.itemIds, function(response) {
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


  //Enable items
  $scope.enable = function () {
    if($scope.itemIds.length > 0){
      OperationsFactory.enableIds($scope.itemIds, function(response) {
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
      OperationsFactory.disableIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('ITEMS_DISABLED_SUCCESSFULLY'));
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }
  };

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("operationsAddEdit",{'id':$scope.itemIds[0]});
    }
  };

  //Clone item
  $scope.clone = function () {
    if($scope.itemIds.length == 1){
      $state.go("operationsAddEdit",{'id':$scope.itemIds[0], 'action': 'clone'});
    }
  };

});


//Add Edit notification controller
myControllerModule.controller('OperationsControllerAddEdit', function ($scope, $stateParams, $state, GatewaysFactory,
  NodesFactory, SensorsFactory, TypesFactory, OperationsFactory, ScriptsFactory, TemplatesFactory, mchelper, alertService, displayRestError, $filter, CommonServices) {
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.item.enabled = true;
  $scope.item.publicAccess = false;
  $scope.cs = CommonServices;

  // Update resources list
  $scope.getResources= function(resourceType){
    if(resourceType === 'Sensor variable'){
      return TypesFactory.getSensorVariables();
    }else if(resourceType === 'Gateway' || resourceType === 'Gateway state'){
      return TypesFactory.getGateways();
    }else if(resourceType === 'Node' || resourceType === 'Node state'){
      return TypesFactory.getNodes();
    }else if(resourceType === 'Resources group'){
      return TypesFactory.getResourcesGroups();
    }else if(resourceType === 'Rule definition'){
      return TypesFactory.getRuleDefinitions();
    }else if(resourceType === 'Timer'){
      return TypesFactory.getTimers();
    }else if(resourceType === 'Value'){
      $scope.updateThresholdValueTypes($scope.item.resourceType);
      return null;
    }else{
      return null;
    }
  }

  //Update Payload operations
  $scope.updatePayloadOperations= function(resourceType){
    $scope.payloadOperations = TypesFactory.getPayloadOperations({"resourceType":resourceType});
  }


  if($stateParams.id){
    OperationsFactory.get({"id":$stateParams.id},function(response) {
        $scope.item = response;
        //Update Operation Type
        if($scope.item.type === 'Send payload'){
          $scope.plResourcesList = $scope.getResources($scope.item.resourceType);
          $scope.plResourceId = parseInt($scope.item.resourceId);
          //Update payload operations
          if($scope.item.resourceType !== 'Sensor variable'){
            $scope.updatePayloadOperations($scope.item.resourceType);
          }
        }else if($scope.item.type === 'Execute script'){
          $scope.item.scriptBindings = angular.toJson(response.scriptBindings);
        }

        //Update delay time
        if($scope.item.delayTime){
          $scope.item.dt = $scope.item.delayTime/1000;
        }

        if($stateParams.action === 'clone'){
          $stateParams.id = undefined;
          $scope.item.id = undefined;
          $scope.item.name = $scope.item.name + '-' + $filter('translate')('CLONE');
        }

      },function(error){
        displayRestError.display(error);
      });
  }else{
    $scope.item.scriptBindings='{ }';
  }

  //--------------pre load -----------
  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "Rule definition"});
  $scope.spResourceTypes = TypesFactory.getResourceTypes({"operationType": "Send payload"});
  $scope.reqPlResourceTypes = TypesFactory.getResourceTypes({"operationType": "Request payload"});
  $scope.operationTypes = TypesFactory.getOperationTypes();
  $scope.templatesList = TemplatesFactory.getAllLessInfo();
  $scope.scriptsList = ScriptsFactory.getAllLessInfo({"type":"Operation"});

  //GUI page settings
  if(!$stateParams.action || $stateParams.action !== 'clone'){
      $scope.showHeaderUpdate = $stateParams.id;
  }
  $scope.headerStringAdd = $filter('translate')('ADD_OPERATION');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_OPERATION');
  $scope.cancelButtonState = "operationsList"; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.save = function(){
    //Update delay time
    if($scope.item.dt){
      $scope.item.delayTime = $scope.item.dt*1000;
    }
    //Change string to JSON string
    if($scope.item.type === 'Execute script'){
      $scope.item.scriptBindings = angular.fromJson(JSON.stringify(eval('('+$scope.item.scriptBindings+')')));
    }
    $scope.saveProgress = true;
    if($stateParams.id){
      OperationsFactory.update($scope.item, function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("operationsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }else{
      OperationsFactory.create($scope.item, function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("operationsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }
  }
});
