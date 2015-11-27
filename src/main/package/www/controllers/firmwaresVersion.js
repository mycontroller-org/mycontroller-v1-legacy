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
myControllerModule.controller('FirmwareVersionController', function(alertService,
$scope, $filter, FirmwaresFactory, $uibModal, displayRestError, $filter) {
    
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
  $scope.orgList = FirmwaresFactory.getAllFirmwareVersions(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Delete a Firmware Version
  $scope.delete = function (firmwareVersion, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'FVMdeleteController',
    size: size,
    resolve: {
      firmwareVersion: function () {return firmwareVersion;}
      }
    });

    modalInstance.result.then(function (selectedFirmwareVersion) {
      $scope.selected = selectedFirmwareVersion;
      FirmwaresFactory.deleteFirmwareVersion({ id: selectedFirmwareVersion.id },function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_DELETED_FIRMWARE_VERSION', selectedFirmwareVersion));
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwareVersions(function(response) {
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
    
  //Add a Firmware Version
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/firmwares/firmwareVersionAddModal.html',
    controller: 'FVMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newFirmwareVersion) {
      $scope.newFirmwareVersion = newFirmwareVersion;
      FirmwaresFactory.createFirmwareVersion($scope.newFirmwareVersion,function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_ADDED_FIRMWARE_VERSION', newFirmwareVersion));        
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwareVersions(function(response) {
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
    
  //Update a Firmware Version
  $scope.update = function (firmwareVersion, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/firmwares/firmwareVersionUpdateModal.html',
    controller: 'FVMupdateController',
    size: size,
    resolve: {firmwareVersion: function () {return firmwareVersion;}}
    });

    editModalInstance.result.then(function (updateFirmwareType) {
      $scope.updateFirmwareType = updateFirmwareType;
      FirmwaresFactory.updateFirmwareVersion($scope.updateFirmwareType,function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_UPDATED_FIRMWARE_VERSION', updateFirmwareType));        
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwareVersions(function(response) {
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
myControllerModule.controller('FVMdeleteController', function ($scope, $modalInstance, $sce, firmwareVersion, $filter) {
  $scope.firmwareVersion = firmwareVersion;
  $scope.header = $filter('translate')('FIRMWARE.TITLE_DELETE_FIRMWARE_VERSION');  
  $scope.deleteMsg = $filter('translate')('FIRMWARE.MESSAGE_DELETE_FIRMWARE_VERSION',firmwareVersion);
  $scope.remove = function() {
    $modalInstance.close($scope.firmwareVersion);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('FVMaddController', function ($scope, $modalInstance, $filter) {
  $scope.firmwareVersion = {};
  $scope.header = $filter('translate')('FIRMWARE.TITLE_ADD_FIRMWARE_VERSION');  
  $scope.add = function() {$modalInstance.close($scope.firmwareVersion); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('FVMupdateController', function ($scope, $modalInstance, firmwareVersion, $filter) {
  $scope.firmwareVersion = firmwareVersion;
  $scope.header = $filter('translate')('FIRMWARE.TITLE_UPDATE_FIRMWARE_VERSION');
  $scope.update = function() {$modalInstance.close($scope.firmwareVersion);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
