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
'use strict';

// Declare app level module which depends on views, and components
var myControllerModule = angular.module('myController',[
  'ui.router',
  'ui.bootstrap',
  'ui-confirm',
  'angular-table',
  'ngResource',
  'ngCookies',
  'nvd3',
  'ui-rangeSlider',
  'isteven-multi-select',
  'ui.bootstrap.datetimepicker',
  'base64',
  'colorpicker.module',
  'ngFileSaver',
  'pascalprecht.translate',
  'ngSanitize',
  'angularModalService'
]).
config(function($stateProvider, $urlRouterProvider) {
  //For any unmatched url, redirect to /dashboard
  $urlRouterProvider.otherwise('/dashboard');
  
	$stateProvider
    .state('sensors', {
      url:"/sensors",
      templateUrl: "partials/sensors/sensors.html",
      controller: "SensorsController",
      data: {
        requireLogin: true // this property will apply to all children of 'sensor'
      }
    })
    .state('dashboard', {
      url:"/dashboard",
      templateUrl: "partials/dashboard/dashboard.html",
      controller: "DashboardController",
       data: {
        requireLogin: true
      }
    }).state('nodes', {
      url:"/nodes",
      templateUrl: "partials/nodes/nodes.html",
      controller: "NodesController",
       data: {
        requireLogin: true
      }
    }).state('sensorsaction', {
      url:"/sensorsaction",
      templateUrl: "partials/sensorsAction/sensorsAction.html",
      controller: "SensorsActionController",
       data: {
        requireLogin: true
      }
    }).state('users', {
      url:"/users",
      templateUrl: "partials/users/users.html",
      controller: "UsersController",
       data: {
        requireLogin: true
      }
    }).state('charts', {
      url:"/charts/:sensorId",
      templateUrl: "partials/charts/lineChart.html",
      controller: "ChartsController",
       data: {
        requireLogin: true
      }
    }).state('alarm', {
      url:"/alarm/:id",
      templateUrl: "partials/alarm/alarms.html",
      controller: "AlarmController",
       data: {
        requireLogin: true
      }
    }).state('timer', {
      url:"/timer/:id",
      templateUrl: "partials/timer/timers.html",
      controller: "TimerController",
       data: {
        requireLogin: true
      }
    }).state('sensorlog', {
      url:"/sensorlog/:id",
      templateUrl: "partials/sensorLogs/sensorsLog.html",
      controller: "SensorLogController",
       data: {
        requireLogin: true
      }
    }).state('logs', {
      url:"/logs",
      templateUrl: "partials/sensorLogs/sensorsLog.html",
      controller: "LogsController",
       data: {
        requireLogin: true
      }
    }).state('forwardPayload', {
      url:"/forwardPayload/:id",
      templateUrl: "partials/forwardPayload/forwardPayload.html",
      controller: "ForwardPayloadController",
       data: {
        requireLogin: true
      }
    }).state('variableMapper', {
      url:"/variableMapper",
      templateUrl: "partials/variableMapper/variableMapper.html",
      controller: "VariableMapperController",
       data: {
        requireLogin: true
      }
    }).state('uidtag', {
      url:"/uidtag",
      templateUrl: "partials/uidTag/uidTag.html",
      controller: "UidTagController",
       data: {
        requireLogin: true
      }
    }).state('sendRawMessage', {
      url:"/sendRawMessage",
      templateUrl: "partials/rawMessage/rawMessage.html",
      controller: "RawMessageController",
       data: {
        requireLogin: true
      }
    }).state('settings', {
      url:"/settings",
      templateUrl: "partials/settings/settings.html",
      controller: "SettingsController",
       data: {
        requireLogin: true
      }
    }).state('systemstatus', {
      url:"/systemstatus",
      templateUrl: "partials/status/systemStatus.html",
      controller: "SystemStatusController",
       data: {
        requireLogin: true
      }
    }).state('gatewaystatus', {
      url:"/gatewaystatus",
      templateUrl: "partials/status/gatewayStatus.html",
      controller: "GatewayStatusController",
       data: {
        requireLogin: true
      }
    }).state('firmware', {
      url:"/firmware",
      templateUrl: "partials/firmwares/firmware.html",
      controller: "FirmwareController",
       data: {
        requireLogin: true
      }
    }).state('firmwareType', {
      url:"/firmwareType",
      templateUrl: "partials/firmwares/firmwareType.html",
      controller: "FirmwareTypeController",
       data: {
        requireLogin: true
      }
    }).state('firmwareVersion', {
      url:"/firmwareVersion",
      templateUrl: "partials/firmwares/firmwareVersion.html",
      controller: "FirmwareVersionController",
       data: {
        requireLogin: true
      }
    }).state('login', {
      url:"/login",
      templateUrl: "partials/authentication/login.html",
      controller: "LoginController",
      data: {
        requireLogin: false
      }
    });
});


