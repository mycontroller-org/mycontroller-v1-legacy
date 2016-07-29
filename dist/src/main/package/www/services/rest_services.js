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

//Sensors Services
myControllerModule.factory('SensorsFactory', function ($resource, $http, $base64) {
  return $resource('/mc/rest/sensors/:sensorId/:id', {sensorId: '@sensorId'}, {
    getAll:  { method: 'GET', isArray: false},
    get:   { method: 'GET', isArray: false, params: {sensorId: '@sensorId', id:null}},
    create: { method: 'POST', params: {sensorId: null}},
    update: { method: 'PUT', params: {sensorId: null}},
    delete: { method: 'DELETE', params: {sensorId: '@sensorId'} },
    deleteIds: { method: 'POST', params: {sensorId: 'deleteIds'} },
    updateVariable: { method: 'PUT', params: {sensorId: 'updateVariable', id:null}},
    updateVariableConfig: { method: 'PUT', params: {sensorId: 'updateVariableConfig', id:null}},
    getVariables: { method: 'GET', isArray: true, params: {sensorId: 'getVariables', id:null}},
    getVariable: { method: 'GET', isArray: false, params: {sensorId: 'getVariable'}},
    sendRawMessage: { method: 'POST', params: {sensorId:'sendRawMessage'} },
  })
});

//Node Services
myControllerModule.factory('NodesFactory', function ($resource) {
  return $resource('/mc/rest/nodes/:nodeId', {nodeId: '@nodeId'}, {
    getAll: { method: 'GET', isArray: false },
    get:    { method: 'GET' },
    create: { method: 'POST'},
    update: { method: 'PUT' },
    delete: { method: 'DELETE'},
    deleteIds: { method: 'POST', params: {nodeId: 'deleteIds'}},
    reboot: { method: 'POST', params: {nodeId: 'reboot'}},
    eraseConfiguration: { method: 'POST', params: {nodeId: 'eraseConfiguration'}},
    executeNodeInfoUpdate: { method: 'POST', params: {nodeId: 'executeNodeInfoUpdate'}},
    uploadFirmware: { method: 'POST', params: {nodeId: 'uploadFirmware'}},
  })
});

//Firmware Services
myControllerModule.factory('FirmwaresFactory', function ($resource) {
  return $resource('/mc/rest/firmwares/:type/:refId', {}, {
    getAllFirmwareTypes:  { method: 'GET', isArray: false, params: {type: 'types'}},
    getAllFirmwareVersions:  { method: 'GET', isArray: false, params: {type: 'versions'}},
    getAllFirmwares:  { method: 'GET', isArray: false, params: {type: 'firmwares'}},
    getFirmwareType:  { method: 'GET', isArray: false, params: {type: 'types', id: '@refId'}},
    getFirmwareVersion:  { method: 'GET', isArray: false, params: {type: 'versions', id: '@refId'}},
    getFirmware:  { method: 'GET', isArray: false,  params: {type: 'firmwares', id: '@refId'}},
    deleteFirmwareTypes:  { method: 'POST', params: {type: 'types', refId: 'delete'}},
    deleteFirmwareVersions:  { method: 'POST', params: {type: 'versions', refId: 'delete'}},
    deleteFirmwares:  { method: 'POST', params: {type: 'firmwares', refId: 'delete'}},
    updateFirmwareType:  { method: 'PUT', params: {type: 'types'}},
    updateFirmwareVersion:  { method: 'PUT', params: {type: 'versions'}},
    updateFirmware:  { method: 'PUT', params: {type: 'firmwares'}},
    createFirmwareType:  { method: 'POST', params: {type: 'types'}},
    createFirmwareVersion:  { method: 'POST', params: {type: 'versions'}},
    createFirmware:  { method: 'POST', params: {type: 'firmwares'}}
  })
});

