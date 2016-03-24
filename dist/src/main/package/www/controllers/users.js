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
 myControllerModule.controller('UsersControllerList', function(alertService,
$scope, SecurityFactory, $state, $uibModal, displayRestError, mchelper, CommonServices, $filter) {

  //GUI page settings
  $scope.headerStringList = $filter('translate')('USERS_DETAIL');
    $scope.noItemsSystemMsg = $filter('translate')('NO_USERS_SETUP');
  $scope.noItemsSystemIcon = "fa fa-users";

  //load empty, configuration, etc.,
  $scope.mchelper = mchelper;
  $scope.filteredList=[];

  //data query details
  $scope.currentPage = 1;
  $scope.query = CommonServices.getQuery();
  $scope.queryResponse = {};

  //Get min number
  $scope.getMin = function(item1, item2){
    return CommonServices.getMin(item1, item2);
  };

  //get all Sensors
  $scope.getAllItems = function(){
    SecurityFactory.getAllUsers($scope.query, function(response) {
      $scope.queryResponse = response;
      $scope.filteredList = $scope.queryResponse.data;
      $scope.filterConfig.resultsCount = $scope.queryResponse.query.filteredCount;
    },function(error){
      displayRestError.display(error);
    });
  }

  //Hold all the selected item ids
  $scope.itemIds = [];

  $scope.selectAllItems = function(){
    CommonServices.selectAllItems($scope);
  };

  $scope.selectItem = function(item){
    CommonServices.selectItem($scope, item);
  };

  //On page change
  $scope.pageChanged = function(newPage){
    CommonServices.updatePageChange($scope, newPage);
  };

  //Filter change method
  var filterChange = function (filters) {
    //Reset filter fields and update items
    CommonServices.updateFiltersChange($scope, filters);
  };

  $scope.filterConfig = {
    fields: [
      {
        id: 'username',
        title:  $filter('translate')('USERNAME'),
        placeholder: $filter('translate')('FILTER_BY_USERNAME'),
        filterType: 'text'
      },
      {
        id: 'fullName',
        title:  $filter('translate')('FULL_NAME'),
        placeholder: $filter('translate')('FILTER_BY_FULL_NAME'),
        filterType: 'text',
      },
      {
        id: 'email',
        title:  $filter('translate')('EMAIL'),
        placeholder: $filter('translate')('FILTER_BY_EMAIL'),
        filterType: 'text',
      }
    ],
    resultsCount: $scope.filteredList.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };

  //Sort columns
  var sortChange = function (sortId, isAscending) {
    //Reset sort type and update items
    CommonServices.updateSortChange($scope, sortId, isAscending);
  };

  $scope.sortConfig = {
    fields: [
      {
        id: 'username',
        title:  $filter('translate')('USERNAME'),
        sortType: 'text'
      },
      {
        id: 'fullName',
        title:  $filter('translate')('FULL_NAME'),
        sortType: 'text'
      },
      {
        id: 'email',
        title:  $filter('translate')('EMAIL'),
        sortType: 'text'
      },
      {
        id: 'enabled',
        title:  $filter('translate')('ENABLED'),
        sortType: 'text'
      }
    ],
    onSortChange: sortChange
  };


  //Edit item
  $scope.edit = function () {
    if($scope.itemIds.length == 1){
      $state.go("settingsUsersAddEdit", {'id':$scope.itemIds[0]});
    }
  };

  //Delete item(s)
  $scope.delete = function (size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/common-html/delete-modal.html',
    controller: 'ControllerDeleteModal',
    size: size,
    resolve: {}
    });

    modalInstance.result.then(function () {
      SecurityFactory.deleteUserIds($scope.itemIds, function(response) {
        alertService.success($filter('translate')('ITEMS_DELETED_SUCCESSFULLY'));
        //Update display table
        $scope.getAllItems();
        $scope.itemIds = [];
      },function(error){
        displayRestError.display(error);
      });
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };

});



//Add Edit item
myControllerModule.controller('UsersControllerAddEdit', function ($scope, $stateParams, $state, SecurityFactory, TypesFactory, mchelper, alertService, displayRestError, $filter) {
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.item.user = {};
  $scope.item.user.enabled = true;

  if($stateParams.id){
    SecurityFactory.getUser({"id":$stateParams.id},function(response) {
        $scope.item = response;
      },function(error){
        displayRestError.display(error);
      });
  }

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('ADD_USER');
  $scope.headerStringUpdate = $filter('translate')('UPDATE_USER');
  $scope.cancelButtonState = "settingsUsersList"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  //Pre load
  $scope.roles = SecurityFactory.getAllRolesSimple();

  $scope.save = function(){
    $scope.saveProgress = true;
    if($stateParams.id){
      SecurityFactory.updateUser($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_UPDATED_SUCCESSFULLY'));
        $state.go("settingsUsersList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }else{
      SecurityFactory.createUser($scope.item,function(response) {
        alertService.success($filter('translate')('ITEM_CREATED_SUCCESSFULLY'));
        $state.go("settingsUsersList");
      },function(error){
        displayRestError.display(error);
        $scope.saveProgress = false;
      });
    }
  }
});

//Add Edit item
myControllerModule.controller('ProfileControllerUpdate', function ($scope, $stateParams, $state, SecurityFactory, TypesFactory, mchelper, alertService, displayRestError, $filter) {
  $scope.mchelper = mchelper;
  $scope.item = {};
  $scope.item.user = {};
  $scope.item.user.enabled = true;

  $scope.resetProfile = function(){
    SecurityFactory.getProfile(function(response) {
        $scope.item = response;
        mchelper.user = angular.copy(response.user);
      },function(error){
        if(error.statusText === 'Unauthorized'){
          $state.go("login");
        }else{
          displayRestError.display(error);
        }
      });
  }

  //GUI page settings
  $scope.showHeaderUpdate = $stateParams.id;
  $scope.headerStringAdd = $filter('translate')('PROFILE');
  $scope.headerStringUpdate = $filter('translate')('PROFILE');
  $scope.cancelButtonState = "dashboard"; //Cancel button state
  $scope.saveProgress = false;
  //$scope.isSettingChange = false;

  //Load self details
  $scope.resetProfile();

  $scope.save = function(){
    $scope.saveProgress = true;
    SecurityFactory.updateProfile($scope.item,function(response) {
      alertService.success($filter('translate')('PROFILE_UPDATED_SUCCESSFULLY'));
      $scope.saveProgress = false;
      $scope.editEnable.profile = false;
      $scope.resetProfile();
    },function(error){
      displayRestError.display(error);
      $scope.saveProgress = false;
    });
  }
});
