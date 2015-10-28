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
myControllerModule.controller('SensorLogController', function(alertService,
$scope, $filter, SensorLogFactory, $location, $uibModal, $stateParams, about) {
  
  $scope.sensor = SensorLogFactory.getSensorData({"id":$stateParams.id},function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
    
  $scope.filteredList=[];
  $scope.orgList=[];
  $scope.config = {
    itemsPerPage: 10,
    maxPages:5,
    fillLastPage: false
  }
  
  //about, Timezone, etc.,
  $scope.about = about;

  //Filter
  $scope.updateFilteredList = function() {
    $scope.filteredList = $filter("filter")($scope.orgList, $scope.query);
  };
    
  //Get logs
  $scope.orgList = SensorLogFactory.getSensorAll({"id":$stateParams.id});
  $scope.filteredList = $scope.orgList;
  
});

myControllerModule.controller('LogsController', function(alertService,
$scope, $filter, SensorLogFactory, $location, $uibModal, $stateParams, about) {
      
  $scope.filteredList=[];
  $scope.orgList=[];
  $scope.config = {
    itemsPerPage: 10,
    maxPages:5,
    fillLastPage: false
  }

  //about, Timezone, etc.,
  $scope.about = about;

  //Filter
  $scope.updateFilteredList = function() {
    $scope.filteredList = $filter("filter")($scope.orgList, $scope.query);
  };
    
  //Get logs
  $scope.orgList = SensorLogFactory.getAll({"id":$stateParams.id});
  $scope.filteredList = $scope.orgList;
  
});
