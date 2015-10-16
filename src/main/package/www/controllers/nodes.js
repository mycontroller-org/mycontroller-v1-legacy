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
$scope, $filter, NodesFactory, $location, $modal, displayRestError) {
    
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
    var modalInstance = $modal.open({
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
        alertService.success("Deleted a node[id:"+selectedNode.id+",name:"+selectedNode.name+"]");
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
    var addModalInstance = $modal.open({
    templateUrl: 'partials/nodes/addModal.html',
    controller: 'NMaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newNode) {
      $scope.newNode = newNode;
      NodesFactory.create($scope.newNode,function(response) {
        alertService.success("Added a node[id:"+newNode.id+",name:"+newNode.name+"]");
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
  
  //Reboot a Node
  $scope.reboot = function (node, size) {
    var addModalInstance = $modal.open({
    templateUrl: 'partials/nodes/rebootModal.html',
    controller: 'NMrebootController',
    size: size,
    resolve: {node: function () {return node;}}
    });

    addModalInstance.result.then(function (node) {
      NodesFactory.reboot(node,function(response) {
        alertService.success("Reboot initiated for Node[id:"+node.id+",name:"+node.name+"]");
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
    var addModalInstance = $modal.open({
    templateUrl: 'partials/nodes/eraseEepromModal.html',
    controller: 'NMeraseEepromController',
    size: size,
    resolve: {node: function () {return node;}}
    });

    addModalInstance.result.then(function (node) {
      node.eraseEEPROM = true;
      NodesFactory.update(node,function(response) {
        alertService.success("Updated erase EEPROM for Node[id:"+node.id+",name:"+node.name+"]");
        //Trigger Reboot
        NodesFactory.reboot(node,function(response) {
          alertService.success("Reboot initiated for Node[id:"+node.id+",name:"+node.name+"]");
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
    var editModalInstance = $modal.open({
    templateUrl: 'partials/nodes/updateModal.html',
    controller: 'NMupdateController',
    size: size,
    resolve: {node: function () {return node;}}
    });

    editModalInstance.result.then(function (updateNode) {
      $scope.updateNode = updateNode;
      $scope.updateNode.updateTime = new Date().getTime();
      NodesFactory.update($scope.updateNode,function(response) {
        alertService.success("Updated a node[id:"+updateNode.id+",name:"+updateNode.name+"]");
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
    var addModalInstance = $modal.open({
    templateUrl: 'partials/nodes/discoverModal.html',
    controller: 'NMdiscoverController',
    size: size
    });

    addModalInstance.result.then(function () {
      NodesFactory.discover(function(response) {
        alertService.success("Node Discover initiated successfully");
      },function(error){
        displayRestError.display(error);            
      });      
    }), 
    function () {
    }
  };
  
  //Node Battery Level graph
  $scope.displayBatteryLevel = function (node, size) {
    var addModalInstance = $modal.open({
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
myControllerModule.controller('NMdeleteController', function ($scope, $modalInstance, $sce, node) {
  $scope.node = node;
  $scope.header = "Delete Node";
  $scope.deleteMsg = $sce.trustAsHtml("You are about to delete a Node"
    +"<br>Deletion process will remove complete trace of this node!" 
    +"<br>Click 'Delete' to proceed."
    +"<br><I>Node: </I>[id:"+node.id+",name:"+node.name +",type:"+node.typeString+"]");
  $scope.remove = function() {
    $modalInstance.close($scope.node);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('NMaddController', function ($scope, $modalInstance, TypesFactory, FirmwaresFactory) {
  $scope.node = {};
  $scope.header = "New Node";
  $scope.nodeTypes = TypesFactory.getNodeTypes();
  $scope.add = function() {$modalInstance.close($scope.node); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('NMupdateController', function ($scope, $modalInstance, node, TypesFactory, FirmwaresFactory) {
  $scope.node = node;
  $scope.header = "Modify Node '"+node.name+"'";
  $scope.nodeTypes = TypesFactory.getNodeTypes();
  $scope.firmwares = FirmwaresFactory.getAllFirmwares();
  $scope.update = function() {$modalInstance.close($scope.node);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//reboot Modal
myControllerModule.controller('NMrebootController', function ($scope, $modalInstance, $sce, node) {
  $scope.node = node;
  $scope.header = "Reboot Node";
  $scope.rebootMsg = $sce.trustAsHtml("You are about to reboot a Node"
    +"<br>Click 'Reboot' to proceed further."
    +"<br><I>Node: </I>[id:"+node.id+",name:"+node.name +",type:"+node.typeString+"]");
  $scope.reboot = function() {$modalInstance.close($scope.node); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});


//Erase Modal
myControllerModule.controller('NMeraseEepromController', function ($scope, $modalInstance, $sce, node) {
  $scope.node = node;
  $scope.header = "Erase EEPROM of a Node";
  $scope.eraseMsg = $sce.trustAsHtml("You are about to erase complete EEPROM of a Node"
    +"<br>This action will remove complete configuration of the node including node Id!"
    +"<br>Click 'Erase EEPROM' to proceed further."
    +"<br><I>Node: </I>[id:"+node.id+",name:"+node.name +",type:"+node.typeString+"]");
  $scope.eraseEeprom = function() {$modalInstance.close($scope.node); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Discover Modal
myControllerModule.controller('NMdiscoverController', function ($scope, $modalInstance, $sce) {
  $scope.header = "Node Discover Util";
  $scope.discoverMsg = $sce.trustAsHtml("You are about trigger Node Discover util"
    +"<br>This util will send REBOOT message for all the nodes (id: 1 to 254)"
    +"<br>Node REBOOT may cause issues on your production setup"
    +"<br>Click 'Discover' to proceed further");
  $scope.discover = function() {$modalInstance.close(); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

//Discover Modal
myControllerModule.controller('NMbatteryLevelController', function ($modalInstance, $scope, $stateParams, MetricsFactory, about, $filter, SettingsFactory, node) {
  $scope.header = "Node Battery Level";
  
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
                text: 'Battery Status ('+node.name+')'
            }
        };
        
  
  //Get Chart Interpolate Type
  $scope.interpolateType = SettingsFactory.get({key_:'graph_interpolate_type'});

  //about, Timezone, etc.,
  $scope.about = about;     
  
  $scope.interpolateType.$promise.then(function (interpolateType) {
    $scope.interpolateType = interpolateType;
  
    var chartDateFormat = 'medium'; //https://docs.angularjs.org/api/ng/filter/date
    
    $scope.chartOptions.chart.type = 'lineChart'; //workaround to suppress 'type undefined error'
    $scope.chartOptions.chart.interpolate = $scope.interpolateType.value;//cardinal
    $scope.chartOptions.chart.color = ["#1f77b4"];
    $scope.chartOptions.chart.yAxis.tickFormat = function(d){return d3.format(',.2f')(d);};
  
    $scope.chartOptions.chart.xAxis.tickFormat = function(d) {return $filter('date')(d, chartDateFormat, about.timezone)};
    $scope.chartOptions.title.text = 'Battery Status of "'+node.name+'" (Id:'+node.id+')';
  });
  
  $scope.getMetrics = function(){
    $scope.batteryUsageChartMetrics = MetricsFactory.batteryUsage({"sensorId":node.id}); //Note here we should use sensorId as key, check rest_service.js
  }
  $scope.getMetrics();
  
  $scope.close = function() {$modalInstance.close(); };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

