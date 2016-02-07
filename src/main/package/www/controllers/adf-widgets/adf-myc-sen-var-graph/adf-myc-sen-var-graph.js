/*
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
// don't forget to declare this service module as a dependency in your main app constructor!
//http://js2.coffee/#coffee2js
//https://coderwall.com/p/r_bvhg/angular-ui-bootstrap-alert-service-for-angular-js

'use strict';

angular.module('adf.widget.myc-sen-var-graph', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycSenVarGraph', {
        title: 'Sensor graphical view',
        description: 'Monitor sensor with graphical support',
        templateUrl: 'controllers/adf-widgets/adf-myc-sen-var-graph/view.html',
        controller: 'mycSenVarGraphController',
        controllerAs: 'mycSenVarGraph',
        config: {
          variableId:null,
          withMinMax:false,
          chartFromTimestamp:'3600000',
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-sen-var-graph/edit.html',
          controller: 'mycSenVarGraphEditController',
          controllerAs: 'mycSenVarGraphEdit',
        }
      });
  })
  .controller('mycSenVarGraphController', function($scope, $interval, config, mchelper, $filter, MetricsFactory, TypesFactory, CommonServices){
    var mycSenVarGraph = this;
    
    mycSenVarGraph.showLoading = true;
    mycSenVarGraph.showError = false;
    mycSenVarGraph.isSyncing = true;
    mycSenVarGraph.variables = {};
    $scope.tooltipEnabled = false;
    $scope.hideVariableName=true;
    $scope.cs = CommonServices;
    
    mycSenVarGraph.chartOptions = {
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
    
    mycSenVarGraph.chartTimeFormat = mchelper.cfg.dateFormat;
    mycSenVarGraph.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, mycSenVarGraph.chartTimeFormat, mchelper.cfg.timezone)};
    
    function updateChart(){
      mycSenVarGraph.isSyncing = true;
      MetricsFactory.getMetricsData({"variableId":config.variableId, "withMinMax":config.withMinMax, "timestampFrom": new Date().getTime() - config.chartFromTimestamp}, function(resource){
        if(resource.length > 0){
           mycSenVarGraph.chartData = resource[0].chartData;
          //Update display time format
          mycSenVarGraph.chartTimeFormat = resource[0].timeFormat;
          mycSenVarGraph.chartOptions.chart.type = resource[0].chartData[0].type;
          mycSenVarGraph.chartOptions.chart.interpolate = resource[0].chartData[0].interpolate;

          if(resource[0].dataType === 'Double'){
            mycSenVarGraph.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format('.02f')(d) + ' ' + resource[0].unit};
          }else if(resource[0].dataType === 'Binary'){
            mycSenVarGraph.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format('.0f')(d)};
          }
          mycSenVarGraph.chartOptions.title.text = resource[0].variableType;
          mycSenVarGraph.resourceName = resource[0].resourceName;
        }else{
          if(config.variableId !== null){
            mycSenVarGraph.showError = true;
          }
        }
        mycSenVarGraph.isSyncing = false;
        if(mycSenVarGraph.showLoading){
          mycSenVarGraph.showLoading = false;
        }
      });
    }
    
    function updateVariables(){
      if(mycSenVarGraph.isSyncing){
        return;
      }else if(config.variableId !== null){
        updateChart();
      }
    }
    
    //load graph initially
    updateChart();
    
    // refresh every second
    var promise = $interval(updateChart, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });
  }).controller('mycSenVarGraphEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory){
    var mycSenVarGraphEdit = this;
    mycSenVarGraphEdit.variables = TypesFactory.getSensorVariables();
  });
