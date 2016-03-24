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
myControllerModule.controller('ResourcesLogsController', function(alertService,
$scope, $filter, ResourcesLogsFactory, SettingsFactory, $uibModal, $stateParams, mchelper, CommonServices, $interval) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('RESOURCES_LOGS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_LOGS_AVAILABLE');
  $scope.noItemsSystemIcon = "fa fa-list";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];

  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};
  $scope.itemsPerPage = mchelper.userSettings.resourcesLogsItemsPerPage;

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


  //Stop if an request sent already
  var updateInprogress = false;
  //get all items
  $scope.getAllItems = function(){
    if(updateInprogress){
      return;
    }
    updateInprogress = true;
    $scope.query.pageLimit = parseInt($scope.itemsPerPage);
    ResourcesLogsFactory.getAll($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
      updateInprogress = false;
    },function(error){
      updateInprogress = false;
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
        title:  $filter('translate')('MESSAGE'),
        placeholder: $filter('translate')('FILTER_BY_MESSAGE'),
        filterType: 'text'
      },
      {
        id: 'resourceType',
        title:  $filter('translate')('TYPE'),
        placeholder: $filter('translate')('FILTER_BY_TYPE'),
        filterType: 'select',
        filterValues:['Gateway','Node','Sensor','Sensor variable','Resources group','Alarm definition','Timer'],
      },
      {
        id: 'logLevel',
        title:  $filter('translate')('LEVEL'),
        placeholder: $filter('translate')('FILTER_BY_LEVEL'),
        filterType: 'select',
        filterValues: ['Trace','Notice','Info','Warning','Error'],
      },
      {
        id: 'messageType',
        title: $filter('translate')('MESSAGE_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_MESSGAE_TYPE'),
        filterType: 'select',
        filterValues: ['Presentation','Set','Request','Internal','Stream'],
      },
      {
        id: 'logDirection',
        title: $filter('translate')('DIRECTION'),
        placeholder: $filter('translate')('FILTER_BY_DIRECTION'),
        filterType: 'select',
        filterValues: ['Internal','Sent','Received'],
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
        title:  $filter('translate')('TIME'),
        sortType: 'alpha'
      },
      {
        id: 'logLevel',
        title:  $filter('translate')('LEVEL'),
        sortType: 'alpha'
      },
      {
        id: 'resourceType',
        title: $filter('translate')('TYPE'),
        sortType: 'alpha'
      },
      {
        id: 'messageType',
        title: $filter('translate')('MESSAGE_TYPE'),
        sortType: 'alpha'
      },
      {
        id: 'logDirection',
        title: $filter('translate')('DIRECTION'),
        sortType: 'alpha'
      },
      {
        id: 'message',
        title: $filter('translate')('MESSAGE'),
        sortType: 'alpha'
      }
    ],
    onSortChange: sortChange,
    isAscending: false,
  };

  //Update items per page
  $scope.updateItemsPerPage = function(itemsPerPage){
    mchelper.userSettings.resourcesLogsItemsPerPage = itemsPerPage;
    SettingsFactory.saveUserSettings(mchelper.userSettings);
    CommonServices.saveMchelper(mchelper);
    $scope.getAllItems();
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

  function updatePage(){
    $scope.getAllItems(true);
  };

  // global page refresh
  var promise = $interval(updatePage, mchelper.cfg.globalPageRefreshTime);

  // cancel interval on scope destroy
  $scope.$on('$destroy', function(){
    $interval.cancel(promise);
  });

});

//purge resources logs
myControllerModule.controller('ResourcesLogsPurgeController', function ($scope, CommonServices, alertService, ResourcesLogsFactory, mchelper, $filter, TypesFactory) {
  $scope.item = {};

  //GUI page settings
  $scope.headerStringAdd = $filter('translate')('PURGE_RESOURCES_LOGS');
  $scope.cancelButtonState = "resourcesLogsList"; //Cancel button state
  $scope.saveProgress = false;
  $scope.saveButtonName = $filter('translate')('PURGE');
  $scope.savingButtonName = $filter('translate')('PURGING');
  $scope.saveButtonTooltip = $filter('translate')('PURGE_WARNING');
  //$scope.isSettingChange = false;

  //Pre load
  $scope.messageTypes = TypesFactory.getResourceLogsMessageTypes();
  $scope.logDirections = TypesFactory.getResourceLogsLogDirections();
  $scope.logLevels = TypesFactory.getResourceLogsLogLevels();

  $scope.resourceTypes = TypesFactory.getResourceTypes();
  $scope.resourcesLogs = {};


  //Get resources
  $scope.getResources = function(resourceType){
    return CommonServices.getResources(resourceType);
  }

  //Convert as display string
  $scope.getDateTimeDisplayFormat = function (newDate) {
    return $filter('date')(newDate, mchelper.cfg.dateFormat, mchelper.cfg.timezone);
  };

  //Save data - here it's purge
  $scope.save = function(){
    //Update validity from/to
    if($scope.purgeBefore){
      $scope.resourcesLogs.timestamp = $scope.purgeBefore.getTime();
    }

    $scope.saveProgress = true;
    ResourcesLogsFactory.purge($scope.resourcesLogs,function(response) {
      alertService.success($filter('translate')('PURGE_DONE_SUCCESSFULLY'));
      $scope.saveProgress = false;
    },function(error){
      displayRestError.display(error);
      $scope.saveProgress = false;
    });
  }
});

