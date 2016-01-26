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
// don't forget to declare this service module as a dependency in your main app constructor!
//http://js2.coffee/#coffee2js
//https://coderwall.com/p/r_bvhg/angular-ui-bootstrap-alert-service-for-angular-js

myControllerModule.factory('CommonServices', function(TypesFactory) {
  var commonService = {};
  
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
  }
  
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
  commonService.selectItem = function (baseScope, item){
    if(baseScope.itemIds.indexOf(item.id) == -1){
      baseScope.itemIds.push(item.id);
    }else{
      baseScope.itemIds.splice(baseScope.itemIds.indexOf(item.id), 1);
    }
  };

  // select ALL/NONE of table row function
  commonService.selectAllItems = function (baseScope) {
    if(baseScope.filteredList.length > 0){
      if(baseScope.filteredList.length == baseScope.itemIds.length){
        baseScope.itemIds = [];
      }else{
        baseScope.itemIds = [];
        angular.forEach(baseScope.filteredList, function(value, key) {
          baseScope.itemIds.push(value.id);
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
  commonService.getResources= function(resourceType){
    if(resourceType === 'Sensor variable'){
      return TypesFactory.getSensorVariables();
    }else if(resourceType === 'Gateway' || resourceType === 'Gateway state'){
      return TypesFactory.getGateways();
    }else if(resourceType === 'Node' || resourceType === 'Node state'){
      return TypesFactory.getNodes();
    }else if(resourceType === 'Resources group'){
      return TypesFactory.getResourcesGroups();
    }else{
      return null;
    }
  }
  
  // get Table configuration
  commonService.getQuery = function(){
    return query = {
      pageLimit: 10,
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
  
 return commonService;

});