//Types Services
myControllerModule.factory('TypesFactory', function ($resource) {
  return $resource('/mc/rest/types/:type/:id', {id: '@id'}, {
    getNodeTypes:  { method: 'GET', isArray: true, params: {type: 'nodeTypes'}  },
    getExternalServerTypes:  { method: 'GET', isArray: true, params: {type: 'externalServerTypes'}  },
    getTrustHostTypes:  { method: 'GET', isArray: true, params: {type: 'trustHostTypes'}  },
    getNodeRegistrationStatuses:  { method: 'GET', isArray: true, params: {type: 'nodeRegistrationStatuses'}  },
    getSensorTypes:  { method: 'GET', isArray: true, params: {type: 'sensorTypes'}},
    getMetricTypes:  { method: 'GET', isArray: true, params: {type: 'metricTypes'}},
    getUnitTypes:  { method: 'GET', isArray: true, params: {type: 'unitTypes'}},
    getSensorVariableTypes:  { method: 'GET', isArray: true, params: {type: 'sensorVariableTypes', id : null}  },
    getGatewayTypes:  { method: 'GET', isArray: true, params: {type: 'gatewayTypes'} },
    getGatewayNetworkTypes:  { method: 'GET', isArray: true, params: {type: 'gatewayNetworkTypes'} },
    getGatewaySerialDrivers:  { method: 'GET', isArray: true, params: {type: 'gatewaySerialDrivers'} },
    getResourceTypes:  { method: 'GET', isArray: true, params: {type: 'resourceTypes'} },
    getGateways:  { method: 'GET', isArray: true, params: {type: 'gateways'}  },
    getNodes:  { method: 'GET', isArray: true, params: {type: 'nodes'}  },
    getExternalServers:  { method: 'GET', isArray: true, params: {type: 'externalServers'}  },
    getSensors:  { method: 'GET', isArray: true, params: {type: 'sensors'} },
    getSensorVariables:  { method: 'GET', isArray: true, params: {type: 'sensorVariables'} },
    getRuleDefinitions:  { method: 'GET', isArray: true, params: {type: 'ruleDefinitions'} },
    getTimers:  { method: 'GET', isArray: true, params: {type: 'timers'} },
    getSensorValueTypes:  { method: 'GET', isArray: true, params: {type: 'sensorValueTypes'}  },
    getResourcesGroups:  { method: 'GET', isArray: true, params: {type: 'resourcesGroups'}  },
    getOperationTypes:  { method: 'GET', isArray: true, params: {type: 'operationTypes'}  },
    getRuleOperatorTypes:  { method: 'GET', isArray: true, params: {type: 'ruleOperatorTypes'}  },
    getRuleThresholdDataTypes:  { method: 'GET', isArray: true, params: {type: 'ruleThresholdDataTypes'}  },
    getRuleDampeningTypes:  { method: 'GET', isArray: true, params: {type: 'ruleDampeningTypes'}  },
    getStateTypes:  { method: 'GET', isArray: true, params: {type: 'stateTypes'}  },
    getPayloadOperations:  { method: 'GET', isArray: true, params: {type: 'payloadOperations'}  },
    getRuleConditionTypes:  { method: 'GET', isArray: true, params: {type: 'ruleConditionTypes'}  },

    //Timers
    getTimerTypes:  { method: 'GET', isArray: true, params: {type: 'timerTypes'}  },
    getTimerFrequencies:  { method: 'GET', isArray: true, params: {type: 'timerFrequencyTypes'}  },
    getTimerWeekDays:  { method: 'GET', isArray: true, params: {type: 'timerWeekDays', id:null}  },
    //Firmwares
    getFirmwares:  { method: 'GET', isArray: true, params: {type: 'firmwares'}},
    getFirmwareTypes:  { method: 'GET', isArray: true, params: {type: 'firmwareTypes'}},
    getFirmwareVersions:  { method: 'GET', isArray: true, params: {type: 'firmwareVersions'}},
    getSensorVariableMapper:  { method: 'GET', isArray: true, params: {type: 'sensorVariableMapper'} },
    getSensorVariableMapperByType:  { method: 'GET', isArray: true, params: {type: 'sensorVariableMapperByType', id:null}  },
    updateSensorVariableMapper:  { method: 'PUT', params: {type: 'sensorVariableMapper', id : null} },
    getLanguages: { method: 'GET', isArray: true, params: {type: 'languages', id : null}},
    getHvacOptionsFlowState: { method: 'GET', isArray: true, params: {type: 'hvacOptionsFlowState', id : null}},
    getHvacOptionsFlowMode: { method: 'GET', isArray: true, params: {type: 'hvacOptionsFlowMode', id : null}},
    getHvacOptionsFanSpeed: { method: 'GET', isArray: true, params: {type: 'hvacOptionsFanSpeed', id : null}},
    getRolePermissions: { method: 'GET', isArray: true, params: {type: 'rolePermissions', id : null}},
    //Operations
    getOperations:  { method: 'GET', isArray: true, params: {type: 'operations'} },
    //Rooms
    getRooms:  { method: 'GET', isArray: true, params: {type: 'rooms'} },

    getResources:  { method: 'GET', isArray: true, params: {type: 'resources'} },
    getUserRoles:  { method: 'GET', isArray: true, params: {type: 'roles'}  },
    getGraphInterpolateTypes:  { method: 'GET', isArray: true, params: {type: 'graphInterpolate'}  },
    getMysConfigTypes:  { method: 'GET', isArray: true, params: {type: 'mysConfigTypes'}  },
    getSensorVariableTypesBySensorRefId:  { method: 'GET', isArray: true, params: {type: 'sensorVariableTypesBySenRef'}  },
    getMessageTypes:  { method: 'GET', isArray: true, params: {type: 'messageTypes'}  },
    getMessageSubTypes:  { method: 'GET', isArray: true, params: {type: 'messageSubTypes'} },
    getGraphSensorVariableTypes:  { method: 'GET', isArray: true, params: {type: 'graphSensorVariableTypes'} },
    getTime12h24hformats:  { method: 'GET', isArray: true, params: {type: 'time12h24hformats'} },
    //ResourcesLogs
    getResourceLogsMessageTypes:  { method: 'GET', isArray: true, params: {type: 'resourceLogsMessageTypes'} },
    getResourceLogsLogDirections:  { method: 'GET', isArray: true, params: {type: 'resourceLogsLogDirections'} },
    getResourceLogsLogLevels:  { method: 'GET', isArray: true, params: {type: 'resourceLogsLogLevels'} },
    //Metrics
    getMetricsSettings:  { method: 'GET', isArray: false, params: {type: 'metricsSettings', id:null} },

  })
});

