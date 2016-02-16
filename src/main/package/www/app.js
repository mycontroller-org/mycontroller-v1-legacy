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
'use strict';

// Declare app level module which depends on views, and components
var myControllerModule = angular.module('myController',[
  'ui.router',
  'ui.bootstrap',
  'ngResource',
  'ngCookies',
  'ui.bootstrap.datetimepicker',
  'base64',
  'colorpicker.module',
  'ngFileSaver',
  'pascalprecht.translate',
  'ngSanitize',
  'nvd3',
  'patternfly',
  'patternfly.charts',
  'patternfly.select',
  'patternfly.views',
  'patternfly.filters',
  'patternfly.card',
  'frapontillo.bootstrap-switch',
  'xeditable',
  'angularUtils.directives.dirPagination',
  'frapontillo.bootstrap-duallistbox',
  'adf',
  'adf.structures.base',
  'adf.widget.myc-sen-vars',
  'adf.widget.myc-sen-var-graph',
  'adf.widget.myc-time',
  'adf.widget.myc-sunrisetime',
  'adf.widget.news',
]);

myControllerModule.constant("mchelper", {
    internal:{},
    cfg:{},
    languages:{},
    user:{},
    userSettings:{},
});

myControllerModule.config(function($stateProvider, $urlRouterProvider) {
  //For any unmatched url, redirect to /dashboard
  $urlRouterProvider.otherwise('/dashboard');
  
	$stateProvider
    .state('dashboard', {
      url:"/dashboard",
      templateUrl: "partials/dashboard/dashboard.html",
      controller: "DashboardListController",
       data: {
        requireLogin: true
      }
    }).state('gatewaysList', {
      url:"/resources/gateways/list",
      templateUrl: "partials/gateways/gateways-list.html",
      controller: "GatewaysController",
       data: {
        requireLogin: true
      }
    }).state('gatewaysAddEdit', {
      url:"/resources/gateways/addedit/:id",
      templateUrl: "partials/gateways/gateway-add-edit.html",
      controller: "GatewaysControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('gatewaysDetail', {
      url:"/resources/gateways/detail/:id",
      templateUrl: "partials/gateways/gateways-detail.html",
      controller: "GatewaysControllerDetail",
       data: {
        requireLogin: true
      }
    }).state('nodesList', {
      url:"/resources/nodes/list/:gatewayId",
      templateUrl: "partials/nodes/nodes-list.html",
      controller: "NodesController",
       data: {
        requireLogin: true
      }
    }).state('nodesAddEdit', {
      url:"/resources/nodes/addedit/:id",
      templateUrl: "partials/nodes/node-add-edit.html",
      controller: "NodesControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('nodesDetail', {
      url:"/resources/nodes/detail/:id",
      templateUrl: "partials/nodes/node-detail.html",
      controller: "NodesControllerDetail",
       data: {
        requireLogin: true
      }
    }).state('sensorsList', {
      url:"/resources/sensors/list/:nodeId",
      templateUrl: "partials/sensors/sensors-list.html",
      controller: "SensorsController",
      data: {
        requireLogin: true
      }
    }).state('sensorsAddEdit', {
      url:"/resources/sensors/addedit/:id",
      templateUrl: "partials/sensors/sensor-add-edit.html",
      controller: "SensorsControllerAddEdit",
      data: {
        requireLogin: true
      }
    }).state('sensorsDetail', {
      url:"/resources/sensors/detail/:id",
      templateUrl: "partials/sensors/sensors-detail.html",
      controller: "SensorsControllerDetail",
      data: {
        requireLogin: true
      }
    }).state('alarmsList', {
      url:"/resources/alarms/list/:resourceType/:resourceId",
      templateUrl: "partials/alarms/alarms-list.html",
      controller: "AlarmsController",
       data: {
        requireLogin: true
      }
    }).state('alarmsAddEdit', {
      url:"/resources/alarms/addedit/:id",
      templateUrl: "partials/alarms/alarm-add-edit.html",
      controller: "AlarmsControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('timersList', {
      url:"/resources/timers/list/:resourceType/:resourceId",
      templateUrl: "partials/timers/timers-list.html",
      controller: "TimersController",
       data: {
        requireLogin: true
      }
    }).state('timersAddEdit', {
      url:"/resources/timers/addedit/:id",
      templateUrl: "partials/timers/timer-add-edit.html",
      controller: "TimersControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('resourcesGroupList', {
      url:"/resources/groups/list/:resourceType/:resourceId",
      templateUrl: "partials/resources-group/resources-group-list.html",
      controller: "ResourcesGroupController",
       data: {
        requireLogin: true
      }
    }).state('resourcesGroupAddEdit', {
      url:"/resources/groups/addedit/:id",
      templateUrl: "partials/resources-group/resources-group-add-edit.html",
      controller: "ResourcesGroupControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('resourcesGroupMapList', {
      url:"/resources/groups/map/list/:id",
      templateUrl: "partials/resources-group/resources-group-map-list.html",
      controller: "ResourcesGroupMapController",
       data: {
        requireLogin: true
      }
    }).state('resourcesGroupMapAddEdit', {
      url:"/resources/groups/map/addedit/:groupId/:id",
      templateUrl: "partials/resources-group/resources-group-map-add-edit.html",
      controller: "ResourcesGroupMapControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('actionBoardSensorsList', {
      url:"/actionboard/sensorsaction/list",
      templateUrl: "partials/action-board/sensors-action-list.html",
      controller: "SensorsActionControllerList",
       data: {
        requireLogin: true
      }
    }).state('forwardPayloadList', {
      url:"/resources/forwardpayload/list/:sensorId",
      templateUrl: "partials/forward-payload/forward-payload-list.html",
      controller: "ForwardPayloadController",
       data: {
        requireLogin: true
      }
    }).state('forwardPayloadAddEdit', {
      url:"/resources/forwardpayload/addedit/:id",
      templateUrl: "partials/forward-payload/forward-payload-add-edit.html",
      controller: "ForwardPayloadControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('firmwaresList', {
      url:"/resources/firmwares/list",
      templateUrl: "partials/firmwares/firmwares-list.html",
      controller: "FirmwaresController",
       data: {
        requireLogin: true
      }
    }).state('firmwaresAddEdit', {
      url:"/resources/firmwares/addedit/:id",
      templateUrl: "partials/firmwares/firmwares-add-edit.html",
      controller: "FirmwaresControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('firmwaresTypeList', {
      url:"/resources/firmwares/type/list",
      templateUrl: "partials/firmwares/firmwares-type-list.html",
      controller: "FirmwaresTypeController",
       data: {
        requireLogin: true
      }
    }).state('firmwaresTypeAddEdit', {
      url:"/resources/firmwares/type/addedit/:id",
      templateUrl: "partials/firmwares/firmwares-type-add-edit.html",
      controller: "FirmwaresTypeControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('firmwaresVersionList', {
      url:"/resources/firmwares/version/list",
      templateUrl: "partials/firmwares/firmwares-version-list.html",
      controller: "FirmwaresVersionController",
       data: {
        requireLogin: true
      }
    }).state('firmwaresVersionAddEdit', {
      url:"/resources/firmwares/version/addedit/:id",
      templateUrl: "partials/firmwares/firmwares-version-add-edit.html",
      controller: "FirmwaresVersionControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('resourcesLogsList', {
      url:"/resources/logs/:resourceType/:resourceId",
      templateUrl: "partials/resources-logs/resources-logs-list.html",
      controller: "ResourcesLogsController",
      data: {
        requireLogin: true
      }
    }).state('resourcesLogsPurge', {
      url:"/resources/logs/purge",
      templateUrl: "partials/resources-logs/resources-logs-purge.html",
      controller: "ResourcesLogsPurgeController",
      data: {
        requireLogin: true
      }
    })
    
    
    
    .state('statusSystem', {
      url:"/status/system",
      templateUrl: "partials/status/system-status.html",
      controller: "StatusSystemController",
      data: {
        requireLogin: true
      }
    })
    
    
    .state('settingsSystem', {
      url:"/settings/system",
      templateUrl: "partials/settings/settings-system.html",
      controller: "SettingsSystemController",
       data: {
        requireLogin: true
      }
    }).state('settingsUnits', {
      url:"/settings/units",
      templateUrl: "partials/settings/settings-units.html",
      controller: "SettingsUnitsController",
       data: {
        requireLogin: true
      }
    }).state('settingsMetrics', {
      url:"/settings/metrics",
      templateUrl: "partials/settings/settings-metrics.html",
      controller: "SettingsMetricsController",
       data: {
        requireLogin: true
      }
    }).state('settingsNotifications', {
      url:"/settings/notifications",
      templateUrl: "partials/settings/settings-notifications.html",
      controller: "SettingsNotificationsController",
       data: {
        requireLogin: true
      }
    }).state('settingsMySensors', {
      url:"/settings/mysensors",
      templateUrl: "partials/settings/settings-mysensors.html",
      controller: "SettingsSystemMySensors",
       data: {
        requireLogin: true
      }
    }).state('settingsVariablesMapperList', {
      url:"/settings/variablesmapper/list",
      templateUrl: "partials/variables-mapper/variables-mapper-list.html",
      controller: "VariablesMapperListController",
       data: {
        requireLogin: true
      }
    }).state('settingsVariablesMapperEdit', {
      url:"/settings/variablesmapper/edit/:sensorType",
      templateUrl: "partials/variables-mapper/variables-mapper-edit.html",
      controller: "VariablesMapperEditController",
       data: {
        requireLogin: true
      }
    }).state('settingsRolesList', {
      url:"/settings/roles/list",
      templateUrl: "partials/users-roles/roles-list.html",
      controller: "RolesControllerList",
       data: {
        requireLogin: true
      }
    }).state('settingsRolesAddEdit', {
      url:"/settings/roles/addedit/:id",
      templateUrl: "partials/users-roles/roles-add-edit.html",
      controller: "RolesControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('settingsUsersList', {
      url:"/settings/users/list",
      templateUrl: "partials/users-roles/users-list.html",
      controller: "UsersControllerList",
       data: {
        requireLogin: true
      }
    }).state('settingsUsersAddEdit', {
      url:"/settings/users/addedit/:id",
      templateUrl: "partials/users-roles/users-add-edit.html",
      controller: "UsersControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('settingsProfileUpdate', {
      url:"/settings/profile/update",
      templateUrl: "partials/users-roles/profile-update.html",
      controller: "ProfileControllerUpdate",
       data: {
        requireLogin: true
      }
    }).state('settingsBackupList', {
      url:"/settings/backup/list",
      templateUrl: "partials/backup/backup-list.html",
      controller: "BackupControllerList",
       data: {
        requireLogin: true
      }
    })
    
    
    
    
    .state('users', {
      url:"/settings/users",
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
    }).state('sensorlog', {
      url:"/sensorlog/:id",
      templateUrl: "partials/sensorLogs/sensorsLog.html",
      controller: "SensorLogController",
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
    }).state('uidtag', {
      url:"/uidtag",
      templateUrl: "partials/uidTag/uidTag.html",
      controller: "UidTagController",
       data: {
        requireLogin: true
      }
    }).state('sendRawMessage', {
      url:"/utils/sendRawMessage",
      templateUrl: "partials/rawMessage/rawMessage.html",
      controller: "RawMessageController",
       data: {
        requireLogin: true
      }
    }).state('gatewaystatus', {
      url:"/status/gatewaystatus",
      templateUrl: "partials/status/gatewayStatus.html",
      controller: "GatewayStatusController",
       data: {
        requireLogin: true
      }
    }).state('firmware', {
      url:"/utils/firmware",
      templateUrl: "partials/firmwares/firmware.html",
      controller: "FirmwareController",
       data: {
        requireLogin: true
      }
    }).state('firmwareType', {
      url:"/utils/firmwareType",
      templateUrl: "partials/firmwares/firmwareType.html",
      controller: "FirmwareTypeController",
       data: {
        requireLogin: true
      }
    }).state('firmwareVersion', {
      url:"/utils/firmwareVersion",
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
      },
      params: { 
      'toState': 'dashboard', // default state to proceed to after login
      'toParams': {}
    },
    });
});


