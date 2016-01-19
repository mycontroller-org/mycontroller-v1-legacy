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
myControllerModule.controller('ResourcesLogsController', function(alertService,
$scope, $filter, ResourcesLogsFactory, $location, $uibModal, $stateParams, about, CommonServices) {
  
  //GUI page settings
  $scope.headerStringList = "Resources logs detail";
  $scope.noItemsSystemMsg = "No logs available.";
  $scope.noItemsSystemIcon = "fa fa-list";

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
  
  
  if($stateParams.resourceType){
    $scope.query.resourceType = $stateParams.resourceType;
    if($stateParams.resourceId){
      $scope.query.resourceId = $stateParams.resourceId;
    }
  }

  //get all items
  $scope.getAllItems = function(){
    ResourcesLogsFactory.getAll($scope.query, function(response) {
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
        id: 'message',
        title:  'Message',
        placeholder: 'Filter by Message',
        filterType: 'text'
      },
      {
        id: 'resource',
        title:  'Resource',
        placeholder: 'Filter by Resource',
        filterType: 'text'
      },
      {
        id: 'conditionString',
        title:  'Condition',
        placeholder: 'Filter by Condition',
        filterType: 'text',
      },
      {
        id: 'dampeningString',
        title:  'Dampening',
        placeholder: 'Filter by Dampening',
        filterType: 'text',
      },
      {
        id: 'notificationString',
        title:  'Notification',
        placeholder: 'Filter by Notification',
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
        id: 'timestamp',
        title:  'Time',
        sortType: 'alpha'
      },
      {
        id: 'logLevel',
        title:  'Level',
        sortType: 'alpha'
      },
      {
        id: 'resourceType',
        title:  'Resource type',
        sortType: 'alpha'
      }
    ],
    onSortChange: sortChange,
    isAscending: false,
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
      ResourcesLogsFactory.delete($scope.itemIds, function(response) {
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

//purge resources logs
myControllerModule.controller('ResourcesLogsPurgeController', function ($scope, CommonServices, alertService, ResourcesLogsFactory, about, $filter, TypesFactory) {
  $scope.item = {};
  
  //GUI page settings
  $scope.headerStringAdd = "Purge resources logs";
  $scope.cancelButtonUrl = about.urlResourcesLogsList+'//'; //Cancel button url
  $scope.saveProgress = false;
  $scope.saveButtonName = 'Purge';
  $scope.savingButtonName = 'Purging...';
  //$scope.isSettingChange = false;
  
  //Pre load
  $scope.resourceTypes = TypesFactory.getResourceTypes();
  $scope.resourcesLogs = {};
  
  
  //Get resources
  $scope.getResources = function(resourceType){
    return CommonServices.getResources(resourceType);
  }
  
  //Convert as display string
  $scope.getDateTimeDisplayFormat = function (newDate) {
    return $filter('date')(newDate, about.dateFormat, about.timezone);
  };
  
  //Save data - here it's purge
  $scope.save = function(){
    //Update validity from/to
    if($scope.purgeBefore){
      $scope.resourcesLogs.timestamp = $scope.purgeBefore.getTime();
    }
    
    $scope.saveProgress = true;
    ResourcesLogsFactory.purge($scope.resourcesLogs,function(response) {
      alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.node));
      $scope.saveProgress = false;
    },function(error){
      displayRestError.display(error);
      $scope.saveProgress = false;
    });
  }
});

