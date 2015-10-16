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
$scope, $filter, FirmwaresFactory, $location, $modal, displayRestError, about) {
    
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
    var modalInstance = $modal.open({
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
        alertService.success("Deleted a firmware["+selectedFirmware.firmwareName+"]");
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
    var addModalInstance = $modal.open({
    templateUrl: 'partials/firmwares/firmwareAddModal.html',
    controller: 'FMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newFirmware) {
      $scope.newFirmware = newFirmware;
      FirmwaresFactory.createFirmware($scope.newFirmware,function(response) {
        alertService.success("Added new firmware[TypeId: "+newFirmware.type.id+", VersionId:"+newFirmware.version.id+"]");
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
    var editModalInstance = $modal.open({
    templateUrl: 'partials/firmwares/firmwareUpdateModal.html',
    controller: 'FMupdateController',
    size: size,
    resolve: {firmware: function () {return firmware;}}
    });

    editModalInstance.result.then(function (updateFirmware) {
      FirmwaresFactory.updateFirmware(updateFirmware,function(response) {
        alertService.success("Updated a firmware["+updateFirmware.firmwareName+"]");
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
myControllerModule.controller('FMdeleteController', function ($scope, $modalInstance, $sce, firmware) {
  $scope.firmware = firmware;
  $scope.header = "Delete Firmware";
  $scope.deleteMsg = $sce.trustAsHtml("You are about to delete a Firmware"
    +"<br>Deletion process will remove complete trace of this firmware!" 
    +"<br>Click 'Delete' to proceed."
    +"<br><I>Firmware: </I>[id:"+firmware.id+",Type:"+firmware.type.name +",Version:"+firmware.version.name+"]");
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

myControllerModule.controller('FMupdateController', function ($scope, $modalInstance, firmware) {
  $scope.firmware = firmware;
  $scope.header = "Update Firmware ["+firmware.firmwareName+"]";
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
