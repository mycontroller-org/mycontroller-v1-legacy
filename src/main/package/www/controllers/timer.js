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
$scope, $filter, TimersFactory, $location, $modal, $stateParams, displayRestError) {
  
  $scope.sensor = TimersFactory.getSensorData({"id":$stateParams.id});
    
  $scope.filteredList=[];
  $scope.orgList=[];
  $scope.config = {
    itemsPerPage: 10,
    maxPages:10,
    fillLastPage: false
  }

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
  

  
  //Add new
  $scope.add = function (size) {
    var addModalInstance = $modal.open({
    templateUrl: 'partials/timer/addModal.html',
    controller: 'TMaddController',
    size: size,
    resolve: {sensor: function () {return $scope.sensor;}}
    });

    addModalInstance.result.then(function (newTimer) {
      TimersFactory.create(newTimer,function(response) {
        alertService.success("Added a timer[Name:"+newTimer.name+"]");
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
    
  //Delete timer
  $scope.delete = function (timer, size) {
    var modalInstance = $modal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'TMdeleteController',
    size: size,
    resolve: {
      timer: function () {return timer;}
      }
    });
    modalInstance.result.then(function (selectedTimer) {
      TimersFactory.delete({id: selectedTimer.id},function(response) {
        alertService.success("Deleted a Timer["+selectedTimer.name+"]");
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
    var editModalInstance = $modal.open({
    templateUrl: 'partials/timer/updateModal.html',
    controller: 'TMupdateController',
    size: size,
    resolve: {timer: function () {return timer;}}
    });

    editModalInstance.result.then(function (updateTimer) {
      TimersFactory.update(updateTimer,function(response) {
        alertService.success("Updated a timer["+updateTimer.name+"]");
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
});

myControllerModule.controller('TMaddController', function ($sce, $filter, $scope, $modalInstance, TypesFactory, sensor) {
  $scope.timer = {};
  $scope.timer.enabled=true;
  $scope.timer.sensor = {};
  $scope.timer.frequencyData = [];
  $scope.timer.sensor.id = sensor.id;
  $scope.header = "Add Timer for '"+sensor.nameWithNode+"'";
  $scope.timerDays = TypesFactory.getTimerDays({allDays:true});
  $scope.timerFrequencies = TypesFactory.getTimerFrequencies();
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
        return $filter('date')(newDate, "dd-MMM-yyyy, HH:mm");
  };
  
  $scope.timeOnChange = function (hour,minute,second) {
        $scope.timer.timeString = hour + ":" + minute + ":" + second;
  };
      
  $scope.htmlTooltipCron = $sce.trustAsHtml('<p align="left">Examples:<br>0 15 10 ? * 6#3 - Fire at 10:15am on the third Friday of every month<br>0 0/5 14 * * ? - Fire every 5 minutes starting at 2pm and ending at 2:55pm, every day<br><table><thead><tr><th>Field Name</th><th>Mandatory</th><th>Allowed Values</th><th>Allowed Special Characters</th></tr></thead><tbody><tr><td>Seconds</td><td>YES</td><td>0-59</td><td>, - * /</td></tr><tr><td>Minutes</td><td>YES</td><td>0-59</td><td>, - * /</td></tr><tr><td>Hours</td><td>YES</td><td>0-23</td><td>, - * /</td></tr><tr><td>Day of month</td><td>YES</td><td>1-31</td><td>, - * ? / L W<br clear="all"></td></tr><tr><td>Month</td><td>YES</td><td>1-12 or JAN-DEC</td><td>, - * /</td></tr><tr><td>Day of week</td><td>YES</td><td>1-7 or SUN-SAT</td><td>, - * ? / L #</td></tr><tr><td>Year</td><td>NO</td><td>empty, 1970-2099</td><td>, - * /</td></tr></tbody></table><br>*  - all values<br>?  - no specific value<br>-  - used to specify ranges<br>,  - used to specify additional values<br>/  - used to specify increments<br>L  - last - ex:the last day of the month<br>W  - weekday<br>#  - used to specify "the nth" XXX day of the month, ex: "6#3" - the third Friday of the month</p>');
        
  $scope.add = function() {$modalInstance.close($scope.timer); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Delete Modal
myControllerModule.controller('TMdeleteController', function ($scope, $modalInstance, $sce, timer) {
  $scope.header = "Delete a Timer";
  $scope.deleteMsg = $sce.trustAsHtml("<b>Warning!</b> You are about to delete an timer"
    +"<br>Deletion process will remove complete trace of this timer!" 
    +"<br>Click 'Delete' to proceed."
    +"<br><I>Timer: [Name:"+timer.name+"]</I>");
  $scope.remove = function() {
    $modalInstance.close(timer);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('TMupdateController', function ($scope, $modalInstance, timer, TypesFactory) {
  $scope.timer = timer;
  $scope.header = "Update Timer : "+timer.name;
  $scope.sensorValueTypes = TypesFactory.getSensorValueTypes();
  //This should be changed in good way. variable3 is String and in select option values in int not matching
  if($scope.timer.type == 0){
    $scope.timer.variable2 = parseInt($scope.timer.variable2);
    $scope.timer.variable1 = parseInt($scope.timer.variable1);
  }
  $scope.nodes = TypesFactory.getNodes();
  $scope.sensors = TypesFactory.getSensors({id: $scope.timer.variable2});
  //Updated sensors for add/edit payload
  $scope.refreshSensors = function(nodeId){
      return TypesFactory.getSensors({id: nodeId});
  };
  $scope.update = function() {$modalInstance.close(timer);}
  $scope.update = function() {$modalInstance.close(timer);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
