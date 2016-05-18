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
myControllerModule.controller('ResourcesGroupController', function(alertService,
$scope, ResourcesGroupFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('RESOURCE_GROUPS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_RESOURCE_GROUPS_SETUP');
  $scope.noItemsSystemIcon = "pficon pficon-replicator";

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

  //get all Sensors
  $scope.getAllItems = function(){
    ResourcesGroupFactory.getAll($scope.query, function(response) {
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
        filterType: 'text'
      },
      {
        id: 'description',
        title: $filter('translate')('DESCRIPTION'),
        placeholder: $filter('translate')('FILTER_BY_DESCRIPTION'),
        filterType: 'integer',
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
      },{
        id: 'description',
        title:  $filter('translate')('DESCRIPTION'),
        sortType: 'text'
      },{
        id: 'state',
        title:  $filter('translate')('STATUS'),
        sortType: 'text'
      },{
        id: 'stateSince',
        title:  $filter('translate')('STATUS_SINCE'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };



  //On,Off switch control
  $scope.changeMystate = function(item, state){
    var itemArray = [item.id];
    if(state){
      ResourcesGroupFactory.turnOnIds(itemArray, function(response) {
        alertService.success($filter('translate')('RESOURCE_GROUP_TURNED_ON'));
        //Update display table
        //$scope.getAllItems();
      },function(error){
        displayRestError.display(error);
      });
    }else{
      ResourcesGroupFactory.turnOffIds(itemArray, function(response) {
        alertService.success($filter('translate')('RESOURCE_GROUP_TURNED_OFF'));
        //Update display table
        //$scope.getAllItems();
      },function(error){
        displayRestError.display(error);
      });
    }
  }

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("resourcesGroupAddEdit", {'id':$scope.itemIds[0]});
    }
  };

  //Turm ON items
  $scope.turnOn = function () {
    if($scope.itemIds.length > 0){
      ResourcesGroupFactory.turnOnIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('RESOURCE_GROUPS_TURNED_ON'));
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }
  };

  //Turm OFF items
  $scope.turnOff = function () {
    if($scope.itemIds.length > 0){
      ResourcesGroupFactory.turnOffIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('RESOURCE_GROUPS_TURNED_OFF'));
        //Update display table
        $scope.getAllItems();
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
      ResourcesGroupFactory.deleteIds($scope.itemIds, function(response) {
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


//Add Edit item controller
myControllerModule.controller('ResourcesGroupControllerAddEdit', function ($scope, $stateParams, $state, ResourcesGroupFactory,  mchelper, alertService, displayRestError, $filter) {
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_RESOURCES_GROUP');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_RESOURCES_GROUP');
  $scope.cancelButtonState = "resourcesGroupList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.mchelper = mchelper;
  $scope.group = {};
  $scope.id = $stateParams.id;


  if($stateParams.id){
    ResourcesGroupFactory.get({'_id':$stateParams.id},function(response) {
        $scope.group = response;
      },function(error){
        displayRestError.display(error);
      });
  }

  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      ResourcesGroupFactory.update($scope.group,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("resourcesGroupList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      ResourcesGroupFactory.create($scope.group,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("resourcesGroupList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});


//ResourcesGroup Map
//------------------------------------------------------------------------------

myControllerModule.controller('ResourcesGroupMapController', function(alertService,
$scope, ResourcesGroupFactory, ResourcesGroupMapFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $stateParams, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('RESOURCE_GROUPS_MAPS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_RESOURCE_GROUPS_MAP_SETUP');
  $scope.noItemsSystemIcon = "pficon pficon-replicator";

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

  //redirect to groups list if id not found
  if(!$stateParams){
    $state.go("resourcesGroupList");
  }

  //always lock with group id
  $scope.query.groupId = $stateParams.id;

  $scope.resourcesGroup = ResourcesGroupFactory.get({"id":$stateParams.id});

  //get all items
  $scope.getAllItems = function(){
    ResourcesGroupMapFactory.getAll($scope.query, function(response) {
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
        filterType: 'text'
      },
      {
        id: 'payloadOn',
        title:  $filter('translate')('PAYLOAD_ON'),
        placeholder: $filter('translate')('FILTER_BY_PAYLOAD_ON'),
        filterType: 'integer',
      },
      {
        id: 'payloadOff',
        title:  $filter('translate')('PAYLOAD_OFF'),
        placeholder: $filter('translate')('FILTER_BY_PAYLOAD_OFF'),
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
        id: 'resourceType',
        title:  $filter('translate')('RESOURCE_TYPE'),
        sortType: 'text'
      },{
        id: 'payloadOn',
        title:  $filter('translate')('PAYLOAD_ON'),
        sortType: 'text'
      },
      {
        id: 'payloadOff',
        title:  $filter('translate')('PAYLOAD_OFF'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("resourcesGroupMapAddEdit", {'groupId':$scope.query.groupId, 'id':$scope.itemIds[0]});
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
      ResourcesGroupMapFactory.deleteIds($scope.itemIds, function(response) {
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


//Add Edit item controller
myControllerModule.controller('ResourcesGroupMapControllerAddEdit', function ($scope, $stateParams, $state, TypesFactory, CommonServices, ResourcesGroupMapFactory, mchelper, alertService, displayRestError, $filter) {
  $scope.mchelper = mchelper;
  $scope.groupMap = {};

  if($stateParams.id){
    ResourcesGroupMapFactory.get({"id":$stateParams.id},function(response) {
        $scope.groupMap = response;
        //Update Resources
        $scope.dspResources = $scope.getResources($scope.groupMap.resourceType);
      },function(error){
        displayRestError.display(error);
      });
  }else if($stateParams.groupId){
    $scope.groupMap.resourcesGroup = {};
    $scope.groupMap.resourcesGroup.id = $stateParams.groupId;
  }else{
    $state.go("resourcesGroupList");
  }

  //pre load
  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "resources group"});

  //Get resources
  $scope.getResources = function(resourceType){
    return CommonServices.getResources(resourceType);
  }

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_AN_ENTRY');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_AN_ENTRY');
  $scope.cancelButtonState = "resourcesGroupMapList({id:"+$stateParams.groupId+"})"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      ResourcesGroupMapFactory.update($scope.groupMap,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("resourcesGroupMapList",{id:$stateParams.groupId});
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      ResourcesGroupMapFactory.create($scope.groupMap,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("resourcesGroupMapList",{id:$stateParams.groupId});
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});