//Metrics Services
myControllerModule.factory('MetricsFactory', function ($resource) {
  return $resource('/mc/rest/metrics/:type', {}, {
    getResourceCount: { method: 'GET', isArray: false, params: {type: 'resourceCount'}},
    getMetricsData: { method: 'GET', isArray: true, params: {type: 'metricsData'}},
    getBatteryMetrics: { method: 'GET', isArray: false, params: {type: 'metricsBattery'}},
    getBulletChart: { method: 'GET', isArray: true, params: {type: 'bulletChart'}},
    getTopologyData: { method: 'GET', isArray: false, params: {type: 'topology'}},

    getHeatMapBatteryLevel: { method: 'GET', isArray: true, params: {type: 'heatMapBatteryLevel'}},
    getHeatMapHeatMapNodeStatus: { method: 'GET', isArray: true, params: {type: 'heatMapNodeStatus'}},
    getHeatMapHeatMapSensorVariable: { method: 'GET', isArray: true, params: {type: 'heatMapSensorVariable'}},
    getHeatMapHeatMapScript: { method: 'GET', isArray: true, params: {type: 'heatMapScript'}},


    getCsvFile: { method: 'GET', isArray: false, params: {type: 'csvFile'}},

  })
});


//Authentication Services
myControllerModule.factory('AuthenticationFactory', function ($resource) {
  return $resource('/mc/rest/authentication/login', {}, {
    login:  { method: 'POST'}
  })
});


myControllerModule.factory('AuthenticationService',
    function (AuthenticationFactory,$base64, $http, CommonServices, mchelper) {
        var service = {};

        service.Login = function (username, password, callback) {
        $http.defaults.headers.common['Authorization'] = 'Basic ' + $base64.encode(username + ':' + password);

            /* Use this for real authentication
             ----------------------------------------------*/
            $http.post('/mc/rest/authentication/login', { username: username, password: password })
               .success(function (response) {
                    callback(response);
                }).error(function(response) {
                    callback(response);
                });
        };

        service.SetCredentials = function (username, password) {
            var authdata = $base64.encode(username + ':' + password);

            mchelper.internal = {
                currentUser: {
                    username: username,
                    authdata: authdata
                }
            };
            $http.defaults.headers.common['Authorization'] = 'Basic ' + authdata; // jshint ignore:line
            //Save in cookies
            CommonServices.saveMchelper(mchelper);
        };

        service.ClearCredentials = function () {
            $http.defaults.headers.common.Authorization = 'Basic ';
            //clear mchelper auth record
            CommonServices.clearMchelper();
            CommonServices.clearCookies();
        };

        return service;
    });


