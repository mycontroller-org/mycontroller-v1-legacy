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

angular.module('adf.widget.myc-a-sensor-graph', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycSingleSensorGraph', {
        title: 'A sensor graphical view',
        description: 'Displays a sensor graphical view',
        templateUrl: 'controllers/adf-widgets/adf-myc-asg/view.html',
        controller: 'mycSingleSensorGraphController',
        controllerAs: 'mycSingleSensorGraph',
        config: {
          variableId:null,
          withMinMax:false,
          chartFromTimestamp:'3600000',
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-asg/edit.html',
          controller: 'mycSingleSensorGraphEditController',
          controllerAs: 'mycSingleSensorGraphEdit',
        }
      });
  })
  .controller('mycSingleSensorGraphController', function($scope, $interval, config, mchelper, $filter, MetricsFactory, TypesFactory, CommonServices){
    var mycSingleSensorGraph = this;

    mycSingleSensorGraph.showLoading = true;
    mycSingleSensorGraph.showError = false;
    mycSingleSensorGraph.isSyncing = false;
    mycSingleSensorGraph.variables = {};
    $scope.tooltipEnabled = false;
    $scope.hideVariableName=true;
    $scope.cs = CommonServices;

    mycSingleSensorGraph.chartOptions = {
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
            color: ["#2ca02c","#1f77b4", "#ff7f0e"],
            noData:"No data available.",
            x: function(d){return d[0];},
            y: function(d){return d[1];},
            useVoronoi: false,
            clipEdge: false,
            useInteractiveGuideline: true,
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

    mycSingleSensorGraph.chartTimeFormat = mchelper.cfg.dateFormat;
    mycSingleSensorGraph.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, mycSingleSensorGraph.chartTimeFormat, mchelper.cfg.timezone)};

    function updateChart(){
      mycSingleSensorGraph.isSyncing = true;
      MetricsFactory.getMetricsData({"variableId":config.variableId, "withMinMax":config.withMinMax, "timestampFrom": new Date().getTime() - config.chartFromTimestamp}, function(resource){
        if(resource.length > 0){
           mycSingleSensorGraph.chartData = resource[0].chartData;
          //Update display time format
          mycSingleSensorGraph.chartTimeFormat = resource[0].timeFormat;
          mycSingleSensorGraph.chartOptions.chart.type = resource[0].chartType;
          mycSingleSensorGraph.chartOptions.chart.interpolate = resource[0].chartInterpolate;

          if(resource[0].dataType === 'Double'){
            mycSingleSensorGraph.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format('.02f')(d) + ' ' + resource[0].unit};
          }else if(resource[0].dataType === 'Binary' || resource[0].dataType === 'Counter'){
            mycSingleSensorGraph.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format('.0f')(d)};
          }
          mycSingleSensorGraph.chartOptions.title.text = resource[0].variableType;
          mycSingleSensorGraph.resourceName = resource[0].resourceName;
        }else{
          if(config.variableId !== null){
            mycSingleSensorGraph.showError = true;
          }
        }
        mycSingleSensorGraph.isSyncing = false;
        if(mycSingleSensorGraph.showLoading){
          mycSingleSensorGraph.showLoading = false;
        }
      });
    }

    function updateVariables(){
      if(mycSingleSensorGraph.isSyncing){
        return;
      }else if(config.variableId !== null){
        updateChart();
      }else{
        mycSingleSensorGraph.showLoading = false;
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
  }).controller('mycSingleSensorGraphEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, CommonServices){
    var mycSingleSensorGraphEdit = this;
    mycSingleSensorGraphEdit.variables = TypesFactory.getSensorVariables({"metricType":["Double","Binary","Counter"]});
    mycSingleSensorGraphEdit.cs = CommonServices;
  });
