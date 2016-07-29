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
myControllerModule.controller('ResourcesDataController', function(alertService,
$scope, ResourcesDataFactory, $stateParams, $state, $uibModal, displayRestError, CommonServices, mchelper, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('RESOURCES_DATA_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_RESOURCES_DATA_SETUP');
  $scope.noItemsSystemIcon = "fa fa-server";

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

  if($stateParams.gatewayId){
    $scope.query.gatewayId = $stateParams.gatewayId;
  }

  //get all ResourcesDatas
  $scope.getAllItems = function(){
    ResourcesDataFactory.getAll($scope.query, function(response) {
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
        id: 'resourceType',
        title:  $filter('translate')('RESOURCE_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_RESOURCE_TYPE'),
        filterType: 'select',
        filterValues: ['Gateway','Node','Sensor','Sensor variable'],
      },      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        placeholder: $filter('translate')('FILTER_BY_ENABLED'),
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
        id: 'resourceType',
        title:  $filter('translate')('RESOURCE_TYPE'),
        sortType: 'text'
      },
      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };



  //Delete items(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      ResourcesDataFactory.deleteIds($scope.itemIds, function(response) {
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

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("resourcesDataAddEdit",{'id':$scope.itemIds[0]});
    }
  };

  //Enable items
  $scope.enable = function () {
    if($scope.itemIds.length > 0){
      ResourcesDataFactory.enableIds($scope.itemIds, function(response) {
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
      ResourcesDataFactory.disableIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('ITEMS_DISABLED_SUCCESSFULLY'));
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }
  };

});


// ResourcesDatas other controllers
// Add/Edit Node
myControllerModule.controller('ResourcesDataControllerAddEdit', function ($scope, $stateParams, CommonServices, ResourcesDataFactory, TypesFactory, mchelper, alertService, displayRestError, $filter, $state) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.cs = CommonServices;
  $scope.item = {};
  if($stateParams.id){
    ResourcesDataFactory.get({"id":$stateParams.id}, function(response){
      $scope.item = response;
      $scope.resourcesList = $scope.getResources($scope.item.resourceType);
    },function(error){

    });

  }else{
    $scope.item.enabled = true;
  }

  $scope.externalServers = TypesFactory.getExternalServers();
  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "Resource data"});

  // Update resources list
  $scope.getResources = function(resourceType){
    if(resourceType === 'Sensor variable'){
      return TypesFactory.getSensorVariables();
    }else if(resourceType === 'Gateway'){
      return TypesFactory.getGateways();
    }else if(resourceType === 'Node'){
      return TypesFactory.getNodes();
    }else if(resourceType === 'Sensor'){
      return TypesFactory.getSensors();
    }else{
      return null;
    }
  }

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_RESOURCE');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_RESOURCE');
  $scope.cancelButtonState = "resourcesDataList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;


  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      ResourcesDataFactory.update($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("resourcesDataList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      ResourcesDataFactory.create($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("resourcesDataList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});
