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
myControllerModule.controller('SystemStatusController', function(alertService,
$scope, $filter, StatusFactory, $location, $modal, $stateParams, displayRestError) {
  
  $scope.config = {
    itemsPerPage: 100,
    maxPages:1,
    fillLastPage: false
  }

  //OS Status
   $scope.osStatus = StatusFactory.getOsStatus(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });  
  //JVM Status
   $scope.jvmStatus = StatusFactory.getJvmStatus(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });  
  
});

myControllerModule.controller('GatewayStatusController', function(alertService,
$scope, $filter, StatusFactory, $location, $modal, $stateParams, displayRestError) {
  
  $scope.config = {
    itemsPerPage: 100,
    maxPages:1,
    fillLastPage: false
  }

  //Gateway Information
   $scope.gatewayInfo = StatusFactory.getGatewayInfo(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    }); 
  
});
