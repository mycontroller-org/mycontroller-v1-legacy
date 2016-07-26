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
// don't forget to declare this service module as a dependency in your main app constructor!
//http://js2.coffee/#coffee2js
//https://coderwall.com/p/r_bvhg/angular-ui-bootstrap-alert-service-for-angular-js

myControllerModule.factory('CommonServices', function(TypesFactory, $filter, $cookies, mchelper) {
  var commonService = {};

  //get mchelper configurations
  commonService.loadMchelper = function(){
    var mchelperLocal = $cookies.getObject('mchelper');
    if(mchelperLocal){
      mchelper.cfg = mchelperLocal.cfg || {};
      mchelper.user = mchelperLocal.user || {};
      mchelper.languages = mchelperLocal.languages || {};
      mchelper.userSettings = mchelperLocal.userSettings || {};
      mchelper.internal = mchelperLocal.internal || {};
    }
    return mchelper;
  };

  //restore store all the configurations locally
  commonService.saveMchelper = function(mchelperRemote){
    mchelper.cfg = mchelperRemote.cfg;
    mchelper.user = mchelperRemote.user;
    mchelper.languages = mchelperRemote.languages;
    mchelper.userSettings = mchelperRemote.userSettings;
    mchelper.internal = mchelperRemote.internal;
    $cookies.putObject('mchelper', mchelper);
  };

  //clear local mchelper
  commonService.clearMchelper = function(){
    mchelper.selectedDashboard = undefined;
    mchelper.cfg = {};
    mchelper.user = {};
    mchelper.languages = {};
    mchelper.userSettings = {};
    mchelper.internal = {};
  };

  //remove cookies
  commonService.clearCookies = function(){
    var cookies = $cookies.getAll();
    angular.forEach(cookies, function (v, k) {
      $cookies.remove(k);
    });
  };

  //Get value nested supported
  commonService.getValue = function(item, key){
    var keys = key.split('.');
    for (var i = 0, n = keys.length; i < n; ++i) {
        var k = keys[i];
        if (k in item) {
            item = item[k];
        } else {
            return;
        }
    }
    return item;
  };

  //Match value
  var matchesFilter = function (item, filter) {
    var match = true;
    var value = commonService.getValue(item, filter.id);
    //TODO: there is an issue with sensors action page. filter is not passing the type.
    //workaround, if undefined, set as type 'text'
    if(filter.type === undefined){
      if(angular.isNumber(value)){
        filter.value = parseInt(filter.value);
        filter.type = 'object';
      }else{
        filter.type = 'text';
      }
    }

    if(filter.type === 'text' || filter.type ===  'select'){
      if(value){
        match = value.toUpperCase().match(filter.value.toUpperCase()) !== null;
      }else{
        match = false;
      }
    }else if(filter.type === 'array'){
      return value.indexOf(filter.value) > -1;
    }else{
        match = angular.equals(value, filter.value);
    }
    return match;
  };

  //Match values with for loop
  var matchesFilters = function (item, filters) {
    var matches = true;
    filters.forEach(function(filter) {
      if (!matchesFilter(item, filter)) {
        matches = false;
        return false;
      }
    });
    return matches;
  };

  //Apply filter
  var applyFilters = function (filters, configMap) {
    configMap.filteredList = [];
    if (filters && filters.length > 0) {
      configMap.orgList.forEach(function (item) {
        if (matchesFilters(item, filters)) {
          configMap.filteredList.push(item);
        }
      });
    } else {
      configMap.filteredList = configMap.orgList;
    }
    configMap.filterConfig.resultsCount = configMap.filteredList.length;
  };

  // Common filter
  commonService.filterChangeLocal = function (filters, configMap) {
  configMap.filtersText = "";
    filters.forEach(function (filter) {
      configMap.filtersText += filter.title + " : " + filter.value + "\n";
    });
    applyFilters(filters, configMap);
  };

  //Select/unselect single row of table
  commonService.selectItem = function (baseScope, item, key){
    if(!key){
      key='id';
    }
    if(baseScope.itemIds.indexOf(item[key]) == -1){
      baseScope.itemIds.push(item[key]);
    }else{
      baseScope.itemIds.splice(baseScope.itemIds.indexOf(item[key]), 1);
    }
  };

  // select ALL/NONE of table row function
  commonService.selectAllItems = function (baseScope, key) {
    if(!key){
      key='id';
    }
    if(baseScope.filteredList.length > 0){
      if(baseScope.filteredList.length == baseScope.itemIds.length){
        baseScope.itemIds = [];
      }else{
        baseScope.itemIds = [];
        angular.forEach(baseScope.filteredList, function(value, keyT) {
          baseScope.itemIds.push(value[key]);
        });
      }
    }
  };

  // Update row selection of table function
  commonService.updateSelection = function (baseScope) {
    if(baseScope.itemIds.length > 0){
      tmpItemIds = baseScope.itemIds;
      baseScope.itemIds = [];
      angular.forEach(baseScope.filteredList, function(value, key) {
        if(tmpItemIds.indexOf(value.id) != -1){
          baseScope.itemIds.push(value.id);
        }
      });
    }
  };

  // get Table configuration
  commonService.getTableConfig = function(){
    return config = {
      itemsPerPage: 15,
      maxPages:10,
      fillLastPage: false
    };
  };


  // get resources
  commonService.getResources= function(resourceType, resourceId){
    if(resourceType === 'Sensor variable'){
      return TypesFactory.getSensorVariables();
    }else if(resourceType === 'Gateway' || resourceType === 'Gateway state'){
      return TypesFactory.getGateways();
    }else if(resourceType === 'Node' || resourceType === 'Node state'){
      return TypesFactory.getNodes({"gatewayId":resourceId});
    }else if(resourceType === 'Sensors'){
      return TypesFactory.getSensors({"nodeId":resourceId});
    }else if(resourceType === 'Resources group'){
      return TypesFactory.getResourcesGroups();
    }else if(resourceType === 'Alarm definition'){
      return TypesFactory.getAlarmDefinitions();
    }else if(resourceType === 'Timer'){
      return TypesFactory.getTimers();
    }else{
      return null;
    }
  }

  // get Table configuration
  commonService.getQuery = function(){
    return query = {
      pageLimit: mchelper.cfg.tableRowsLimit,
      page: 1,
      orderBy: "id",
      order: "asc"
    };
  };

  //Apply filters
  commonService.updateFiltersChange = function (remoteScope, filters) {
    //Clears filter
    remoteScope.filterConfig.fields.forEach(function (filter) {
      if(filter.filterType === 'text'){
        remoteScope.query[filter.id]=[];
      }else if(filter.filterType === 'select'){
        remoteScope.query[filter.id] = null;
      }
    });
    //Update filters
    filters.forEach(function (filter) {
      //This is to fix sensors action page
      //console.log(''+angular.toJson(filter));
      if(filter.type === undefined){
        remoteScope.filterConfig.fields.forEach(function (orgFilter) {
          if(filter.id === orgFilter.id){
            filter.type =  orgFilter.filterType;
          }
        });
      }
      if(filter.type === 'text'){
        remoteScope.query[filter.id].push(filter.value);
      }else if(filter.type === 'select'){
        remoteScope.query[filter.id] = filter.value;
      }
    });

    //move to page number 1 on filter change
    remoteScope.currentPage = 1;
    remoteScope.query.page=1;

    remoteScope.getAllItems();
  };

  //Update sort columns(orderBy)
  commonService.updateSortChange = function (remoteScope, sortId, isAscending) {
    if(isAscending){
      remoteScope.query.order = "asc";
    }else{
      remoteScope.query.order = "dsec";
    }
    remoteScope.query.orderBy = sortId.id;
    remoteScope.getAllItems();
  };

  //Update page change
  commonService.updatePageChange = function (remoteScope, newPage) {
    remoteScope.query.page = newPage;
    remoteScope.getAllItems();
  };

  //Get min number
  commonService.getMin = function(item1, item2){
    return Math.min(item1, item2);
  };

  //item for sensor actions
  //--------------------------------------------------

  //Defined variable types list
  commonService.getSensorVariablesKnownList = function(){
    var definedVariableTypes = ["Status","Watt","Temperature","Humidity","Pressure","Forecast","Armed","Tripped","Lock status","Percentage","Weight","Stop","Up","Down","Rain","Rain rate",
    "HVAC flow state","HVAC flow mode","HVAC speed","HVAC setpoint cool","HVAC setpoint heat","Variable 1","Variable 2","Variable 3","Variable 4","Variable 5","RGB","RGBW","Distance",
    "Current","Voltage","Impedance", "Volume"];
    return definedVariableTypes;
  };

  //Forecast mapper
    var forecastMapper = [
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

  commonService.getForecastValue = function(key){
    if(key === undefined){
      key = 'na';
    }
    var result = $filter('filter')(forecastMapper, {id: key}, true)[0];
    if(!result){
      return "na";
    }else{
      return result.value
    }
    //return $filter('filter')($scope.forecastMapper, {id: key}, true)[0].value;
  };


  //Sensor icons
    var sensorIcons = [
        {id:"default", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Door", value:"fa fa-building-o", ucode:"\uf0f7", fname:"FontAwesome"},
        {id:"Motion", value:"fa fa-paw", ucode:"\uf1b0", fname:"FontAwesome"},
        {id:"Smoke", value:"wi wi-smoke", ucode:"\uf062", fname:"weathericons"},
        {id:"Binary", value:"fa fa-power-off", ucode:"\uf011", fname:"FontAwesome"},
        {id:"Dimmer", value:"fa fa-lightbulb-o", ucode:"\uf0eb", fname:"FontAwesome"},
        {id:"Cover", value:"fa fa-archive", ucode:"\uf187", fname:"FontAwesome"},
        {id:"Temperature", value:"wi wi-thermometer", ucode:"\uf055", fname:"weathericons"},
        {id:"Humidity", value:"wi wi-humidity", ucode:"\uf07a", fname:"weathericons"},
        {id:"Barometer", value:"wi wi-barometer", ucode:"\uf079", fname:"weathericons"},
        {id:"Wind", value:"wi wi-windy", ucode:"\uf079", fname:"weathericons"},
        {id:"Rain", value:"wi wi-raindrops", ucode:"\uf04e", fname:"weathericons"},
        {id:"UV", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Weight", value:"fa fa-balance-scale", ucode:"\uf24e", fname:"FontAwesome"},
        {id:"Power", value:"fa fa-bolt", ucode:"\uf0e7", fname:"FontAwesome"},
        {id:"Heater", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Distance", value:"fa fa-binoculars", ucode:"\uf1e5", fname:"FontAwesome"},
        {id:"Light level", value:"wi wi-moon-alt-waxing-crescent-5", ucode:"\uf0d4", fname:"weathericons"},
        {id:"Node", value:"fa fa-sitemap", ucode:"\uf0e8", fname:"FontAwesome"},
        {id:"Repeater node", value:"fa fa-sitemap", ucode:"\uf0e8", fname:"FontAwesome"},
        {id:"Lock", value:"fa fa-lock", ucode:"\uf023", fname:"FontAwesome"},
        {id:"IR", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Water", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Air quality", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Custom", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Dust", value:"wi wi-dust", ucode:"\uf063", fname:"weathericons"},
        {id:"Scene controller", value:"fa fa-picture-o", ucode:"\uf03e", fname:"FontAwesome"},
        {id:"RGB light", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"RGBW light", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Color sensor", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"HVAC", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Multimeter", value:"fa fa-calculator", ucode:"\uf1ec", fname:"FontAwesome"},
        {id:"Sprinkler", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Water leak", value:"fa fa-tint", ucode:"\uf043", fname:"FontAwesome"},
        {id:"Sound", value:"fa fa-volume-up", ucode:"\uf028", fname:"FontAwesome"},
        {id:"Vibration", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Moisture", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"Information", value:"fa fa-info", ucode:"\uf129", fname:"FontAwesome"},
        {id:"Gas", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
        {id:"GPS", value:"fa fa-eye", ucode:"\uf06e", fname:"FontAwesome"},
      ];

  commonService.getSensorIcon = function(key){
    return commonService.getSensorIconData(key).value;
  };

  commonService.getSensorIconData = function(key){
    if(key === undefined || key == 'Undefined'){
      key = 'default';
    }
    return $filter('filter')(sensorIcons, {id: key}, true)[0];
  };


  //RGBA functions
  //Function to convert rgba format to hex color
  commonService.rgba2hex = function rgb2hex(rgb){
    rgb = rgb.replace("rgba","").replace("(","").replace(")","").split(",");
    return "#"+parseInt(rgb[0],10).toString(16)
      + parseInt(rgb[1],10).toString(16)
      + parseInt(rgb[2],10).toString(16)
      + parseInt((parseFloat(rgb[3],10)*255)).toString(16);
  };

  //Function to convert hex format to RGBA color
  commonService.hex2rgba = function(hex){
    if(hex){
      hex = hex.replace('#','');
      r = parseInt(hex.substring(0,2), 16);
      g = parseInt(hex.substring(2,4), 16);
      b = parseInt(hex.substring(4,6), 16);
      opacity = parseInt(hex.substring(6,8), 16);
      result = 'rgba('+r+','+g+','+b+','+(opacity/255).toFixed(2)+')';
      return result;
    }
    return undefined;
  };

  //Get integer value for switch
  commonService.getInteger = function(value){
    if(!value){
      return undefined;
    }else{
      return parseInt(value);
    }
  };

  //Switch settings
  commonService.mcbStyle = {
      handleWidth: "60px",
      stateHandleWidth: "35px",
      labelWidth: "3px",
      animate:true,
      size:"small",
    };

  //validation methods
  //-----------------------

  //Number validation
  commonService.isNumber = function (value) {
    if (isNaN(value)) {
      return false;
    }
    return true;
  };

  //is contains space validation
  commonService.isContainsSpace = function (value) {
    if(value !== undefined){
      return !value.match(/\s/g);
    }
    return true;
  };

  //is valid JSON
  commonService.isJsonString = function (value) {
    try {
        JSON.stringify(eval('('+value+')'));
        return true;
    } catch(err) {
        return false;
    }
  };

  //guid helper
  var s4 = function() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  //get guid
  commonService.guid = function() {
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
  };

  //get friendly time
  commonService.getTimestampJson = function(timestamp){
    var timestampJson = {};
    if(timestamp % 31536000000  == 0){
      timestampJson.timestamp = timestamp / 31536000000;
      timestampJson.timeConstant = "31536000000";
      timestampJson.timeConstantString = $filter('translate')('YEARS');
    }else if(timestamp % 86400000  == 0){
      timestampJson.timestamp = timestamp / 86400000;
      timestampJson.timeConstant = "86400000";
      timestampJson.timeConstantString = $filter('translate')('DAYS');
    }else if(timestamp % 3600000  == 0){
      timestampJson.timestamp = timestamp / 3600000;
      timestampJson.timeConstant = "3600000";
      timestampJson.timeConstantString = $filter('translate')('Hours');
    }else if(timestamp % 60000  == 0){
      timestampJson.timestamp = timestamp / 60000;
      timestampJson.timeConstant = "60000";
      timestampJson.timeConstantString = $filter('translate')('Minutes');
    }
    return timestampJson;
  };

  //get timestamp
  commonService.getTimestamp = function(timestampJson){
    return timestampJson.timeConstant * timestampJson.timestamp;
  };

 return commonService;

});
