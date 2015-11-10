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
myControllerModule.controller('SensorsController', function(alertService,
$scope, $filter, SensorsFactory, TypesFactory, $location, $uibModal, displayRestError, about) {
    
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
  $scope.getSensors = function(){
     for (var sId=0; sId<$scope.filteredList.length; sId++){
       $scope.filteredList[sId] = SensorsFactory.get({"nodeId":$scope.filteredList[sId].node.id,"sensorId":$scope.filteredList[sId].sensorId});
       //$scope.filteredList[sId].status = tmpSensor.status;
       //console.log("status:"+tmpSensor.status);
     }
  }
    
  // Call and Run function every 30 second
  $scope.orgList = SensorsFactory.query(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  //setInterval($scope.getSensors, 1000*30);
  
  //Get all Nodes
  $scope.nodes = TypesFactory.getNodes();
  //
  $scope.nodeChange = function(selectedNodeId){
    $scope.orgList = SensorsFactory.query({nodeId: selectedNodeId}, function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
    $scope.filteredList = $scope.orgList;
  };
  
  
  //Delete a Sensor
  $scope.delete = function (sensor, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'SMdeleteController',
    size: size,
    resolve: {
      sensor: function () {return sensor;}
      }
    });

    modalInstance.result.then(function (selectedSensor) {
      SensorsFactory.delete({ nodeId: selectedSensor.node.id, sensorId: selectedSensor.sensorId },function(response) {
        alertService.success("Deleted a sensor[id:"+selectedSensor.sensorId+", Name:"+selectedSensor.name+"]");
        //Update display table
        $scope.orgList = SensorsFactory.query({nodeId:$scope.nodeId},function(response) {
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
    
  //Add a Sensor
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/sensors/addModal.html',
    controller: 'SMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newSensor) {
      SensorsFactory.create({nodeId: newSensor.node.id}, newSensor, function(response) {
        alertService.success("Added a sensor[id:"+newSensor.sensorId+", Name:"+newSensor.name+"]");
        //Update display table
        $scope.nodeId = null; //Remove node selection
        $scope.orgList = SensorsFactory.query(function(response) {
        },function(error){
          displayRestError.display(error);            
        });
      $scope.filteredList = $scope.orgList;
      });
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
    
  //Update a Sensor
  $scope.update = function (sensor, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/sensors/updateModal.html',
    controller: 'SMupdateController',
    size: size,
    resolve: {sensor: function () {return sensor;}}
    });

    editModalInstance.result.then(function (updateSensor) {
      updateSensor.unit = null;
      SensorsFactory.update({nodeId: updateSensor.node.id}, updateSensor,function(response) {
        alertService.success("Updated a sensor[id:"+updateSensor.sensorId+", Name:"+updateSensor.name+"]");
        //Update display table
        $scope.orgList = SensorsFactory.query({nodeId:$scope.nodeId},function(response) {
        },function(error){
          displayRestError.display(error);            
        });
      $scope.filteredList = $scope.orgList;
      });
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
});


//Sensors Modal
myControllerModule.controller('SMdeleteController', function ($scope, $modalInstance, $sce, sensor) {
  $scope.sensor = sensor;
  $scope.header = "Delete Sensor";
  $scope.deleteMsg = $sce.trustAsHtml("You are about to delete a Sensor"
    +"<br>Deletion process will remove complete trace of this resource!" 
    +"<br>Click 'Delete' to proceed."
    +"<br><I>Sensor:</I> "+sensor.nameWithNode+" [nodeId:"+sensor.node.id+",sensorId:"+sensor.sensorId +",type:"+sensor.typeString+"]");
  $scope.remove = function() {
    $modalInstance.close($scope.sensor);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('SMaddController', function ($scope, $modalInstance, TypesFactory) {
  $scope.sensor = {};
  $scope.header = "New Sensor";
  $scope.sensorTypes = TypesFactory.getSensorTypes();
  $scope.sensorVariableTypes = {};
  $scope.nodes = TypesFactory.getNodes();
  
  $scope.refreshVariableTypes = function(sensorTypeId){
    return TypesFactory.getSensorVariableTypes({id: sensorTypeId});
  }
  
  $scope.updateVariableTypes= function(){
    $scope.sensor.variableTypes = {};
    var variableTypesArray=[];
    if($scope.variableTypes.length >0){
        angular.forEach($scope.variableTypes, function(key, value) {
          variableTypesArray.push(key.displayName);
        });
      }
      $scope.sensor.variableTypes = variableTypesArray.join(', ');//Refer Api
  }
  $scope.add = function() {$modalInstance.close($scope.sensor); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('SMupdateController', function ($scope, $modalInstance, sensor, TypesFactory) {
  $scope.sensor = sensor;
  $scope.sensorTypes = TypesFactory.getSensorTypes();
  $scope.sensorValueTypes = TypesFactory.getSensorValueTypes();
  $scope.header = "Modify Sensor";
  $scope.sensorVariableTypes = TypesFactory.getSensorVariableTypesBySensorRefId({id: sensor.id});
  
  $scope.refreshVariableTypes = function(sensorTypeId){
    return TypesFactory.getSensorVariableTypes({id: sensorTypeId});
  }
  
  $scope.updateVariableTypes= function(){
    $scope.sensor.variableTypes = {};
    var variableTypesArray=[];
    if($scope.variableTypes.length >0){
        angular.forEach($scope.variableTypes, function(key, value) {
          variableTypesArray.push(key.displayName);
        });
      }
      $scope.sensor.variableTypes = variableTypesArray.join(', ');
  }
  $scope.update = function() {$modalInstance.close($scope.sensor);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
