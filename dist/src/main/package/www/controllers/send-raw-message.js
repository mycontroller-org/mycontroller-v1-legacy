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
myControllerModule.controller('SendRawMessageController', function(alertService, $scope, displayRestError, TypesFactory, SensorsFactory, $filter, CommonServices) {

  //GUI page settings
  $scope.headerStringAdd = $filter('translate')('SEND_RAW_MESSAGE');
  $scope.cancelButtonState = "sendRawMessage"; //Cancel button state
  $scope.sendProgress = false;
  $scope.cs = CommonServices;

  $scope.message = {};

  //Get subtypes
  $scope.updateSubTypes = function(type){
    $scope.subTypes = TypesFactory.getMessageSubTypes({"messageType":type});
  };

  //Pre load
  $scope.gateways = CommonServices.getResources("Gateway");
  $scope.messageTypes = TypesFactory.getMessageTypes();

  //Send raw message
  $scope.send = function(){
    $scope.saveProgress = true;
    SensorsFactory.sendRawMessage($scope.message, function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $scope.saveProgress = false;
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
  };
});