//McNavCtrl
myControllerModule.controller('McNavBarCtrl', function($scope, $location, $translate) {
   $scope.isCollapsed = true;
    $scope.isActive = function (viewLocation) { 
        return viewLocation === $location.path();
    };
    
    $scope.changeLanguage = function (langKey) {
      $translate.use(langKey);
    };
});

myControllerModule.run(function ($rootScope, $state, $location, $cookieStore, $http, about) {
  
  // keep user logged in after page refresh
  $rootScope.globals = $cookieStore.get('globals') || {};
  var mcabout = $cookieStore.get('mcabout') || {};
  about.timezone = mcabout.timezone;
  about.timezoneMilliseconds = mcabout.timezoneMilliseconds;
  about.timezoneString = mcabout.timezoneString;
  about.systemDate = mcabout.systemDate;
  about.appName = mcabout.appName;
  about.appVersion = mcabout.appVersion;
  
  if ($rootScope.globals.currentUser) {
      $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata; // jshint ignore:line
  }

  $rootScope.$on('$stateChangeStart', function (event, toState, toParams) {
    var requireLogin = toState.data.requireLogin;
    // redirect to login page if not logged in
    if (requireLogin && !$rootScope.globals.currentUser) {
      event.preventDefault();
      return $state.go('login');
    }
  });

});

myControllerModule.controller('LoginController',
    function ($state, $scope, $rootScope, AuthenticationService, alertService, StatusFactory, displayRestError, about, $cookieStore, $translate) {
        // reset login status
        AuthenticationService.ClearCredentials();
 
        $scope.login = function () {
            $scope.dataLoading = true;
            AuthenticationService.Login($scope.username, $scope.password, function(response) {
                if(response.success) {
                    AuthenticationService.SetCredentials($scope.username, $scope.password);
                    StatusFactory.about(function(response) {
                        about.timezone = response.timezone;
                        about.timezoneMilliseconds = response.timezoneMilliseconds;
                        about.timezoneString = response.timezoneString;
                        about.systemDate = response.systemDate;
                        about.appName = response.appName;
                        about.appVersion = response.appVersion;
                        about.language = response.language;
                        $cookieStore.put('mcabout', about);
                        $translate.use(about.language);
                    },function(error){
                      displayRestError.display(error);            
                    });
                    
                    alertService.success("Login success!");
                    $state.go('dashboard'); 
                } else {
                    alertService.danger("Username or Password is incorrect!");
                    $scope.dataLoading = false;
                }
            });
        };
    });
    
myControllerModule.filter('millSecondsToTimeString', function() {
  return function(millseconds) {
    var seconds = Math.floor(millseconds / 1000);
    var tmpSeconds = seconds % 60;
    var days = Math.floor(seconds / 86400);
    var hours = Math.floor((seconds % 86400) / 3600);
    var minutes = Math.floor(((seconds % 86400) % 3600) / 60);
    var timeString = '';
    if(days > 0){
      timeString += (days > 1) ? (days + " days ") : (days + " day ");
    }
    if(hours >0){
      timeString += (hours > 1) ? (hours + " hours ") : (hours + " hour ");
    }
    if(minutes > 0){
      timeString += (minutes >1) ? (minutes + " minutes ") : (minutes + " minute ");
    }
    if(tmpSeconds >= 0){
      timeString += (tmpSeconds >1) ? (tmpSeconds + " seconds ") : (tmpSeconds + " second ");
    }
    return timeString;
  }
});    

myControllerModule.filter('byteToMBsizeConvertor', function() {
  return function(sizeInByte) {
    if(sizeInByte < 0){
      return "n/a";
    }
    return Math.floor(sizeInByte /(1024 * 1024)) + " MB";
  }
});

myControllerModule.value("about", {
    timezone: '-',
    timezoneString: '-',
    systemDate: '-',
    appVersion:'-',
    appName: '-',
    language: 'en-us'    
});

//FooterCtrl
myControllerModule.controller('FooterCtrl', function($scope, about) {
  //about, Timezone, etc.,
  $scope.about = about;
});

/** 
 * i18n Language support
 * */
 
myControllerModule.config(function($translateProvider) {
  // Enable escaping of HTML
  //$translateProvider.useSanitizeValueStrategy('sanitize');
  $translateProvider.useSanitizeValueStrategy(null);
  $translateProvider.useStaticFilesLoader({
    prefix: 'languages/mc_locale_',
    suffix: '.json'
  });
 
  $translateProvider.preferredLanguage('en-us');
  
});
