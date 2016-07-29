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
myControllerModule.controller('NodesController', function(alertService,
$scope, NodesFactory, $stateParams, $state, $uibModal, displayRestError, CommonServices, mchelper, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('NODES_DETAIL');
  $scope.noItemsSystemMsg = $filter('translate')('NO_NODES_SETUP');
  $scope.noItemsSystemIcon = "fa fa-sitemap";

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

  if($stateParams.gatewayId){
    $scope.query.gatewayId = $stateParams.gatewayId;
  }

  //get all Nodes
  $scope.getAllItems = function(){
    NodesFactory.getAll($scope.query, function(response) {
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
        id: 'state',
        title:  $filter('translate')('STATUS'),
        placeholder: $filter('translate')('FILTER_BY_STATUS'),
        filterType: 'select',
        filterValues: ['Up','Down','Unavailable'],
      },
      {
        id: 'type',
        title:  'Type',
        placeholder: $filter('translate')('FILTER_BY_TYPE'),
        filterType: 'select',
        filterValues: ['Node','Repeater node'],
      },
      {
        id: 'eui',
        title:  'EUI',
        placeholder: $filter('translate')('FILTER_BY_EUI'),
        filterType: 'text',
      },
      {
        id: 'version',
        title:  'Version',
        placeholder: $filter('translate')('FILTER_BY_VERSION'),
        filterType: 'text',
      },
      {
        id: 'libVersion',
        title:  'Library Version',
        placeholder: $filter('translate')('FILTER_BY_LIBRARY_VERSION'),
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
        title:  $filter('translate')('NAME'),
        sortType: 'text'
      },
      {
        id: 'state',
        title:  $filter('translate')('STATUS'),
        sortType: 'text'
      },
      {
        id: 'eui',
        title:  $filter('translate')('EUI'),
        sortType: 'text'
      },
      {
        id: 'type',
        title:  $filter('translate')('TYPE'),
        sortType: 'text'
      },
      {
        id: 'version',
        title:  $filter('translate')('VERSION'),
        sortType: 'text'
      },
      {
        id: 'libVersion',
        title:  $filter('translate')('LIBRARY_VERSION'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };



  //Delete Node(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      NodesFactory.deleteIds($scope.itemIds, function(response) {
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

  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("nodesAddEdit",{'id':$scope.itemIds[0]});
    }
  };

  //Upload Firmware
  $scope.uploadFirmware = function (size) {
    if($scope.itemIds.length > 0){
      NodesFactory.uploadFirmware($scope.itemIds,function(response) {
        alertService.success($filter('translate')('FIRMWARE_UPLOAD_INITIATED'));
      },function(error){
        displayRestError.display(error);
      });
    }
  };

  //Refresh nodes information
  $scope.refreshNodesInfo = function (size) {
    if($scope.itemIds.length > 0){
      NodesFactory.executeNodeInfoUpdate($scope.itemIds,function(response) {
        alertService.success($filter('translate')('REFRESH_NODES_INFO_INITIATED_SUCCESSFULLY'));
      },function(error){
        displayRestError.display(error);
      });
    }
  };

  //Reboot a Node
  $scope.reboot = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/node-reboot-modal.html',
    controller: 'NodesControllerReboot',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function () {
      NodesFactory.reboot($scope.itemIds, function(response) {
        alertService.success($filter('translate')('REBOOT_INITIATED'));
      },function(error){
        displayRestError.display(error);
      });
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

  //Erase Configuration of Nodes
  $scope.eraseConfiguration = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/node-erase-configuration-modal.html',
    controller: 'NodesControllerEraseConfiguration',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function () {
      NodesFactory.eraseConfiguration($scope.itemIds, function(response) {
        alertService.success($filter('translate')('ERASE_CONFIGURATION_INITIATED'));
      },function(error){
        displayRestError.display(error);
      });
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

});


// Nodes other controllers

//Add/Edit Node
myControllerModule.controller('NodesControllerAddEdit', function ($scope, $stateParams, GatewaysFactory, NodesFactory, TypesFactory, mchelper, alertService, displayRestError, $filter, $state) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.node = {};
  if($stateParams.id){
    $scope.node = NodesFactory.get({"nodeId":$stateParams.id});
  }
  $scope.node.gateway = {};
  $scope.gateways = TypesFactory.getGateways();
  $scope.nodeTypes = TypesFactory.getNodeTypes();
  $scope.nodeRstatuses = TypesFactory.getNodeRegistrationStatuses();
  $scope.firmwares = TypesFactory.getFirmwares();

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_NODE');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_NODE');
  $scope.cancelButtonState = "nodesList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;


  $scope.save = function(){
      $scope.saveProgress = true;
    if($stateParams.id){
      NodesFactory.update($scope.node,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("nodesList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      NodesFactory.create($scope.node,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("nodesList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});


//Node Detail
myControllerModule.controller('NodesControllerDetail', function ($scope, $stateParams, mchelper, NodesFactory, TypesFactory, MetricsFactory, $filter, $timeout, $window) {
  //Load mchelper variables to this scope
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.headerStringList = $filter('translate')('NODE_DETAILS');

  $scope.item = NodesFactory.get({"nodeId":$stateParams.id});
  $scope.resourceCount = MetricsFactory.getResourceCount({"resourceType":"NODE", "resourceId":$stateParams.id});

  $scope.chartOptions = {
        chart: {
            type: 'lineChart',
            noErrorCheck: true,
            height: 325,
            width:null,
            margin : {
                top: 0,
                right: 10,
                bottom: 90,
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
                    return d3.time.format('hh:mm:ss a')(new Date(d))
                },
                //axisLabel: 'Timestamp',
                rotateLabels: -20
            },
            yAxis: {
                tickFormat: function(d){
                    return d3.format(',.2f')(d) + ' %';
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
  TypesFactory.getMetricsSettings(function(response){
    $scope.metricsSettings = response;
    $scope.chartEnableMinMax = $scope.metricsSettings.enabledMinMax;
    $scope.chartFromTimestamp = $scope.metricsSettings.defaultTimeRange.toString();
    MetricsFactory.getBatteryMetrics({"nodeId":$stateParams.id, "withMinMax":$scope.chartEnableMinMax, "timestampFrom": new Date().getTime() - $scope.chartFromTimestamp},function(response){
      $scope.batteryChartData = response;
      //Update display time format
      $scope.chartTimeFormat = response.timeFormat;
      $scope.chartOptions.chart.type = response.chartType;
      $scope.chartOptions.chart.interpolate = response.chartInterpolate;
      $scope.fetching = false;
    });
  });
  $scope.chartTimeFormat = mchelper.cfg.dateFormat;
  $scope.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, $scope.chartTimeFormat, mchelper.cfg.timezone)};

  $scope.updateChart = function(){
    MetricsFactory.getBatteryMetrics({"nodeId":$stateParams.id, "withMinMax":$scope.chartEnableMinMax, "timestampFrom": new Date().getTime() - $scope.chartFromTimestamp}, function(resource){
      $scope.batteryChartData.chartData = resource.chartData;
      //Update display time format
      $scope.chartTimeFormat = resource.timeFormat;
    });
  }

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


//Erase Configuration Modal
myControllerModule.controller('NodesControllerEraseConfiguration', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('ERASE_CONFIGURATION_CONFIRMATION_TITLE');
  $scope.eraseMsg = $filter('translate')('ERASE_CONFIGURATION_CONFIRMATION_MESSAGE');
  $scope.eraseNodeConfiguration = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});

//reboot Modal
myControllerModule.controller('NodesControllerReboot', function ($scope, $uibModalInstance, $filter) {
  $scope.header = $filter('translate')('REBOOT_CONFIRMATION_TITLE');
  $scope.rebootMsg = $filter('translate')('REBOOT_CONFIRMATION_MESSAGE');
  $scope.reboot = function() {$uibModalInstance.close(); };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});
