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
myControllerModule.controller('SensorsController', function(alertService,
$scope, SensorsFactory, TypesFactory, NodesFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $stateParams, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('SENSORS_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_SENSORS_SETUP');
  $scope.noItemsSystemIcon = "fa fa-eye";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];

  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};

  //Get min number
  $scope.getMin = function(item1, item2){
    return CommonServices.getMin(item1, item2);
  };

  if($stateParams.nodeId){
    //$scope.nodeId = $stateParams.nodeId;
    $scope.query.nodeId = $stateParams.nodeId;
  }

  //get all Sensors
  $scope.getAllItems = function(){
    SensorsFactory.getAll($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
    },function(error){
      displayRestError.display(error);
    });
  }

  //Hold all the selected item ids
  $scope.itemIds = [];

  $scope.selectAllItems = function(){
    CommonServices.selectAllItems($scope);
  };

  $scope.selectItem = function(item){
    CommonServices.selectItem($scope, item);
  };

  //On page change
  $scope.pageChanged = function(newPage){
    CommonServices.updatePageChange($scope, newPage);
  };

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.updateFiltersChange($scope, filters);
  };

  $scope.filterConfig = {
    fields: [
      {
        id: 'name',
        title: $filter('translate')('NAME'),
        placeholder: $filter('translate')('FILTER_BY_NAME'),
        filterType: 'text'
      },
      {
        id: 'sensorId',
        title:  $filter('translate')('SENSOR_ID'),
        placeholder: $filter('translate')('FILTER_BY_SENSOR_ID'),
        filterType: 'integer',
      },
      {
        id: 'nodeName',
        title:  $filter('translate')('NODE_NAME'),
        placeholder: $filter('translate')('FILTER_BY_NAME'),
        filterType: 'text',
      },
      {
        id: 'nodeEui',
        title:  $filter('translate')('NODE_EUI'),
        placeholder: $filter('translate')('FILTER_BY_EUI'),
        filterType: 'text',
      },
      {
        id: 'type',
        title: $filter('translate')('TYPE'),
        placeholder: $filter('translate')('FILTER_BY_TYPE'),
        filterType: 'text',
      },
      {
        id: 'variableTypes',
        title: $filter('translate')('VARIABLE_TYPES'),
        placeholder: $filter('translate')('FILTER_BY_VARIABLE_TYPES'),
        filterType: 'text',
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };

  //Sort columns
  var sortChange = function (sortId, isAscending) {
    //Reset sort type and update items
    CommonServices.updateSortChange($scope, sortId, isAscending);
  };

  $scope.sortConfig = {
    fields: [
      {
        id: 'name',
        title: $filter('translate')('NAME'),
        sortType: 'text'
      },
      {
        id: 'sensorId',
        title: $filter('translate')('SENSOR_ID'),
        sortType: 'number'
      },
      {
        id: 'type',
        title: $filter('translate')('TYPE'),
        sortType: 'text'
      },
      {
        id: 'nodeEui',
        title: $filter('translate')('NODE_EUI'),
        sortType: 'text'
      },
      {
        id: 'nodeName',
        title: $filter('translate')('NODE_NAME'),
        sortType: 'text'
      },
      {
        id: 'lastSeen',
        title: $filter('translate')('LAST_SEEN'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };


  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("sensorsAddEdit", {'id':$scope.itemIds[0]});
    }
  };


  //Delete item(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      SensorsFactory.deleteIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('ITEMS_DELETED_SUCCESSFULLY'));
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

  //Get sensor variable types
  $scope.getSensorVariableTypes = function(variables){
    var types = [];
    angular.forEach(variables, function(variable){
      types.push(variable.type.locale);
    });
    return types.join(', ');
  }

});

//Add Edit sensor controller
myControllerModule.controller('SensorsControllerAddEdit', function ($scope, $stateParams, $state, GatewaysFactory, NodesFactory, SensorsFactory, TypesFactory, mchelper, alertService, displayRestError, $filter) {
  $scope.mchelper = mchelper;
  $scope.sensor = {};
  $scope.sensor.node = {};
  $scope.sensor.node.gateway = {};

  $scope.nodes = {};
  $scope.sensorVariableTypes = {};


  if($stateParams.id){
    SensorsFactory.get({"sensorId":$stateParams.id},function(response) {
        $scope.sensor = response;
        $scope.sensorVariableTypes = TypesFactory.getSensorVariableTypes({'sensorType': $scope.sensor.type.en, 'sensorId': $scope.sensor.id});
      },function(error){
        displayRestError.display(error);
      });
  }
  $scope.sensorTypes = TypesFactory.getSensorTypes();

  $scope.nodes = TypesFactory.getNodes();
  $scope.rooms = TypesFactory.getRooms();
/*
  $scope.updateNodes= function(gatewayId){
    $scope.nodes = TypesFactory.getNodes({"gatewayId":gatewayId});
  }
  */
  $scope.refreshVariableTypes = function(sensorType){
    $scope.sensorVariableTypes = TypesFactory.getSensorVariableTypes({'sensorType': sensorType});
  }

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_SENSOR');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_SENSOR');
  $scope.cancelButtonState = "sensorsList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  $scope.save = function(){
    $scope.saveProgress = true;
    //TODO: for now REST request fails if we send with 'lastSeen'. drop this here
    $scope.sensor.lastSeen = null;
    if($stateParams.id){
      SensorsFactory.update($scope.sensor,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("sensorsList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      SensorsFactory.create($scope.sensor,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("sensorsList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});

//item Detail
myControllerModule.controller('SensorsControllerDetail', function ($scope, $stateParams, mchelper, SensorsFactory,
 MetricsFactory, $filter, CommonServices, TypesFactory, $timeout, $window, displayRestError, $interval) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.node = {};
  $scope.headerStringList = $filter('translate')('SENSOR_DETAILS');
  $scope.cs = CommonServices;

  $scope.item = SensorsFactory.get({"id":$stateParams.id});

  $scope.chartOptions = {
        chart: {
            type: 'lineChart',
            noErrorCheck: true,
            height: 270,
            margin : {
                top: 5,
                right: 20,
                bottom: 60,
                left: 65
            },
            color: ["#2ca02c","#1f77b4", "#ff7f0e"],
            noData: $filter('translate')('NO_DATA_AVAILABLE'),
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

  //pre select, should be updated from server
  TypesFactory.getMetricsSettings(function(response){
    $scope.metricsSettings = response;
    $scope.chartEnableMinMax = $scope.metricsSettings.enabledMinMax;
    $scope.chartFromTimestamp = $scope.metricsSettings.defaultTimeRange.toString();
    MetricsFactory.getMetricsData({"sensorId":$stateParams.id, "withMinMax":$scope.chartEnableMinMax, "timestampFrom": new Date().getTime() - $scope.chartFromTimestamp},function(response){
      $scope.chartData = response;
      $scope.fetching = false;
    });
  });
  $scope.tooltipPlacement = 'top';
  $scope.chartTimeFormat = mchelper.cfg.dateFormat;
  $scope.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, $scope.chartTimeFormat, mchelper.cfg.timezone)};



  $scope.updateChart = function(){
    MetricsFactory.getMetricsData({"sensorId":$stateParams.id, "withMinMax":$scope.chartEnableMinMax, "timestampFrom": new Date().getTime() - $scope.chartFromTimestamp}, function(resource){
      //$scope.chartData = resource;
      resource.forEach(function(item) {
        $scope.chartData.forEach(function(itemLocal) {
          if(itemLocal.id === item.id){
            itemLocal.chartData = item.chartData;
            //Update display time format
            $scope.chartTimeFormat = item.timeFormat;
          }
        });
      });
    });
  }


  $scope.resourceCount = MetricsFactory.getResourceCount({"resourceType":"Sensor", "resourceId":$stateParams.id});

  $scope.updateChartOptions = function(chData){
    var chOptions = angular.copy($scope.chartOptions);
    chOptions.chart.type = chData.chartType;
    chOptions.chart.interpolate = chData.chartInterpolate;
    //Update display time format
    $scope.chartTimeFormat = chData.timeFormat;
    if(chData.dataType === 'Double'){
      chOptions.chart.yAxis.tickFormat = function(d){return d3.format('.02f')(d) + ' ' + chData.unit};
    }else if(chData.dataType === 'Binary' || chData.dataType === 'Counter'){
      chOptions.chart.yAxis.tickFormat = function(d){return d3.format('.0f')(d)};
    }
    chOptions.title.text = chData.variableType;
    return chOptions;
  }

  //Update Variable / Send Payload
  $scope.updateVariable = function(variable){
    SensorsFactory.updateVariable(variable, function(){
      //update Success
    },function(error){
      displayRestError.display(error);
    });
  };

  //update variable unit
  $scope.updateVariableUnit = function(variable){
    SensorsFactory.updateVariableUnit(variable, function(){
      //update Success
    },function(error){
      displayRestError.display(error);
    });
  }

  //HVAC heater options - HVAC flow state
  $scope.hvacOptionsFlowState = TypesFactory.getHvacOptionsFlowState();
  //HVAC heater options - HVAC flow mode
  $scope.hvacOptionsFlowMode = TypesFactory.getHvacOptionsFlowMode();
  //HVAC heater options - HVAC fan speed
  $scope.hvacOptionsFanSpeed = TypesFactory.getHvacOptionsFanSpeed();

  //Defined variable types list
  $scope.definedVariableTypes = CommonServices.getSensorVariablesKnownList();

  //Hide variable names
  $scope.hideVariableName=true;


  //update rgba color
  $scope.updateRgba = function(variable){
    variable.value = CommonServices.rgba2hex(variable.rgba);
    $scope.updateVariable(variable);
  };

  //Graph resize issue, see: https://github.com/krispo/angular-nvd3/issues/40
  $scope.$watch('fetching', function() {
      if(!$scope.fetching) {
        $timeout(function() {
          $window.dispatchEvent(new Event('resize'));
          $scope.fetching = true;
        }, 1000);
      }
    });

  //Get sensor variable types
  $scope.getSensorVariableTypes = function(variables){
    var types = [];
    angular.forEach(variables, function(variable){
      types.push(variable.type.locale);
    });
    return types.join(', ');
  }

  //Update data for N seconds once
  var updatePageData = function(){
    //$scope.item = SensorsFactory.get({"id":$stateParams.id});
    $scope.updateChart();
  }

  // global page refresh
  var promise = $interval(updatePageData, mchelper.cfg.globalPageRefreshTime);

  // cancel interval on scope destroy
  $scope.$on('$destroy', function(){
    $interval.cancel(promise);
  });

});

//Edit sensor variable controller
myControllerModule.controller('SensorVariableControllerEdit', function ($scope, $stateParams, $state, SensorsFactory, TypesFactory,
  mchelper, alertService, displayRestError, $filter, CommonServices, $uibModal) {
  $scope.mchelper = mchelper;
  $scope.cs = CommonServices;
  $scope.sensorVariable = {};
  $scope.metricTypes = {};
  $scope.unitTypes = {};
  $scope.orgSvar = {};

  if($stateParams.id){
    SensorsFactory.getVariable({"id":$stateParams.id},function(response) {
        $scope.sensorVariable = response;
        $scope.orgSvar = angular.copy(response);
      },function(error){
        displayRestError.display(error);
      });
  }

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_SENSOR_VARIABLE');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_SENSOR_VARIABLE');
  $scope.cancelButtonState = "sensorsDetail({id: sensorVariable.sensorId})"; //Cancel button state
  $scope.saveProgress = false;
  $scope.unitTypes = TypesFactory.getUnitTypes();
  $scope.metricTypes = TypesFactory.getMetricTypes();

  $scope.saveFinal = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      SensorsFactory.updateVariableConfig($scope.sensorVariable,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("sensorsDetail", {"id": $scope.sensorVariable.sensorId});
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }

  //Show update warning
  $scope.save = function (size) {
    if($scope.orgSvar.metricType === $scope.sensorVariable.metricType){
      $scope.saveFinal();
    }else{
      var modalInstance = $uibModal.open({
      templateUrl: 'partials/common-html/edit-confirmation-modal.html',
      controller: 'SensorVariableUpdateWarnController',
      size: size,
      resolve: {}
      });

      modalInstance.result.then(function () {
        $scope.saveFinal();
      }),
      function () {
        //console.log('Modal dismissed at: ' + new Date());
      }
    }

  };

});

//sensor variable change Modal
myControllerModule.controller('SensorVariableUpdateWarnController', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('S_VARIABLE_DIALOG_TITLE');
  $scope.message = $filter('translate')('S_VARIABLE_DIALOG_CONFIRMATION_MSG');
  $scope.continute = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});
