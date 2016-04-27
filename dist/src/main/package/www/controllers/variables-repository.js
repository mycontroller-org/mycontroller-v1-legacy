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
myControllerModule.controller('VariablesRepositoryController', function(alertService,
$scope, VariablesRepositoryFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('VARIABLES_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_VARIABLES_SETUP');
  $scope.noItemsSystemIcon = "fa fa-list-alt";

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

  //get all items
  $scope.getAllItems = function(){
    VariablesRepositoryFactory.getAll($scope.query, function(response) {
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
        id: 'key',
        title:  $filter('translate')('KEY'),
        placeholder: $filter('translate')('FILTER_BY_KEY'),
        filterType: 'text'
      },
      {
        id: 'value',
        title:  $filter('translate')('VALUE'),
        placeholder: $filter('translate')('FILTER_BY_VALUE'),
        filterType: 'text'
      },
      {
        id: 'value2',
        title:  $filter('translate')('VALUE2'),
        placeholder: $filter('translate')('FILTER_BY_VALUE2'),
        filterType: 'text'
      },
      {
        id: 'value3',
        title:  $filter('translate')('VALUE3'),
        placeholder: $filter('translate')('FILTER_BY_VALUE3'),
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
        id: 'key',
        title:  $filter('translate')('KEY'),
        sortType: 'text'
      },
      {
        id: 'value',
        title:  $filter('translate')('VALUE'),
        sortType: 'text'
      },
      {
        id: 'value2',
        title:  $filter('translate')('VALUE2'),
        sortType: 'text'
      },
      {
        id: 'value3',
        title:  $filter('translate')('VALUE3'),
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
      VariablesRepositoryFactory.deleteIds($scope.itemIds, function(response) {
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
      $state.go("variablesRepositoryAddEdit",{'id':$scope.itemIds[0]});
    }
  };

});

//Add Edit script controller
myControllerModule.controller('VariablesRepositoryControllerAddEdit', function ($scope, $stateParams, $state,
  VariablesRepositoryFactory, mchelper, alertService, displayRestError, $filter, CommonServices) {
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.cs = CommonServices;
  $scope.showData = false;

  if($stateParams.id){
    VariablesRepositoryFactory.get({"id":$stateParams.id},function(response) {
        $scope.item = response;
        $scope.showData = true;
      },function(error){
        displayRestError.display(error);
        $scope.showData = true;
      });
  }else{
    $scope.showData = true;
  }

  $scope.editorOptions = {
        lineWrapping : true,
        lineNumbers: true,
        readOnly: 'nocursor',
        mode: 'xml',
    };

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_VARIABLE');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_VARIABLE');
  $scope.cancelButtonState = "variablesRepositoryList"; //Cancel button url
  $scope.saveProgress = false;

  $scope.save = function(){
    $scope.saveProgress = true;
    VariablesRepositoryFactory.update($scope.item, function(response) {
      alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
      $state.go("variablesRepositoryList");
    },function(error){
      displayRestError.display(error);
        $scope.saveProgress = false;
    });
  }
});