//McNavCtrl
myControllerModule.controller('McNavBarCtrl', function($scope, $location, $translate, $state, mchelper, SettingsFactory, CommonServices) {
    $scope.isCollapsed = true;
    $scope.mchelper = mchelper;
    $scope.$state = $state;
    
    $scope.isAuthenticated = function () { 
        return mchelper.internal.currentUser;
    };

    $scope.changeLanguage = function (lang) {
      $translate.use(lang.id);
      $scope.languageId = lang.id;
      mchelper.cfg.languageId = lang.id;
      mchelper.cfg.language = lang.displayName;
      //Update selected language
      if(mchelper.user.permission === 'Super admin'){
        SettingsFactory.updateLanguage(lang.displayName);
      }
      //Update mchelper
      CommonServices.saveMchelper(mchelper);
    };
});

myControllerModule.run(function ($rootScope, $state, $location, $http, mchelper, $translate, editableOptions, CommonServices) {
  //Load mchelper from cookies
  CommonServices.loadMchelper();

  // keep user logged in after page refresh
  if(!mchelper){
    CommonServices.saveMchelper(CommonServices.loadMchelper());
  };
  
  if(mchelper.cfg){
    $translate.use(mchelper.cfg.languageId);
  }

  
  if (mchelper.internal.currentUser) {
      $http.defaults.headers.common['Authorization'] = 'Basic ' + mchelper.internal.currentUser.authdata; // jshint ignore:line
  }

  $rootScope.$on('$stateChangeStart', function (event, toState, toParams) {
    //alert(angular.toJson(toState));
    if(toState.name.indexOf('login') === 0){
        angular.element( document.querySelector( '#rootId' ) ).addClass( "login-pf" );
        angular.element( document.querySelector( '#rootView' ) ).removeClass( "container-fluid top-buffer" );
    }else{
      angular.element( document.querySelector( '#rootId' ) ).removeClass( "login-pf" );
      angular.element( document.querySelector( '#rootView' ) ).addClass( "container-fluid top-buffer" );
    }
    var requireLogin = toState.data.requireLogin;
    // redirect to login page if not logged in
    if (requireLogin && !mchelper.internal.currentUser) {
      event.preventDefault();
      //return $state.go('login');
      return $state.go('login', {'toState': toState.name, 'toParams': toParams});
    }
  });

  //update xeditable theme
  editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'

});

