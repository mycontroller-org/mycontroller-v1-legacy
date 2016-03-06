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
myControllerModule.controller('AlarmsControllerAddEdit', function ($scope, $stateParams, $state, GatewaysFactory, NodesFactory, SensorsFactory, TypesFactory, AlarmsFactory, mchelper, alertService, displayRestError, $filter, CommonServices) {
 
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.item.alarmDefinition = {};
  $scope.item.alarmDefinition.ignoreDuplicate = true;
  $scope.item.alarmDefinition.enabled = true;
  $scope.item.alarmDefinition.disableWhenTrigger = false;
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
      $scope.updateThresholdValueTypes($scope.item.alarmDefinition.resourceType);
      return null;
    }else{
      return null;
    }
  }
  
  //Update trigger types
  $scope.updateTriggerTypes = function(resourceType){
    $scope.alarmTriggerTypes = TypesFactory.getAlarmTriggerTypes({"resourceType":resourceType}); 
  }
  
  //Update Threshold types value
  $scope.updateThresholdValueTypes= function(resourceType){
    $scope.stateTypes = TypesFactory.getStateTypes({"resourceType":resourceType}); 
  }

  //Update Payload operations
  $scope.updatePayloadOperations= function(resourceType){
    $scope.payloadOperations = TypesFactory.getPayloadOperations({"resourceType":resourceType}); 
  }

  
  if($stateParams.id){
    AlarmsFactory.get({"id":$stateParams.id},function(response) {
        $scope.item = response;

        //Update trigger types
        $scope.updateTriggerTypes($scope.item.alarmDefinition.resourceType);
        
        //Update Resource Type
        $scope.rsResourcesList = $scope.getResources($scope.item.alarmDefinition.resourceType);        
        
        //Update Threshold Type        
        if($scope.item.alarmDefinition.thresholdType === 'Sensor variable'){
          $scope.thResourcesList = $scope.getResources($scope.item.alarmDefinition.thresholdType);
          $scope.item.alarmDefinition.thresholdValue = parseInt($scope.item.alarmDefinition.thresholdValue);
        }else{
          $scope.updateThresholdValueTypes($scope.item.alarmDefinition.resourceType);
        }
        
        //Update delay time
        if($scope.item.alarmDefinition.variable4){
          $scope.item.alarmDefinition.variable4 = $scope.item.alarmDefinition.variable4/1000;
        }

        //Update dampening value
        if($scope.item.alarmDefinition.dampeningType === 'Active time'){
          if($scope.item.alarmDefinition.dampeningVar1 % 86400000  == 0){
            $scope.item.alarmDefinition.dampeningTime = $scope.item.alarmDefinition.dampeningVar1 / 86400000;
            $scope.item.alarmDefinition.dampeningTimeConstant = "86400000";
          }else if($scope.item.alarmDefinition.dampeningVar1 % 3600000  == 0){
            $scope.item.alarmDefinition.dampeningTime = $scope.item.alarmDefinition.dampeningVar1 / 3600000;
            $scope.item.alarmDefinition.dampeningTimeConstant = "3600000";
          }else if($scope.item.alarmDefinition.dampeningVar1 % 60000  == 0){
            $scope.item.alarmDefinition.dampeningTime = $scope.item.alarmDefinition.dampeningVar1 / 60000;
            $scope.item.alarmDefinition.dampeningTimeConstant = "60000";
          }else{
            $scope.item.alarmDefinition.dampeningTime = $scope.item.alarmDefinition.dampeningVar1;
            $scope.item.alarmDefinition.dampeningTimeConstant = "1000";
          }
          $scope.item.alarmDefinition.dampeningVar1 = $scope.item.alarmDefinition.dampeningTime * $scope.item.alarmDefinition.dampeningTimeConstant;
        }
      },function(error){
        displayRestError.display(error);
      });
  }

  //--------------pre load -----------
  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "Alarm definition"});
  $scope.spResourceTypes = TypesFactory.getResourceTypes({"resourceType": "Alarm definition", "isSendPayload":true});
  $scope.alarmThresholdTypes = TypesFactory.getAlarmThresholdTypes();
  $scope.dampeningTypes = TypesFactory.getAlarmDampeningTypes();
  $scope.notifications = TypesFactory.getNotifications();

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_ALARM');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_ALARM');
  $scope.cancelButtonState = "alarmsList"; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.save = function(){
    //Update Threshold type
    if(!$scope.item.alarmDefinition.thresholdType){
      $scope.item.alarmDefinition.thresholdType = 'Value';
    }
    
    //Update dampening value
    if($scope.item.alarmDefinition.dampeningType === 'Active time'){
      $scope.item.alarmDefinition.dampeningVar1 = $scope.item.alarmDefinition.dampeningTime * $scope.item.alarmDefinition.dampeningTimeConstant;
    }
    $scope.saveProgress = true;

    if($stateParams.id){
      AlarmsFactory.update($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("alarmsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }else{
      AlarmsFactory.create($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("alarmsList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }
  }
});
