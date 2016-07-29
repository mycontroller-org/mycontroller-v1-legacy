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

angular.module('adf.widget.myc-time', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycTime', {
        title: 'MyController time',
        description: 'Displays date and time of MyController',
        templateUrl: 'controllers/adf-widgets/adf-myc-time/view.html',
        controller: 'mycTimeController',
        controllerAs: 'mycTime',
        config: {
          datePattern: 'MMM dd, yyyy',
          refreshTime:120,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-time/edit.html'
        }
      });
  })
  .controller('mycTimeController', function($scope, $interval, config, mchelper, $filter, StatusFactory){
    var mycTime = this;

    mycTime.isSyncing = false;
    mycTime.showLoading = true;
    mycTime.mycTimestamp = {};


    function updateDateTime(){
      mycTime.time = $filter('date')(mycTime.mycTimestamp.timestamp, mchelper.cfg.timeFormat, mchelper.cfg.timezone);
      mycTime.date = $filter('date')(mycTime.mycTimestamp.timestamp, config.datePattern, mchelper.cfg.timezone);
      mycTime.timezone = mchelper.cfg.timezone;
      mycTime.timezoneString = mchelper.cfg.timezoneString;
    };

    function getTimestampFromServer(){
      mycTime.isSyncing = true;
      StatusFactory.getTimestamp(function(response){
          mycTime.mycTimestamp = response;
          updateDateTime();
          mycTime.isSyncing = false;
          if(mycTime.showLoading){
            mycTime.showLoading = false;
          }
      });
    };

    function setDateAndTime(){
      if(mycTime.isSyncing){
        return;
      }
      if((mycTime.mycTimestamp.timestamp/1000 | 0) % config.refreshTime == 0){
        if(!mycTime.isSyncing){
          getTimestampFromServer();
        }
      }else{
        mycTime.mycTimestamp.timestamp += 1000;
        updateDateTime();
      }
    }

    getTimestampFromServer();
    setDateAndTime();

    // refresh every second
    var promise = $interval(setDateAndTime, 1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });
  });
