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
  'patternfly.toolbars',
  'frapontillo.bootstrap-switch',
  'xeditable',
  'angularUtils.directives.dirPagination',
  'frapontillo.bootstrap-duallistbox',
  'angularMoment',
  'adf',
  'adf.structures.base',
  'adf.widget.myc-sen-vars',
  'adf.widget.myc-a-sensor-graph',
  'adf.widget.myc-sensors-grouped-graph',
  'adf.widget.myc-sensors-mixed-graph',
  'adf.widget.myc-sensors-bullet-graph',
  'adf.widget.myc-heat-map',
  'adf.widget.myc-custom-buttons',
  'adf.widget.myc-dsi',
  'adf.widget.myc-groups',
  'adf.widget.myc-time',
  'adf.widget.myc-sunrisetime',
  'adf.widget.news',
  'adf.widget.myc-custom-widget',
  'ngMap',
  'kubernetesUI',
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

    /* Dashboard */
    .state('dashboardMain', {
      url:"/dashboard",
      templateUrl: "partials/dashboard/dashboard.html",
      controller: "DashboardListController",
       data: {
        requireLogin: true
      }
    }).state('dashboardRoomsSensorsList', {
      url:"/dashboard/rooms/list/:id",
      templateUrl: "partials/rooms/rooms-sensors-list.html",
      controller: "RoomsSensorsControllerList",
       data: {
        requireLogin: true
      }
    }).state('dashboardTopology', {
      url:"/dashboard/topology/:resourceType/:resourceId",
      templateUrl: "partials/topology/topology.html",
      controller: "TopologyController",
       data: {
        requireLogin: true
      }
    })

    /* Resources */
    .state('gatewaysList', {
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
    }).state('sensorVariableEdit', {
      url:"/resources/sensorvariable/edit/:id",
      templateUrl: "partials/sensors/sensor-variable-edit.html",
      controller: "SensorVariableControllerEdit",
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
    }).state('rulesList', {
      url:"/resources/rules/list/:resourceType/:resourceId",
      templateUrl: "partials/rule-engine/rules-list.html",
      controller: "RuleEngineController",
       data: {
        requireLogin: true
      }
    }).state('rulesAddEdit', {
      url:"/resources/rules/addedit/:id/:action",
      templateUrl: "partials/rule-engine/rules-add-edit.html",
      controller: "RuleEngineControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('operationsList', {
      url:"/resources/operations/list",
      templateUrl: "partials/operations/operations-list.html",
      controller: "OperationsController",
       data: {
        requireLogin: true
      }
    }).state('operationsAddEdit', {
      url:"/resources/operations/addedit/:id/:action",
      templateUrl: "partials/operations/operations-add-edit.html",
      controller: "OperationsControllerAddEdit",
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
    }).state('timersList', {
      url:"/resources/timers/list/:resourceType/:resourceId",
      templateUrl: "partials/timers/timers-list.html",
      controller: "TimersController",
       data: {
        requireLogin: true
      }
    }).state('timersAddEdit', {
      url:"/resources/timers/addedit/:id/:action",
      templateUrl: "partials/timers/timer-add-edit.html",
      controller: "TimersControllerAddEdit",
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
    }).state('roomsList', {
      url:"/resources/rooms/list",
      templateUrl: "partials/rooms/rooms-list.html",
      controller: "RoomsControllerList",
       data: {
        requireLogin: true
      }
    }).state('roomsAddEdit', {
      url:"/resources/rooms/addedit/:id",
      templateUrl: "partials/rooms/rooms-add-edit.html",
      controller: "RoomsControllerAddEdit",
       data: {
        requireLogin: true
      }
    })

    /* Action board */
    .state('actionBoardSensorsList', {
      url:"/actionboard/sensorsaction/list",
      templateUrl: "partials/action-board/sensors-action-list.html",
      controller: "SensorsActionControllerList",
       data: {
        requireLogin: true
      }
    }).state('sendRawMessage', {
      url:"/actionboard/sendrawmessage",
      templateUrl: "partials/send-raw-message/send-raw-message.html",
      controller: "SendRawMessageController",
       data: {
        requireLogin: true
      }
    })

    /* Status */
    .state('aboutMyController', {
      url:"/status/about",
      templateUrl: "partials/status/about.html",
      controller: "McAboutController",
      data: {
        requireLogin: true
      }
    }).state('statusSystem', {
      url:"/status/system",
      templateUrl: "partials/status/system-status.html",
      controller: "StatusSystemController",
      data: {
        requireLogin: true
      }
    }).state('resourcesLogsList', {
      url:"/status/resourceslogs/:resourceType/:resourceId",
      templateUrl: "partials/resources-logs/resources-logs-list.html",
      controller: "ResourcesLogsController",
      data: {
        requireLogin: true
      }
    }).state('resourcesLogsPurge', {
      url:"/status/resourceslogs/purge",
      templateUrl: "partials/resources-logs/resources-logs-purge.html",
      controller: "ResourcesLogsPurgeController",
      data: {
        requireLogin: true
      }
    }).state('mycontrollerLogList', {
      url:"/status/log/mycontroller",
      templateUrl: "partials/status/mc-log-list.html",
      controller: "StatusMcLogController",
      data: {
        requireLogin: true
      }
    })

    /* Utilities */
    .state('scriptsList', {
      url:"/utilities/scripts/list",
      templateUrl: "partials/scripts/scripts-list.html",
      controller: "ScriptsController",
       data: {
        requireLogin: true
      }
    }).state('scriptsAddEdit', {
      url:"/utilities/scripts/addedit/:name",
      templateUrl: "partials/scripts/scripts-add-edit.html",
      controller: "ScriptsControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('templatesList', {
      url:"/utilities/templates/list",
      templateUrl: "partials/templates/templates-list.html",
      controller: "TemplatesController",
       data: {
        requireLogin: true
      }
    }).state('templatesAddEdit', {
      url:"/utilities/templates/addedit/:name",
      templateUrl: "partials/templates/templates-add-edit.html",
      controller: "TemplatesControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('additionalHeadersUpdate', {
      url:"/utilities/additionalheaders/update",
      templateUrl: "partials/additional-headers/additional-headers-update.html",
      controller: "AdditionalHeadersUpdateController",
       data: {
        requireLogin: true
      }
    }).state('variablesRepositoryList', {
      url:"/utilities/variables/list",
      templateUrl: "partials/variables-repository/variables-list.html",
      controller: "VariablesRepositoryController",
       data: {
        requireLogin: true
      }
    }).state('variablesRepositoryAddEdit', {
      url:"/utilities/variables/addedit/:id",
      templateUrl: "partials/variables-repository/variables-add-edit.html",
      controller: "VariablesRepositoryControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('firmwaresList', {
      url:"/utilities/firmwares/list",
      templateUrl: "partials/firmwares/firmwares-list.html",
      controller: "FirmwaresController",
       data: {
        requireLogin: true
      }
    }).state('firmwaresAddEdit', {
      url:"/utilities/firmwares/addedit/:id",
      templateUrl: "partials/firmwares/firmwares-add-edit.html",
      controller: "FirmwaresControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('firmwaresTypeList', {
      url:"/utilities/firmwares/type/list",
      templateUrl: "partials/firmwares/firmwares-type-list.html",
      controller: "FirmwaresTypeController",
       data: {
        requireLogin: true
      }
    }).state('firmwaresTypeAddEdit', {
      url:"/utilities/firmwares/type/addedit/:id",
      templateUrl: "partials/firmwares/firmwares-type-add-edit.html",
      controller: "FirmwaresTypeControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('firmwaresVersionList', {
      url:"/utilities/firmwares/version/list",
      templateUrl: "partials/firmwares/firmwares-version-list.html",
      controller: "FirmwaresVersionController",
       data: {
        requireLogin: true
      }
    }).state('firmwaresVersionAddEdit', {
      url:"/utilities/firmwares/version/addedit/:id",
      templateUrl: "partials/firmwares/firmwares-version-add-edit.html",
      controller: "FirmwaresVersionControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('uidTagsList', {
      url:"/utilities/uidtags/list",
      templateUrl: "partials/uid-tags/uid-tags-list.html",
      controller: "UidTagsController",
       data: {
        requireLogin: true
      }
    }).state('uidTagsAddEdit', {
      url:"/utilities/uidtags/addedit/:id",
      templateUrl: "partials/uid-tags/uid-tags-add-edit.html",
      controller: "UidTagsControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('resourcesDataList', {
      url:"/utilities/resourcesdata/list",
      templateUrl: "partials/resources-data/resources-data-list.html",
      controller: "ResourcesDataController",
       data: {
        requireLogin: true
      }
    }).state('resourcesDataAddEdit', {
      url:"/utilities/resourcesdata/addedit/:id",
      templateUrl: "partials/resources-data/resources-data-add-edit.html",
      controller: "ResourcesDataControllerAddEdit",
       data: {
        requireLogin: true
      }
    }).state('externalServersList', {
      url:"/utilities/externalserver/list",
      templateUrl: "partials/external-servers/external-servers-list.html",
      controller: "ExternalServerController",
       data: {
        requireLogin: true
      }
    }).state('externalServersAddEdit', {
      url:"/utilities/externalserver/addedit/:id",
      templateUrl: "partials/external-servers/external-server-add-edit.html",
      controller: "ExternalServersControllerAddEdit",
       data: {
        requireLogin: true
      }
    })

    /* Settings */
    .state('settingsProfileUpdate', {
      url:"/settings/profile/update",
      templateUrl: "partials/users-roles/profile-update.html",
      controller: "ProfileControllerUpdate",
       data: {
        requireLogin: true
      }
    }).state('settingsSystem', {
      url:"/settings/system",
      templateUrl: "partials/settings/settings-system.html",
      controller: "SettingsSystemController",
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
    }).state('settingsMqttBroker', {
      url:"/settings/mqttbroker",
      templateUrl: "partials/settings/settings-mqtt-broker.html",
      controller: "SettingsMqttBrokerController",
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
    }).state('settingsBackupList', {
      url:"/settings/backup/list",
      templateUrl: "partials/backup/backup-list.html",
      controller: "BackupControllerList",
       data: {
        requireLogin: true
      }
    }).state('settingsBackupAuto', {
      url:"/settings/backup/settings",
      templateUrl: "partials/backup/automatic-backup-settings.html",
      controller: "BackupControllerAutoSettings",
       data: {
        requireLogin: true
      }
    })

    /* Login */
    .state('login', {
      url:"/login",
      templateUrl: "partials/authentication/login.html",
      controller: "LoginController",
      data: {
        requireLogin: false
      },
      params: {
      'toState': 'dashboardMain', // default state to proceed to after login
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

    //Show hide main menu
    $scope.showHideMainMenu = function () {
      if(mchelper.userSettings.hideMenu){
        mchelper.userSettings.hideMenu = false;
      }else{
        mchelper.userSettings.hideMenu = true;
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
        angular.element( document.querySelector( '#rootView' ) ).removeClass( "container-fluid top-buffer-m top-buffer-nm" );
    }else{
      angular.element( document.querySelector( '#rootId' ) ).removeClass( "login-pf" );
      if(!mchelper.userSettings.hideMenu){
        angular.element( document.querySelector( '#rootView' ) ).addClass( "container-fluid top-buffer-m");
      }else{
        angular.element( document.querySelector( '#rootView' ) ).addClass( "container-fluid top-buffer-nm");
      }
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
                          mchelper.userSettings.hideMenu = false;
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
               .replace(/\[RD\]:/g, '<i class="fa fa-cogs"></i> ');
  }
});


myControllerModule.filter('mcHtml', function($sce) {
    return function(htmlText) {
       return $sce.trustAsHtml(htmlText);
       //return htmlText
    };
});

myControllerModule.filter('slice', function() {
  return function(arr, start, end) {
    return (arr || []).slice(start, end);
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

//Dashboard custom layouts
myControllerModule.config(function(dashboardProvider){
  dashboardProvider
    .structure('4-4-4/12', {
      rows: [{
        columns: [{
          styleClass: 'col-md-4'
        }, {
          styleClass: 'col-md-4'
        }, {
          styleClass: 'col-md-4'
        }]
      }, {
        columns: [{
          styleClass: 'col-md-12'
        }]
      }]
    }).structure('6-6/12', {
      rows: [{
        columns: [{
          styleClass: 'col-md-6'
        }, {
          styleClass: 'col-md-6'
        }]
      }, {
        columns: [{
          styleClass: 'col-md-12'
        }]
      }]
    });
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

/*
//Global exception handler
myControllerModule.factory('$exceptionHandler', function () {
  return function errorCatcherHandler(exception, cause) {
    console.log('Exception cause:'+cause+', Exception:'+angular.toJson(exception));
    //throw exception;
  };
});
*/
