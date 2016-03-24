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

angular.module('adf.widget.myc-sen-vars', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycSenVars', {
        title: 'Sensors',
        description: 'Monitor and change sensors state',
        templateUrl: 'controllers/adf-widgets/adf-myc-sen-vars/view.html',
        controller: 'mycSenVarsController',
        controllerAs: 'mycSenVars',
        config: {
          variableIds:[],
          itemsPerRow:"2",
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-sen-vars/edit.html',
          controller: 'mycSenVarsEditController',
          controllerAs: 'mycSenVarsEdit',
        }
      });
  })
  .controller('mycSenVarsController', function($scope, $interval, config, mchelper, $filter, SensorsFactory, TypesFactory, CommonServices){
    var mycSenVars = this;

    mycSenVars.showLoading = true;
    mycSenVars.isSyncing = true;
    mycSenVars.variables = {};
    $scope.tooltipEnabled = false;
    $scope.hideVariableName=true;
    $scope.cs = CommonServices;

    //HVAC heater options - HVAC flow state
    $scope.hvacOptionsFlowState = TypesFactory.getHvacOptionsFlowState();
    //HVAC heater options - HVAC flow mode
    $scope.hvacOptionsFlowMode = TypesFactory.getHvacOptionsFlowMode();
    //HVAC heater options - HVAC fan speed
    $scope.hvacOptionsFanSpeed = TypesFactory.getHvacOptionsFanSpeed();

    //Defined variable types list
    $scope.definedVariableTypes = CommonServices.getSensorVariablesKnownList();



  //update rgba color
  $scope.updateRgba = function(variable){
    variable.value = CommonServices.rgba2hex(variable.rgba);
    $scope.updateVariable(variable);
  };

    function loadVariables(){
      mycSenVars.isSyncing = true;
      SensorsFactory.getVariables({'ids':config.variableIds}, function(response){
          mycSenVars.variables = response;
          mycSenVars.isSyncing = false;
          if(mycSenVars.showLoading){
            mycSenVars.showLoading = false;
          }
      });
    };

    function updateVariables(){
      if(mycSenVars.isSyncing){
        return;
      }else if(config.variableIds.length > 0){
        loadVariables();
      }
    }

    //load variables initially
    loadVariables();
    //updateVariables();

    //Update Variable / Send Payload
    $scope.updateVariable = function(variable){
      SensorsFactory.updateVariable(variable, function(){
        //update Success
      },function(error){
        displayRestError.display(error);
      });
    };

    // refresh every second
    var promise = $interval(updateVariables, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });
  }).controller('mycSenVarsEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, CommonServices){
    var mycSenVarsEdit = this;
    mycSenVarsEdit.cs = CommonServices;
    mycSenVarsEdit.variables = TypesFactory.getSensorVariables();
  });
