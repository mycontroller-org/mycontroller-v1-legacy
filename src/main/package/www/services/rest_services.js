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

//Sensors Services
myControllerModule.factory('SensorsFactory', function ($resource, $http, $base64) {
  return $resource('/mc/rest/sensors/:nodeId/:sensorId', {nodeId: '@nodeId'}, {
    query:  { method: 'GET', isArray: true, params: {nodeId: '@nodeId'}  },
    getByRefId:  { method: 'GET', isArray: false  },
    get:   { method: 'GET', isArray: false, params: {sensorId: '@sensorId'}},
    create: { method: 'POST'},
    update: { method: 'PUT' },
    delete: { method: 'DELETE', params: {sensorId: '@sensorId'} },
    getByType: { method: 'GET', isArray: true, params: {typeString: '@typeString'} },
    sendPayload: { method: 'POST', params: {sensorId: '@payload'} }
  })
});

//Node Services
myControllerModule.factory('NodesFactory', function ($resource) {
  return $resource('/mc/rest/nodes/:nodeId', {nodeId: '@nodeId'}, {
    getAll: { method: 'GET', isArray: true },
    get:    { method: 'GET' },
    create: { method: 'POST'},
    update: { method: 'PUT' },
    delete: { method: 'DELETE' },
    reboot: { method: 'POST', params: {nodeId: 'reboot'}}
  })
});

//Firmware Services
myControllerModule.factory('FirmwaresFactory', function ($resource) {
  return $resource('/mc/rest/firmwares/:type/:id', {}, {
    getAllFirmwareTypes:  { method: 'GET', isArray: true, params: {type: 'types'}  },
    getAllFirmwareVersions:  { method: 'GET', isArray: true, params: {type: 'versions'}  },
    getAllFirmwares:  { method: 'GET', isArray: true  },
    getFirmwareType:  { method: 'GET', isArray: false, params: {type: 'types', id: '@id'}  },
    getFirmwareVersion:  { method: 'GET', isArray: false, params: {type: 'versions', id: '@id'}  },
    getFirmware:  { method: 'GET', isArray: false,  params: {id: '@id'}},
    deleteFirmwareType:  { method: 'DELETE', params: {type: 'types', id: '@id'}  },
    deleteFirmwareVersion:  { method: 'DELETE', params: {type: 'versions', id: '@id'}  },
    deleteFirmware:  { method: 'DELETE', params: {id: '@id'}},
    updateFirmwareType:  { method: 'PUT', params: {type: 'types'}  },
    updateFirmwareVersion:  { method: 'PUT', params: {type: 'versions'}  },
    updateFirmware:  { method: 'PUT' },
    createFirmwareType:  { method: 'POST', params: {type: 'types'}  },
    createFirmwareVersion:  { method: 'POST', params: {type: 'versions'}  },
    createFirmware:  { method: 'POST' }
  })
});

//Types Services
myControllerModule.factory('TypesFactory', function ($resource) {
  return $resource('/mc/rest/types/:type/:id', {id: '@id'}, {
    getSensorTypes:  { method: 'GET', isArray: true, params: {type: 'sensorTypes'}  },
    getUserRoles:  { method: 'GET', isArray: true, params: {type: 'roles'}  },
    getAlarmTypes:  { method: 'GET', isArray: true, params: {type: 'alarmtypes'}  },
    getAlarmDampeningTypes:  { method: 'GET', isArray: true, params: {type: 'alarmDampeningTypes'}  },
    getAlarmTriggers:  { method: 'GET', isArray: true, params: {type: 'alarmtriggers'}  },
    getSensorValueTypes:  { method: 'GET', isArray: true, params: {type: 'sensorValueTypes'}  },
    getNodes:  { method: 'GET', isArray: true, params: {type: 'nodes'}  },
    getSensors:  { method: 'GET', isArray: true, params: {type: 'sensors'} },
    getTimerTypes:  { method: 'GET', isArray: true, params: {type: 'timerTypes'}  },
    getTimerFrequencies:  { method: 'GET', isArray: true, params: {type: 'timerFrequencies'}  },
    getTimerDays:  { method: 'GET', isArray: true, params: {type: 'timerDays'}  }
  })
});

//Metrics Services
myControllerModule.factory('MetricsFactory', function ($resource) {
  return $resource('/mc/rest/metrics/:type/:sensorId', {sensorId: '@sensorId'}, {
    lastMinute: { method: 'GET', isArray: true, params: {type: 'lastMinute'}},
    last5Minutes: { method: 'GET', isArray: true, params: {type: 'last5Minutes'}},
    lastOneHour: { method: 'GET', isArray: true, params: {type: 'lastOneHour'}},
    last24Hours: { method: 'GET', isArray: true, params: {type: 'last24Hours'}},
    last30Days: { method: 'GET', isArray: true, params: {type: 'last30Days'}},
    lastYear: { method: 'GET', isArray: true, params: {type: 'lastYear'}},
    allYears: { method: 'GET', isArray: true, params: {type: 'allYears'}},
    sensorData: { method: 'GET', isArray: false, params: {type: 'sensorData'}}         
  })
});


