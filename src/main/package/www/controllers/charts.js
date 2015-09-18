/*
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
myControllerModule.controller('ChartsController', function($scope, $stateParams, MetricsFactory, about, $filter) {
  
  //http://krispo.github.io/angular-nvd3
    var chartOptions = {
            chart: {
                type: 'lineChart',
                interpolate: 'linear',
                noErrorCheck: true,
                height: 270,
                margin : {
                    top: 0,
                    right: 20,
                    bottom: 60,
                    left: 40
                },
                color: ["#2ca02c","#1f77b4", "#ff7f0e"],
              
                x: function(d){return d[0];},
                y: function(d){return d[1];},
                useVoronoi: false,
                clipEdge: false,
                transitionDuration: 500,
                useInteractiveGuideline: true,
                xAxis: {
                    showMaxMin: false,
                    tickFormat: function(d) {
                        return d3.time.format('HH:mm')(new Date(d))
                    },
                    //axisLabel: 'Timestamp',
                    rotateLabels: -20
                },
                yAxis: {
                    tickFormat: function(d){
                        return d3.format(',.2f')(d);
                    },
                    //axisLabel: ''
                }
            },
              title: {
                enable: false,
                text: 'Last one hour (1 minute interval)'
            }
        };
        
  
  $scope.sensor = MetricsFactory.sensorData({"sensorId":$stateParams.sensorId}, function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  //about, Timezone, etc.,
  $scope.about = about;                    
  
  $scope.sensor.$promise.then(function (sensor) {
    $scope.sensor = sensor;
    if($scope.sensor.metricType === 0){
    var yAxisD3Format=',.2f';
    var chartLineColor=["#2ca02c","#1f77b4", "#ff7f0e"];
    var chartInterpolate='linear';//cardinal
    var lastOneHourDateFormat = 'HH:mm'; //https://docs.angularjs.org/api/ng/filter/date
    var last24HoursDateFormat = 'HH:mm';
    var last30DaysDateFormat = 'dd-MMM HH:mm';
    var allDataDateFormat = 'dd-MMM-yyyy';
    var lastOneHourText = 'Last one hour (1 minute interval)';
    var last24HoursText = 'Last 24 hours (5 minutes interval)';
    var last30DaysText = 'Last 30 days (1 hour interval)';
    var allDataText = 'All available data (1 day interval)';
  }else if($scope.sensor.metricType === 1){
    var yAxisD3Format='.0f';
    var chartLineColor=["#1f77b4"];
    var chartInterpolate='step-after';
    var lastOneHourDateFormat = 'HH:mm:ss';
    var last24HoursDateFormat = 'HH:mm:ss';
    var last30DaysDateFormat = 'dd-MMM HH:mm:ss';
    var allDataDateFormat = 'dd-MMM-yyyy HH:mm:ss';
    var lastOneHourText = 'Last one hour';
    var last24HoursText = 'Last 24 hours';
    var last30DaysText = 'Last 30 days';
    var allDataText = 'All available data';
  }
   //http://www.d3noob.org/2013/01/smoothing-out-lines-in-d3js.html
   

        
          chartOptions.chart.type = 'lineChart'; //workaround to suppress 'type undefined error'
          chartOptions.chart.interpolate = chartInterpolate;
          chartOptions.chart.color = chartLineColor;
          chartOptions.chart.yAxis.tickFormat = function(d){return d3.format(yAxisD3Format)(d);};
        
          $scope.chartOptionsLastOneHour = chartOptions;
          $scope.chartOptionsLastOneHour.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, lastOneHourDateFormat, about.timezone)};
          $scope.chartOptionsLastOneHour.title.text = lastOneHourText;

          $scope.chartOptionsLast24Hours = angular.copy(chartOptions);
          $scope.chartOptionsLast24Hours.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, last24HoursDateFormat, about.timezone)};
          $scope.chartOptionsLast24Hours.title.text = last24HoursText;
          
          $scope.chartOptionsLast30Days = angular.copy(chartOptions);
          $scope.chartOptionsLast30Days.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, last30DaysDateFormat, about.timezone)};
          $scope.chartOptionsLast30Days.title.text = last30DaysText;
          
          $scope.chartOptionsAllDays = angular.copy(chartOptions);
          $scope.chartOptionsAllDays.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, allDataDateFormat, about.timezone)};
          $scope.chartOptionsAllDays.title.text = allDataText;
          
});
  
  $scope.chartOptionsLastOneHour = chartOptions;
  $scope.chartOptionsLast24Hours = angular.copy(chartOptions);
  $scope.chartOptionsLast30Days = angular.copy(chartOptions);
  $scope.chartOptionsAllDays = angular.copy(chartOptions);
  
 //Get list of Sensors
    $scope.getMetrics = function(){
      //$scope.data = MetricsFactory.last5Minutes({"sensorId":"1"});
      //$scope.sensor = MetricsFactory.sensorData({"sensorId":$stateParams.sensorId});
      $scope.lastOneHourChartMetrics = MetricsFactory.lastOneHour({"sensorId":$stateParams.sensorId});
      $scope.last24HoursChartMetrics = MetricsFactory.last24Hours({"sensorId":$stateParams.sensorId});
      $scope.last30DaysChartMetrics = MetricsFactory.last30Days({"sensorId":$stateParams.sensorId});
      $scope.allDaysChartMetrics = MetricsFactory.allYears({"sensorId":$stateParams.sensorId});
    }
    $scope.getMetrics();
    //setInterval($scope.getMetrics, 1000*60);
  
  
        
});
