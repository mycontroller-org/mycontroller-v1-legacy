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
myControllerModule.controller('AlarmController', function(alertService,
$scope, $filter, AlarmsFactory, $location, $modal, $stateParams, displayRestError, about) {
  
  $scope.sensor = AlarmsFactory.getSensorData({"id":$stateParams.id});
    
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
  $scope.getAlarms = function(){
     for (var sId=0; sId<$scope.filteredList.length; sId++){
       $scope.filteredList[sId] = AlarmsFactory.get({"id":$scope.filteredList[sId].id});
     }
  }
    
  // Call and Run function every second
  $scope.orgList = AlarmsFactory.getAll({"id":$stateParams.id}, function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  //setInterval($scope.getAlarms, 1000*30);
  

  
    //Add a Node
  $scope.add = function (size) {
    var addModalInstance = $modal.open({
    templateUrl: 'partials/alarm/addModal.html',
    controller: 'AMaddController',
    size: size,
    resolve: {sensor: function () {return $scope.sensor;}}
    });

    addModalInstance.result.then(function (newAlarm) {
      AlarmsFactory.create(newAlarm,function(response) {
        alertService.success("Added an alarm[Name:"+newAlarm.name+"]");
        //Update display table
        $scope.orgList = AlarmsFactory.getAll({"id":$stateParams.id}, function(response) {
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
    
  //Delete alarm
  $scope.delete = function (alarm, size) {
    var modalInstance = $modal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'AMdeleteController',
    size: size,
    resolve: {
      alarm: function () {return alarm;}
      }
    });
    modalInstance.result.then(function (selectedAlarm) {
      AlarmsFactory.delete({id: selectedAlarm.id},function(response) {
        alertService.success("Deleted an Alarm["+selectedAlarm.name+"]");
        //Update display table
        $scope.orgList = AlarmsFactory.getAll({"id":$stateParams.id}, function(response) {
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
    
    //Update an alarm
  $scope.update = function (alarm, size) {
    var editModalInstance = $modal.open({
    templateUrl: 'partials/alarm/updateModal.html',
    controller: 'AMupdateController',
    size: size,
    resolve: {alarm: function () {return alarm;}}
    });

    editModalInstance.result.then(function (updateAlarm) {
      AlarmsFactory.update(updateAlarm,function(response) {
        alertService.success("Updated an alarm["+updateAlarm.name+"]");
        //Update display table
        $scope.orgList = AlarmsFactory.getAll({"id":$stateParams.id}, function(response) {
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

myControllerModule.controller('AMaddController', function ($sce, $scope, $modalInstance, TypesFactory, sensor) {
  $scope.htmlTooltipSplOper = $sce.trustAsHtml('<p align="left">For Special Operations:<br>All the operations done with last sensor value<br><table><thead><tr><th>Operation</th><th>Value</th><th>Example</th><th style="text-align: center;">Result</th></tr></thead><tbody><tr><td style="padding:0 5px 0 5px;">Invert</td><td style="padding:0 5px 0 5px;">!</td><td style="padding:0 5px 0 5px;">!</td><td style="padding:0 5px 0 5px;">!{sensor.value}</td></tr><tr><td style="padding:0 5px 0 5px;">Increment</td><td style="padding:0 5px 0 5px;">++</td><td style="padding:0 5px 0 5px;">++</td><td style="padding:0 5px 0 5px;">++{sensor.value}</td></tr><tr><td style="padding:0 5px 0 5px;">Decrement</td><td style="padding:0 5px 0 5px;">--</td><td style="padding:0 5px 0 5px;">--</td><td style="padding:0 5px 0 5px;">--{sensor.value}</td></tr><tr><td style="padding:0 5px 0 5px;">Addition</td><td style="padding:0 5px 0 5px;">+{user.value}</td><td style="padding:0 5px 0 5px;">+2</td><td style="padding:0 5px 0 5px;">{sensor.value}+2</td></tr><tr><td style="padding:0 5px 0 5px;">Subtraction</td><td style="padding:0 5px 0 5px;">-{user.value}</td><td style="padding:0 5px 0 5px;">-5</td><td style="padding:0 5px 0 5px;">{sensor.value}-5</td></tr><tr><td style="padding:0 5px 0 5px;">Multiplication</td><td style="padding:0 5px 0 5px;">*{user.value}</td><td style="padding:0 5px 0 5px;">*2</td><td style="padding:0 5px 0 5px;">{sensor.value}*2</td></tr><tr><td style="padding:0 5px 0 5px;">Division</td><td style="padding:0 5px 0 5px;">/{user.value}</td><td style="padding:0 5px 0 5px;">/9</td><td style="padding:0 5px 0 5px;">{sensor.value}/9</td></tr><tr><td style="padding:0 5px 0 5px;">Modulus</td><td style="padding:0 5px 0 5px;">%{user.value}</td><td style="padding:0 5px 0 5px;">%4</td><td style="padding:0 5px 0 5px;">{sensor.value}%4</td></tr><tr><td style="padding:0 5px 0 5px;">Reboot</td><td style="padding:0 5px 0 5px;">reboot</td><td style="padding:0 5px 0 5px;">reboot</td><td style="padding:0 5px 0 5px;">Reboot Node</td></tr></tbody></table><br><b><I>Note: Space not allowed</b></I><br></p>');
  $scope.alarm = {};
  $scope.alarm.enabled=true;
  $scope.alarm.sensor = {};
  $scope.alarm.sensor.node = {};
  $scope.alarm.sensor.id = sensor.id;
  $scope.alarm.ignoreDuplicate = true;
  $scope.header = "Add Alarm for '"+sensor.nameWithNode+"'";
  $scope.alarmNotifications = TypesFactory.getAlarmTypes();
  $scope.sensorvalueTypes = TypesFactory.getSensorValueTypes();
  $scope.alarmTriggers = TypesFactory.getAlarmTriggers();
  $scope.alarmDampeningTypes = TypesFactory.getAlarmDampeningTypes();
  $scope.nodes = TypesFactory.getNodes();
      //Updated sensors for add/edit payload
  $scope.refreshSensors = function(nodeId){
      return TypesFactory.getSensors({id: nodeId});
  };
  $scope.add = function() {$modalInstance.close($scope.alarm); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Delete Modal
myControllerModule.controller('AMdeleteController', function ($scope, $modalInstance, $sce, alarm) {
  $scope.header = "Delete an Alarm";
  $scope.deleteMsg = $sce.trustAsHtml("<b>Warning!</b> You are about to delete an alarm"
    +"<br>Deletion process will remove complete trace of this alarm!" 
    +"<br>Click 'Delete' to proceed."
    +"<br><I>Alarm: [Name:"+alarm.name+"]</I>");
  $scope.remove = function() {
    $modalInstance.close(alarm);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('AMupdateController', function ($sce, $scope, $modalInstance, alarm, TypesFactory, SensorsFactory) {
  $scope.htmlTooltipSplOper = $sce.trustAsHtml('<p align="left">For Special Operations:<br>All the operations done with last sensor value<br><table><thead><tr><th>Operation</th><th>Value</th><th>Example</th><th style="text-align: center;">Result</th></tr></thead><tbody><tr><td style="padding:0 5px 0 5px;">Invert</td><td style="padding:0 5px 0 5px;">!</td><td style="padding:0 5px 0 5px;">!</td><td style="padding:0 5px 0 5px;">!{sensor.value}</td></tr><tr><td style="padding:0 5px 0 5px;">Increment</td><td style="padding:0 5px 0 5px;">++</td><td style="padding:0 5px 0 5px;">++</td><td style="padding:0 5px 0 5px;">++{sensor.value}</td></tr><tr><td style="padding:0 5px 0 5px;">Decrement</td><td style="padding:0 5px 0 5px;">--</td><td style="padding:0 5px 0 5px;">--</td><td style="padding:0 5px 0 5px;">--{sensor.value}</td></tr><tr><td style="padding:0 5px 0 5px;">Addition</td><td style="padding:0 5px 0 5px;">+{user.value}</td><td style="padding:0 5px 0 5px;">+2</td><td style="padding:0 5px 0 5px;">{sensor.value}+2</td></tr><tr><td style="padding:0 5px 0 5px;">Subtraction</td><td style="padding:0 5px 0 5px;">-{user.value}</td><td style="padding:0 5px 0 5px;">-5</td><td style="padding:0 5px 0 5px;">{sensor.value}-5</td></tr><tr><td style="padding:0 5px 0 5px;">Multiplication</td><td style="padding:0 5px 0 5px;">*{user.value}</td><td style="padding:0 5px 0 5px;">*2</td><td style="padding:0 5px 0 5px;">{sensor.value}*2</td></tr><tr><td style="padding:0 5px 0 5px;">Division</td><td style="padding:0 5px 0 5px;">/{user.value}</td><td style="padding:0 5px 0 5px;">/9</td><td style="padding:0 5px 0 5px;">{sensor.value}/9</td></tr><tr><td style="padding:0 5px 0 5px;">Modulus</td><td style="padding:0 5px 0 5px;">%{user.value}</td><td style="padding:0 5px 0 5px;">%4</td><td style="padding:0 5px 0 5px;">{sensor.value}%4</td></tr><tr><td style="padding:0 5px 0 5px;">Reboot</td><td style="padding:0 5px 0 5px;">reboot</td><td style="padding:0 5px 0 5px;">reboot</td><td style="padding:0 5px 0 5px;">Reboot Node</td></tr></tbody></table><br><b><I>Note: Space not allowed</b></I><br></p>');
  $scope.alarm = alarm;
  $scope.header = "Update Alarm : "+alarm.name;
  $scope.payLoadsensor = {};
  $scope.alarmTriggers = TypesFactory.getAlarmTriggers();
  $scope.sensorvalueTypes = TypesFactory.getSensorValueTypes();
  $scope.alarmDampeningTypes = TypesFactory.getAlarmDampeningTypes();
  //This should be changed in good way. variable3 is String and in select option values in int not matching
  if($scope.alarm.type == 0){
    $scope.alarm.variable1 = parseInt($scope.alarm.variable1);
    $scope.payLoadsensor  = SensorsFactory.getByRefId({sensorRefId: $scope.alarm.variable1}, function(response) {
        $scope.sensors = TypesFactory.getSensors({id: $scope.alarm.sensor.node.id});
        },function(error){
          displayRestError.display(error);            
        });
  }  
  if($scope.alarm.dampeningType == 1 || $scope.alarm.dampeningType == 2){
    $scope.alarm.dampeningVar1 = parseInt($scope.alarm.dampeningVar1);
  } 
  if($scope.alarm.dampeningType == 2){
    $scope.alarm.dampeningVar2 = parseInt($scope.alarm.dampeningVar2);
  }  
  
  $scope.nodes = TypesFactory.getNodes();
  //$scope.sensors = TypesFactory.getSensors({id: $scope.alarm.sensor.node.id});
  //Updated sensors for add/edit payload
  $scope.refreshSensors = function(nodeId){
      return TypesFactory.getSensors({id: nodeId});
  };
  $scope.update = function() {$modalInstance.close(alarm);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
