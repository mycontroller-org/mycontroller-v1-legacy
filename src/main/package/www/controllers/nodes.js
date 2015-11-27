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
myControllerModule.controller('NodesController', function(alertService,
$scope, $filter, NodesFactory, $location, $uibModal, displayRestError, $filter) {
  
  $scope.filter = $filter;  
  
  $scope.filteredList=[];
  $scope.orgList=[];
  $scope.config = {
    itemsPerPage: 10,
    maxPages:10,
    fillLastPage: false
  }

  //Filter
  $scope.updateFilteredList = function() {
    $scope.filteredList = $filter("filter")($scope.orgList, $scope.query);
  };
  
  //Send list of Nodes
  $scope.orgList = NodesFactory.getAll(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Delete a Node
  $scope.delete = function (node, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'NMdeleteController',
    size: size,
    resolve: {
      node: function () {return node;}
      }
    });

    modalInstance.result.then(function (selectedNode) {
      $scope.selected = selectedNode;
      NodesFactory.delete({ nodeId: selectedNode.id },function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_DELETE', selectedNode));
        //Update display table
        $scope.orgList = NodesFactory.getAll(function(response) {
        },function(error){
          displayRestError.display(error);            
        });
      $scope.filteredList = $scope.orgList;
      },function(error){
        displayRestError.display(error);            
      }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
    
  //Add a Node
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/addModal.html',
    controller: 'NMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newNode) {
      $scope.newNode = newNode;
      NodesFactory.create($scope.newNode,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_ADD', newNode));
        //Update display table
        $scope.orgList = NodesFactory.getAll(function(response) {
        },function(error){
          displayRestError.display(error);            
        });
      $scope.filteredList = $scope.orgList;
      },function(error){
        displayRestError.display(error);            
      });      
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
  // Upload Firmware
  $scope.uploadFirmware = function (node, size) {
    NodesFactory.uploadFirmware(node,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_FIRMWARE_UPDATE', node));
      },function(error){
        displayRestError.display(error);            
      });      
    };
  
  //Reboot a Node
  $scope.reboot = function (node, size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/rebootModal.html',
    controller: 'NMrebootController',
    size: size,
    resolve: {node: function () {return node;}}
    });

    addModalInstance.result.then(function (node) {
      NodesFactory.reboot(node,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_REBOOT', node));
      },function(error){
        displayRestError.display(error);            
      });      
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
    //Erase EEPROM of a Node
  $scope.eraseEeprom = function (node, size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/eraseEepromModal.html',
    controller: 'NMeraseEepromController',
    size: size,
    resolve: {node: function () {return node;}}
    });

    addModalInstance.result.then(function (node) {
      node.eraseEEPROM = true;
      NodesFactory.update(node,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_EEPROM_ERASE', node));
        //Trigger Reboot
        NodesFactory.reboot(node,function(response) {
          alertService.success($filter('translate')('NODE.NOTIFY_REBOOT', node));
        },function(error){
          displayRestError.display(error);            
      });  
    }); 
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
    
    
  //Update a Node
  $scope.update = function (node, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/updateModal.html',
    controller: 'NMupdateController',
    size: size,
    resolve: {node: function () {return node;}}
    });

    editModalInstance.result.then(function (updateNode) {
      $scope.updateNode = updateNode;
      $scope.updateNode.updateTime = new Date().getTime();
      NodesFactory.update($scope.updateNode,function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_UPDATE', updateNode));
        //Update display table
        $scope.orgList = NodesFactory.getAll(function(response) {
        },function(error){
          displayRestError.display(error);            
        });
      $scope.filteredList = $scope.orgList;
      },function(error){
        displayRestError.display(error);            
      });      
    }), 
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
  
  //Node Discover
  $scope.discover = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/discoverModal.html',
    controller: 'NMdiscoverController',
    size: size
    });

    addModalInstance.result.then(function () {
      NodesFactory.discover(function(response) {
        alertService.success($filter('translate')('NODE.NOTIFY_DISCOVER'));
      },function(error){
        displayRestError.display(error);            
      });      
    }), 
    function () {
    }
  };
  
  //Node Battery Level graph
  $scope.displayBatteryLevel = function (node, size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/nodes/batteryLevelChart.html',
    controller: 'NMbatteryLevelController',
    windowClass: 'battery-modal-window',
    size: size,
    resolve: {node: function () {return node;}}
    });

    addModalInstance.result.then(function () {
    }), 
    function () {
    }
  };
  
});


