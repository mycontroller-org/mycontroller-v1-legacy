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
myControllerModule.controller('AlarmsController', function(alertService,
$scope, AlarmsFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter) {
  
  //GUI page settings
  $scope.headerStringList = $filter('translate')('ALARMS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_ALARMS_SETUP');
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
    AlarmsFactory.getAll($scope.query, function(response) {
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
        id: 'resource',
        title:  $filter('translate')('RESOURCE'),
        placeholder: $filter('translate')('FILTER_BY_RESOURCE'),
        filterType: 'text'
      },
      {
        id: 'conditionString',
        title: $filter('translate')('CONDITION'),
        placeholder: $filter('translate')('FILTER_BY_CONDITION'),
        filterType: 'text',
      },
      {
        id: 'dampeningString',
        title:  $filter('translate')('DAMPENING'),
        placeholder: $filter('translate')('FILTER_BY_DAMPENING'),
        filterType: 'text',
      },
      {
        id: 'notificationString',
        title:  $filter('translate')('NOTIFICATION'),
        placeholder: $filter('translate')('FILTER_BY_NOTIFICATION'),
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
        title:  $filter('translate')('NAME'),
        sortType: 'text'
      },
      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        sortType: 'text'
      },
      {
        id: 'resourceType',
        title:  $filter('translate')('RESOURCE_TYPE'),
        sortType: 'text'
      },
      {
        id: 'lastTrigger',
        title: $filter('translate')('LAST_TRIGGER'),
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
      AlarmsFactory.deleteIds($scope.itemIds, function(response) {
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
      AlarmsFactory.enableIds($scope.itemIds, function(response) {
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
      AlarmsFactory.disableIds($scope.itemIds, function(response) {
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
      $state.go("alarmsAddEdit",{'id':$scope.itemIds[0]});
    }
  };

});


//Add Edit alarm defination controller
myControllerModule.controller('AlarmsControllerAddEdit', function ($scope, $stateParams, $state, GatewaysFactory, NodesFactory, SensorsFactory, TypesFactory, AlarmsFactory, mchelper, alertService, displayRestError, $filter) {
 
  $scope.mchelper = mchelper;
  $scope.alarm = {};
  $scope.alarm.ignoreDuplicate = true;
  $scope.alarm.enabled = true;
  
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
    }else if(resourceType === 'Value'){
      $scope.updateThresholdValueTypes($scope.alarm.resourceType);
      return null;
    }else{
      return null;
    }
  }
  
  //Update trigger types
  $scope.updateTriggerTypes = function(resourceType){
    $scope.alarmTriggerTypes = TypesFactory.getAlarmTriggerTypes({"resourceType":resourceType}); 
  }
  
  //Update Threshold types
  $scope.updateThresholdValueTypes= function(resourceType){
    $scope.stateTypes = TypesFactory.getStateTypes({"resourceType":resourceType}); 
  }
  
  if($stateParams.id){
    AlarmsFactory.get({"id":$stateParams.id},function(response) {
        $scope.alarm = response;
        //Update Notification Type
        if($scope.alarm.notificationType === 'Send payload'){
          $scope.plResourcesList = $scope.getResources($scope.alarm.variable1);
          $scope.plResourceId = parseInt($scope.alarm.variable2);
        }       
        
        //Update trigger types
        $scope.updateTriggerTypes($scope.alarm.resourceType);
        
        //Update Resource Type
        $scope.rsResourcesList = $scope.getResources($scope.alarm.resourceType);        
        
        //Update Threshold Type        
        if($scope.alarm.thresholdType === 'Sensor variable' || $scope.alarm.thresholdType === 'Gateway state' || $scope.alarm.thresholdType === 'Node state'){
          $scope.thResourcesList = $scope.getResources($scope.alarm.thresholdType);
          $scope.alarm.thresholdValue = parseInt($scope.alarm.thresholdValue);
        }else{
          $scope.updateThresholdValueTypes($scope.alarm.resourceType);
        }
          
        //Update dampening value
        if($scope.alarm.dampeningType === 'Active time'){
          if($scope.alarm.dampeningVar1 % 86400000  == 0){
            $scope.alarm.dampeningTime = $scope.alarm.dampeningVar1 / 86400000;
            $scope.alarm.dampeningTimeConstant = "86400000";
          }else if($scope.alarm.dampeningVar1 % 3600000  == 0){
            $scope.alarm.dampeningTime = $scope.alarm.dampeningVar1 / 3600000;
            $scope.alarm.dampeningTimeConstant = "3600000";
          }else if($scope.alarm.dampeningVar1 % 60000  == 0){
            $scope.alarm.dampeningTime = $scope.alarm.dampeningVar1 / 60000;
            $scope.alarm.dampeningTimeConstant = "60000";
          }else{
            $scope.alarm.dampeningTime = $scope.alarm.dampeningVar1;
            $scope.alarm.dampeningTimeConstant = "1000";
          }
          $scope.alarm.dampeningVar1 = $scope.alarm.dampeningTime * $scope.alarm.dampeningTimeConstant;
        }
      },function(error){
        displayRestError.display(error);
      });
  }

  //--------------pre load -----------
  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "Alarm definition"});
  //$scope.alarmTriggerTypes = TypesFactory.getAlarmTriggerTypes();
  $scope.alarmThresholdTypes = TypesFactory.getAlarmThresholdTypes();
  //$scope.stateTypes = TypesFactory.getStateTypes();
  $scope.dampeningTypes = TypesFactory.getAlarmDampeningTypes();
  $scope.notificationTypes = TypesFactory.getAlarmNotificationTypes();

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_ALARM');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_ALARM');
  $scope.cancelButtonState = "alarmsList"; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.save = function(){
    //Update Notification Type
    if($scope.alarm.notificationType === 'Send payload'){
       $scope.alarm.variable2 = $scope.plResourceId;
    }
    
    //Update dampening value
    if($scope.alarm.dampeningType === 'Active time'){
      $scope.alarm.dampeningVar1 = $scope.alarm.dampeningTime * $scope.alarm.dampeningTimeConstant;
    }
    
      $scope.saveProgress = true;
    if($stateParams.id){
      AlarmsFactory.update($scope.alarm,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("alarmsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }else{
      AlarmsFactory.create($scope.alarm,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("alarmsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }
  }
});
