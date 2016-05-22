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
myControllerModule.service('TopologyService', function($filter) {

  this.tooltip = function tooltip(d) {
    var status = [
      $filter('translate')('NAME') + ': ' + d.item.name,
      $filter('translate')('TYPE') + ': ' + $filter('translate')(d.item.type)
    ];

    if(d.item.kind === "Sensor" || d.item.kind === "SensorVariable"){
      status.push($filter('translate')('SUB_TYPE') + ': ' + d.item.subType.locale);
    }

    if(d.item.kind === "Gateway" || d.item.kind === "Node"){
      status.push($filter('translate')('STATUS') + ': ' + $filter('translate')(d.item.status.toUpperCase()));
    }else if(d.item.kind === "SensorVariable"){
      status.push($filter('translate')('STATUS') + ': ' + d.item.status);
    }
    return status;
  };

  this.addContextMenuOption = function(popup, text, data, callback) {
    popup.append("p").text(text)
      .on('click' , function() {callback(data);});
  };

  this.searchNode = function(svg, query) {
    var nodes = svg.selectAll("g");
    if (query != "") {
      var selected = nodes.filter(function (d) {
        return d.item.name != query;
      });
      selected.style("opacity", "0.2");
      var links = svg.selectAll("line");
      links.style("opacity", "0.2");
    }
  };

  this.resetSearch = function(d3) {
    // Display all topology nodes and links
    d3.selectAll("g, line").transition()
      .duration(2000)
      .style("opacity", 1);
  };

  this.getSVG = function(d3) {
    var graph = d3.select("kubernetes-topology-graph");
    var svg = graph.select('svg');
    return svg;
  };

  this.defaultElementDimensions = function() {
    return { x: 0, y: 9, r: 17 };
  };

  this.getItemStatusClass = function(d) {
    switch (d.item.status) {
      case "Armed":
      case "On":
      case "ON":
      case "Up":
      case "Untripped":
        return "success";
      case "Armed":
      case "OFF":
      case "Off":
      case "Tripped":
      case "Down":
        return "error";
    }
  };


});
