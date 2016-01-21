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
myControllerModule.controller('SensorsActionControllerList', function(
  alertService, $scope, SensorsFactory, TypesFactory, NodesFactory, $uibModal, displayRestError, mchelper, CommonServices, pfViewUtils, $filter) {
  
  //GUI page settings
  //$scope.headerStringList = "Sesnors detail";
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
        id: 'node.gateway.name',
        title:  'Gateway',
        placeholder: 'Filter by Gateway',
        filterType: 'text'
      },
      {
        id: 'node.eui',
        title:  'Node EUI',
        placeholder: 'Filter by Type',
        filterType: 'text',
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
  
  
  //View selection
  var viewSelected = function(viewId) {
    $scope.viewType = viewId
  };
  
  //View configuration
  $scope.viewsConfig = {
      views: [pfViewUtils.getListView(), pfViewUtils.getTilesView()],
      onViewSelect: viewSelected,
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
  
  
  // Item tool bar config
  $scope.sensorsToolbarConfig = {
      viewsConfig: $scope.viewsConfig,
      filterConfig: $scope.filterConfig,
      sortConfig: $scope.sortConfig,
    };
  
  
  //refresh sensor
  $scope.refreshSensor = function(sensor){
    SensorsFactory.get({"id":sensor.id}, function(response) {
      var newSensor = response;
      sensor.lastSeen = newSensor.lastSeen;
      sensor.variables = newSensor.variables;
    },function(error){
      displayRestError.display(error);
    });
  };
  
  
  //Update Variable / Send Payload
  $scope.updateVariable = function(variable){
    SensorsFactory.updateVariable(variable, function(){
      //update Success
    },function(error){
      displayRestError.display(error);
    });
  };
  
  
  //Switch settings
  $scope.mcbStyle = {
      handleWidth: "60px",
      stateHandleWidth: "35px",
      labelWidth: "3px",
      animate:true,
      size:"small",
    };
  
  //Defined variable types list
  $scope.definedVariableTypes = ["Status","Watt","Temperature","Humidity","Pressure","Forecast","Armed","Tripped","Lock status","Percentage","Weight","Stop","Up","Down","Rain","Rain rate",
    "HVAC flow state","HVAC flow mode","HVAC speed","HVAC setpoint cool","HVAC setpoint heat","Variable 1","Variable 2","Variable 3","Variable 4","Variable 5","RGB","RGBW","Distance",
    "Current","Voltage","Impedance"];
    
  //HVAC heater options - HVAC flow state
  $scope.hvacOptionsFlowState = [
    {
      id:"AutoChangeOver",
      label:"Auto Change Over",
    },
    {
      id:"HeatOn",
      label:"Heat On",
    },
    {
      id:"CoolOn",
      label:"Cool On",
    },
    {
      id:"Off",
      label:"Off",
    },
  ];
  
  //HVAC heater options - HVAC flow mode
  $scope.hvacOptionsFlowMode = [
    {
      id:"Auto",
      label:"Auto",
    },
    {
      id:"ContinuousOn",
      label:"Continuous On",
    },
    {
      id:"PeriodicOn",
      label:"Periodic On",
    },
  ];
  
  //HVAC heater options - HVAC fan speed
  $scope.hvacOptionsFanSpeed = [
    {
      id:"Min",
      label:"Minimum",
    },
    {
      id:"Normal",
      label:"Normal",
    },
    {
      id:"Max",
      label:"Maximum",
    },
    {
      id:"Auto",
      label:"Auto",
    },
  ];
  
  //Forecast mapper
  $scope.forecastMapper = [
    {
      id:"sunny",
      value:"day-sunny",
    },{
      id:"cloudy",
      value:"day-cloudy",
    },{
      id:"thunderstorm",
      value:"day-thunderstorm",
    },{
      id:"stable",
      value:"na",
    },{
      id:"unstable",
      value:"na",
    },{
      id:"na",
      value:"na",
    },
  ];
  
  $scope.getForecastValue = function(key){
    if(key === undefined){
      key = 'na';
    }
    var result = $filter('filter')($scope.forecastMapper, {id: key}, true)[0];
    if(!result){
      return "na";
    }else{
      return result.value
    }
    //return $filter('filter')($scope.forecastMapper, {id: key}, true)[0].value;
  };
  
  //Sesnor type icon mapper
  $scope.sensorIcons = [
    { id:"na",value:"na"},{id:"Door",value:"fa fa-building-o"},{ id:"Motion",value:"fa fa-paw"},{ id:"Smoke",value:"wi wi-smoke"},{ id:"Binary",value:"fa fa-power-off"},{ id:"Dimmer",value:"fa fa-lightbulb-o"},
    { id:"Cover",value:"fa fa-archive"},{ id:"Temperature",value:"wi wi-thermometer"},{ id:"Humidity",value:"wi wi-humidity"},{ id:"Barometer",value:"wi wi-barometer"},{ id:"Wind",value:"wi wi-windy"},
    { id:"Rain",value:"wi wi-raindrops"},{ id:"UV",value:"fa fa-star-o"},{ id:"Weight",value:"fa fa-balance-scale"},{ id:"Power",value:"fa fa-bolt"},{ id:"Heater",value:"fa fa-star-o"},{ id:"Distance",value:"fa fa-binoculars"},
    { id:"Light level",value:"wi wi-moon-alt-waxing-crescent-5"},{ id:"Node",value:"fa fa-sitemap"},{ id:"Repeater node",value:"fa fa-sitemap"},{ id:"Lock",value:"fa fa-lock"},{ id:"IR",value:"fa fa-star-o"},
    { id:"Water",value:"fa fa-star-o"},{ id:"Air quality",value:"fa fa-star-o"},{ id:"Custom",value:"fa fa-star-o"},    { id:"Dust",value:"wi wi-dust"},{ id:"Scene controller",value:"fa fa-picture-o"},
    { id:"RGB light",value:"fa fa-star-o"},{ id:"RGBW light",value:"fa fa-star-o"},{ id:"Color sensor",value:"fa fa-star-o"},{ id:"HVAC",value:"fa fa-star-o"},{ id:"Multimeter",value:"fa fa-calculator"},
    { id:"Sprinkler",value:"fa fa-star-o"},{ id:"Water leak",value:"fa fa-tint"},{ id:"Sound",value:"fa fa-volume-up"},{ id:"Vibration",value:"fa fa-star-o"},{ id:"Moisture",value:"fa fa-star-o"},
    { id:"Information",value:"fa fa-info"},{ id:"Gas",value:"fa fa-star-o"},{ id:"GPS",value:"fa fa-star-o"},
  ];
  
  $scope.getSensorIcon = function(key){
    if(key === undefined){
      key = 'na';
    }
    return $filter('filter')($scope.sensorIcons, {id: key}, true)[0].value;
  };
  
  //Get integer value for switch
  $scope.getInteger = function(value){
    if(!value){
      return undefined;
    }else{
      return parseInt(value);
    }
  };
  

  
  //update rgba color
  $scope.updateRgba = function(variable){
    variable.value = rgba2hex(variable.rgba);
    $scope.updateVariable(variable);
  };
  
  //RGBA functions
  //Function to convert rgba format to hex color
  var rgba2hex = function rgb2hex(rgb){  
    rgb = rgb.replace("rgba","").replace("(","").replace(")","").split(",");
    return "#"+parseInt(rgb[0],10).toString(16)
      + parseInt(rgb[1],10).toString(16)
      + parseInt(rgb[2],10).toString(16)
      + parseInt((parseFloat(rgb[3],10)*255)).toString(16);
  };

  
  //Function to convert hex format to rgba color
  $scope.hex2rgba = function(hex){
    if(hex){
      hex = hex.replace('#','');
      r = parseInt(hex.substring(0,2), 16);
      g = parseInt(hex.substring(2,4), 16);
      b = parseInt(hex.substring(4,6), 16);
      opacity = parseInt(hex.substring(4,6), 16);
      result = 'rgba('+r+','+g+','+b+','+(opacity/255).toFixed(2)+')';
      return result;
    }
    return undefined;
  };
  
  
  //Pre load
  $scope.viewsConfig.currentView = $scope.viewsConfig.views[0].id;
  $scope.viewType = $scope.viewsConfig.currentView;
  //Update list table
  //getAllItems();

});
