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
myControllerModule.controller('ForwardPayloadController', function(alertService,
$scope, $filter, ForwardPayloadFactory, $location, $uibModal, displayRestError, about, CommonServices, $stateParams) {
  //GUI page settings
  $scope.headerStringList = "Forward payloads detail";
  $scope.noItemsSystemMsg = "No forward payloads set up.";
  $scope.noItemsSystemIcon = "fa fa-forward";

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
  
  if($stateParams.sensorId){
    $scope.query.sensorId = $stateParams.sensorId;
  }

  //get all items
  $scope.getAllItems = function(){
    ForwardPayloadFactory.getAll($scope.query, function(response) {
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
        id: 'sourceId',
        title:  'Source id',
        placeholder: 'Filter by Source id',
        filterType: 'text'
      },
      {
        id: 'destinationId',
        title:  'Destination id',
        placeholder: 'Filter by Destination id',
        filterType: 'text'
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
        id: 'sourceId',
        title:  'Source id',
        sortType: 'text'
      },{
        id: 'destinationId',
        title:  'Destination id',
        sortType: 'text'
      },
      {
        id: 'enabled',
        title:  'Enabled',
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };
  

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $location.path(about.urlForwardPayloadAddEdit.replace('#', '') + '/' + $scope.itemIds[0]);
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
      ForwardPayloadFactory.deleteIds($scope.itemIds, function(response) {
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
  
  //Enable items
  $scope.enable = function () {
    if($scope.itemIds.length > 0){
      ForwardPayloadFactory.enableIds($scope.itemIds, function(response) {
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
      ForwardPayloadFactory.disableIds($scope.itemIds, function(response) {
        alertService.success('Disabled '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      }); 
    }
  };

});

//add edit item
myControllerModule.controller('ForwardPayloadControllerAddEdit', function ($scope, CommonServices, alertService, ForwardPayloadFactory, about, $stateParams, $filter) {
  $scope.fpayload = {};
  $scope.fpayload.enabled=true;
  $scope.fpayload.source={};
  $scope.fpayload.destination={};

    if($stateParams.id){
      ForwardPayloadFactory.get({"id":$stateParams.id},function(response) {
        $scope.fpayload = response;
      },function(error){
        displayRestError.display(error);
      });
  }
  
    //Get resources
  $scope.getResources = function(resourceType){
    return CommonServices.getResources(resourceType);
  }
  
  
  //pre load
  $scope.resources = $scope.getResources("Sensor variable");
  
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add forward payload entry";
  $scope.headerStringUpdate = "Update forward payload entry";
  $scope.cancelButtonUrl = about.urlForwardPayloadList+'/'; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  

  //Save data
  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      ForwardPayloadFactory.update($scope.fpayload,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      ForwardPayloadFactory.create($scope.fpayload,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }

});
