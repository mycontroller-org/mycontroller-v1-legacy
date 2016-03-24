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

angular.module('adf.widget.myc-sunrisetime', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycSunriseTime', {
        title: 'Sunrise and sunset time',
        description: 'Displays sunrise and sunset time from MyController configuration',
        templateUrl: 'controllers/adf-widgets/adf-myc-sunrisetime/view.html',
        controller: 'mycSunriseController',
        controllerAs: 'mycSunriseTime',
        config: {
          refreshTime:300,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-sunrisetime/edit.html'
        }
      });
  })
  .controller('mycSunriseController', function($scope, $interval, config, mchelper, $filter, SettingsFactory){
    var mycSunriseTime = this;

    mycSunriseTime.isSyncing = false;
    mycSunriseTime.showLoading = true;

    function updateLocationSettings(){
      if(mycSunriseTime.isSyncing){
        return;
      }
      mycSunriseTime.isSyncing = true;
      SettingsFactory.getLocation(function(response){
          mycSunriseTime.sunriseTime = $filter('date')(response.sunriseTime, mchelper.cfg.timeFormatWithoutSeconds, mchelper.cfg.timezone);
          mycSunriseTime.sunsetTime = $filter('date')(response.sunsetTime, mchelper.cfg.timeFormatWithoutSeconds, mchelper.cfg.timezone);
          mycSunriseTime.latitude = response.latitude;
          mycSunriseTime.longitude = response.longitude;
          mycSunriseTime.isSyncing = false;
          if(mycSunriseTime.showLoading){
            mycSunriseTime.showLoading = false;
          }
      });
    };

    updateLocationSettings();

    // refresh every five minutes
    var promise = $interval(updateLocationSettings, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });
  });
