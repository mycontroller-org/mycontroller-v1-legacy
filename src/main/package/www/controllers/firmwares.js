/*
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
/* Firmwares type */
myControllerModule.controller('FirmwaresTypeController', function(
  alertService, $scope, $filter, FirmwaresFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $stateParams) {
  
  //GUI page settings
  $scope.headerStringList = "Firmware types detail";
  $scope.noItemsSystemMsg = "No firmware types set up.";
  $scope.noItemsSystemIcon = "fa fa-file-code-o";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];
    
  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};
  
  //Get min number
  $scope.getMin = function(item1, item2){
    return CommonServices.getMin(item1, item2);
  };  
  
  //get all items
  $scope.getAllItems = function(){
    FirmwaresFactory.getAllFirmwareTypes($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
    },function(error){
      displayRestError.display(error);
    });
  }

  //Hold all the selected item ids
  $scope.itemIds = [];

  $scope.selectAllItems = function(){
    CommonServices.selectAllItems($scope);
  };

  $scope.selectItem = function(item){
    CommonServices.selectItem($scope, item);
  };
  
  //On page change
  $scope.pageChanged = function(newPage){
    CommonServices.updatePageChange($scope, newPage);
  };

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.updateFiltersChange($scope, filters);
  };
  
  $scope.filterConfig = {
    fields: [
      {
        id: 'name',
        title:  'Name',
        placeholder: 'Filter by Name',
        filterType: 'text'
      },{
        id: 'id',
        title:  'Type id',
        placeholder: 'Filter by Type id',
        filterType: 'text'
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };
  
  //Sort columns
  var sortChange = function (sortId, isAscending) {
    //Reset sort type and update items
    CommonServices.updateSortChange($scope, sortId, isAscending);
  };

  $scope.sortConfig = {
    fields: [
      {
        id: 'name',
        title:  'Name',
        sortType: 'text'
      },{
        id: 'id',
        title:  'Type id',
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };
  
  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("firmwaresTypeAddEdit", {'id': $scope.itemIds[0]});
    }
  };

  //Delete item(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      FirmwaresFactory.deleteFirmwareTypes($scope.itemIds, function(response) {
        alertService.success('Deleted '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
});



//add edit item
myControllerModule.controller('FirmwaresTypeControllerAddEdit', function ($scope, CommonServices, alertService, FirmwaresFactory, mchelper, $stateParams, $filter) {
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add firmware type";
  $scope.headerStringUpdate = "Update firmware type";
  $scope.cancelButtonState = "firmwaresTypeList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  
  $scope.firmwareType = {};
  $scope.ftypeId = $stateParams.id;

    if($stateParams.id){
      FirmwaresFactory.getFirmwareType({"refId":$stateParams.id},function(response) {
        $scope.firmwareType = response;
      },function(error){
        displayRestError.display(error);
      });
  }
  

  //Save data
  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      FirmwaresFactory.updateFirmwareType($scope.firmwareType,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      FirmwaresFactory.createFirmwareType($scope.firmwareType,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }

});

//type over

/* Firmwares version */
myControllerModule.controller('FirmwaresVersionController', function(
  alertService, $scope, $filter, FirmwaresFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $stateParams) {
  
  //GUI page settings
  $scope.headerStringList = "Firmware versions detail";
  $scope.noItemsSystemMsg = "No firmware versions set up.";
  $scope.noItemsSystemIcon = "fa fa-file-code-o";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];
    
  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};
  
  //Get min number
  $scope.getMin = function(item1, item2){
    return CommonServices.getMin(item1, item2);
  };  

  //get all items
  $scope.getAllItems = function(){
    FirmwaresFactory.getAllFirmwareVersions($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
    },function(error){
      displayRestError.display(error);
    });
  }

  //Hold all the selected item ids
  $scope.itemIds = [];

  $scope.selectAllItems = function(){
    CommonServices.selectAllItems($scope);
  };

  $scope.selectItem = function(item){
    CommonServices.selectItem($scope, item);
  };
  
  //On page change
  $scope.pageChanged = function(newPage){
    CommonServices.updatePageChange($scope, newPage);
  };

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.updateFiltersChange($scope, filters);
  };
  
  $scope.filterConfig = {
    fields: [
      {
        id: 'version',
        title:  'Version',
        placeholder: 'Filter by Name',
        filterType: 'text'
      },{
        id: 'id',
        title:  'Version id',
        placeholder: 'Filter by Version id',
        filterType: 'text'
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };
  
  //Sort columns
  var sortChange = function (sortId, isAscending) {
    //Reset sort type and update items
    CommonServices.updateSortChange($scope, sortId, isAscending);
  };

  $scope.sortConfig = {
    fields: [
      {
        id: 'version',
        title:  'Version',
        sortType: 'text'
      },{
        id: 'id',
        title:  'Version id',
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };
  
  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("firmwaresVersionAddEdit", {'id':$scope.itemIds[0]});
    }
  };

  //Delete item(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      FirmwaresFactory.deleteFirmwareVersions($scope.itemIds, function(response) {
        alertService.success('Deleted '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
});

//add edit item
myControllerModule.controller('FirmwaresVersionControllerAddEdit', function ($scope, CommonServices, alertService, FirmwaresFactory, mchelper, $stateParams, $filter) {
  
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add firmware version";
  $scope.headerStringUpdate = "Update firmware version";
  $scope.cancelButtonState = "firmwaresVersionList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  
  $scope.item = {};
  $scope.itemId = $stateParams.id;

    if($stateParams.id){
      FirmwaresFactory.getFirmwareVersion({"refId":$stateParams.id},function(response) {
        $scope.item = response;
      },function(error){
        displayRestError.display(error);
      });
  }
  

  //Save data
  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      FirmwaresFactory.updateFirmwareVersion($scope.item,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      FirmwaresFactory.createFirmwareVersion($scope.item,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }

});

//Version over



/* Firmware controller */
myControllerModule.controller('FirmwaresController', function(
  alertService, $scope, $filter, FirmwaresFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $stateParams) {  
  
  //GUI page settings
  $scope.headerStringList = "Firmwares detail";
  $scope.noItemsSystemMsg = "No firmwares set up.";
  $scope.noItemsSystemIcon = "fa fa-file-code-o";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];
    
  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};
  
  //Get min number
  $scope.getMin = function(item1, item2){
    return CommonServices.getMin(item1, item2);
  };  
  
  if($stateParams.sensorId){
    $scope.sensorId = $stateParams.sensorId;
  }

  //get all items
  $scope.getAllItems = function(){
    FirmwaresFactory.getAllFirmwares($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
    },function(error){
      displayRestError.display(error);
    });
  }

  //Hold all the selected item ids
  $scope.itemIds = [];

  $scope.selectAllItems = function(){
    CommonServices.selectAllItems($scope);
  };

  $scope.selectItem = function(item){
    CommonServices.selectItem($scope, item);
  };
  
  //On page change
  $scope.pageChanged = function(newPage){
    CommonServices.updatePageChange($scope, newPage);
  };

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.updateFiltersChange($scope, filters);
  };
  
