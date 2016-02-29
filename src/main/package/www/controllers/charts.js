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
myControllerModule.controller('ChartsController', function($scope, $stateParams, MetricsFactory,
  mchelper, $filter, SettingsFactory, TypesFactory, SensorsFactory, displayRestError, FileSaver, Blob, $filter) {
    
  //Get Chart Interpolate Type
  $scope.interpolateType = SettingsFactory.get({key_:'graph_interpolate_type'});
  $scope.hourFormat = 'hh';
  $scope.hourFormatSufix = ' a';
  SettingsFactory.get({key_:'mc_time_12_24_format'}, function(response) {
      if(response.value == '24'){
        $scope.hourFormat = 'HH';
        $scope.hourFormatSufix = '';
      }
    },function(error){
      displayRestError.display(error);            
    });
  
  $scope.sensor = SensorsFactory.getSensorByRefId({"sensorRefId":$stateParams.sensorId}, function(response) {
    },function(error){
      displayRestError.display(error);            
    });
  //config, language, user, etc.,
  $scope.mchelper = mchelper;  
  $scope.variableType = {};
  
  $scope.variableTypes = TypesFactory.getGraphSensorVariableTypes({id:$stateParams.sensorId}, function(response) {
    if(response.length == 1){
        $scope.variableTypeId = response[0].id;
        $scope.updateSensorVariableType($scope.variableTypeId);
    }      
  },function(error){
    displayRestError.display(error);            
  });
  
  
  //Download csv file for metrics
  $scope.downloadCSV = function(variableTypeId, aggregationType){
    MetricsFactory.getCsvFile({"variableTypeId":variableTypeId, "aggregationType": aggregationType},function(response) {
        var data = new Blob([response.data], { type: 'text/plain;charset=utf-8' });
        var config = {
          data: data,
          filename: response.fileName
        };
        FileSaver.saveAs(config);
      },function(error){
        displayRestError.display(error);            
      });
  };
 
  
  //Update Sensor Variable Type
  $scope.updateSensorVariableType = function(variableTypeId){
    if(variableTypeId == null){
      return;
    }  
    $scope.variableType = SensorsFactory.getSensorValue({sensorId:variableTypeId});
  
    //http://krispo.github.io/angular-nvd3
    //http://www.d3noob.org/2013/01/smoothing-out-lines-in-d3js.html
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
                left: 65
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
                    return d3.time.format('hh:mm a')(new Date(d))
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

    $scope.sensor.$promise.then(function (sensor) {
      $scope.sensor = sensor;
      $scope.variableType.$promise.then(function (variableType) {
        if($scope.variableType.metricType == 1){
          var yAxisD3Format=',.2f';
          var chartLineColor=["#2ca02c","#1f77b4", "#ff7f0e"];
          var chartInterpolate= $scope.interpolateType.value;//cardinal
          var dateFormatRawData = $scope.hourFormat + ':mm:ss' + $scope.hourFormatSufix;
          var dateFormatMinuteData = $scope.hourFormat + ':mm' + $scope.hourFormatSufix; //https://docs.angularjs.org/api/ng/filter/date
          var dateFormat5MinutesData = $scope.hourFormat + ':mm' + $scope.hourFormatSufix;
          var dateFormatHourData = 'dd-MMM ' + $scope.hourFormat + ':mm' + $scope.hourFormatSufix;
          var dateFormatDayData = 'dd-MMM-yyyy';
          var textRawData = $filter('translate')('CHART.TITLE_DOUBLE_RAW_DATA');
          var textMinuteData = $filter('translate')('CHART.TITLE_DOUBLE_6_HOURS');
          var text5MinutesData = $filter('translate')('CHART.TITLE_DOUBLE_24_HOURS');
          var textHourData = $filter('translate')('CHART.TITLE_DOUBLE_30_DAYS');
          var textDayData = $filter('translate')('CHART.TITLE_DOUBLE_ALL_DATA');
        }else if($scope.variableType.metricType == 2){
          var yAxisD3Format='.0f';
          var chartLineColor=["#1f77b4"];
          var chartInterpolate='step-after';
          var dateFormatMinuteData = $scope.hourFormat + ':mm:ss' + $scope.hourFormatSufix;
          var dateFormat5MinutesData = $scope.hourFormat + ':mm:ss' + $scope.hourFormatSufix;
          var dateFormatHourData = 'dd-MMM ' + $scope.hourFormat + ':mm:ss' + $scope.hourFormatSufix;
          var dateFormatDayData = 'dd-MMM-yyyy ' + $scope.hourFormat + ':mm:ss' + $scope.hourFormatSufix;
          var textRawData = $filter('translate')('CHART.TITLE_BINARY_RAW_DATA');
          var textMinuteData = $filter('translate')('CHART.TITLE_BINARY_6_HOURS');
          var text5MinutesData = $filter('translate')('CHART.TITLE_BINARY_24_HOURS');
          var textHourData = $filter('translate')('CHART.TITLE_BINARY_30_DAYS');
          var textDayData = $filter('translate')('CHART.TITLE_BINARY_ALL_DATA');
        }
        //http://www.d3noob.org/2013/01/smoothing-out-lines-in-d3js.html
     
        chartOptions.chart.type = 'lineChart'; //workaround to suppress 'type undefined error'
        chartOptions.chart.interpolate = chartInterpolate;
        chartOptions.chart.color = chartLineColor;
        chartOptions.chart.yAxis.tickFormat = function(d){return d3.format(yAxisD3Format)(d) + ' ' + $scope.variableType.unit ;};
        
        if($scope.variableType.metricType == 1){
          //Chart options for one Minute sample interval data
          $scope.chartRawDataOptions = chartOptions;
          $scope.chartRawDataOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, dateFormatRawData, mchelper.cfg.timezone)};
          $scope.chartRawDataOptions.title.text = textRawData;
        }
      
        //Chart options for one Minute sample interval data
        $scope.chartMinuteDataOptions = angular.copy(chartOptions);
        $scope.chartMinuteDataOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, dateFormatMinuteData, mchelper.cfg.timezone)};
        $scope.chartMinuteDataOptions.title.text = textMinuteData;

        //Chart options for 5 Minutes sample interval data
        $scope.chart5MinutesDataOptions = angular.copy(chartOptions);
        $scope.chart5MinutesDataOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, dateFormat5MinutesData, mchelper.cfg.timezone)};
        $scope.chart5MinutesDataOptions.title.text = text5MinutesData;
        
        //Chart options for one Hour sample interval data
        $scope.chartHourDataOptions = angular.copy(chartOptions);
        $scope.chartHourDataOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, dateFormatHourData, mchelper.cfg.timezone)};
        $scope.chartHourDataOptions.title.text = textHourData;
        
        //Chart options for one Day sample interval data
        $scope.chartDayDataOptions = angular.copy(chartOptions);
        $scope.chartDayDataOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, dateFormatDayData, mchelper.cfg.timezone)};
        $scope.chartDayDataOptions.title.text = textDayData;
        
        //Get list of Metrics types
        var hour = 60*60*1000;
        if($scope.variableType.metricType == 1){
          $scope.metricsDataRaw = MetricsFactory.getRawData({"variableTypeId":variableType.id, "lastNmilliSeconds": hour});          
        }
        $scope.metricsDataMinute = MetricsFactory.getOneMinuteData({"variableTypeId":variableType.id, "lastNmilliSeconds": hour*6});
        $scope.metricsData5Minutes = MetricsFactory.getFiveMinutesData({"variableTypeId":variableType.id, "lastNmilliSeconds": hour*24});
        $scope.metricsDataHour = MetricsFactory.getOneHourData({"variableTypeId":variableType.id, "lastNmilliSeconds": hour*24*30});
        $scope.metricsDataDay = MetricsFactory.getOneDayData({"variableTypeId":variableType.id});
      });
    });
  };
        
});
