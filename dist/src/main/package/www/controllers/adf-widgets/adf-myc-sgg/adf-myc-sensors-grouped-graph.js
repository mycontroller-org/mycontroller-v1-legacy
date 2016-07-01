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

angular.module('adf.widget.myc-sensors-grouped-graph', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycSensorsGroupedGraph', {
        title: 'Grouped sensors graph',
        description: 'Similar type of sensors grouped graphical view',
        templateUrl: 'controllers/adf-widgets/adf-myc-sgg/view.html',
        controller: 'mycSensorsGroupedGraphController',
        controllerAs: 'mycSensorsGroupedGraph',
        config: {
          useInteractiveGuideline:true,
          variableId:[],
          variableType:null,
          chartFromTimestamp:'3600000',
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-sgg/edit.html',
          controller: 'mycSensorsGroupedGraphEditController',
          controllerAs: 'mycSensorsGroupedGraphEdit',
        }
      });
  })
  .controller('mycSensorsGroupedGraphController', function($scope, $interval, config, mchelper, $filter, MetricsFactory, CommonServices){
    var mycSensorsGroupedGraph = this;
    mycSensorsGroupedGraph.showLoading = true;
    mycSensorsGroupedGraph.showError = false;
    mycSensorsGroupedGraph.isSyncing = false;
    mycSensorsGroupedGraph.cs = CommonServices;

    mycSensorsGroupedGraph.chartOptions = {
        chart: {
            type: 'lineChart',
            noErrorCheck: true,
            height: 225,
            margin : {
                top: 5,
                right: 20,
                bottom: 60,
                left: 65
            },
            color: d3.scale.category10().range(),
            noData: $filter('translate')('NO_DATA_AVAILABLE'),
            x: function(d){return d[0];},
            y: function(d){return d[1];},
            useVoronoi: !config.useInteractiveGuideline,
            useInteractiveGuideline: config.useInteractiveGuideline,
            clipEdge: false,
            xAxis: {
                showMaxMin: false,
                tickFormat: function(d) {
                    return d3.time.format('hh:mm a')(new Date(d))
                },
                //axisLabel: 'Timestamp',
                rotateLabels: -20
            },
            yAxis: {
                tickFormat: function(d){
                    return d3.format(',.2f')(d);
                },
                axisLabelDistance: -10,
                //axisLabel: ''
            },
        },
          title: {
            enable: false,
            text: 'Title'
        }
    };

    mycSensorsGroupedGraph.chartTimeFormat = mchelper.cfg.dateFormat;


    function updateChart(){
      mycSensorsGroupedGraph.isSyncing = true;
      MetricsFactory.getMetricsData({"variableId":config.variableId, "chartType":"lineChart", "timestampFrom": new Date().getTime() - config.chartFromTimestamp}, function(resource){
        if(resource.length > 0){
           mycSensorsGroupedGraph.chartData = resource[0].chartData;
          //Update display time format
          mycSensorsGroupedGraph.chartTimeFormat = resource[0].timeFormat;
          mycSensorsGroupedGraph.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, mycSensorsGroupedGraph.chartTimeFormat, mchelper.cfg.timezone)};
          mycSensorsGroupedGraph.chartOptions.chart.interpolate = resource[0].chartInterpolate;

          if(resource[0].unit === ''){
            mycSensorsGroupedGraph.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format('.0f')(d);};
          }else{
            mycSensorsGroupedGraph.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format('.02f')(d) + ' ' + resource[0].unit;};
          }

        }else{
          if(config.variableId.length !== 0){
            mycSensorsGroupedGraph.showError = true;
          }
        }
        mycSensorsGroupedGraph.isSyncing = false;
        if(mycSensorsGroupedGraph.showLoading){
          mycSensorsGroupedGraph.showLoading = false;
        }
      });
    }

    function updateVariables(){
      if(mycSensorsGroupedGraph.isSyncing){
        return;
      }else if(config.variableId.length !== 0){
        updateChart();
      }else{
        mycSensorsGroupedGraph.showLoading = false;
      }
    }

    //load graph initially
    updateVariables();

    // refresh every second
    var promise = $interval(updateVariables, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });


  }).controller('mycSensorsGroupedGraphEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, CommonServices){
    var mycSensorsGroupedGraphEdit = this;
    mycSensorsGroupedGraphEdit.cs = CommonServices;

    mycSensorsGroupedGraphEdit.onVariableTypeChange = function(){
      config.variableId = [];
      if(config.variableType){
        mycSensorsGroupedGraphEdit.variables = TypesFactory.getSensorVariables({"variableType":config.variableType});
      }else{
        mycSensorsGroupedGraphEdit.variables = {};
      }
    };

    //Load variable types
    mycSensorsGroupedGraphEdit.variableTypes = TypesFactory.getSensorVariableTypes({"metricType":["Double","Binary", "Counter"]});
    if(config.variableType){
      var variableIdRef = config.variableId;
      mycSensorsGroupedGraphEdit.onVariableTypeChange();
      config.variableId = variableIdRef;
    }
  });
