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
myControllerModule.controller('NotificationsController', function(alertService,
$scope, NotificationsFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter) {
  
  //GUI page settings
  $scope.headerStringList = $filter('translate')('NOTIFICATIONS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_NOTIFICATIONS_SETUP');
  $scope.noItemsSystemIcon = "fa fa-bell-o";

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

  //get all Sensors
  $scope.getAllItems = function(){
    NotificationsFactory.getAll($scope.query, function(response) {
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
        title:  $filter('translate')('NAME'),
        placeholder: $filter('translate')('FILTER_BY_NAME'),
        filterType: 'text'
      },
      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        placeholder: $filter('translate')('FILTER_BY_ENABLED'),
        filterType: 'select',
        filterValues: ['True','False'],
      },
      {
        id: 'publicAccess',
        title: $filter('translate')('PUBLIC_ACCESS'),
        placeholder: $filter('translate')('FILTER_BY_PUBLIC_ACCESS'),
        filterType: 'select',
        filterValues: ['True','False'],
      },
      {
        id: 'type',
        title:  $filter('translate')('NOTIFICATION_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_NOTIFICATION_TYPE'),
        filterType: 'select',
        filterValues: ['Send payload','Send SMS','Send email'],
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
      },
      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        sortType: 'text'
      },
      {
        id: 'type',
        title:  $filter('translate')('NOTIFICATION_TYPE'),
        sortType: 'text'
      },
      {
        id: 'publicAccess',
        title:  $filter('translate')('PUBLIC_ACCESS'),
        sortType: 'text'
      },
      {
        id: 'lastExecution',
        title: $filter('translate')('LAST_EXECUTION'),
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
      NotificationsFactory.deleteIds($scope.itemIds, function(response) {
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

  
  //Enable items
  $scope.enable = function () {
    if($scope.itemIds.length > 0){
      NotificationsFactory.enableIds($scope.itemIds, function(response) {
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
      NotificationsFactory.disableIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('ITEMS_DISABLED_SUCCESSFULLY'));
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      }); 
    }
  };
  
    //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("alarmsNotificationsAddEdit",{'id':$scope.itemIds[0]});
    }
  };

});


//Add Edit notification controller
myControllerModule.controller('NotificationsControllerAddEdit', function ($scope, $stateParams, $state, GatewaysFactory, NodesFactory, SensorsFactory, TypesFactory, NotificationsFactory, mchelper, alertService, displayRestError, $filter, CommonServices) {
  $scope.mchelper = mchelper;
  $scope.notification = {};
  $scope.notification.enabled = true;
  $scope.notification.publicAccess = false;
  $scope.cs = CommonServices;
  
  // Update resources list
  $scope.getResources= function(resourceType){
    if(resourceType === 'Sensor variable'){
      return TypesFactory.getSensorVariables();
    }else if(resourceType === 'Gateway' || resourceType === 'Gateway state'){
      return TypesFactory.getGateways();
    }else if(resourceType === 'Node' || resourceType === 'Node state'){
      return TypesFactory.getNodes();
    }else if(resourceType === 'Resources group'){
      return TypesFactory.getResourcesGroups();
    }else if(resourceType === 'Alarm definition'){
      return TypesFactory.getAlarmDefinitions();
    }else if(resourceType === 'Timer'){
      return TypesFactory.getTimers();
    }else if(resourceType === 'Value'){
      $scope.updateThresholdValueTypes($scope.notification.resourceType);
      return null;
    }else{
      return null;
    }
  }

  //Update Payload operations
  $scope.updatePayloadOperations= function(resourceType){
    $scope.payloadOperations = TypesFactory.getPayloadOperations({"resourceType":resourceType}); 
  }

  
  if($stateParams.id){
    NotificationsFactory.get({"id":$stateParams.id},function(response) {
        $scope.notification = response;
        //Update Notification Type
        if($scope.notification.type === 'Send payload'){
          $scope.plResourcesList = $scope.getResources($scope.notification.variable1);
          $scope.plResourceId = parseInt($scope.notification.variable2);
          //Update payload operations
          if($scope.notification.variable1 !== 'Sensor variable'){
            $scope.updatePayloadOperations($scope.notification.variable1);
          }
        }
        
        //Update delay time
        if($scope.notification.variable4){
          $scope.notification.variable4 = $scope.notification.variable4/1000;
        }

      },function(error){
        displayRestError.display(error);
      });
  }

  //--------------pre load -----------
  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "Alarm definition"});
  $scope.spResourceTypes = TypesFactory.getResourceTypes({"resourceType": "Alarm definition", "isSendPayload":true});
  $scope.notificationTypes = TypesFactory.getAlarmNotificationTypes();

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_NOTIFICATION');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_NOTIFICATION');
  $scope.cancelButtonState = "alarmsNotificationsList"; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.save = function(){    
    //Update delay time
    if($scope.notification.variable4){
      $scope.notification.variable4 = $scope.notification.variable4*1000;
    }
    
      $scope.saveProgress = true;
    if($stateParams.id){
      NotificationsFactory.update($scope.notification, function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("alarmsNotificationsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }else{
      NotificationsFactory.create($scope.notification, function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("alarmsNotificationsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }
  }
});