//Authentication Services
myControllerModule.factory('AuthenticationFactory', function ($resource) {
  return $resource('/mc/rest/authentication/login', {}, {
    login:  { method: 'POST'}
  })
});


myControllerModule.factory('AuthenticationService',
    function (AuthenticationFactory,$base64, $http, $cookieStore, $rootScope) {
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
 
            $rootScope.globals = {
                currentUser: {
                    username: username,
                    authdata: authdata
                }
            };
 
            $http.defaults.headers.common['Authorization'] = 'Basic ' + authdata; // jshint ignore:line
            $cookieStore.put('globals', $rootScope.globals);
        };
 
        service.ClearCredentials = function () {
            $rootScope.globals = {};
            $cookieStore.remove('globals');
            $http.defaults.headers.common.Authorization = 'Basic ';
        };
 
        return service;
    });


//User Services
myControllerModule.factory('UsersFactory', function ($resource) {
  return $resource('/mc/rest/users/:userId', {userId: '@userId'}, {
    getAll: { method: 'GET', isArray: true },
    get:    { method: 'GET' },
    create: { method: 'POST'},
    update: { method: 'PUT' },
    delete: { method: 'DELETE' }
  })
});

//Alarm Services
myControllerModule.factory('AlarmsFactory', function ($resource) {
  return $resource('/mc/rest/alarms/:id/:avar', {sensorRefId: '@id'}, {
    getAll: { method: 'GET', isArray: true, params: {avar: 'all'} },
    get:    { method: 'GET' },
    create: { method: 'POST'},
    update: { method: 'PUT' },
    delete: { method: 'DELETE' },
    getSensorData: { method: 'GET', isArray: false, params: {avar: 'sensorData'} }
  })
});

//Timer Services
myControllerModule.factory('TimersFactory', function ($resource) {
  return $resource('/mc/rest/timers/:id/:avar', {sensorRefId: '@id'}, {
    getAll: { method: 'GET', isArray: true, params: {avar: 'all'} },
    get:    { method: 'GET' },
    create: { method: 'POST'},
    update: { method: 'PUT' },
    delete: { method: 'DELETE' },
    getSensorData: { method: 'GET', isArray: false, params: {avar: 'sensorData'} }
  })
});

//ForwardPayload Services
myControllerModule.factory('ForwardPayloadFactory', function ($resource) {
  return $resource('/mc/rest/forwardpayload/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: true},
    create: { method: 'POST'},
    delete: { method: 'DELETE'}
  })
});

//ForwardPayload Services
myControllerModule.factory('UidTagFactory', function ($resource) {
  return $resource('/mc/rest/uidtag/:id', {id: '@id'}, {
    getAll: { method: 'GET', isArray: true},
    create: { method: 'POST'},
    delete: { method: 'DELETE'}
  })
});

//Sensor Log Services
myControllerModule.factory('SensorLogFactory', function ($resource) {
  return $resource('/mc/rest/sensorlog/:id/:avar', {sensorRefId: '@id'}, {
    getAll: { method: 'GET', isArray: true },
    getSensorAll: { method: 'GET', isArray: true, params: {avar: 'all'} },
    getSensorData: { method: 'GET', isArray: false, params: {avar: 'sensorData'} }
  })
});


//MyController Settings Services
myControllerModule.factory('SettingsFactory', function ($resource) {
  return $resource('/mc/rest/settings/:type', {}, {
    getSunriseSunset: { method: 'GET', isArray: true, params: {type:'sunriseSunset'} },
    getNodeDefaults: { method: 'GET', isArray: true, params: {type:'nodeDefaults'} },
    getEmail: { method: 'GET', isArray: true, params: {type:'email'} },
    getSMS: { method: 'GET', isArray: true, params: {type:'sms'} },
    getVersion: { method: 'GET', isArray: true, params: {type:'version'} },
    getUnits: { method: 'GET', isArray: true, params: {type:'units'} },
    update: { method: 'PUT'}
  })
});

//MyController Status Services
myControllerModule.factory('StatusFactory', function ($resource) {
  return $resource('/mc/rest/:type', {}, {
   getOsStatus: { method: 'GET', params: {type:'osStatus'} },
   getJvmStatus: { method: 'GET', params: {type:'jvmStatus'} }
  })
});

//About Services
myControllerModule.factory('AboutFactory', function ($resource) {
  return $resource('/mc/rest/about', {}, {
    about:  { method: 'GET'}
  })
});