//Nodes Modal
myControllerModule.controller('NMdeleteController', function ($scope, $modalInstance, node, $filter) {
  $scope.node = node;
  $scope.header = $filter('translate')('NODE.TITLE_DELETE');
  $scope.deleteMsg = $filter('translate')('NODE.MESSAGE_DELETE', node);
  $scope.remove = function() {
    $modalInstance.close($scope.node);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('NMaddController', function ($scope, $modalInstance, TypesFactory, FirmwaresFactory, $filter) {
  $scope.node = {};
  $scope.header = $filter('translate')('NODE.TITLE_NEW');
  $scope.nodeTypes = TypesFactory.getNodeTypes();
  $scope.add = function() {$modalInstance.close($scope.node); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('NMupdateController', function ($scope, $modalInstance, node, TypesFactory, FirmwaresFactory, $filter) {
  $scope.node = node;
  $scope.header = $filter('translate')('NODE.TITLE_EDIT', node);
  $scope.nodeTypes = TypesFactory.getNodeTypes();
  $scope.firmwares = FirmwaresFactory.getAllFirmwares();
  $scope.update = function() {$modalInstance.close($scope.node);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//reboot Modal
myControllerModule.controller('NMrebootController', function ($scope, $modalInstance, node, $filter) {
  $scope.node = node;
  $scope.header = $filter('translate')('NODE.TITLE_REBOOT');
  $scope.rebootMsg = $filter('translate')('NODE.MESSAGE_REBOOT', node);
  $scope.reboot = function() {$modalInstance.close($scope.node); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});


//Erase Modal
myControllerModule.controller('NMeraseEepromController', function ($scope, $modalInstance, node, $filter) {
  $scope.node = node;
  $scope.header = $filter('translate')('NODE.TITLE_ERASE_EEPROM');
  $scope.eraseMsg = $filter('translate')('NODE.MESSAGE_ERASE_EEPROM', node);
  $scope.eraseEeprom = function() {$modalInstance.close($scope.node); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Discover Modal
myControllerModule.controller('NMdiscoverController', function ($scope, $modalInstance, $filter) {
  $scope.header = $filter('translate')('NODE.TITLE_DISCOVER_UTILITY');
  $scope.discoverMsg = $filter('translate')('NODE.MESSAGE_DISCOVER_UTILITY');
  $scope.discover = function() {$modalInstance.close(); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Batter Level display Modal
myControllerModule.controller('NMbatteryLevelController', function ($modalInstance, $scope, $stateParams, MetricsFactory, about, $filter, SettingsFactory, node, displayRestError, $filter) {
  $scope.header = $filter('translate')('NODE.TITLE_BATTERY');
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
  
  //http://krispo.github.io/angular-nvd3
  //http://www.d3noob.org/2013/01/smoothing-out-lines-in-d3js.html
    $scope.chartOptions = {
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
                color: ["#1f77b4"],
                
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
                text: 'Title 2'
            }
        };
        
  
  //Get Chart Interpolate Type
  $scope.interpolateType = SettingsFactory.get({key_:'graph_interpolate_type'});

  //about, Timezone, etc.,
  $scope.about = about;     
  
  $scope.interpolateType.$promise.then(function (interpolateType) {
    $scope.interpolateType = interpolateType;
  
    var chartDateFormat = 'MMM d, y ' + $scope.hourFormat + ':mm:ss' + $scope.hourFormatSufix; //https://docs.angularjs.org/api/ng/filter/date
    
    $scope.chartOptions.chart.type = 'lineChart'; //workaround to suppress 'type undefined error'
    $scope.chartOptions.chart.interpolate = $scope.interpolateType.value;//cardinal
    $scope.chartOptions.chart.color = ["#1f77b4"];
    $scope.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format(',.2f')(d);};
  
    $scope.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, chartDateFormat, about.timezone)};
    $scope.chartOptions.title.text = $filter('translate')('NODE.TITLE2_BATTERY', node);
  });
  
  $scope.getMetrics = function(){
    $scope.batteryUsageChartMetrics = MetricsFactory.batteryUsage({"nodeId":node.id});
  }
  $scope.getMetrics();
  
  $scope.close = function() {$modalInstance.close(); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

