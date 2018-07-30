/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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
// don't forget to declare this service module as a dependency in your main app constructor!
//http://js2.coffee/#coffee2js
//https://coderwall.com/p/r_bvhg/angular-ui-bootstrap-alert-service-for-angular-js

'use strict';

angular.module('adf.widget.myc-os-commands', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycOsCommands', {
        title: 'OS Commands',
        description: 'Create buttons to run Operating System Commands',
        templateUrl: 'controllers/adf-widgets/adf-myc-os/view.html?mcv=${mc.gui.version}',
        controller: 'mycOsCommandController',
        controllerAs: 'mycOsBtns',
        config: {
          minBtnHeight:30,
          minBtnWidth:90,
          buttonsJson:"[\n]",
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-os/edit.html?mcv=${mc.gui.version}',
          controller: 'mycOsCommandEditController',
          controllerAs: 'mycOsBtnsEdit',
        }
      });
  })
  .controller('mycOsCommandController', function($scope, $interval, config, mchelper, $uibModal, $filter, OSCommandFactory, CommonServices){
    var mycOsBtns = this;

    mycOsBtns.showLoading = false;
    $scope.tooltipEnabled = false;
    $scope.cs = CommonServices;
    mycOsBtns.buttons = angular.fromJson(config.buttonsJson);

    // execute OS command directly
    $scope.executeOsCommandDirect = function(button){
      var request = {};
      request.os = button.os;
      request.command = button.command;
      OSCommandFactory.execute(request, function(response){
        if(response.error === undefined){
          alertService.success(response.result);
        }else {
          alertService.danger(angular.toJson(response));
        }
      },function(error){
        displayRestError.display(error);
      });
    };

    // execute OS command with confirmation check
    $scope.executeOsCommand = function (button) {
      if(button.confirmation  === true){
         var addModalInstance = $uibModal.open({
        templateUrl: 'controllers/adf-widgets/adf-myc-os/confirmation-modal.html?mcv=${mc.gui.version}',
        controller: 'CommandConfirmationController',
        resolve: {button: button}
        });

        addModalInstance.result.then(function () {
          $scope.executeOsCommandDirect(button);
        }),
        function () {
          //console.log('Modal dismissed at: ' + new Date());
        }
      } else {
        $scope.executeOsCommandDirect(button);
      }
    };


  }).controller('mycOsCommandEditController', function($scope, $interval, config, mchelper, $filter, CommonServices){
    var mycOsBtnsEdit = this;
    mycOsBtnsEdit.cs = CommonServices;

  }).controller('CommandConfirmationController', function ($scope, $uibModalInstance, $filter, button) {
  $scope.header = $filter('translate')('OS_COMMAND_EXECTION_CONFIRMATION_TITLE');
  $scope.button = button;
  $scope.reboot = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});
