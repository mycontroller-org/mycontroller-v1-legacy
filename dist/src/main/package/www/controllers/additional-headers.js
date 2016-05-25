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
//Additional headers update controller
myControllerModule.controller('AdditionalHeadersUpdateController', function ($scope, $stateParams, $state,
  SettingsFactory, mchelper, alertService, displayRestError, $filter, CommonServices, $base64) {
  $scope.mchelper = mchelper;
  $scope.additionalHeaders = {};

  //Get data
  $scope.loadData = function(){
    SettingsFactory.getHtmlAdditionalHeaders(function(response){
      $scope.additionalHeaders = response;
      $scope.cssFiles = response.links.join('\n');
      $scope.scriptFiles = response.scripts.join('\n');
    },function(error){
      displayRestError.display(error);
    });
  };

  //GUI page settings
  $scope.headerStringAdd = $filter('translate')('HTML_ADDITIONAL_HEADERS');
  $scope.cancelButtonState = "additionalHeadersUpdate"; //Cancel button url
  $scope.saveProgress = false;
  $scope.loadData();

  $scope.save = function(){
    $scope.saveProgress = true;
    SettingsFactory.updateHtmlAdditionalHeaders($scope.additionalHeaders, function(response) {
      alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
      $scope.saveProgress = false;
    },function(error){
      displayRestError.display(error);
        $scope.saveProgress = false;
    });
  }
});
