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

angular.module('adf.widget.myc-sensors-mixed-graph', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycSensorsMixedGraph', {
        title: 'Mixed sensors graph',
        description: 'Different type of sensors mixed graphical view [refer document]',
        templateUrl: 'controllers/adf-widgets/adf-myc-smg/view.html',
        controller: 'mycSensorsMixedGraphController',
        controllerAs: 'mycSensorsMixedGraph',
        config: {
          useInteractiveGuideline:false,
          chartInterpolate:"linear",
          variableId:[],
          variableType:[],
          chartFromTimestamp:'3600000',
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-smg/edit.html',
          controller: 'mycSensorsMixedGraphEditController',
          controllerAs: 'mycSensorsMixedGraphEdit',
        }
      });
  })
  .controller('mycSensorsMixedGraphController', function($scope, $interval, config, mchelper, $filter, MetricsFactory){
    var mycSensorsMixedGraph = this;
    mycSensorsMixedGraph.showLoading = true;
    mycSensorsMixedGraph.showError = false;
    mycSensorsMixedGraph.isSyncing = false;

    mycSensorsMixedGraph.chartOptions = {
        chart: {
            type: 'multiChart',
            noErrorCheck: true,
            height: 225,
            margin : {
                top: 5,
                bottom: 60,
                right: 65,
                left: 65
            },
            color: d3.scale.category10().range(),
            duration: 500,
            noData: $filter('translate')('NO_DATA_AVAILABLE'),

            //x: function(d,i){return d[0];},
            //y: function(d,i){return d[1];},
            clipEdge: false,
            useVoronoi: !config.useInteractiveGuideline,
            useInteractiveGuideline: config.useInteractiveGuideline,

            xAxis: {
                showMaxMin: false,
                tickFormat: function(d) {
                    return d3.time.format('hh:mm a')(new Date(d))
                },
                //axisLabel: 'Timestamp',
                rotateLabels: -20
            },
            yAxis1: {
               axisLabelDistance: -10,
                //axisLabel: ''
            },
            yAxis2: {
                axisLabelDistance: -10,
                //axisLabel: ''
            },
        },
          title: {
            enable: false,
            text: 'Title'
        }
    };

    mycSensorsMixedGraph.chartTimeFormat = mchelper.cfg.dateFormat;


    function updateChart(){
      mycSensorsMixedGraph.isSyncing = true;
      MetricsFactory.getMetricsData({"variableId":config.variableId, "chartType":"multiChart", "timestampFrom": new Date().getTime() - config.chartFromTimestamp}, function(resource){
        if(resource.length > 0){
           mycSensorsMixedGraph.chartData = resource[0].chartData;
          //Update display time format
          mycSensorsMixedGraph.chartTimeFormat = resource[0].timeFormat;
          mycSensorsMixedGraph.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, mycSensorsMixedGraph.chartTimeFormat, mchelper.cfg.timezone)};
          mycSensorsMixedGraph.chartOptions.chart.interpolate = config.chartInterpolate;

          if(resource[0].unit === ''){
            mycSensorsMixedGraph.chartOptions.chart.yAxis1.tickFormat = function(d){return d3.format('.0f')(d);};
          }else{
            //Not displaying properly axis unit, there is an issue
            if(config.useInteractiveGuideline){
              mycSensorsMixedGraph.chartOptions.chart.yAxis1.tickFormat = function(d){return d3.format('.02f')(d) + ' ' + resource[0].unit;};
            }else{
              mycSensorsMixedGraph.chartOptions.chart.yAxis1.tickFormat = function(d){return d3.format('.02f')(d)};
            }
          }

          if(resource[0].unit2 === ''){
            mycSensorsMixedGraph.chartOptions.chart.yAxis2.tickFormat = function(d){return d3.format('.0f')(d);};
          }else{
            mycSensorsMixedGraph.chartOptions.chart.yAxis2.tickFormat = function(d){return d3.format('.02f')(d) + ' ' + resource[0].unit2;};
          }

        }else{
          if(config.variableId.length !== 0){
            mycSensorsMixedGraph.showError = true;
          }
        }
        mycSensorsMixedGraph.isSyncing = false;
        if(mycSensorsMixedGraph.showLoading){
          mycSensorsMixedGraph.showLoading = false;
        }
      });
    }

    function updateVariables(){
      if(mycSensorsMixedGraph.isSyncing){
        return;
      }else if(config.variableId.length !== 0){
        updateChart();
      }
    }

    //load graph initially
    updateVariables();

    // refresh every second
    var promise = $interval(updateChart, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });


  }).controller('mycSensorsMixedGraphEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, CommonServices){
    var mycSensorsMixedGraphEdit = this;

    mycSensorsMixedGraphEdit.onVariableTypeChange = function(){
      if(config.variableType.length > 0){
        TypesFactory.getSensorVariables({"variableType":config.variableType}, function(response){
          mycSensorsMixedGraphEdit.variables = response;
          var newVariableId = [];
          response.forEach(function(item) {
            if(config.variableId.indexOf(item.id.toString()) !== -1){
              newVariableId.push(item.id.toString());
            }
          });
          config.variableId = newVariableId;
        });
      }else{
        mycSensorsMixedGraphEdit.variables = {};
        config.variableId = [];
      }
    };

    //Pre load
    mycSensorsMixedGraphEdit.cs = CommonServices;
    //Load variable types
    mycSensorsMixedGraphEdit.variableTypes = TypesFactory.getSensorVariableTypes({"metricType":["Double","Binary","Counter"]});
    if(config.variableType.length > 0){
      var variableIdRef = config.variableId;
      mycSensorsMixedGraphEdit.onVariableTypeChange();
      config.variableId = variableIdRef;
    }
  });
