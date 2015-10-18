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
myControllerModule.controller('VariableMapperController', function(alertService, $scope, $filter, $uibModal, displayRestError, TypesFactory) {
  
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
  
  $scope.orgList = TypesFactory.getSensorVariableMapper(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Update
  $scope.edit = function (keyValue, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/variableMapper/editModal.html',
    controller: 'VMPeditController',
    size: size,
    resolve: {keyValue: function () {return keyValue;}}
    });

    editModalInstance.result.then(function (keyValue) {
      TypesFactory.updateSensorVariableMapper(keyValue, function(response) {
        alertService.success("Updated "+keyValue.key);
        //Update display table
        $scope.orgList = TypesFactory.getSensorVariableMapper(function(response) {
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
myControllerModule.controller('VMPeditController', function ($scope, $modalInstance, keyValue, TypesFactory) {
  $scope.keyValue = keyValue;
  //$scope.keyValue.id  = keyValueOrg.id;
  //$scope.keyValue.key = keyValueOrg.key;

  $scope.sensorVariableTypes = TypesFactory.getSensorVariableTypesAll({id: keyValue.id});
  
  $scope.updateVariableTypes= function(){
    var variableTypesArray=[];
    if($scope.variableTypes.length >0){
        angular.forEach($scope.variableTypes, function(key, value) {
          variableTypesArray.push(key.displayName);
        });
      }
      $scope.keyValue.value = variableTypesArray.join(', ');
  }
  
  $scope.header = "Update '"+$scope.keyValue.key+"'";
  $scope.update = function() {$modalInstance.close($scope.keyValue); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
