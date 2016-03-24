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

angular.module('adf.widget.myc-dsi', [])
  .config(function(dashboardProvider){
    dashboardProvider
      .widget('mycDisplayStaticImage', {
        title: 'Display image file',
        description: 'Displays image file from local disk or url',
        templateUrl: 'controllers/adf-widgets/adf-myc-dsi/view.html',
        controller: 'mycDisplayStaticImageController',
        controllerAs: 'mycDisplayStaticImage',
        config: {
          locationType:"disk",
          imageNameUrl:"",
          refreshTime:30,
        },
        edit: {
          templateUrl: 'controllers/adf-widgets/adf-myc-dsi/edit.html',
          controller: 'mycDisplayStaticImageEditController',
          controllerAs: 'mycDisplayStaticImageEdit',
        }
      });
  })
  .controller('mycDisplayStaticImageController', function($scope, $interval, config, mchelper, $filter, StatusFactory, displayRestError, CommonServices){
    var mycDisplayStaticImage = this;

    mycDisplayStaticImage.showLoading = true;
    mycDisplayStaticImage.isSyncing = true;
    mycDisplayStaticImage.fileData = {};
    mycDisplayStaticImage.error = false;
    mycDisplayStaticImage.errorMsg;
    mycDisplayStaticImage.imageNameUrl = config.imageNameUrl;
    $scope.cs = CommonServices;


    function loadImage(){
      mycDisplayStaticImage.isSyncing = true;
      if(config.locationType === "disk"){
        StatusFactory.getStaticImageFile({'fileName':config.imageNameUrl}, function(response){
          mycDisplayStaticImage.fileData = response;
          mycDisplayStaticImage.isSyncing = false;
          if(mycDisplayStaticImage.showLoading){
            mycDisplayStaticImage.showLoading = false;
          }
          mycDisplayStaticImage.error = false;
        },function(error){
          mycDisplayStaticImage.showLoading = false;
          mycDisplayStaticImage.isSyncing = false;
          mycDisplayStaticImage.error = true;
          if(error.data && error.data.errorMessage){
            mycDisplayStaticImage.errorMsg = error.data.errorMessage;
          }else{
            mycDisplayStaticImage.errorMsg = error.statusText;
            displayRestError.display(error);
          }
        });
      }else{
        mycDisplayStaticImage.imageNameUrl = config.imageNameUrl+'?t='+Date.now(); // Image should updated on every refresh
        mycDisplayStaticImage.isSyncing = false;
          if(mycDisplayStaticImage.showLoading){
            mycDisplayStaticImage.showLoading = false;
        }
      }
    };

    function updateImage(){
      if(mycDisplayStaticImage.isSyncing){
        return;
      }else if(config.imageNameUrl && config.imageNameUrl.length > 0){
        loadImage();
      }
    }

    //load image initially
    if(config.imageNameUrl && config.imageNameUrl.length > 0){
      loadImage();
    }else{
      mycDisplayStaticImage.showLoading = false;
    }

    // refresh every second
    var promise = $interval(updateImage, config.refreshTime*1000);

    // cancel interval on scope destroy
    $scope.$on('$destroy', function(){
      $interval.cancel(promise);
    });
  }).controller('mycDisplayStaticImageEditController', function($scope, config, StatusFactory, displayRestError, CommonServices){
    var mycDisplayStaticImageEdit = this;
    mycDisplayStaticImageEdit.cs = CommonServices;
    mycDisplayStaticImageEdit.locationTypes = ["disk","url"];
    mycDisplayStaticImageEdit.filesList = [];

    mycDisplayStaticImageEdit.onLocationTypeChange = function(){
      config.imageNameUrl = "";
      if(config.locationType === "disk"){
        StatusFactory.getStaticImageFilesList(function(response){
          mycDisplayStaticImageEdit.filesList = response;
        },function(error){
          displayRestError.display(error);
        });
      }
    };

    if(config.locationType === "disk"){
      var tmpImageUrl = config.imageNameUrl;
      mycDisplayStaticImageEdit.onLocationTypeChange();
      config.imageNameUrl = tmpImageUrl;
    }


  });
