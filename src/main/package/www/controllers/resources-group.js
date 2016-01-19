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
myControllerModule.controller('ResourcesGroupController', function(alertService,
$scope, ResourcesGroupFactory, $location, $uibModal, displayRestError, about, CommonServices) {

  //GUI page settings
  $scope.headerStringList = "Resource groups detail";
  $scope.noItemsSystemMsg = "No resource groups set up.";
  $scope.noItemsSystemIcon = "pficon pficon-replicator";

  //load empty, configuration, etc.,
  $scope.about = about;
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
        title:  'Name',
        placeholder: 'Filter by Name',
        filterType: 'text'
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
      },{
        id: 'description',
        title:  'Description',
        sortType: 'text'
      },{
        id: 'state',
        title:  'Status',
        sortType: 'text'
      },{
        id: 'stateSince',
        title:  'Status since',
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
        alertService.success('Turned ON '+item.name+' group.');
        //Update display table
        //$scope.getAllItems()();
      },function(error){
        displayRestError.display(error);            
      });
    }else{
      ResourcesGroupFactory.turnOffIds(itemArray, function(response) {
        alertService.success('Turned OFF '+item.name+' group.');
        //Update display table
        //$scope.getAllItems()();
      },function(error){
        displayRestError.display(error);            
      });
    }
  }
    
  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $location.path(about.urlResourcesGroupAddEdit.replace('#', '') + '/' + $scope.itemIds[0]);
    }
  };
  
  //Turm ON items
  $scope.turnOn = function () {
    if($scope.itemIds.length > 0){
      ResourcesGroupFactory.turnOnIds($scope.itemIds, function(response) {
        alertService.success('Turned ON '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems()();
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
        alertService.success('Turned OFF '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems()();
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
        alertService.success('Deleted '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems()();
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
myControllerModule.controller('ResourcesGroupControllerAddEdit', function ($scope, $stateParams, ResourcesGroupFactory,  about, alertService, displayRestError, $filter) {
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add resources group";
  $scope.headerStringUpdate = "Update resources group";
  $scope.cancelButtonUrl = about.urlResourcesGroupList+'//'; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  
  $scope.about = about;
  $scope.group = {};
  $scope.id = $stateParams.id;

 
  if($stateParams.id){
    ResourcesGroupFactory.get({"id":$stateParams.id},function(response) {
        $scope.group = response;
      },function(error){
        displayRestError.display(error);
      });
  }
  
  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      ResourcesGroupFactory.update($scope.group,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.group));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      ResourcesGroupFactory.create($scope.group,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.group));
        $scope.saveProgress = false;
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
$scope, ResourcesGroupFactory, ResourcesGroupMapFactory, $location, $uibModal, displayRestError, about, CommonServices, $stateParams) {

  //GUI page settings
  $scope.headerStringList = "Resource groups maps detail";
  $scope.noItemsSystemMsg = "No resource groups map set up.";
  $scope.noItemsSystemIcon = "pficon pficon-replicator";

  //load empty, configuration, etc.,
  $scope.about = about;
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
    $location.path(about.urlResourcesGroupList.replace('#', ''));
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
        title:  'Resource type',
        placeholder: 'Filter by Resource type',
        filterType: 'text'
      },
      {
        id: 'payloadOn',
        title:  'Payload ON',
        placeholder: 'Filter by Payload ON',
        filterType: 'integer',
      },
      {
        id: 'payloadOff',
        title:  'Payload OFF',
        placeholder: 'Filter by Payload OFF',
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
        title:  'Resource type',
        sortType: 'text'
      },{
        id: 'payloadOn',
        title:  'Payload ON',
        sortType: 'text'
      },
      {
        id: 'payloadOff',
        title:  'Payload OFF',
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };
    
  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $location.path(about.urlResourcesGroupMapAddEdit.replace('#', '') +'/' + $scope.query.groupId + '/' + $scope.itemIds[0]);
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
        alertService.success('Deleted '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems()();
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
myControllerModule.controller('ResourcesGroupMapControllerAddEdit', function ($scope, $stateParams, $location, TypesFactory, CommonServices, ResourcesGroupMapFactory, about, alertService, displayRestError, $filter) {
  $scope.about = about;
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
    $location.path(about.urlResourcesGroupList.replace('#', ''));
  }
  
  //pre load
  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "resources group"});

  //Get resources
  $scope.getResources = function(resourceType){
    return CommonServices.getResources(resourceType);
  }
  
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add an entry";
  $scope.headerStringUpdate = "Update an entry";
  $scope.cancelButtonUrl = about.urlResourcesGroupMapList+'/'+$stateParams.groupId; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  
  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      ResourcesGroupMapFactory.update($scope.groupMap,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.group));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      ResourcesGroupMapFactory.create($scope.groupMap,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.group));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});
