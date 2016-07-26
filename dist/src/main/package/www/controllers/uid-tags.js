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
myControllerModule.controller('UidTagsController', function(alertService,
$scope, $filter, UidTagsFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $stateParams) {
  //GUI page settings
  $scope.headerStringList = $filter('translate')('UID_TAGS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_UID_TAGS_SETUP');
  $scope.noItemsSystemIcon = "fa fa-tags";

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

  if($stateParams.sensorId){
    $scope.query.sensorId = $stateParams.sensorId;
  }

  //get all items
  $scope.getAllItems = function(){
    UidTagsFactory.getAll($scope.query, function(response) {
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
        id: 'uid',
        title:  $filter('translate')('UID'),
        placeholder: $filter('translate')('FILTER_BY_UID'),
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
        id: 'uid',
        title:  $filter('translate')('UID'),
        sortType: 'numeric'
      }
    ],
    onSortChange: sortChange,
    isAscending: true,
  };


  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("uidTagsAddEdit", {'id':$scope.itemIds[0]});
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
      UidTagsFactory.deleteIds($scope.itemIds, function(response) {
        alertService.success('ITEMS_DELETED_SUCCESSFULLY');
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

//add edit item
myControllerModule.controller('UidTagsControllerAddEdit', function ($scope, CommonServices, alertService, UidTagsFactory, mchelper, $stateParams, $state, $filter, displayRestError) {
  $scope.item = {};
  $scope.item.sensorVariable={};
  $scope.cs = CommonServices;

    if($stateParams.id){
      UidTagsFactory.get({"id":$stateParams.id},function(response) {
        $scope.item = response;
      },function(error){
        displayRestError.display(error);
      });
  }


  //pre load
  $scope.resources = CommonServices.getResources("Sensor variable");

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_UID_TAG_ENTRY');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_UID_TAG_ENTRY');
  $scope.cancelButtonState = "uidTagsList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;


  //Save data
  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      UidTagsFactory.update($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("uidTagsList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      UidTagsFactory.create($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("uidTagsList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }

});
