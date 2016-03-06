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
myControllerModule.controller('BackupControllerList', function(alertService, $scope, $filter, displayRestError, BackupRestoreFactory, $filter, mchelper, CommonServices, $uibModal) {
  //GUI page settings
  $scope.headerStringList = $filter('translate')('BACKUPS_DETAIL');

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];
    
  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};
  

  //get all items
  $scope.getAllItems = function(){
    BackupRestoreFactory.getBackupList(function(response) {
      $scope.orgList = response;
      $scope.filteredList = $scope.orgList;
      $scope.filterConfig.resultsCount = $scope.filteredList.length;
    },function(error){
      displayRestError.display(error);
      $scope.orgList = {};
      $scope.orgList.$resolved = true;
    });
  }

  //Pre load
  $scope.getAllItems();
  $scope.itemName = null;
  $scope.disableRunBackup = false;

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.filterChangeLocal(filters, $scope);
    $scope.itemName = null;
  };
  
  $scope.filterConfig = {
    fields: [
      {
        id: 'name',
        title:  $filter('translate')('FILE_NAME'),
        placeholder:  $filter('translate')('FILTER_BY_FILE_NAME'),
        filterType: 'text'
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };
  
   //Select item
  $scope.selectItem = function (item) {
    $scope.restoreItem = item;
    if($scope.itemName === item.name){
      $scope.itemName = null;
    }else{
      $scope.itemName = item.name;
    }
  };

  //backup now
  $scope.backupNow = function(){
    $scope.disableRunBackup = true;
    BackupRestoreFactory.backupNow(function(){
      $scope.getAllItems();
      alertService.success($filter('translate')('BACKUP_COMPLETED_SUCCESSFULLY'));
      $scope.disableRunBackup = false;
    },function(error){
        displayRestError.display(error);
        $scope.disableRunBackup = false;
    });
  };
  
  
  //Restore
  $scope.restoreItemFn = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/backup/restore-confirmation-modal.html',
    controller: 'BackupControllerRestore',
    size: size,
    resolve: {backupFile: function () {return $scope.restoreItem;}}
    });

    addModalInstance.result.then(function () {
      BackupRestoreFactory.restore($scope.restoreItem, function(response) {
        alertService.success($filter('translate')('RESTORE_INITIATED'));
      },function(error){
        displayRestError.display(error);
      });      
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
  //Delete Item
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      BackupRestoreFactory.delete($scope.restoreItem, function(response) {
        alertService.success($filter('translate')('ITEMS_DELETED_SUCCESSFULLY'));
        //Update display table
        $scope.getAllItems();
        $scope.itemName = null;
      },function(error){
        displayRestError.display(error);
      }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
});

//restore Modal
myControllerModule.controller('BackupControllerRestore', function ($scope, $uibModalInstance, $filter, backupFile) {
  $scope.header = $filter('translate')('RESTORE_CONFIRMATION_TITLE', backupFile);
  $scope.rebootMsg = $filter('translate')('RESTORE_CONFIRMATION_MESSAGE', backupFile);
  $scope.restore = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});

//Automatice backup settings
myControllerModule.controller('BackupControllerAutoSettings', function ($scope, BackupRestoreFactory, mchelper, alertService, displayRestError, $filter, CommonServices) {
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.item.enabled = false;
  $scope.cs = CommonServices;
  
  $scope.resetSettings = function(){
    BackupRestoreFactory.getBackupSettings(function(response) {
        $scope.item = response;
         //Update dropdown
        if($scope.item.interval % 86400000  == 0){
          $scope.intervalLocal = $scope.item.interval / 86400000;
          $scope.intervalTimeConstant = "86400000";
          $scope.intervalTimeConstantString = $filter('translate')('DAYS');
        }else if($scope.item.interval % 3600000  == 0){
          $scope.intervalLocal = $scope.item.interval / 3600000;
          $scope.intervalTimeConstant = "3600000";
          $scope.intervalTimeConstantString = $filter('translate')('Hours');
        }else if($scope.item.interval % 60000  == 0){
          $scope.intervalLocal = $scope.item.interval / 60000;
          $scope.intervalTimeConstant = "60000";
          $scope.intervalTimeConstantString = $filter('translate')('Minutes');
        }
      },function(error){
        displayRestError.display(error);
      });
  }

  //GUI page settings
  $scope.saveProgress = false;
  
  //Load details
  $scope.resetSettings();

  $scope.save = function(){
    if($scope.item.enabled){
      //Update time
      $scope.item.interval = $scope.intervalLocal * $scope.intervalTimeConstant;
    }    
    $scope.saveProgress = true;
    BackupRestoreFactory.updateBackupSettings($scope.item,function(response) {
      $scope.saveProgress = false;
      $scope.editEnable.backupSettings = false;
      $scope.resetSettings();
    },function(error){
      displayRestError.display(error);
      $scope.saveProgress = false;
    });
  }
});
