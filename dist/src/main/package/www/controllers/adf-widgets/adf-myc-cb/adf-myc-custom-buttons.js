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
// don't forget to declare this service module as a dependency in your main app constructor!
//http://js2.coffee/#coffee2js
//https://coderwall.com/p/r_bvhg/angular-ui-bootstrap-alert-service-for-angular-js

'use strict';

angular.module('adf.widget.myc-custom-buttons', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycCustomBtns', {
        title: 'Sensor custom buttons',
        description: 'Create custom buttons',
        templateUrl: 'controllers/adf-widgets/adf-myc-cb/view.html',
        controller: 'mycCusBtnsController',
        controllerAs: 'mycCBtns',
        config: {
          variableId:null,
          refreshTime:30,
          minBtnHeight:30,
          minBtnWidth:90,
          buttonsJson:"[\n]",
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-cb/edit.html',
          controller: 'mycSenVarsEditController',
          controllerAs: 'mycCBtnsEdit',
        }
      });
  })
  .controller('mycCusBtnsController', function($scope, $interval, config, mchelper, $filter, SensorsFactory, TypesFactory, CommonServices){
    var mycCBtns = this;

    mycCBtns.showLoading = true;
    mycCBtns.isSyncing = true;
    mycCBtns.variable = {};
    $scope.tooltipEnabled = false;
    $scope.hideVariableName=true;
    $scope.cs = CommonServices;
    mycCBtns.buttons = angular.fromJson(config.buttonsJson);


    function loadVariable(){
      mycCBtns.isSyncing = true;
      SensorsFactory.getVariables({'ids':config.variableId}, function(response){
        if(response.length > 0){
          mycCBtns.variable = response[0];
        }
        mycCBtns.isSyncing = false;
        if(mycCBtns.showLoading){
          mycCBtns.showLoading = false;
        }
      });
    };

    function updateVariable(){
      if(mycCBtns.isSyncing){
        return;
      }else if(config.variableId){
        loadVariable();
      }
    }

    //load variables initially
    loadVariable();
    //updateVariables();

    //Update Variable / Send Payload
    $scope.updateSVariable = function(button){
      var variable = angular.copy(mycCBtns.variable);
      variable.value = button.payload;
      SensorsFactory.updateVariable(variable, function(){
        //update Success
        loadVariable();
      },function(error){
        displayRestError.display(error);
      });
    };

    // refresh every second
    var promise = $interval(updateVariable, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });
  }).controller('mycCusBtnsEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, CommonServices){
    var mycCBtnsEdit = this;
    mycCBtnsEdit.cs = CommonServices;
    mycCBtnsEdit.variables = TypesFactory.getSensorVariables();
  });
