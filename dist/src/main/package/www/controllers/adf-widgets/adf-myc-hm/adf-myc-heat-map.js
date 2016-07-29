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

'use strict';

angular.module('adf.widget.myc-heat-map', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycHeatMap', {
        title: 'Heatmap chart',
        description: 'Displays data as heatmap chart',
        templateUrl: 'controllers/adf-widgets/adf-myc-hm/view.html',
        controller: 'mycHeatMapController',
        controllerAs: 'mycHeatMap',
        config: {
          dataType: null,
          upperLimit: null,
          thresholds: [],
          colorPattern: [],
          legendLabels: [],
          dataKey:null,
          refreshTime:30,
          height:200,
          maxBlockSize:50,
          showLegends:true,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-hm/edit.html',
          controller: 'mycHeatMapEditController',
          controllerAs: 'mycHeatMapEdit',
        }
      });
  })
  .controller('mycHeatMapController', function($scope, $interval, config, mchelper, $filter, MetricsFactory,$state){
    var mycHeatMap = this;
    mycHeatMap.showLoading = true;
    mycHeatMap.isSyncing = false;
    mycHeatMap.data = {};
    mycHeatMap.dataAvailable = false;

    function updateState(){
      if(mycHeatMap.data.length === 0){
        mycHeatMap.dataAvailable = false;
      }else{
        mycHeatMap.dataAvailable = true;
      }
      mycHeatMap.isSyncing = false;
      if(mycHeatMap.showLoading){
        mycHeatMap.showLoading = false;
      }
    };

    function loadData(){
      mycHeatMap.isSyncing = true;
      if(config.dataType === 'NODE_STATUS'){
        MetricsFactory.getHeatMapHeatMapNodeStatus({'nodeId':config.dataKey}, function(response){
          mycHeatMap.data = response;
          updateState();
        });
      }else if(config.dataType === 'BATTERY_LEVEL'){
        MetricsFactory.getHeatMapBatteryLevel({'nodeId':config.dataKey}, function(response){
          mycHeatMap.data = response;
          updateState();
        });
      }else if(config.dataType === 'SENSOR_VARIABLES'){
        MetricsFactory.getHeatMapHeatMapSensorVariable({'variableId':config.dataKey, 'upperLimit':config.upperLimit}, function(response){
          mycHeatMap.data = response;
          updateState();
        });
      }else if(config.dataType === 'SCRIPT'){
        MetricsFactory.getHeatMapHeatMapScript({'scriptName':config.dataKey}, function(response){
          mycHeatMap.data = response;
          updateState();
        });
      }else{
        mycHeatMap.isSyncing = false;
        if(mycHeatMap.showLoading){
          mycHeatMap.showLoading = false;
        }
      }
      //remove this line
      if(mycHeatMap.showLoading){
          mycHeatMap.showLoading = false;
        }
    };

    function updateData(){
      if(mycHeatMap.isSyncing){
        return;
      }else if(config.dataKey !== null){
        loadData();
      }
    }

    //load variables initially
    if(config.dataKey !== null){
      updateData();
    }else{
      mycHeatMap.showLoading = false;
    }

    //Heat map click action
    mycHeatMap.hmClickAction = function(block){
      if(config.dataType === 'NODE_STATUS' || config.dataType === 'BATTERY_LEVEL'){
        $state.go("nodesDetail", {'id':block.altId});
      }else if(config.dataType === 'SENSOR_VARIABLES'){
       $state.go("sensorsDetail", {'id':block.altId});
      }else if(config.dataType === 'SCRIPT'){
        //Not implemented yet
      }
    };

    // refresh every second
    var promise = $interval(updateData, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });

  }).controller('mycHeatMapEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, ScriptsFactory, CommonServices){
    var mycHeatMapEdit = this;
    mycHeatMapEdit.cs = CommonServices;

    var generalColorPattern = ['#21781F', '#3F9C35', '#57A8D4', '#F9D67A', '#EC7A08', '#CC0000', '#F00'];
    var generalThresholds = [0.1, 0.3, 0.5, 0.7, 0.8, 0.9];
    var generalLabels = ['< 10%', '10-30%', '30-50%', '50-70%', '70-80%', '80-90%', '> 90%'];
    var statusColorPattern = ['#C00', '#808080', '#3F9C35'];
    var statusThresholds = [0.4, 0.6];
    var statusLabels = ['Down', 'Unavailable', 'Up'];

    //Change data type
    mycHeatMapEdit.changeDataType = function(){
      //Update pattern, colors, labels
      if(config.dataType === 'NODE_STATUS'){
        config.thresholds = angular.copy(statusThresholds);
        config.colorPattern = angular.copy(statusColorPattern);
        config.legendLabels = angular.copy(statusLabels);
      }else if(config.dataType){
        config.thresholds = angular.copy(generalThresholds);
        config.colorPattern = angular.copy(generalColorPattern);
        config.legendLabels = angular.copy(generalLabels);
      }
      //Change color to reverse order if selected object is battery level
      if(config.dataType === 'BATTERY_LEVEL'){
        mycHeatMapEdit.swapColors();
      }
      //Change object type
      if(config.dataType === 'SCRIPT'){
        config.dataKey = {};
      }else{
        config.dataKey = [];
      }
      //load variables
      loadVariables();
    };

    var loadVariables = function(){
      if(config.dataType === 'NODE_STATUS' || config.dataType === 'BATTERY_LEVEL'){
        mycHeatMapEdit.variables = TypesFactory.getNodes();
      }else if(config.dataType === 'SENSOR_VARIABLES'){
        //Get only DOUBLE type devices
        mycHeatMapEdit.variables = TypesFactory.getSensorVariables({"metricType":"Double"});
      }else if(config.dataType === 'SCRIPT'){
        mycHeatMapEdit.variables = ScriptsFactory.getAllLessInfo({"type":"Operation"});
      }
    }

    // load variables at startup
    loadVariables();

    //Swap up down color
    mycHeatMapEdit.swapColors = function(){
      config.colorPattern.reverse();
    }
  });
