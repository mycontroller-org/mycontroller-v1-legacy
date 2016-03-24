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

myControllerModule.factory('validationServices', function() {
  var validationService = {};

  //Validate isNumber
  validationService.isNumber = function (value) {
    if (isNaN(value)) {
      return false;
    }
    return true;
  };

  //Validate isString
  validationService.isString = function (value) {
    if (isNaN(value)) {
      return false;
    }
    return true;
  };

  //Validate isString
  validationService.isEmpty = function (value) {
    if (!value || value === "") {
      return false;
    }
    return true;
  };

 return validationService;

});