$scope.filterConfig = {
    fields: [
      {
        id: 'blocks',
        title:  'Blocks',
        placeholder: 'Filter by Blocks',
        filterType: 'text'
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };
  
  //Sort columns
  var sortChange = function (sortId, isAscending) {
    //Reset sort type and update items
    CommonServices.updateSortChange($scope, sortId, isAscending);
  };

  $scope.sortConfig = {
    fields: [
      {
        id: 'typeId',
        title:  'Type id',
        sortType: 'text'
      },{
        id: 'versionId',
        title:  'Version id',
        sortType: 'text'
      },{
        id: 'blocks',
        title:  'Blocks',
        sortType: 'text'
      },{
        id: 'timestamp',
        title:  'Uploaded on',
        sortType: 'text'
      },{
        id: 'crc',
        title:  'CRC',
        sortType: 'text'
      },
    ],
    onSortChange: sortChange
  };
  
  
  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("firmwaresAddEdit", {'id':$scope.itemIds[0]});
    }
  };

  //Delete item(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      FirmwaresFactory.deleteFirmwares($scope.itemIds, function(response) {
        alertService.success('Deleted '+$scope.itemIds.length+' items(s).');
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
});


//add edit item
myControllerModule.controller('FirmwaresControllerAddEdit', function ($scope, CommonServices, alertService, FirmwaresFactory, mchelper, $stateParams, $filter, TypesFactory) {
  $scope.item = {};
  
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add firmware";
  $scope.headerStringUpdate = "Update firmware";
  $scope.cancelButtonState = "firmwaresList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

    if($stateParams.id){
      FirmwaresFactory.getFirmware({"refId":$stateParams.id},function(response) {
        $scope.item = response;
      },function(error){
        displayRestError.display(error);
      });
  }
  
  //Pre load
  $scope.firmwareTypes = TypesFactory.getFirmwareTypes();
  $scope.firmwareVersions = TypesFactory.getFirmwareVersions();
  
  //Read File and put it in textarea
  $scope.displayFileContents = function(contents) {
        $scope.item.fileString = contents;
  };

  //Save data
  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      FirmwaresFactory.updateFirmware($scope.item,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      FirmwaresFactory.createFirmware($scope.item,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.node));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
  
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
