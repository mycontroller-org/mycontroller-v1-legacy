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
myControllerModule.controller('ScriptsController', function(alertService,
$scope, ScriptsFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter, $base64) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('SCRIPTS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_SCRIPTS_SETUP');
  $scope.noItemsSystemIcon = "fa fa-code";

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
    ScriptsFactory.getAll($scope.query, function(response) {
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
    CommonServices.selectAllItems($scope, 'name');
  };

  $scope.selectItem = function(item){
    CommonServices.selectItem($scope, item, 'name');
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
        id: 'type',
        title:  $filter('translate')('TYPE'),
        placeholder: $filter('translate')('FILTER_BY_TYPE'),
        filterType: 'select',
        filterValues: ['Condition','Operation'],
      },
      {
        id: 'extension',
        title: $filter('translate')('EXTENSION'),
        placeholder: $filter('translate')('FILTER_BY_EXTENSION'),
        filterType: 'select',
        filterValues: ['js','groovy','py','rb'],
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
      ScriptsFactory.deleteIds($scope.itemIds, function(response) {
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
      $state.go("scriptsAddEdit",{'name':$base64.encode($scope.itemIds[0])});
    }
  };
/*
  //execute item
  $scope.runNow = function () {
    if($scope.itemIds.length == 1){
      ScriptsFactory.runNow({"script":$scope.itemIds[0]}, function(response) {
        alertService.success('Result printed in console also.<br>'+angular.toJson(response));
        console.log('Script['+$scope.itemIds[0]+'] result:\n'+angular.toJson(response));
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }
  };

*/
  //Execute item
  $scope.runNow = function (size) {
    if($scope.itemIds.length != 1){
      return;
    }
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/scripts/run-now-modal.html',
    controller: 'ControllerScriptRunNowModal',
    size: size,
    resolve: {itemId: function () {return $scope.itemIds[0]}}
    });

    modalInstance.result.then(function () {
      //Nothing to do...
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

});

//Script run now Modal
myControllerModule.controller('ControllerScriptRunNowModal', function ($scope, $uibModalInstance, $filter, CommonServices, ScriptsFactory, itemId) {
  $scope.header = $filter('translate')('RUN_NOW');
  $scope.cs = CommonServices;
  $scope.request = {};
  $scope.request.script = itemId;
  $scope.request.bindings = '{ }';
  //$uibModalInstance.close();
  $scope.runNow = function() {
    $scope.runningInProgress = true;
    $scope.request.scriptBindings = angular.fromJson(JSON.stringify(eval('('+$scope.request.bindings+')')));
    ScriptsFactory.runNow($scope.request, function(response) {
      $scope.scriptResult = angular.toJson(response, true);
      $scope.runningInProgress = false;
    },function(error){
      if(error.data.errorMessage){
        $scope.scriptResult = error.data.errorMessage;
      }else{
        $scope.scriptResult = angular.toJson(error.data);
      }
      $scope.scriptResult = angular.toJson(error.data, true);
      $scope.runningInProgress = false;
      displayRestError.display(error);
    });
  };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});

//Add Edit script controller
myControllerModule.controller('ScriptsControllerAddEdit', function ($scope, $stateParams, $state,
  ScriptsFactory, mchelper, alertService, displayRestError, $filter, CommonServices, $base64) {
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.cs = CommonServices;
  $scope.showData = false;
  $scope.editMode = false;

  if($stateParams.name){
    $scope.editMode = true;
    ScriptsFactory.get({"name":$base64.decode($stateParams.name)},function(response) {
        $scope.item = response;
        $scope.showData = true;
      },function(error){
        displayRestError.display(error);
        $scope.showData = true;
      });
  }else{
    $scope.showData = true;
  }

  //Read File and put it in textarea
  $scope.displayFileContents = function(contents) {
    $scope.item.data = contents;
  };

  $scope.editorOptions = {
        lineWrapping : true,
        lineNumbers: true,
        readOnly: 'nocursor',
        mode: 'xml',
    };

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.name;
  $scope.headerStringAdd = $filter('translate')('ADD_SCRIPT');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_SCRIPT');
  $scope.cancelButtonState = "scriptsList"; //Cancel button url
  $scope.saveProgress = false;

  $scope.save = function(){
    $scope.saveProgress = true;
    ScriptsFactory.upload($scope.item, function(response) {
      alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
      $state.go("scriptsList");
    },function(error){
      displayRestError.display(error);
        $scope.saveProgress = false;
    });
  }
});
