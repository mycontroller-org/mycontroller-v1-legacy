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
myControllerModule.controller('UidTagController', function(alertService,
$scope, $filter, TimersFactory, UidTagFactory, $uibModal, $stateParams, displayRestError, $filter) {
  
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
  
  // Call and Run function every second
  $scope.orgList = UidTagFactory.getAll(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Add new
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/uidTag/addModal.html',
    controller: 'UTMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (uidTag) {
      UidTagFactory.create(uidTag, function(response) {
		alertService.success($filter('translate')('UID.TITLE_ADDED', uidTag));
        //Update display table
        $scope.orgList = UidTagFactory.getAll(function(response) {
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
  $scope.delete = function (uidTag, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'UTMdeleteController',
    size: size,
    resolve: {
      uidTag: function () {return uidTag;}
      }
    });
    modalInstance.result.then(function (uidTag) {
      UidTagFactory.delete({id: uidTag.uid},function(response) {
		alertService.success($filter('translate')('UID.TITLE_DELETED', uidTag));
        //Update display table
        $scope.orgList = UidTagFactory.getAll(function(response) {
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
myControllerModule.controller('UTMaddController', function ($scope, $modalInstance, TypesFactory, $filter) {
  $scope.uidTag = {};
  $scope.uidTag.sensor = {};
  $scope.uidTag.sensor.node = {};
  $scope.nodes = TypesFactory.getNodes();
      //Update sensors
  $scope.refreshSensors = function(nodeId){
      return TypesFactory.getSensors({id: nodeId});
  };
  
  $scope.refreshVariableTypes = function(sensorRefId){
      return TypesFactory.getSensorVariableTypesBySensorRefId({id:sensorRefId});
  };
  $scope.header = $filter('translate')('UID.NOTIFY_ADDED');
  $scope.add = function() {$modalInstance.close($scope.uidTag); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Delete Modal
myControllerModule.controller('UTMdeleteController', function ($scope, $modalInstance, $sce, uidTag, $filter) {
  $scope.header = $filter('translate')('UID.NOTIFY_DELTED',uidTag);
  $scope.deleteMsg = $filter('translate')('UID.MESSAGE_DELETE',uidTag);
  $scope.remove = function() {
    $modalInstance.close(uidTag);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