//Security Services
myControllerModule.factory('SecurityFactory', function ($resource) {
  return $resource('/mc/rest/security/:type/:id', {}, {
    getAllRoles: { method: 'GET', isArray: false, params: {type: 'roles', id:null, 'onlyRolename':null}},
    getAllRolesSimple: { method: 'GET', isArray: true, params: {type: 'roles', id:null, 'onlyRolename':true}},
    getRole: { method: 'GET', isArray: false, params: {type: 'roles', id:'@id'}},
    createRole: { method: 'POST', isArray: false, params: {type: 'roles', id:null}},
    updateRole: { method: 'PUT', isArray: false, params: {type: 'roles', id:null}},
    deleteRoleIds: { method: 'POST', isArray: false, params: {type: 'roles', id:'delete'}},

    getAllUsers: { method: 'GET', isArray: false, params: {type: 'users', id:null, 'onlyUsername':null}},
    getAllUsersSimple: { method: 'GET', isArray: true, params: {type: 'users', id:null, 'onlyUsername':true}},
    getUser: { method: 'GET', isArray: false, params: {type: 'users', id:'@id'}},
    createUser: { method: 'POST', isArray: false, params: {type: 'users', id:null}},
    updateUser: { method: 'PUT', isArray: false, params: {type: 'users', id:null}},
    deleteUserIds: { method: 'POST', isArray: false, params: {type: 'users', id:'delete'}},
    enableUserIds: { method: 'POST', isArray: false, params: {type: 'users', id:'enable'}},
    disableUserIds: { method: 'POST', isArray: false, params: {type: 'users', id:'disable'}},
    getProfile: { method: 'GET', isArray: false, params: {type: 'profile', id:null}},
    updateProfile: { method: 'PUT', isArray: false, params: {type: 'profile', id:null}},
  })
});

//Alarm Services
myControllerModule.factory('RulesFactory', function ($resource) {
  return $resource('/mc/rest/rules/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null} },
    get:    { method: 'GET' },
    create: { method: 'POST', params: {id: null}},
    update: { method: 'PUT', params: {id: null}},
    deleteIds: { method: 'POST', params: {id: 'delete'}},
    enableIds: { method: 'POST', params: {id: 'enable'}},
    disableIds: { method: 'POST', params: {id: 'disable'}},
  })
});

//Operations Services
myControllerModule.factory('OperationsFactory', function ($resource) {
  return $resource('/mc/rest/operations/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null} },
    get:    { method: 'GET' },
    create: { method: 'POST', params: {id: null}},
    update: { method: 'PUT', params: {id: null}},
    deleteIds: { method: 'POST', params: {id: 'delete'}},
    enableIds: { method: 'POST', params: {id: 'enable'}},
    disableIds: { method: 'POST', params: {id: 'disable'}},
  })
});

//Timer Services
myControllerModule.factory('TimersFactory', function ($resource) {
  return $resource('/mc/rest/timers/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null} },
    get:    { method: 'GET' },
    create: { method: 'POST', params: {id: null}},
    update: { method: 'PUT', params: {id: null} },
    deleteIds: { method: 'POST', params: {id: 'delete'} },
    disableIds: { method: 'POST', params: {id: 'disable'} },
    enableIds: { method: 'POST', params: {id: 'enable'} },
  })
});

//ForwardPayload Services
myControllerModule.factory('ForwardPayloadFactory', function ($resource) {
  return $resource('/mc/rest/forwardpayload/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null}},
    create: { method: 'POST', params: {id: null}},
    deleteIds: { method: 'POST', params: {id: 'delete'}},
    enableIds: { method: 'POST', params: {id: 'enable'}},
    disableIds: { method: 'POST', params: {id: 'disable'}},
    get: { method: 'GET'},
    update: { method: 'PUT', params: {id: null}},
  })
});

//UidTags Services
myControllerModule.factory('UidTagsFactory', function ($resource) {
  return $resource('/mc/rest/uidtags/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null}},
    create: { method: 'POST', params: {id: null}},
    deleteIds: { method: 'POST', params: {id: 'delete'}},
    update: { method: 'PUT', params: {id: null}},
  })
});

//Resources Logs Services
myControllerModule.factory('ResourcesLogsFactory', function ($resource) {
  return $resource('/mc/rest/resources/logs/:action', {}, {
    getAll: { method: 'GET', isArray: false },
    purge: { method: 'PUT', isArray: false },
    delete: { method: 'POST', isArray: false, params: {action:'delete'} },
  })
});

