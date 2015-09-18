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
myControllerModule.controller('SettingsController', function(alertService,
$scope, $filter, SettingsFactory, $location, $modal, $stateParams, displayRestError, about) {
  
  $scope.config = {
    itemsPerPage: 100,
    maxPages:1,
    fillLastPage: false
  }
  
  //about, Timezone, etc.,
  $scope.about = about;

  //settings SunriseSunset List
   $scope.settingsSunriseSunsetList = SettingsFactory.getSunriseSunset(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });  

  //settings NodeDefaults List
   $scope.settingsNodeDefaultsList = SettingsFactory.getNodeDefaults(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });  
                    
  //settings SMS List
   $scope.settingsSMSList = SettingsFactory.getSMS(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    }); 
                    
  //settings Email List
   $scope.settingsEmailList = SettingsFactory.getEmail(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    }); 

  //settings Units List
   $scope.settingsUnitsList = SettingsFactory.getUnits(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
                    
                    
  //settings Version List
   $scope.settingsVersionList = SettingsFactory.getVersion(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  
  //Update settings
  $scope.update = function (settings, size) {
    var editModalInstance = $modal.open({
    templateUrl: 'partials/settings/updateModal.html',
    controller: 'SSMupdateController',
    size: size,
    resolve: {settings: function () {return settings;}}
    });

    editModalInstance.result.then(function (updateSettings) {
      SettingsFactory.update(updateSettings,function(response) {
        alertService.success("Updated ["+settings.frindlyName+"]");
        //Update display table
        $scope.orgList = SettingsFactory.getAll( function(response) {
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

myControllerModule.controller('SSMupdateController', function ($scope, $modalInstance, settings, TypesFactory, FirmwaresFactory) {
  $scope.settings = settings;
  $scope.header = "Update Settings : "+settings.frindlyName;
  if(settings.key == 'default_firmware'){
    $scope.firmwares = FirmwaresFactory.getAllFirmwares();
  }
  $scope.update = function() {$modalInstance.close(settings);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
