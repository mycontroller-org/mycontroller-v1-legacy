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
// don't forget to declare this service module as a dependency in your main app constructor!
//http://js2.coffee/#coffee2js
//https://coderwall.com/p/r_bvhg/angular-ui-bootstrap-alert-service-for-angular-js

myControllerModule.factory('alertService', function() {
    return alertService = {
      default: function(msg) {
          $.notify({
            icon: 'fa fa-info-circle fa-lg',
            message: '<strong>'+msg+'</strong>'
          },{
            delay: 1000,
            timer: 1000,
            placement: {
              from: "bottom"
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            }
          });
        },
    success: function(msg){
        $.notify({
            icon: 'fa fa-thumbs-o-up fa-lg',
            message: '<strong>'+msg+'</strong>'
          },{
            type: 'success',
            delay: 1000,
            timer: 1000,
            placement: {
              from: "bottom"
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            }
          });
        },
    warning: function(msg){
        $.notify({
            icon: 'fa fa-warning fa-lg',
            message: '<strong>'+msg+'</strong>'
          },{
            type: 'warning',
            delay: 1000,
            timer: 1000,
            placement: {
              from: "bottom"
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            }
          });
        },
    danger: function(msg){
        $.notify({
            icon: 'fa fa-ban fa-lg',
            message: '<strong>'+msg+'</strong>'
          },{
            type: 'danger',
            delay: 1000,
            timer: 1000,
            placement: {
              from: "bottom"
            },
            animate: {
              enter: 'animated lightSpeedIn',
              exit: 'animated lightSpeedOut'
            }
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
        displayMessage = '0 : NO RESPONSE, Check your network connection [or] Server status!';
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
