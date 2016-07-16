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
myControllerModule.controller('TopologyController', function(alertService,
$scope, MetricsFactory, $stateParams, $state, displayRestError, mchelper, CommonServices, $filter, TopologyService, $interval, mchelper) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('TOPOLOGY');
  $scope.noItemsSystemMsg = $filter('translate')('NO_DATA_AVAILABLE');
  $scope.noItemsSystemIcon = "pficon pficon-topology";

  $scope.mchelper = mchelper;

  $scope.query = {};
  //Update $stateParams
  if($stateParams.resourceType && $stateParams.resourceId){
    $scope.query = {'resourceType': $stateParams.resourceType, 'resourceId': $stateParams.resourceId};
  }

  $scope.query.realtime = true;

  var self = this;
  $scope.vs = null;
  var d3 = window.d3;
  $scope.data = {};
  $scope.search = {};
  $scope.topologyHeight;
  $scope.displayKinds= {};

  $scope.kinds = {
    "Gateway": "#vertex-Gateway",
    "Node": "#vertex-Node",
    "Sensor": "#vertex-Sensor",
    "SensorVariable": "#vertex-SensorVariable"
  };

  // Refresh topology
  $scope.refresh = function() {
    MetricsFactory.getTopologyData($scope.query, function(data){
        $scope.data = data;
        //$scope.relations = data.relations;
        $scope.displayKinds = data.kinds;
      },function(error){
      displayRestError.display(error);
    });
  };

  $scope.isEmpty = function(item){
    return angular.equals({}, item);
  }

  $scope.checkboxModel = {
    value : false
  };
    $scope.legendTooltip = "Click here to show/hide entities of this type";

    $scope.show_hide_names = function() {
       var vertices = $scope.vs;
       if($scope.checkboxModel.value) {
            vertices.selectAll("tspan")
               .style("display", "block");
       }
       else {
           vertices.selectAll("tspan")
              .style("display", "none");
       }
    };

    $scope.refresh();
    var promise = $interval( $scope.refresh, mchelper.cfg.globalPageRefreshTime);

    $scope.$on('$destroy', function() {
        $interval.cancel(promise);
    });

    $scope.$on("render", function(ev, vertices, added) {
        /*
         * We are passed two selections of <g> elements:
         * vertices: All the elements
         * added: Just the ones that were added
         */
        added.attr("class", function(d) { return self.getKindClass(d); });
        added.append("circle")
            .attr("r", function(d) { return self.getDimensions(d).r})
            .attr('class' , function(d) {
              return TopologyService.getItemStatusClass(d);
            });
        added.append("title");
        added.on("dblclick", function(d) {
            return self.dblclick(d);});
        added.append("text")
          .attr("x",  function(d) { return self.getDimensions(d).x})
          .attr("y", function(d) { return self.getDimensions(d).y})
          .style("font-family", function(d) {return self.getIcon(d).fname;})
          .style("fill", function(d) { return self.getDimensions(d).fc})
          .attr('font-size', function(d) { return self.getDimensions(d).fs +'px'} )
          .text(function(d) {return self.getIcon(d).ucode;})

          .append("tspan")
            .attr("x", 26)
            .attr("y", 24)
            .text(function(d) { return d.item.name })
            .style("font-size", function(d) {return "12px"}).style("fill", function(d) {return "black"})
            .style("font-family","FontAwesome")
            .style("display", function(d) {if ($scope.checkboxModel.value) {return "block"} else {return "none"}});

        added.selectAll("title").text(function(d) {
            return TopologyService.tooltip(d).join("\n");
        });
        $scope.vs = vertices;

        /* Don't do default rendering */
        ev.preventDefault();
    });

    self.class_name = function class_name(d) {
        var class_name = d.item.icon;
        return class_name;
    };

    this.dblclick = function dblclick(d) {
      //window.location.assign(TopologyService.geturl(d));
      switch(d.item.kind) {
        case "Gateway":
          $state.go("gatewaysDetail", {'id': d.item.id});
          break;
        case "Node":
          $state.go("nodesDetail", {'id': d.item.id});
          break;
        case "Sensor":
          $state.go("sensorsDetail", {'id': d.item.id});
          break;
        case "SensorVariable":
          $state.go("sensorVariableEdit", {'id': d.item.id});
          break;

      }
    };

    self.getDimensions = function getDimensions(d) {
        switch (d.item.kind) {
            case "Gateway":
                return { x: 0, y: 11, r: 28, fs: 33, fc:'#663333'};
            case "Node" :
                return { x: 0, y: 9, r: 21, fs: 27, fc:'#1186C1'};
            case "Sensor" :
                return { x: 0.2, y: 8, r: 15, fs: 21, fc:'#9467bd'};
            default :
                return { x: 0, y: 6, r: 12, fs:16, fc:'#ff7f0e'};
        }
    };

    self.getIcon = function(d) {
        switch (d.item.kind) {
            case "Gateway":
                return {ucode:'\uf1e6', fname:'FontAwesome'};
            case "Node" :
               return {ucode:'\uf0e8', fname:'FontAwesome'};
            case "Sensor" :
                return CommonServices.getSensorIconData(d.item.subType.en);
            default :
                return {ucode:'\uf005', fname:'FontAwesome'};
        }
    };

    self.getKindClass = function(d) {
        switch (d.item.kind) {
            case "Node":
                return 'McNode';
            default :
                return d.item.kind;
        }
    };

    $scope.searchNode = function() {
      var svg = TopologyService.getSVG(d3);
      var query = $scope.search.query;
      TopologyService.searchNode(svg, query);
    };

    $scope.resetSearch = function() {
        TopologyService.resetSearch(d3);
        // Reset the search term in search input
        $scope.search.query = "";
    };

  var resized = function(){
    var height = window.innerHeight - 265;
    if(height < 350){
      $scope.topologyHeight = 350;
    }else{
      $scope.topologyHeight = height;
    }
  }

  //set layout height
  resized();

});
