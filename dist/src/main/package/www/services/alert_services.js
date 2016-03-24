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

myControllerModule.factory('alertService', function() {

    var alertCfg = {};

    alertCfg.displayTemplate = '<div class="col-xs-11 col-sm-3 toast-pf alert alert-{0}  alert-dismissable">' +
                            '<button type="button" class="close" data-dismiss="alert" data-notify="dismiss" aria-hidden="true">' +
                              '<span class="pficon pficon-close"></span>' +
                            '</button>'+
                            '<span data-notify="icon"></span>' +
                            '<strong>{1}</strong> {2}' +
                            '<a href="{3}" target="{4}" data-notify="url"></a>'+
                          '</div>';
    alertCfg.placement = "top";
    alertCfg.delay = 1000;
    alertCfg.timer = 1000;

    return alertService = {
      default: function(msg) {
          $.notify({
            icon: 'pficon pficon-info',
            message: msg
          },{
            type: 'info',
            delay: alertCfg.delay,
            timer: alertCfg.timer,
            placement: {
              from: alertCfg.placement,
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            },
            template: alertCfg.displayTemplate,
          });
        },
    success: function(msg){
        $.notify({
            icon: 'pficon pficon-ok',
            message: msg
          },{
            type: 'success',
            delay: alertCfg.delay,
            timer: alertCfg.timer,
            placement: {
              from: alertCfg.placement,
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            },
            template: alertCfg.displayTemplate,
          });
        },
    warning: function(msg){
        $.notify({
            icon: 'pficon pficon-warning-triangle-o',
            message: msg
          },{
            type: 'warning',
            delay: alertCfg.delay,
            timer: alertCfg.timer,
            placement: {
              from: alertCfg.placement,
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            },
            template: alertCfg.displayTemplate,
          });
        },
    danger: function(msg){
        $.notify({
            icon: 'pficon pficon-error-circle-o',
            message: msg
          },{
            type: 'danger',
            delay: alertCfg.delay,
            timer: alertCfg.timer,
            placement: {
              from: alertCfg.placement,
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            },
            template: alertCfg.displayTemplate,
          });
        }
    };
  }
);

myControllerModule.factory('displayRestError', function(alertService){
  return displayRestError = {
    display: function(error){
      //alertService.danger(angular.toJson(error));
      var displayMessage = '';
      if(error.status === 0){
        displayMessage = 'NO RESPONSE, Check your network connection [or] Server status!';
      }else if(error.data != null){
        if(error.data.errorMessage != null){
          displayMessage = error.status +': '+ error.statusText+'<br>'+error.data.errorMessage;
        }else{
         displayMessage = error.status +': '+ error.statusText;
        }
      }else if(data != null){
        displayMessage = error.status +': '+ error.statusText;
      }
      alertService.danger(displayMessage);
    },
    displayMsg: function(response, expectedCode, successDisplay){
      alertService.success(angular.toJson(response));
    }
  };
});
