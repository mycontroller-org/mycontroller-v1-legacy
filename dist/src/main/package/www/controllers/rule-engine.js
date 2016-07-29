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
myControllerModule.controller('RuleEngineController', function(alertService,
$scope, RulesFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('RULES_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_RULES_SETUP');
  $scope.noItemsSystemIcon = "fa fa-cogs";

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
    RulesFactory.getAll($scope.query, function(response) {
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
        id: 'resourceType',
        title:  $filter('translate')('RESOURCE_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_RESOURCE_TYPE'),
        filterType: 'select',
        filterValues: ['Gateway','Node','Sensor variable','Resources group','Script'],
      },
      {
        id: 'enabled',
        title: $filter('translate')('ENABLED'),
        placeholder: $filter('translate')('FILTER_BY_ENABLED'),
        filterType: 'select',
        filterValues: ['True','False'],
      },
      {
        id: 'conditionType',
        title: $filter('translate')('CONDITION_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_CONDITION_TYPE'),
        filterType: 'select',
        filterValues: ['Threshold','Threshold range','Compare','State','String','Script'],
      },
      {
        id: 'dampeningType',
        title:  $filter('translate')('DAMPENING_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_DAMPENING_TYPE'),
        filterType: 'select',
        filterValues: ['None','Consecutive','Last N evaluations','Active time'],
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
        id: 'conditionType',
        title:  $filter('translate')('CONDITION_TYPE'),
        sortType: 'text'
      },
      {
        id: 'dampeningType',
        title:  $filter('translate')('DAMPENING_TYPE'),
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
      RulesFactory.deleteIds($scope.itemIds, function(response) {
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
      RulesFactory.enableIds($scope.itemIds, function(response) {
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
      RulesFactory.disableIds($scope.itemIds, function(response) {
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
      $state.go("rulesAddEdit",{'id':$scope.itemIds[0]});
    }
  };

  //Clone item
  $scope.clone = function () {
    if($scope.itemIds.length == 1){
      $state.go("rulesAddEdit",{'id':$scope.itemIds[0], 'action': 'clone'});
    }
  };

});


//Add Edit alarm defination controller
myControllerModule.controller('RuleEngineControllerAddEdit', function ($scope, $stateParams, $state, GatewaysFactory, NodesFactory, SensorsFactory, TypesFactory, RulesFactory, ScriptsFactory,
  mchelper, alertService, displayRestError, $filter, CommonServices) {

  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.item.ignoreDuplicate = true;
  $scope.item.enabled = true;
  $scope.item.disableWhenTrigger = false;
  $scope.cs = CommonServices;

  // Update resources list
  $scope.getResources= function(resourceType, filterValue){
    if(resourceType === 'Sensor variable'){
      return TypesFactory.getSensorVariables({'metricType':filterValue});
    }else if(resourceType === 'Gateway' || resourceType === 'Gateway state'){
      return TypesFactory.getGateways();
    }else if(resourceType === 'Node' || resourceType === 'Node state'){
      return TypesFactory.getNodes();
    }else if(resourceType === 'Resources group'){
      return TypesFactory.getResourcesGroups();
    }else if(resourceType === 'Rule definition'){
      return TypesFactory.getRuleDefinitions();
    }else if(resourceType === 'Timer'){
      return TypesFactory.getTimers();
    }else if(resourceType === 'Value'){
      $scope.updateThresholdValueTypes($scope.item.resourceType);
      return null;
    }else{
      return null;
    }
  }

  //Update operator types
  $scope.getOperatorTypes = function(resourceType){
    return TypesFactory.getRuleOperatorTypes({"resourceType":resourceType});
  }

  //Update State types
  $scope.updateStateTypes= function(resourceType){
    $scope.stateTypes = TypesFactory.getStateTypes({"resourceType":resourceType});
  }

  //Update Payload operations
  $scope.updatePayloadOperations= function(resourceType){
    $scope.payloadOperations = TypesFactory.getPayloadOperations({"resourceType":resourceType});
  }

  //Update on condition type change
  $scope.updateOnConditionTypChange = function(){
    $scope.item.resourceType = '';
    $scope.item.resourceId = '';
    if($scope.item.conditionType === 'Threshold'
      || $scope.item.conditionType === 'Threshold range'
      || $scope.item.conditionType === 'Compare'
      || $scope.item.conditionType === 'String'){
      $scope.item.resourceType = 'Sensor variable';
    }
    if($scope.item.conditionType === 'Threshold'){
      $scope.sensorVariablesList = TypesFactory.getSensorVariables({"metricType":"Double"});
      $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
      $scope.ruleThresholdDataTypes = TypesFactory.getRuleThresholdDataTypes({"resourceType":$scope.item.resourceType});
      $scope.item.operator = '';
      $scope.item.dataType = '';
      $scope.item.data = '';
    }else if($scope.item.conditionType === 'Threshold range'){
      $scope.sensorVariablesList = TypesFactory.getSensorVariables({"metricType":"Double"});
      $scope.item.inRange = true;
      $scope.item.includeOperatorLow = true;
      $scope.item.includeOperatorHigh = true;
      $scope.item.thresholdLow = '';
      $scope.item.thresholdHigh = '';
    }else if($scope.item.conditionType === 'Compare'){
      $scope.sensorVariablesList = TypesFactory.getSensorVariables({"metricType":"Double"});
      $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
      $scope.item.operator = '';
      $scope.item.data2Multiplier = '';
      $scope.item.data2ResourceId = '';
      $scope.item.data2ResourceType = 'Sensor variable';
    }else if($scope.item.conditionType === 'State'){
      $scope.resourceTypes = TypesFactory.getResourceTypes({"conditionType":$scope.item.conditionType});
      $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
      $scope.item.operator = '';
      $scope.item.state = '';
    }else if($scope.item.conditionType === 'String'){
      $scope.sensorVariablesList = TypesFactory.getSensorVariables();
      $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
      $scope.item.ignoreCase = true;
      $scope.item.operator = '';
      $scope.item.pattern = '';
    }else if($scope.item.conditionType === 'Script'){
      $scope.scriptsList = ScriptsFactory.getAllLessInfo({"type":"Condition"});
      $scope.item.resourceId = -1;
      $scope.item.resourceType = 'Script';
    }
  }



  if($stateParams.id){
    RulesFactory.get({"id":$stateParams.id},function(response) {
          $scope.item = response;
          if($scope.item.conditionType === 'Threshold'){
            $scope.sensorVariablesList = TypesFactory.getSensorVariables({"metricType":"Double"});
            $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
            $scope.ruleThresholdDataTypes = TypesFactory.getRuleThresholdDataTypes({"resourceType":$scope.item.resourceType});
          }else if($scope.item.conditionType === 'Threshold range'){
            $scope.sensorVariablesList = TypesFactory.getSensorVariables({"metricType":"Double"});
          }else if($scope.item.conditionType === 'Compare'){
            $scope.sensorVariablesList = TypesFactory.getSensorVariables({"metricType":"Double"});
            $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
            $scope.item.targetResourceType = 'Sensor variable';
          }else if($scope.item.conditionType === 'State'){
            $scope.resourceTypes = TypesFactory.getResourceTypes({"conditionType":$scope.item.conditionType});
            $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
            $scope.updateStateTypes($scope.item.resourceType);
            $scope.stateResourcesList = $scope.getResources($scope.item.resourceType, 'Binary');
          }else if($scope.item.conditionType === 'String'){
            $scope.sensorVariablesList = TypesFactory.getSensorVariables();
            $scope.ruleOperatorTypes = TypesFactory.getRuleOperatorTypes({"conditionType":$scope.item.conditionType});
          }else if($scope.item.conditionType === 'Script'){
            $scope.scriptsList = ScriptsFactory.getAllLessInfo({"type":"Condition"});
            $scope.item.scriptBindings = angular.toJson(response.scriptBindings);
          }


        //Update dampening value
        if($scope.item.dampeningType === 'Active time'){
          if($scope.item.dampening.activeTime % 86400000  == 0){
            $scope.item.dampening.activeTime = $scope.item.dampening.activeTime / 86400000;
            $scope.item.dampening.activeTimeConstant = "86400000";
          }else if($scope.item.dampening.activeTime % 3600000  == 0){
            $scope.item.dampening.activeTime = $scope.item.dampening.activeTime / 3600000;
            $scope.item.dampening.activeTimeConstant = "3600000";
          }else if($scope.item.dampening.activeTime % 60000  == 0){
            $scope.item.dampening.activeTime = $scope.item.dampening.activeTime / 60000;
            $scope.item.dampening.activeTimeConstant = "60000";
          }else{
            $scope.item.dampening.activeTime = $scope.item.dampening.activeTime / 1000;
            $scope.item.dampening.activeTimeConstant = "1000";
          }
        }
        if($stateParams.action === 'clone'){
          $stateParams.id = undefined;
          $scope.item.id = undefined;
          $scope.item.name = $scope.item.name + '-' + $filter('translate')('CLONE');
        }
      },function(error){
        displayRestError.display(error);
      });
  }else{
    $scope.item.scriptBindings='{ }';
  }

  //--------------pre load -----------
  $scope.dampeningTypes = TypesFactory.getRuleDampeningTypes();
  $scope.operations = TypesFactory.getOperations();
  $scope.ruleConditionTypes = TypesFactory.getRuleConditionTypes();

  //GUI page settings
  if(!$stateParams.action || $stateParams.action !== 'clone'){
      $scope.showHeaderUpdate = $stateParams.id;
  }
  $scope.headerStringAdd = $filter('translate')('ADD_RULE');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_RULE');
  $scope.cancelButtonState = "rulesList"; //Cancel button url
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.save = function(){
    //Update dampening active time
    if($scope.item.dampeningType === 'Active time'){
      $scope.item.dampening.activeTime = $scope.item.dampening.activeTime * $scope.item.dampening.activeTimeConstant;
    }
    //Change string to JSON string
    if($scope.item.conditionType === 'Script'){
      $scope.item.scriptBindings = angular.fromJson(JSON.stringify(eval('('+$scope.item.scriptBindings+')')));
    }
    $scope.saveProgress = true;

    if($stateParams.id){
      RulesFactory.update($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("rulesList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }else{
      RulesFactory.create($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("rulesList");
      },function(error){
        displayRestError.display(error);
          $scope.saveProgress = false;
      });
    }
  }
});