myControllerModule.controller('LoginController',
    function ($state, $scope, $rootScope, AuthenticationService, ReadFileFactory, alertService, StatusFactory, TypesFactory, SettingsFactory, displayRestError, CommonServices, mchelper, $translate, $filter) {
        // load login page settings
        $scope.loginSettings = {};
        // reset login status
        AuthenticationService.ClearCredentials();
        // remove mchelper cookies
        //CommonServices.clearCookies();
        //Update login page details
        ReadFileFactory.getConfigFile(function(configFile){
          $scope.loginSettings = configFile;
          //Update language
          $translate.use($scope.loginSettings.languageId);
        });

        $scope.login = function () {
            $scope.dataLoading = true;
            AuthenticationService.Login($scope.username, $scope.password, function(authResponse) {
                if(authResponse.success) {
                    AuthenticationService.SetCredentials($scope.username, $scope.password);
                    mchelper.user = authResponse.user;//Update user details
                    StatusFactory.getConfig(function(response) {
                      mchelper.cfg = response;//Update config
                      //Update language
                      $translate.use(mchelper.cfg.languageId);
                      TypesFactory.getLanguages(function(langResponse){
                        mchelper.languages = langResponse;
                        SettingsFactory.getUserSettings(function(userNativeSettings){
                          mchelper.userSettings = userNativeSettings;
                          //Store all the configurations locally
                          CommonServices.saveMchelper(mchelper);
                        });
                      });
                    },function(error){
                      displayRestError.display(error);            
                    });
                    //$state.go('dashboard'); 
                    $state.go($state.params.toState, $state.params.toParams);
                } else {
                    if(authResponse.message){
                      alertService.danger(authResponse.message);
                    }else{
                      alertService.danger($filter('translate')('INVALID_USERNAME_OR_PASSWORD'));
                    }
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

myControllerModule.filter('byteToFriendlyConvertor', function() {
  return function(sizeInByte) {
    if(sizeInByte < 0){
      return "n/a";
    }else if((sizeInByte /(1024 * 1024)) > 1024){
      return (sizeInByte /(1024 * 1024 * 1024)).toFixed(2) + " GB";
    }else if((sizeInByte /(1024)) > 1024){
      return (sizeInByte /(1024 * 1024)).toFixed(2) + " MB";
    }else if(sizeInByte > 1024){
    return (sizeInByte /1024).toFixed(2) + " KB";
    }
    return sizeInByte + " Bytes";
  }
});

myControllerModule.filter('mcResourceRepresentation', function() {
  return function(text){
    if(text === undefined){
      return undefined;
    }
    return text.replace(/>>/g, '<i class="fa fa-chevron-right"></i>')
               .replace(/\[RG\]:/g, '<i class="pficon pficon-replicator fa-lg mc-margin-icon"></i> ')
               .replace(/\[G\]:/g, '<i class="fa fa-plug"></i> ')
               .replace(/\[N\]:/g, '<i class="fa fa-sitemap"></i> ')
               .replace(/\[S\]:/g, '<i class="fa fa-eye"></i> ')
               .replace(/\[SV\]:/g, '')
               .replace(/\[T\]:/g, '<i class="fa fa-clock-o"></i> ')
               .replace(/\[AD\]:/g, '<i class="fa fa-bell-o"></i> ');
  }
});


myControllerModule.filter('mcHtml', function($sce) {
    return function(htmlText) {
       return $sce.trustAsHtml(htmlText);
       //return htmlText
    };
});

/** 
 * i18n Language support
 * */
 
myControllerModule.config(function($translateProvider) {
  // Enable escaping of HTML
  //$translateProvider.useSanitizeValueStrategy('sanitize');
  $translateProvider.useSanitizeValueStrategy(null);
  $translateProvider.useStaticFilesLoader({
    prefix: 'languages/mc_locale_gui-',
    suffix: '.json'
  });
  $translateProvider.preferredLanguage('en_us');
  
});



//Items Delete Modal
myControllerModule.controller('ControllerDeleteModal', function ($scope, $uibModalInstance, $sce, $filter) {
  $scope.header = $filter('translate')('DELETE_ITEMS');
  $scope.deleteMsg = $filter('translate')('DELETE_MESSAGE');
  $scope.remove = function() {
    $uibModalInstance.close();
  };
  $scope.cancel = function () { $uibModalInstance.dismiss('cancel'); }
});

//Global exception handler
myControllerModule.factory('$exceptionHandler', function () {
  return function errorCatcherHandler(exception, cause) {
    throw exception;
  };
});
