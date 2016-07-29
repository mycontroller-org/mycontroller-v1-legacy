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

'use strict';

angular.module('adf.widget.myc-groups', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycGroups', {
        title: 'Groups',
        description: 'Change state(ON/OFF) of resources group(Scene control)',
        templateUrl: 'controllers/adf-widgets/adf-myc-groups/view.html',
        controller: 'mycGroupsController',
        controllerAs: 'mycGroups',
        config: {
          itemIds:[],
          itemsPerRow:"1",
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-groups/edit.html',
          controller: 'mycGroupsEditController',
          controllerAs: 'mycGroupsEdit',
        }
      });
  })
  .controller('mycGroupsController', function($scope, $interval, config, mchelper, $filter, ResourcesGroupFactory, TypesFactory, CommonServices){
    var mycGroups = this;

    mycGroups.showLoading = true;
    mycGroups.isSyncing = false;
    mycGroups.items = {};
    $scope.cs = CommonServices;

    function loadItems(){
      mycGroups.isSyncing = true;
      ResourcesGroupFactory.getAll({'id':config.itemIds, 'page':1, 'pageLimit':30}, function(response){
          mycGroups.items = response.data;
          mycGroups.isSyncing = false;
          if(mycGroups.showLoading){
            mycGroups.showLoading = false;
          }
      });
    };

    function updateItems(){
      if(mycGroups.isSyncing){
        return;
      }else if(config.itemIds.length > 0){
        loadItems();
      }
    }

    //load items initially
    updateItems();

    //On,Off switch control
    $scope.changeMystate = function(item, state){
      var itemArray = [item.id];
      if(state){
        ResourcesGroupFactory.turnOnIds(itemArray, function(response) {
          //alertService.success($filter('translate')('RESOURCE_GROUP_TURNED_ON'));
        },function(error){
          displayRestError.display(error);
        });
      }else{
        ResourcesGroupFactory.turnOffIds(itemArray, function(response) {
          //alertService.success($filter('translate')('RESOURCE_GROUP_TURNED_OFF'));
        },function(error){
          displayRestError.display(error);
        });
      }
    }

    // refresh every second
    var promise = $interval(updateItems, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });
  }).controller('mycGroupsEditController', function($scope, $interval, config, mchelper, $filter, TypesFactory, CommonServices){
    var mycGroupsEdit = this;
    mycGroupsEdit.cs = CommonServices;
    mycGroupsEdit.items = TypesFactory.getResourcesGroups();
  });
