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
myControllerModule.controller('DashboardListController', function(alertService,
$scope, $filter, $location, $uibModal, $stateParams, $state, displayRestError, DashboardFactory, mchelper, CommonServices) {
  $scope.dId;
  $scope.dashboards ={};
  $scope.showLoading = false;
  $scope.showLoadingMain = false;
  $scope.mchelper = mchelper;

  $scope.updateDashboard = function(){
    DashboardFactory.getAll({'lessInfo':true}, function(responseDashboards){
      $scope.dashboards = $filter('orderBy')(responseDashboards, 'id', false);
      if(mchelper.user.selectedDashboard === undefined){
        mchelper.user.selectedDashboard = $scope.dashboards[0].id;
        //Update mchelper
        CommonServices.saveMchelper(mchelper);
      }
      $scope.dId = mchelper.user.selectedDashboard;
      $scope.showLoadingMain = false;
      DashboardFactory.get({'dId':$scope.dId}, function(responseDashboard){
        $scope.model = responseDashboard;
        $scope.model.titleTemplateUrl = "partials/dashboard/dashboard-title.html";
        $scope.selectedName = $scope.model.name;
      });
    });
  };

  //Initial load
  $scope.updateDashboard();

  $scope.changeDashboard = function (item){
    $scope.showLoading = true;
    $scope.dId = item.id;
    mchelper.user.selectedDashboard = item.id;
    //Update mchelper
    CommonServices.saveMchelper(mchelper);
    DashboardFactory.get({'dId':$scope.dId}, function(response){
      $scope.model = response;
      $scope.model.titleTemplateUrl = "partials/dashboard/dashboard-title.html";
      $scope.selectedName = $scope.model.name;
      //console.log(angular.toJson($scope.model));
      $scope.showLoading = false;
    });
  };

  $scope.createNewDashboad = function(){
    if($scope.dashboards.length < mchelper.cfg.dashboardLimit){
      DashboardFactory.get({'getNew':true,'title':$filter('translate')('NEW_DASHBOARD')},function(response){
        //Update items
        $scope.updateDashboard();
      });
    }
  };

    //Delete item(s)
  $scope.deleteDashboad = function (size) {
    if($scope.dashboards.length == 1){
      return;
    }
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      DashboardFactory.delete({'dId':$scope.dId}, function(response) {
        alertService.success('Deleted an item successfully!');
        $scope.dId = undefined;
        mchelper.selectedDashboard = undefined;
        //Update items
        $scope.updateDashboard();
      },function(error){
        displayRestError.display(error);
      });
    }),
    function () {
    }
  };

 var eventFired = function (event, name, model) {
        //$scope.eventsFired.push(event);
        //console.log(angular.toJson(model));
        DashboardFactory.update(model);
        $scope.dashboards.forEach
        angular.forEach($scope.dashboards, function(dashboard) {
          if(dashboard.id === model.id){
            dashboard.title = model.title;
          }
        });
    };

    $scope.$on('adfDashboardChanged', eventFired);
    //$scope.$on('adfWidgetAdded', eventFired);
    //$scope.$on('adfWidgetMoved', eventFired);
    //$scope.$on('adfWidgetAddedToColumn', eventFired);
    //$scope.$on('adfWidgetRemovedFromColumn', eventFired);
    //$scope.$on('adfWidgetMovedInColumn', eventFired);
});
