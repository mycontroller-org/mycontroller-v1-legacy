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

angular.module('adf.widget.myc-sensors-bullet-graph', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycSensorsBulletGraph', {
        title: 'Sensors bullet graph',
        description: 'Monitor sensors value with bullet graph',
        templateUrl: 'controllers/adf-widgets/adf-myc-sbg/view.html',
        controller: 'mycSensorsBulletGraphController',
        controllerAs: 'mycSensorsBulletGraph',
        config: {
          colorUp:"#3f9c35",
          colorDown:"#c00000",
          chartFromTimestamp:'3600000',
          variableIds:[],
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-sbg/edit.html',
          controller: 'mycSensorsBulletGraphEditController',
          controllerAs: 'mycSensorsBulletGraphEdit',
        }
      });
  })
  .controller('mycSensorsBulletGraphController', function($scope, $interval, config, mchelper, $filter, MetricsFactory){
    var mycSensorsBulletGraph = this;
    mycSensorsBulletGraph.showLoading = true;
    mycSensorsBulletGraph.isSyncing = true;
    mycSensorsBulletGraph.variables = {};
    $scope.tooltipEnabled = false;
    $scope.hideVariableName=true;

    mycSensorsBulletGraph.variables = {};

    mycSensorsBulletGraph.chartOptions = {
        chart: {
            type: 'bulletChart',
            transitionDuration: 500,
            //color: config.color, //rgb(31, 119, 180)
            noData: $filter('translate')('NO_DATA_AVAILABLE'),
            margin: {
              top: 8,
              right: 10,
              bottom: 21,
              left: 5,
            },
        }
      };

    mycSensorsBulletGraph.getChartOptions = function(){
      return angular.copy(mycSensorsBulletGraph.chartOptions);
    }

    function loadVariables(){
      mycSensorsBulletGraph.isSyncing = true;
      MetricsFactory.getBulletChart({'variableId':config.variableIds, "timestampFrom": new Date().getTime() - config.chartFromTimestamp}, function(response){
          mycSensorsBulletGraph.sensorVariables = response;
          angular.forEach(mycSensorsBulletGraph.sensorVariables, function(item){
            if(item.markers && item.markers[0]){
              if(parseFloat(item.markers[0]) <= parseFloat(item.measures[0])){
                item.color = config.colorUp;
              }else{
                item.color = config.colorDown;
              }
            }else{
              item.color = "#1f77b4";//default color
            }
          });
          mycSensorsBulletGraph.isSyncing = false;
          if(mycSensorsBulletGraph.showLoading){
            mycSensorsBulletGraph.showLoading = false;
          }
      });
    };

    function updateVariables(){
      if(mycSensorsBulletGraph.isSyncing){
        return;
      }else if(config.variableIds.length > 0){
        loadVariables();
      }
    }

    //load variables initially
    if(config.variableIds.length > 0){
      loadVariables();
    }else{
      mycSensorsBulletGraph.showLoading = false;
    }
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
  }).controller('mycSensorsBulletGraphEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, CommonServices){
    var mycSensorsBulletGraphEdit = this;
    mycSensorsBulletGraphEdit.cs = CommonServices;
    //TODO: get only DOUBLE type devices
    mycSensorsBulletGraphEdit.variables = TypesFactory.getSensorVariables({"metricType":"Double"});
    //Swap up down color
    mycSensorsBulletGraphEdit.swapColor = function(){
      var colorUp = config.colorUp;
      config.colorUp = config.colorDown;
      config.colorDown = colorUp;
    }
  });
