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
myControllerModule.controller('NodesController', function(alertService,
$scope, NodesFactory, $state, $uibModal, displayRestError, CommonServices, mchelper, $filter) {

  //GUI page settings
  $scope.headerStringList = "Nodes detail";
  $scope.noItemsSystemMsg = "No nodes set up.";
  $scope.noItemsSystemIcon = "fa fa-sitemap";

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

  //get all Nodes
  $scope.getAllItems = function(){
    NodesFactory.getAll($scope.query, function(response) {
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
      },
      {
        id: 'state',
        title:  'Status',
        placeholder: 'Filter by Status',
        filterType: 'select',
        filterValues: ['Up','Down','Unavailable'],
      },
      {
        id: 'type',
        title:  'Type',
        placeholder: 'Filter by Type',
        filterType: 'select',
        filterValues: ['Node','Repeater node'],
      },
      {
        id: 'eui',
        title:  'EUI',
        placeholder: 'Filter by EUI',
        filterType: 'text',
      },
      {
        id: 'version',
        title:  'Version',
        placeholder: 'Filter by Version',
        filterType: 'text',
      },
      {
        id: 'libVersion',
        title:  'Library Version',
        placeholder: 'Filter by Library Version',
        filterType: 'text',
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
      },
      {
        id: 'state',
        title:  'Status',
        sortType: 'text'
      },
      {
        id: 'eui',
        title:  'EUI',
        sortType: 'text'
      },
      {
        id: 'type',
        title:  'Type',
        sortType: 'text'
      },
      {
        id: 'version',
        title:  'Version',
        sortType: 'text'
      },
      {
        id: 'libVersion',
        title:  'Library Version',
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };



  //Delete Node(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      NodesFactory.deleteIds($scope.itemIds, function(response) {
        alertService.success('Deleted '+$scope.itemIds.length+' node(s).');
        //Update display table
        $scope.getAllNodes();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);            
      }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("nodesAddEdit",{'id':$scope.itemIds[0]});
    }
  };

  //Upload Firmware
  $scope.uploadFirmware = function (size) {
    if($scope.itemIds.length > 0){
      NodesFactory.uploadFirmware($scope.itemIds,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_FIRMWARE_UPDATE',  $scope.itemIds));
      },function(error){
        displayRestError.display(error);
      });  
    }
  };

  //Reboot a Node
  $scope.reboot = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/node-reboot-modal.html',
    controller: 'NodesControllerReboot',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function () {
      NodesFactory.reboot($scope.itemIds, function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_REBOOT', $scope.itemIds));
      },function(error){
        displayRestError.display(error);
      });      
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
  //Erase Configuration of Nodes
  $scope.eraseConfiguration = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/node-erase-configuration-modal.html',
    controller: 'NodesControllerEraseConfiguration',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function () {
      NodesFactory.eraseConfiguration($scope.itemIds, function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_EEPROM_ERASE', node));
      },function(error){
        displayRestError.display(error);            
      });
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

});


// Nodes other controllers

//Add/Edit Node
myControllerModule.controller('NodesControllerAddEdit', function ($scope, $stateParams, GatewaysFactory, NodesFactory, TypesFactory, mchelper, alertService, displayRestError, $filter, $state) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.node = {};
  if($stateParams.id){
    $scope.node = NodesFactory.get({"nodeId":$stateParams.id});
  }
  $scope.node.gateway = {};
  $scope.gateways = TypesFactory.getGateways();
  $scope.nodeTypes = TypesFactory.getNodeTypes();
  $scope.firmwares = TypesFactory.getFirmwares();
  
  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = "Add node";
  $scope.headerStringUpdate = "Update node";
  $scope.cancelButtonState = "nodesList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  
  
  $scope.save = function(){
      $scope.saveProgress = true;
    if($stateParams.id){
      NodesFactory.update($scope.node,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.node));
        $state.go("nodesList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      NodesFactory.create($scope.node,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.node));
        $state.go("nodesList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});


//Node Detail
myControllerModule.controller('NodesControllerDetail', function ($scope, $stateParams, mchelper, NodesFactory, MetricsFactory) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.headerStringList = "Node details";
  
  $scope.item = NodesFactory.get({"nodeId":$stateParams.id});
  $scope.resourceCount = MetricsFactory.getResourceCount({"resourceType":"NODE", "resourceId":$stateParams.id});
});

//Nodes Modal - Delete
myControllerModule.controller('NodesControllerDelete', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('NODE.TITLE_DELETE');
  $scope.deleteMsg = $filter('translate')('NODE.MESSAGE_DELETE', $scope.nodeIds);
  $scope.remove = function() {
    $uibModalInstance.close();
  };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});

//Erase Configuration Modal
myControllerModule.controller('NodesControllerEraseConfiguration', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('NODE.TITLE_ERASE_EEPROM');
  $scope.eraseMsg = $filter('translate')('NODE.MESSAGE_ERASE_EEPROM', $scope);
  $scope.eraseNodeConfiguration = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});

//reboot Modal
myControllerModule.controller('NodesControllerReboot', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('NODE.TITLE_REBOOT');
  $scope.rebootMsg = $filter('translate')('NODE.MESSAGE_REBOOT', $scope);
  $scope.reboot = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});
