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
myControllerModule.controller('FirmwareTypeController', function(alertService,
$scope, $filter, FirmwaresFactory, $location, $uibModal, displayRestError, $filter) {
    
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
  
  //Send list of Firmware Types
  $scope.orgList = FirmwaresFactory.getAllFirmwareTypes(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Delete a Firmware Type
  $scope.delete = function (firmwareType, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'FTMdeleteController',
    size: size,
    resolve: {
      firmwareType: function () {return firmwareType;}
      }
    });

    modalInstance.result.then(function (selectedFirmwareType) {
      $scope.selected = selectedFirmwareType;
      FirmwaresFactory.deleteFirmwareType({ id: selectedFirmwareType.id },function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_DELETE_FIRMWARE_TYPE', selectedFirmwareType));
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwareTypes(function(response) {
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
    
  //Add a Firmware Type
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/firmwares/firmwareTypeAddModal.html',
    controller: 'FTMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newFirmwareType) {
      $scope.newFirmwareType = newFirmwareType;
      FirmwaresFactory.createFirmwareType($scope.newFirmwareType,function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_ADDED_FIRMWARE_TYPE', newFirmwareType));
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwareTypes(function(response) {
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
    
  //Update a Firmware Type
  $scope.update = function (firmwareType, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/firmwares/firmwareTypeUpdateModal.html',
    controller: 'FTMupdateController',
    size: size,
    resolve: {firmwareType: function () {return firmwareType;}}
    });

    editModalInstance.result.then(function (updateFirmwareType) {
      $scope.updateFirmwareType = updateFirmwareType;
      FirmwaresFactory.updateFirmwareType($scope.updateFirmwareType,function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_UPDATED_FIRMWARE_TYPE', updateFirmwareType));
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwareTypes(function(response) {
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


//Firmware Types Modal
myControllerModule.controller('FTMdeleteController', function ($scope, $modalInstance, $sce, firmwareType, $filter) {
  $scope.firmwareType = firmwareType;
  $scope.header = $filter('translate')('FIRMWARE.TITLE_DELETE_FIRMWARE_TYPE');
  $scope.deleteMsg = $filter('translate')('FIRMWARE.MESSAGE_DELETE_FIRMWARE_TYPE', firmwareType);
  $scope.remove = function() {
    $modalInstance.close($scope.firmwareType);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('FTMaddController', function ($scope, $modalInstance, $filter) {
  $scope.firmwareType = {};
   $scope.header = $filter('translate')('FIRMWARE.TITLE_ADD_FIRMWARE_TYPE');
  $scope.add = function() {$modalInstance.close($scope.firmwareType); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('FTMupdateController', function ($scope, $modalInstance, firmwareType, $filter) {
  $scope.firmwareType = firmwareType;
  $scope.header = $filter('translate')('FIRMWARE.TITLE_UPDATE_FIRMWARE_TYPE');
  $scope.update = function() {$modalInstance.close($scope.firmwareType);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