//MyController Settings Services
myControllerModule.factory('SettingsFactory', function ($resource) {
  return $resource('/mc/rest/settings/:type', {}, {
    getLocation: { method: 'GET', isArray: false, params: {type:'location'} },
    saveLocation: { method: 'POST', params: {type:'location'} },
    getController: { method: 'GET', isArray: false, params: {type:'controller'} },
    saveController: { method: 'POST', params: {type:'controller'} },
    getEmail: { method: 'GET', isArray: false, params: {type:'email'} },
    saveEmail: { method: 'POST', params: {type:'email'} },
    getPushbullet: { method: 'GET', isArray: false, params: {type:'pushbullet'} },
    savePushbullet: { method: 'POST', params: {type:'pushbullet'} },
    getSms: { method: 'GET', isArray: false, params: {type:'sms'} },
    saveSms: { method: 'POST', params: {type:'sms'} },
    getMySensors: { method: 'GET', isArray: false, params: {type:'mySensors'} },
    saveMySensors: { method: 'POST', params: {type:'mySensors'} },
    getUnits: { method: 'GET', isArray: false, params: {type:'units'} },
    saveUnits: { method: 'POST', params: {type:'units'} },
    updateLanguage: { method: 'PUT', params: {type:'updateLanguage'} },
    getMetrics: { method: 'GET', isArray: false, params: {type:'metricsGraph'} },
    saveMetrics: { method: 'POST', params: {type:'metricsGraph'} },
    getMetricsRetention: { method: 'GET', isArray: false, params: {type:'metricsRetention'} },
    saveMetricsRetention: { method: 'POST', params: {type:'metricsRetention'} },
    getUserSettings: { method: 'GET', isArray: false, params: {type:'userSettings'} },
    saveUserSettings: { method: 'POST', params: {type:'userSettings'} },
    getMqttBroker: { method: 'GET', isArray: false, params: {type:'mqttBroker'} },
    saveMqttBroker: { method: 'POST', params: {type:'mqttBroker'} },
    getHtmlAdditionalHeaders: { method: 'GET', isArray: false, params: {type:'htmlAdditionalHeaders'} },
    updateHtmlAdditionalHeaders: { method: 'POST', params: {type:'htmlAdditionalHeaders'} },
  })
});

//MyController Status Services
myControllerModule.factory('StatusFactory', function ($resource) {
  return $resource('/mc/rest/:type', {}, {
   getOsStatus: { method: 'GET', params: {type:'osStatus'} },
   getJvmStatus: { method: 'GET', params: {type:'jvmStatus'} },
   runGarbageCollection: { method: 'PUT', params: {type:'runGarbageCollection'} },
   getScriptEngines: { method: 'GET', isArray: true, params: {type:'scriptEngines'} },
   getConfig: { method: 'GET', params: {type:'guiSettings'} },
   getMcAbout: { method: 'GET', params: {type:'mcAbout'} },
   getTimestamp: { method: 'GET', params: {type:'timestamp'} },
   getMcServerLog: { method: 'GET', isArray: false, params: {type:'mcServerLogFile'} },
   getStaticImageFile: { method: 'GET', isArray: false, params: {type:'imageFiles'} },
   getStaticImageFilesList: { method: 'GET', isArray: true, params: {type:'imageFiles'} },
  })
});

//Gateway Services
myControllerModule.factory('GatewaysFactory', function ($resource) {
  return $resource('/mc/rest/gateways/:gatewayId', {gatewayId: '@gatewayId'}, {
    getAll: { method: 'GET', isArray: false },
    get:    { method: 'GET' },
    create: { method: 'POST'},
    update: { method: 'PUT' },
    delete: { method: 'POST', params: {gatewayId:'delete'} },
    enable: { method: 'POST', params: {gatewayId:'enable'} },
    disable: { method: 'POST', params: {gatewayId:'disable'} },
    reload: { method: 'POST', params: {gatewayId:'reload'} },
    discover: { method: 'POST', params: {gatewayId:'discover'} },
    executeNodeInfoUpdate: { method: 'POST', params: {gatewayId:'executeNodeInfoUpdate'} },
  })
});


//ResourcesGroup Services
myControllerModule.factory('ResourcesGroupFactory', function ($resource) {
  return $resource('/mc/rest/resources/group/:_id', {_id: '@_id'}, {
    getAll: { method: 'GET', isArray: false, params: {_id: null} },
    get:    { method: 'GET'},
    create: { method: 'POST', params: {_id: null}},
    update: { method: 'PUT', params: {_id: null} },
    deleteIds: { method: 'POST', params: {_id: 'delete'} },
    turnOnIds: { method: 'POST', params: {_id: 'on'} },
    turnOffIds: { method: 'POST', params: {_id: 'off'} },
  })
});

