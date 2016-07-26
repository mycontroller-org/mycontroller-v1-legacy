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
myControllerModule.controller('TemplatesController', function(alertService,
$scope, TemplatesFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter, $base64) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('TEMPLATES_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_TEMPLATES_SETUP');
  $scope.noItemsSystemIcon = "fa fa-file-text";

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
    TemplatesFactory.getAll($scope.query, function(response) {
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
        id: 'extension',
        title: $filter('translate')('EXTENSION'),
        placeholder: $filter('translate')('FILTER_BY_EXTENSION'),
        filterType: 'select',
        filterValues: ['html'],
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
      TemplatesFactory.deleteIds($scope.itemIds, function(response) {
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
      $state.go("templatesAddEdit",{'name':$base64.encode($scope.itemIds[0])});
    }
  };

  //Execute item
  $scope.runNow = function (size) {
    if($scope.itemIds.length != 1){
      return;
    }
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/templates/run-now-modal.html',
    controller: 'ControllerTemplateRunNowModal',
    size: size,
    resolve: {itemId: function () {return $scope.itemIds[0]}}
    });

    modalInstance.result.then(function () {
      ScriptsFactory.deleteIds($scope.itemIds, function(response) {
        //Nothing to do...
      },function(error){
        displayRestError.display(error);
      });
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

});

//Template run now Modal
myControllerModule.controller('ControllerTemplateRunNowModal', function ($scope, $uibModalInstance, $filter, CommonServices, ScriptsFactory, TemplatesFactory, $sce, itemId) {
  $scope.header = $filter('translate')('RUN_NOW');
  $scope.cs = CommonServices;
  $scope.request = {};
  $scope.request.template = itemId;
  $scope.scripts = ScriptsFactory.getAllLessInfo({"type":"Operation"});
  $scope.request.bindings = '{ }';
  //$uibModalInstance.close();
  $scope.runNow = function() {
    $scope.runningInProgress = true;
    $scope.request.scriptBindings = angular.fromJson(JSON.stringify(eval('('+$scope.request.bindings+')')));
    TemplatesFactory.getHtml($scope.request, function(response) {
      $scope.templateResult = $sce.trustAsHtml(response.message);
      $scope.runningInProgress = false;
    },function(error){
      if(error.data.errorMessage){
        $scope.templateResult = $sce.trustAsHtml('<pre class=\"pre-scrollable\">'+error.data.errorMessage+'</pre>');
      }else{
        $scope.templateResult = $sce.trustAsHtml('<pre class=\"pre-scrollable\">'+angular.toJson(error.data)+'</pre>');
      }
      $scope.runningInProgress = false;
      displayRestError.display(error);
    });
  };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});

//Add Edit script controller
myControllerModule.controller('TemplatesControllerAddEdit', function ($scope, $stateParams, $state,
  TemplatesFactory, mchelper, alertService, displayRestError, $filter, CommonServices, $base64) {
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.cs = CommonServices;
  $scope.showData = false;
  $scope.editMode = false;

  if($stateParams.name){
    $scope.editMode = true;
    TemplatesFactory.get({"name":$base64.decode($stateParams.name)},function(response) {
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
  $scope.headerStringAdd = $filter('translate')('ADD_TEMPLATE');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_TEMPLATE');
  $scope.cancelButtonState = "templatesList"; //Cancel button url
  $scope.saveProgress = false;

  $scope.save = function(){
    $scope.saveProgress = true;
    TemplatesFactory.upload($scope.item, function(response) {
      alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
      $state.go("templatesList");
    },function(error){
      displayRestError.display(error);
        $scope.saveProgress = false;
    });
  }
});
