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
myControllerModule.controller('TimersController', function(alertService,
$scope, TimersFactory, $state, $uibModal, $stateParams, displayRestError, mchelper, CommonServices, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('TIMERS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_TIMERS_SETUP');
  $scope.noItemsSystemIcon = "fa fa-clock-o";

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
    TimersFactory.getAll($scope.query, function(response) {
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
        id: 'timerType',
        title:  $filter('translate')('TIMER_TYPE'),
        placeholder: $filter('translate')('FILTER_BY_RESOURCE_TYPE'),
        filterType: 'select',
        filterValues: ['Simple','Normal','Cron','Before sunrise','After sunrise','Before sunset','After sunset'],
      },
      {
        id: 'frequency',
        title:  $filter('translate')('FREQUENCY'),
        placeholder: $filter('translate')('FILTER_BY_FREQUENCY'),
        filterType: 'select',
        filterValues: ['Daily','Weekly','Monthly'],
      },
      {
        id: 'enabled',
        title: $filter('translate')('ENABLED'),
        placeholder: $filter('translate')('FILTER_BY_ENABLED'),
        filterType: 'select',
        filterValues: ['True','False'],
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
        id: 'timerType',
        title:  $filter('translate')('TIMER_TYPE'),
        sortType: 'text'
      },
      {
        id: 'frequency',
        title:  $filter('translate')('FREQUENCY'),
        sortType: 'text'
      },
      {
        id: 'lastFire',
        title:  $filter('translate')('LAST_FIRED'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("timersAddEdit",{'id':$scope.itemIds[0]});
    }
  };

  //Clone item
  $scope.clone = function () {
    if($scope.itemIds.length == 1){
      $state.go("timersAddEdit",{'id':$scope.itemIds[0], 'action': 'clone'});
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
      TimersFactory.deleteIds($scope.itemIds, function(response) {
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
      TimersFactory.enableIds($scope.itemIds, function(response) {
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
      TimersFactory.disableIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('ITEMS_DISABLED_SUCCESSFULLY'));
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }
  };


});

myControllerModule.controller('TimersControllerAddEdit', function ($scope, TypesFactory, CommonServices, alertService, TimersFactory, mchelper, $stateParams, $state, $filter, displayRestError) {
  $scope.timer = {};
  $scope.timer.enabled=true;
  $scope.showMeridian = angular.equals(mchelper.cfg.timeFormatSet, "12 hours");
  $scope.cs = CommonServices;

    if($stateParams.id){
      TimersFactory.get({"id":$stateParams.id},function(response) {
        $scope.timer = response;

        //Update Resource Type
        $scope.dspResources = $scope.getResources($scope.timer.resourceType);

        //Update frequency data
        if($scope.timer.timerType === 'Simple'){
          var array = $scope.timer.frequencyData.split(',');
          $scope.rpInterval = array[0]/1000;
          $scope.rpCount = array[1];
        }else if($scope.timer.timerType === 'Cron'){
          $scope.cronFrequencyData = $scope.timer.frequencyData;
        }else{
          if($scope.timer.frequencyType === 'Daily'){
            $scope.dailyFrequencyData = $scope.timer.frequencyData.split(',');
          }else if($scope.timer.frequencyType === 'Weekly'){
            $scope.weeklyFrequencyData = $scope.timer.frequencyData;
          }else if($scope.timer.frequencyType === 'Monthly'){
            $scope.monthlyFrequencyData = $scope.timer.frequencyData;
          }
        }

         //Update payload operations
        if($scope.timer.resourceType !== 'Sensor variable'){
          $scope.updatePayloadOperations($scope.timer.resourceType);
        }

        //Update date
        if($scope.timer.timerType !== 'Simple' || $scope.timer.timerType !== 'Cron'){
          $scope.lTriggerTime = new Date($scope.timer.triggerTime);
        }
        //Update validity from/to
        if($scope.timer.validityFrom){
          $scope.vFromString = $filter('date')($scope.timer.validityFrom, mchelper.cfg.dateFormat, mchelper.cfg.timezone);
          $scope.vFromDate = new Date($scope.timer.validityFrom);
        }
        if($scope.timer.validityTo){
          $scope.vToString = $filter('date')($scope.timer.validityTo, mchelper.cfg.dateFormat, mchelper.cfg.timezone);
          $scope.vToDate = new Date($scope.timer.validityTo);
        }
        //Clone job
        if($stateParams.action === 'clone'){
          $stateParams.id = undefined;
          $scope.timer.id = undefined;
          $scope.timer.name = $scope.timer.name + '-' + $filter('translate')('CLONE');
        }
      },function(error){
        displayRestError.display(error);
      });
  }

  //pre load
  $scope.dailyFrequencyData = [];
  $scope.monthDays = ['00','01','02','03','04','05','06','07','08','09','10','11','12','13','14','15','16','17','18','19','20','21','22','23','24','25','26','27','28','29','30','31'];
  $scope.hours = ['00','01','02','03','04','05','06','07','08','09','10','11','12','13','14','15','16','17','18','19','20','21','22','23'];
  $scope.minutes = ['00','01','02','03','04','05','06','07','08','09','10','11','12','13','14','15','16','17','18','19','20','21','22','23','24','25','26','27','28','29','30','31',
                    '32','33','34','35','36','37','38','39','40','41','42','43','44','45','46','47','48','49','50','51','52','53','54','55','56','57','58','59'];

  $scope.operations = TypesFactory.getOperations();

  $scope.timerTypes = TypesFactory.getTimerTypes();
  $scope.timerFrequencyTypes = TypesFactory.getTimerFrequencies();
  $scope.timerWeekDays = TypesFactory.getTimerWeekDays();

  $scope.resourceTypes = TypesFactory.getResourceTypes({"resourceType": "timer", "isSendPayload":true});

  //Get resources
  $scope.getResources = function(resourceType){
    return CommonServices.getResources(resourceType);
  }

  //Update Payload operations
  $scope.updatePayloadOperations= function(resourceType){
    $scope.payloadOperations = TypesFactory.getPayloadOperations({"resourceType":resourceType});
  }

  //Get trigger time
  $scope.setTriggerTime = function(isDefault){
    if(!$scope.lTriggerTime){
      $scope.lTriggerTime = new Date();
      if(isDefault){
        $scope.lTriggerTime.setHours(00,00,00,00);
      }
    }
    $scope.lTriggerTime.setFullYear(0000,00,00);
  };

  $scope.frequencyData;
  //Update Frequency Data
  $scope.updateFrequencyData = function(value1,value2){
    if($scope.timer.timerType === 'Simple'){
    }else if($scope.timer.timerType === 'Cron'){
      $scope.frequencyData = value1;
    }else{
      if($scope.timer.frequencyType === 'Daily'){
        $scope.frequencyData = value1.join();
      }else if($scope.timer.frequencyType === 'Weekly'){
        $scope.frequencyData = value1;
      }else if($scope.timer.frequencyType === 'Monthly'){
        $scope.frequencyData = value1;
      }
    }
    console.log('FrequencyData:'+$scope.frequencyData);
  };

  //Update daily frequency
  $scope.updateFrequency = function() {
    if($scope.timer.frequencyType === 'Daily' && $scope.dailyFrequencyData.length == 0){
      angular.forEach($scope.timerWeekDays, function(value, key){
        $scope.dailyFrequencyData.push(value.displayName);
      });
    }
  };

  //Convert as display string
  $scope.getDateTimeDisplayFormat = function (newDate) {
    return $filter('date')(newDate, mchelper.cfg.dateFormat, mchelper.cfg.timezone);
  };

  //GUI page settings
  if(!$stateParams.action || $stateParams.action !== 'clone'){
      $scope.showHeaderUpdate = $stateParams.id;
  }
  $scope.headerStringAdd = $filter('translate')('ADD_TIMER');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_TIMER');
  $scope.cancelButtonState = "timersList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  //Save data
  $scope.save = function(){

    //Clear update required values
    $scope.timer.frequencyData = null;

    //Update validity from/to
    if($scope.vFromDate){
      $scope.timer.validityFrom = $scope.vFromDate.getTime();
    }
    if($scope.vToDate){
      $scope.timer.validityTo = $scope.vToDate.getTime();
    }

    //Update Frequency Data
    if($scope.timer.timerType === 'Simple'){
      $scope.timer.frequencyData = ($scope.rpInterval*1000)+','+$scope.rpCount;
    }else if($scope.timer.timerType === 'Cron'){
      $scope.timer.frequencyData = $scope.cronFrequencyData;
    }else{
      if($scope.timer.frequencyType === 'Daily'){
        $scope.timer.frequencyData = $scope.dailyFrequencyData.join();
      }else if($scope.timer.frequencyType === 'Weekly'){
        $scope.timer.frequencyData = $scope.weeklyFrequencyData;
      }else if($scope.timer.frequencyType === 'Monthly'){
        $scope.timer.frequencyData = $scope.monthlyFrequencyData;
      }
    }

    //Update Time
    if($scope.timer.timerType === 'Simple' || $scope.timer.timerType === 'Cron'){
      $scope.timer.triggerTime = null;
      $scope.timer.frequency = null;
    }else{
      if(!$scope.lTriggerTime){
        $scope.lTriggerTime = new Date();
      }
      $scope.lTriggerTime.setFullYear(0000,00,00);
      //set seconds to zero until the issue resolved >> https://github.com/mycontroller-org/mycontroller/issues/214
      $scope.lTriggerTime.setSeconds(00);
      $scope.timer.triggerTime = $scope.lTriggerTime.getTime();
    }
      $scope.saveProgress = true;
    if($stateParams.id){
      TimersFactory.update($scope.timer,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("timersList");
      },function(error){
        $scope.saveProgress = false;
        displayRestError.display(error);
      });
    }else{
      TimersFactory.create($scope.timer,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("timersList");
      },function(error){
        $scope.saveProgress = false;
        displayRestError.display(error);
      });
    }
  }

});
