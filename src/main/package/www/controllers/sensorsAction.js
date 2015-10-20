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
myControllerModule.controller('SensorsActionController', function(alertService,
$scope, $interval, $filter, SensorsFactory, TypesFactory, $location, $uibModal, displayRestError, about) {
    
  $scope.filteredList=[];
  $scope.orgList=[];
  $scope.config = {
    itemsPerPage: 5,
    fillLastPage: false
  }
  
  //about, Timezone, etc.,
  $scope.about = about;
  
  //Filter
  $scope.updateFilteredList = function() {
    $scope.filteredList = $filter("filter")($scope.orgList, $scope.query);
  };
  
  //Send list of Sensors
  $scope.orgList = {};
  $scope.orgList = SensorsFactory.query(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Get all Nodes
  $scope.nodes = TypesFactory.getNodes();
  
  $scope.nodeChange = function(selectedNodeId){
    $scope.orgList = SensorsFactory.query({nodeId: selectedNodeId}, function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
    $scope.filteredList = $scope.orgList;
  };
 
 //Refresh sensor data
  $scope.refresh = function (sensor) {
    //SensorsFactory.get({ nodeId: sensor.node.id, sensorId: sensor.sensorId });
    return SensorsFactory.get({ nodeId: sensor.node.id, sensorId: sensor.sensorId },function(response) {
        //Nothing to do.
    },function(error){
        displayRestError.display(error);
    });
  }
  
    /*
     * Update only one sensor data
     */
  $scope.updateSensor = function (sensor, updateOnlyStatus) {
    for (var sId=0; sId<$scope.orgList.length; sId++){
       if($scope.orgList[sId].id == sensor.id){
         SensorsFactory.get({ nodeId: sensor.node.id, sensorId: sensor.sensorId },function(response) {
           if(updateOnlyStatus){
              $scope.orgList[sId].status = response.status;
              $scope.orgList[sId].lastSeen = response.lastSeen;
           }else{
              $scope.orgList[sId] = response;
           }
        },function(error){
          displayRestError.display(error);
        });
         break;
       }
     }    
    }
    
    
  //Update all sensors data
  $scope.updateAllSensors = function () {  
   SensorsFactory.query(function(response) {
     $scope.tmpList = response;
     for (var sId=0; sId<$scope.orgList.length; sId++){
      for (var tId=0; tId<$scope.tmpList.length; tId++){
        if($scope.orgList[sId].id == $scope.tmpList[tId].id){
          $scope.orgList[sId] = $scope.tmpList[tId];
        }
      }
     }
    },function(error){
      displayRestError.display(error);            
    });
  }
  
  //Initiate the Refresh Timer object.
  $scope.refreshTimer = null;
  $scope.refreshTime = 0;
  
  $scope.refreshTimeChange = function (){
    $scope.StopRefreshTimer();
    if($scope.refreshTime == 0){
      return;
    }
    $scope.StartRefreshTimer();
  }
  
  //Start Refresh Timer function.
  $scope.StartRefreshTimer = function () {
    // Don't start a new timer, if one running already
     if ( angular.isDefined($scope.refreshTimer) ) return;
    //Initialize the Timer to run every milliseconds defined in $scope.refreshTime
    $scope.refreshTimer = $interval($scope.updateAllSensors, $scope.refreshTime);
  };
  
  //Stop and distroy function.
  $scope.StopRefreshTimer = function () {
    //Cancel the Refresh Timer.
    if (angular.isDefined($scope.refreshTimer)) {
      $interval.cancel($scope.refreshTimer);
      $scope.refreshTimer = undefined;
    }
  };
  
  $scope.$on('$destroy', function() {
    // Make sure that the interval is destroyed too
    $scope.StopRefreshTimer();
  });
    
    
  //Send payload by button
  $scope.sendPL = function (sensor, payloadJson, multipleCalls) {
    SensorsFactory.sendPayload({nodeId: "sendPayload"}, payloadJson,function(response) {
        if(multipleCalls){
          alertService.success("Payload sent to ["+sensor.nameWithNode+"], Payload:"+payloadJson.payload);
          $scope.updateSensor(sensor, false); 
        }else{
          $scope.updateSensor(sensor, true); 
        }
    },function(error){
        displayRestError.display(error);            
    });
  }
  
  //ON/OFF Sensor
  $scope.onOff = function (sensor) {
    if(sensor.status === '0' || sensor.status === 'OFF'){
      sensor.newPayload = 1;
    }else{
      sensor.newPayload = 0;
    }
    SensorsFactory.sendPayload({nodeId: sensor.id, payload: sensor.newPayload},function(response) {
        alertService.success("Payload sent to ["+sensor.nameWithNode+"], Payload:"+sensor.newPayload);
        $scope.updateSensor(sensor);
    },function(error){
        displayRestError.display(error);            
    });
  }
 
  //Update payload model
  $scope.sendPayload = function (sensor, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/sensorsAction/sendPayloadModal.html',
    controller: 'SAsendPayloadController',
    size: size,
    resolve: {sensor: function () {return sensor;}}
    });

    editModalInstance.result.then(function (payloadJson) {
      SensorsFactory.sendPayload({nodeId: "sendPayload"},payloadJson,function(response) {
        //displayRestError.display(response, 201, "Payload sent to ["+sensor.nameWithNode+"]");   
        alertService.success("Payload sent to ["+sensor.nameWithNode+"], Payload:"+payloadJson.payload);
        $scope.updateSensor(sensor);
      },function(error){
        displayRestError.display(error);            
      });        
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
  //Update sensor model
  $scope.editSensor = function (sensor, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/sensorsAction/editModal.html',
    controller: 'SAeditController',
    size: size,
    resolve: {sensor: function () {return sensor;}}
    });

    editModalInstance.result.then(function (keyValues) {
      SensorsFactory.updateOthers({sensorId: sensor.id}, keyValues,function(response) {
        alertService.success("Updat success ["+sensor.nameWithNode+"]");
        $scope.updateSensor(sensor);
      },function(error){
        displayRestError.display(error);            
      });        
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };


});

myControllerModule.controller('SAsendPayloadController', function ($scope, $modalInstance, sensor, SensorsFactory, TypesFactory) {
  $scope.sensor = sensor;
  $scope.header = "Send Payload to '"+$scope.sensor.nameWithNode+"'";  
  $scope.sensor.sliderPayload=sensor.lastValue;
  $scope.payloadJson={};
  $scope.payloadJson.nodeId=sensor.node.id;
  $scope.payloadJson.sensorId=sensor.sensorId;
  $scope.$watch('sensor.sliderPayload', function() {
     $scope.sliderOnChange();
  });
  $scope.sliderReleased = function(){
    SensorsFactory.sendPayload({nodeId: sensor.id, payload: sensor.newPayload},function(response) {
      },function(error){
        displayRestError.display(error);            
      });        
  }
  $scope.sliderOnChange = function() {
        $scope.sensor.newPayload = $scope.sensor.sliderPayload;  
  };
  
  $scope.variableTypes = TypesFactory.getSensorVariableTypes({id: sensor.type});
  
  $scope.send = function() {$modalInstance.close($scope.payloadJson);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('SAeditController', function ($scope, $modalInstance, sensor, SensorsFactory) {
  $scope.sensor = sensor;
  $scope.keyValues = SensorsFactory.getOthers({sensorId : sensor.id});
  $scope.header = "Update '"+$scope.sensor.nameWithNode+"'";
  $scope.edit = function() {$modalInstance.close($scope.keyValues);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
