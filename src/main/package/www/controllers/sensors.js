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
myControllerModule.controller('SensorsController', function(alertService,
$scope, SensorsFactory, TypesFactory, NodesFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $stateParams) {

  //GUI page settings
  $scope.headerStringList = "Sesnors detail";
  $scope.noItemsSystemMsg = "No sensors set up.";
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
        title:  'Name',
        placeholder: 'Filter by Name',
        filterType: 'text'
      },
      {
        id: 'sensorId',
        title:  'Id',
        placeholder: 'Filter by Id',
        filterType: 'integer',
      },
      {
        id: 'type',
        title:  'Type',
        placeholder: 'Filter by Type',
        filterType: 'text',
      },
      {
        id: 'variableTypes',
        title:  'Variable Types',
        placeholder: 'Filter by Variable Types',
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
        title:  'Name',
        sortType: 'text'
      },
      {
        id: 'nodeId',
        title:  'Node Id',
        sortType: 'text'
      },
      {
        id: 'sensorId',
        title:  'Sensor Id',
        sortType: 'number'
      },
      {
        id: 'type',
        title:  'Type',
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
        alertService.success('Deleted '+$scope.itemIds.length+' items(s).');
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
        $scope.sensorVariableTypes = TypesFactory.getSensorVariableTypes({'sensorType': $scope.sensor.type, 'sensorId': $scope.sensor.id});
      },function(error){
        displayRestError.display(error);
      });
  }
  $scope.sensorTypes = TypesFactory.getSensorTypes();
  
  $scope.nodes = TypesFactory.getNodes();
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
  $scope.headerStringAdd = "Add sensor";
  $scope.headerStringUpdate = "Update sensor";
  $scope.cancelButtonState = "sensorsList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;
  
  $scope.save = function(){
    $scope.saveProgress = true;
    //TODO: for now REST request fails if we send with 'lastSeen'. drop this here
    $scope.sensor.lastSeen = null;
    if($stateParams.id){
      SensorsFactory.update($scope.sensor,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', $scope.node));
        $state.go("sensorsList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      SensorsFactory.create($scope.sensor,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', $scope.node));
        $state.go("sensorsList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});

//item Detail
myControllerModule.controller('SensorsControllerDetail', function ($scope, $stateParams, mchelper, SensorsFactory, MetricsFactory, $filter, CommonServices, TypesFactory, $timeout, $window) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.node = {};
  $scope.headerStringList = "Sensor details";
  $scope.cs = CommonServices;
  
  $scope.item = SensorsFactory.get({"id":$stateParams.id});
  
  $scope.chartOptions = {
        chart: {
            type: 'lineChart',
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
            text: 'Title'
        }
    };
  
  //pre select, should be updated from server
  $scope.tooltipPlacement = 'top';
  $scope.chartEnableMinMax = true;
  $scope.chartFromTimestamp = "3600000";
  $scope.chartTimeFormat = mchelper.cfg.dateFormat;
  $scope.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, $scope.chartTimeFormat, mchelper.cfg.timezone)};
  
  $scope.chartData = MetricsFactory.getMetricsData({"sensorId":$stateParams.id, "withMinMax":$scope.chartEnableMinMax, "timestampFrom": new Date().getTime() - 3600000});
  
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
    chOptions.chart.type = chData.chartData[0].type;
    chOptions.chart.interpolate = chData.chartData[0].interpolate;
    //Update display time format
    $scope.chartTimeFormat = chData.timeFormat;
    if(chData.dataType === 'Double'){
      chOptions.chart.yAxis.tickFormat = function(d){return d3.format('.02f')(d) + ' ' + chData.unit};
    }else if(chData.dataType === 'Binary'){
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
  
});
