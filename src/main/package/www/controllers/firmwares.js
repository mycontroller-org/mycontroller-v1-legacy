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
myControllerModule.controller('FirmwareController', function(alertService,
$scope, $filter, FirmwaresFactory, $location, $uibModal, displayRestError, about, $filter) {
    
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
  
  //Send list of Firmwares
  $scope.orgList = FirmwaresFactory.getAllFirmwares(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Delete a Firmware
  $scope.delete = function (firmware, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'FMdeleteController',
    size: size,
    resolve: {
      firmware: function () {return firmware;}
      }
    });

    modalInstance.result.then(function (selectedFirmware) {
      $scope.selected = selectedFirmware;
      FirmwaresFactory.deleteFirmware({ id: selectedFirmware.id },function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_DELETED', selectedFirmware));
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwares(function(response) {
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
    
  //Add a Firmware
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/firmwares/firmwareAddModal.html',
    controller: 'FMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newFirmware) {
      $scope.newFirmware = newFirmware;
      FirmwaresFactory.createFirmware($scope.newFirmware,function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_ADDED', newFirmware));        
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwares(function(response) {
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
    
  //Update a Firmware
  $scope.update = function (firmware, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/firmwares/firmwareUpdateModal.html',
    controller: 'FMupdateController',
    size: size,
    resolve: {firmware: function () {return firmware;}}
    });

    editModalInstance.result.then(function (updateFirmware) {
      FirmwaresFactory.updateFirmware(updateFirmware,function(response) {
        alertService.success($filter('translate')('FIRMWARE.NOTIFY_UPDATED', updateFirmware));         
        //Update display table
        $scope.orgList = FirmwaresFactory.getAllFirmwares(function(response) {
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


//Firmwares Modal
myControllerModule.controller('FMdeleteController', function ($scope, $modalInstance, $sce, firmware, $filter) {
  $scope.firmware = firmware;
  $scope.header = $filter('translate')('FIRMWARE.TITLE_DELETE');
  $scope.deleteMsg = $filter('translate')('FIRMWARE.MESSAGE_DELETE', firmware);  
  $scope.remove = function() {
    $modalInstance.close($scope.firmware);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('FMaddController', function ($scope, $modalInstance, FirmwaresFactory) {
  $scope.firmware = {};
  $scope.firmware.type = {};
  $scope.firmware.version = {};
  $scope.header = "Add Firmware";
  $scope.types = FirmwaresFactory.getAllFirmwareTypes();
  $scope.versions = FirmwaresFactory.getAllFirmwareVersions();
    
  //Read File and put it in textarea
  $scope.displayFileContents = function(contents) {
        $scope.firmware.hexFileString = contents;
  };
  $scope.add = function() {$modalInstance.close($scope.firmware); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
}).directive('onReadFile', function ($parse) {
    return {
        restrict: 'A',
        scope: false,
        link: function(scope, element, attrs) {
            element.bind('change', function(e) {
                
                var onFileReadFn = $parse(attrs.onReadFile);
                var reader = new FileReader();
                
                reader.onload = function() {
                    var fileContents = reader.result;
                    // invoke parsed function on scope
                    // special syntax for passing in data
                    // to named parameters
                    // in the parsed function
                    // we are providing a value for the property 'contents'
                    // in the scope we pass in to the function
                    scope.$apply(function() {
                        onFileReadFn(scope, {
                            'contents' : fileContents
                        });
                    });
                };
                reader.readAsText(element[0].files[0]);
            });
        }
    };
});

myControllerModule.controller('FMupdateController', function ($scope, $modalInstance, firmware, $filter) {
  $scope.firmware = firmware;
  $scope.header = $filter('translate')('FIRMWARE.TITLE_UPDATE',firmware);
  //Read File and put it in textarea
  $scope.displayFileContents = function(contents) {
        $scope.firmware.hexFileString = contents;
  };
  $scope.update = function() {$modalInstance.close($scope.firmware);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
}).directive('onReadFile', function ($parse) {
    return {
        restrict: 'A',
        scope: false,
        link: function(scope, element, attrs) {
            element.bind('change', function(e) {
                
                var onFileReadFn = $parse(attrs.onReadFile);
                var reader = new FileReader();
                
                reader.onload = function() {
                    var fileContents = reader.result;
                    // invoke parsed function on scope
                    // special syntax for passing in data
                    // to named parameters
                    // in the parsed function
                    // we are providing a value for the property 'contents'
                    // in the scope we pass in to the function
                    scope.$apply(function() {
                        onFileReadFn(scope, {
                            'contents' : fileContents
                        });
                    });
                };
                reader.readAsText(element[0].files[0]);
            });
        }
    };
});
