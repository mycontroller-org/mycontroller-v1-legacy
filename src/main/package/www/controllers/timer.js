/*
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
myControllerModule.controller('TimerController', function(alertService,
$scope, $filter, TimersFactory, $location, $uibModal, $stateParams, displayRestError, about, $filter) {
  
  $scope.sensor = TimersFactory.getSensorData({"id":$stateParams.id});
    
  $scope.filteredList=[];
  $scope.orgList=[];
  $scope.config = {
    itemsPerPage: 10,
    maxPages:10,
    fillLastPage: false
  }
  //about, Timezone, etc.,
  $scope.about = about;


  //Filter
  $scope.updateFilteredList = function() {
    $scope.filteredList = $filter("filter")($scope.orgList, $scope.query);
  };
  
  //Get list of Sensors
  $scope.getTimers = function(){
     for (var sId=0; sId<$scope.filteredList.length; sId++){
       $scope.filteredList[sId] = TimersFactory.get({"id":$scope.filteredList[sId].id});
     }
  }
    
  // Call and Run function every second
  $scope.orgList = TimersFactory.getAll({"id":$stateParams.id},function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  //setInterval($scope.getTimers, 1000*30);
  
  //Update display table
  $scope.updateDisplayTable = function(){
    TimersFactory.getAll({"id":$stateParams.id}, function(response) {
      $scope.orgList = response;
      $scope.filteredList = $scope.orgList;    
    },function(error){
        displayRestError.display(error);            
      });
  };
  
  // Enable/Disable Timer
  $scope.enableDisable = function(timer){
    if(timer.enabled){
      timer.enabled = false;
    }else{
      timer.enabled = true;
    }
    if(timer.type != 1 && timer.frequency == 0){
      timer.frequencyData = timer.frequencyData.split(',');
    }else{
      timer.frequencyDataString = timer.frequencyData ;
      timer.frequencyData = null; 
    }
    $scope.updateTimer(timer);
  };

  // Update timer
  $scope.updateTimer = function(updateTimer) {
    TimersFactory.update(updateTimer,function(response) {
        alertService.success($filter('translate')('TIMER.NOTIFY_UPDATED', name));
        //Update display table
        $scope.updateDisplayTable();
      },function(error){
        displayRestError.display(error);            
      });
  };
  
  //Add new
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/timer/addModal.html',
    controller: 'TMaddController',
    size: size,
    resolve: {sensor: function () {return $scope.sensor;}}
    });

    addModalInstance.result.then(function (newTimer) {
      TimersFactory.create(newTimer,function(response) {
        alertService.success($filter('translate')('TIMER.NOTIFY_ADDED', name));
        //Update display table
        $scope.updateDisplayTable();
      },function(error){
        displayRestError.display(error);            
      });
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
    
  //Delete timer
  $scope.delete = function (timer, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'TMdeleteController',
    size: size,
    resolve: {
      timer: function () {return timer;}
      }
    });
    modalInstance.result.then(function (selectedTimer) {
      TimersFactory.delete({id: selectedTimer.id},function(response) {
        alertService.success($filter('translate')('TIMER.NOTIFY_DELETED', name));
        //Update display table
        $scope.orgList = TimersFactory.getAll({"id":$stateParams.id}, function(response) {
        },function(error){
          displayRestError.display(error);            
        });
        $scope.filteredList = $scope.orgList;
      },function(error){
        displayRestError.display(error);            
      });    
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
    
    //Update an timer
  $scope.update = function (timer, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/timer/updateModal.html',
    controller: 'TMupdateController',
    size: size,
    resolve: {timer: function () {return timer;}, sensor: function () {return $scope.sensor;}}
    });

    editModalInstance.result.then(function (updateTimer) {
      $scope.updateTimer(updateTimer);
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
});

myControllerModule.controller('TMaddController', function ($sce, $filter, $scope, $modalInstance, TypesFactory, sensor, about, $filter) {
  $scope.timer = {};
  $scope.timer.enabled=true;
  $scope.timer.sensor = {};
  $scope.timer.frequencyData = [];
  $scope.timer.sensor.id = sensor.id;
  $scope.header = $filter('translate')('TIMER.TITLE_NEW_TIMER',nameWithNode);
  $scope.header = "New Timer for '"+sensor.nameWithNode+"'";
  $scope.timerDays = TypesFactory.getTimerDays({allDays:true});
  $scope.timerFrequencies = TypesFactory.getTimerFrequencies();
  $scope.variableTypes = TypesFactory.getSensorVariableTypes({id:sensor.type});
  $scope.timerTypes = TypesFactory.getTimerTypes();
  $scope.monthDays = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31];
  $scope.hours = [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23];
  $scope.minutes = [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59];
  $scope.timer.frequencyData = [0,1,2,3,4,5,6];
  $scope.updateWeekDays= function(){
    $scope.timer.frequencyData = [];
    if($scope.weekDaysOutput.length >0){
        angular.forEach($scope.weekDaysOutput, function(key, value) {
          $scope.timer.frequencyData.push(key.id);
        });
      }else{
        $scope.timer.frequencyData = [0,1,2,3,4,5,6];
      } 
  }
  $scope.onFrequencyChange = function(){
    if($scope.timer.frequency == 0){
      $scope.updateWeekDays();
      $scope.timer.frequencyDataString = null;
    }else{
      $scope.timer.frequencyData = null;
    }    
  }  
    
  $scope.timer.timeString = "0:0:0"; 
  $scope.validityOnTimeSet = function (newDate) {
        return $filter('date')(newDate, about.dateFormat);
  };
  
  $scope.timeOnChange = function (hour,minute,second) {
        $scope.timer.timeString = hour + ":" + minute + ":" + second;
  };
  $scope.htmlTooltipCron = $filter('translate')('TIMER.TOOLTIP_CRON');      
  $scope.htmlTooltipCron = $filter('translate')('TIMER.TOOLTIP_SPL_OPER');
  
  $scope.add = function() {$modalInstance.close($scope.timer); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Delete Modal
myControllerModule.controller('TMdeleteController', function ($scope, $modalInstance, $sce, timer, $filter) {
  $scope.header = $filter('translate')('TIMER.TITLE_DELETE',name);
  $scope.deleteMsg = $filter('translate')('TIMER.MESSAGE_DELETE', name);
  $scope.remove = function() {
    $modalInstance.close(timer);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('TMupdateController', function ($sce, $filter, $scope, $modalInstance, TypesFactory, sensor, timer, about, $filter) {
  $scope.timer = timer;
  $scope.header = $filter('translate')('TIMER.TITLE_MODIFY',name);
  $scope.header = "Modify Timer : '"+timer.name+"'";
  $scope.timerDays = TypesFactory.getTimerDays();
  $scope.timerFrequencies = TypesFactory.getTimerFrequencies();
  $scope.variableTypes = TypesFactory.getSensorVariableTypes({id:sensor.type});
  $scope.timerTypes = TypesFactory.getTimerTypes();
  $scope.monthDays = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31];
  $scope.hours = [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23];
  $scope.minutes = [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59];
  
  $scope.timerTypes.$promise.then(function () {    
    var frequencyData = $scope.timer.frequencyData;
    $scope.timer.frequencyData = [0,1,2,3,4,5,6];
  
      
    if($scope.timer.type != 1){
      if($scope.timer.frequency == 0){
        $scope.timer.frequencyData = frequencyData.split(',');
      }else if(timer.frequency == 1){
        $scope.timer.frequencyDataString = parseInt(frequencyData);
      }else if(timer.frequency == 2){
        $scope.timer.frequencyDataString = parseInt(frequencyData);
      }
    }else{
        $scope.timer.frequencyDataString = frequencyData ;
    }
    
    
    angular.forEach($scope.timerDays, function(value, key){
      angular.forEach($scope.timer.frequencyData, function(dayId, keySub){
        if(dayId == value.id){
          value.ticked = true;
        }
      });
    });
    
    //Update From/To validity  
    $scope.timer.validFromString = $filter('date')($scope.timer.validFrom, about.dateFormat);
    $scope.timer.validToString = $filter('date')($scope.timer.validTo, about.dateFormat);
    
    $scope.validFrom = $scope.timer.validFrom;
    $scope.validTo = $scope.timer.validTo;
    
    //Set these values to null
    $scope.timer.validFrom = null;
    $scope.timer.validTo = null;
    
    //Update trigger time
    $scope.triggerHour = parseInt($filter('date')($scope.timer.time, "HH"));
    $scope.triggerMinute = parseInt($filter('date')($scope.timer.time, "mm"));
    $scope.triggerSecond = parseInt($filter('date')($scope.timer.time, "ss"));  
    $scope.timer.timeString = $filter('date')($scope.timer.time, "HH:mm:ss"); 
    
    $scope.updateWeekDays= function(){
      $scope.timer.frequencyData = [];
      if($scope.weekDaysOutput.length >0){
          angular.forEach($scope.weekDaysOutput, function(key, value) {
            $scope.timer.frequencyData.push(key.id);
          });
        }else{
          $scope.timer.frequencyData = [0,1,2,3,4,5,6];
        } 
    }
    $scope.onFrequencyChange = function(){
      if($scope.timer.frequency == 0){
        $scope.updateWeekDays();
        $scope.timer.frequencyDataString = null;
      }else{
        $scope.timer.frequencyData = null;
      }    
    }  
      
    $scope.validityOnTimeSet = function (newDate) {
          return $filter('date')(newDate, about.dateFormat);
    };
    
    $scope.timeOnChange = function (hour,minute,second) {
          $scope.timer.timeString = hour + ":" + minute + ":" + second;
    };
        
    $scope.htmlTooltipCron = $filter('translate')('TIMER.TOOLTIP_CRON');
    $scope.htmlTooltipCron = $filter('translate')('TIMER.TOOLTIP_SPL_OPER');
    
    $scope.update = function() {$modalInstance.close($scope.timer); }
    $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
  });
});