//ResourcesGroupMap Services
myControllerModule.factory('ResourcesGroupMapFactory', function ($resource) {
  return $resource('/mc/rest/resources/group/map/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null} },
    get:    { method: 'GET' },
    create: { method: 'POST', params: {id: null}},
    update: { method: 'PUT', params: {id: null} },
    deleteIds: { method: 'POST', params: {id: 'delete'} },
  })
});

//Read static files
myControllerModule.factory('ReadFileFactory', function ($resource) {
  return $resource('/:locationName/:fileName', {}, {
   getConfigFile: { method: 'GET', isArray: false, params: {locationName: 'configurations', fileName:'mycontroller-configs.json'} },
  })
});

//Dashboard Services
myControllerModule.factory('DashboardFactory', function ($resource) {
  return $resource('/mc/rest/dashboard/:id', {}, {
    getAll: { method: 'GET', isArray: true},
    get:    { method: 'GET',isArray: false},
    update: { method: 'PUT'},
    delete: { method: 'DELETE'},
  })
});

//Backup and restore services
myControllerModule.factory('BackupRestoreFactory', function ($resource) {
  return $resource('/mc/rest/backup/:type', {}, {
    getAll:  { method: 'GET', isArray: false, params: {type: 'backupFiles'}},
    backupNow:  { method: 'PUT', params: {type: 'backupNow'}},
    restore:  { method: 'POST', params: {type: 'restore'}},
    deleteIds:  { method: 'POST', params: {type: 'delete'}},
    getBackupSettings:  { method: 'GET', params: {type: 'backupSettings'}},
    updateBackupSettings:  { method: 'PUT', params: {type: 'backupSettings'}},

  })
});

//Rooms Services
myControllerModule.factory('RoomsFactory', function ($resource) {
  return $resource('/mc/rest/rooms/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null} },
    get:    { method: 'GET' },
    create: { method: 'POST', params: {id: null}},
    update: { method: 'PUT', params: {id: null} },
    deleteIds: { method: 'POST', params: {id: 'delete'} },
  })
});

//Scripts Services
myControllerModule.factory('ScriptsFactory', function ($resource) {
  return $resource('/mc/rest/scripts/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null} },
    getAllLessInfo: { method: 'GET', isArray: true, params: {id: null, 'lessInfo':true} },
    get:    { method: 'GET', params: {id: 'get'}},
    runNow:    { method: 'GET', params: {id: 'runNow'}},
    upload: { method: 'POST', params: {id: null} },
    deleteIds: { method: 'POST', params: {id: 'delete'} },
  })
});

//Templates Services
myControllerModule.factory('TemplatesFactory', function ($resource) {
  return $resource('/mc/rest/templates/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null} },
    getAllLessInfo: { method: 'GET', isArray: true, params: {id: null, 'lessInfo':true} },
    get:    { method: 'GET', params: {id: 'get'}},
    getHtml:    { method: 'GET', params: {id: 'getHtml'}},
    upload: { method: 'POST', params: {id: null} },
    deleteIds: { method: 'POST', params: {id: 'delete'} },
  })
});

//Variables repository Services
myControllerModule.factory('VariablesRepositoryFactory', function ($resource) {
  return $resource('/mc/rest/variables/:pid', {pid: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {pid: null} },
    get:    { method: 'GET', params: {pid: 'get'}},
    create: { method: 'POST', params: {pid: null} },
    update: { method: 'PUT', params: {pid: null} },
    deleteIds: { method: 'POST', params: {pid: 'delete'} },
  })
});


//Resources Data Services
myControllerModule.factory('ResourcesDataFactory', function ($resource) {
  return $resource('/mc/rest/resourcesdata/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null}},
    create: { method: 'POST', params: {id: null}},
    deleteIds: { method: 'POST', params: {id: 'delete'}},
    enableIds: { method: 'POST', params: {id: 'enable'}},
    disableIds: { method: 'POST', params: {id: 'disable'}},
    get: { method: 'GET'},
    update: { method: 'PUT', params: {id: null}},
  })
});

//External Server Services
myControllerModule.factory('ExternalServersFactory', function ($resource) {
  return $resource('/mc/rest/externalserver/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: false, params: {id: null}},
    create: { method: 'POST', params: {id: null}},
    deleteIds: { method: 'POST', params: {id: 'delete'}},
    enableIds: { method: 'POST', params: {id: 'enable'}},
    disableIds: { method: 'POST', params: {id: 'disable'}},
    get: { method: 'GET'},
    update: { method: 'PUT', params: {id: null}},
  })
});
