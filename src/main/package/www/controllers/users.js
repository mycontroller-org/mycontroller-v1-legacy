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
myControllerModule.controller('UsersController', function(alertService,
$scope, $filter, UsersFactory, $location, $uibModal, displayRestError, $filter) {
    
  $scope.filteredList=[];
  $scope.orgList=[];
  $scope.config = {
    itemsPerPage: 10,
    maxPages:10,
    fillLastPage: false
  }

  //Filter
  $scope.updateFilteredList = function() {
    $scope.filteredList = $filter("filter")($scope.orgList, $scope.query);
  };
  
    
  // Call and Run function every second
  $scope.orgList = UsersFactory.query(function(response) {
                    },function(error){
                      displayRestError.display(error);            
                    });
  $scope.filteredList = $scope.orgList;
  
  //Delete
  $scope.delete = function (user, size) {
    var modalInstance = $uibModal.open({
    templateUrl: 'partials/models/deleteModal.html',
    controller: 'UdeleteController',
    size: size,
    resolve: {
      user: function () {return user;}
      }
    });

    modalInstance.result.then(function (selectedUser) {
      UsersFactory.delete({ userId: selectedUser.id},function(response) {
		alertService.success($filter('translate')('USERS.NOTIFY_DELETE', selectedUser));
        //Update display table
        $scope.orgList = UsersFactory.query(function(response) {
        },function(error){
          displayRestError.display(error);            
        });
        $scope.filteredList = $scope.orgList;
      },function(error){
        displayRestError.display(error);            
      });         
    }), 
    function () {
     //console.log('Modal dismissed at: ' + new Date());
    }
  };
    
  //Add a User
  $scope.add = function (size) {
    var addModalInstance = $uibModal.open({
    templateUrl: 'partials/users/addModal.html',
    controller: 'UaddController',
    size: size,
    resolve: {}
    });

    addModalInstance.result.then(function (newUser) {
      UsersFactory.create(newUser,function(response) {
		alertService.success($filter('translate')('USERS.NOTIFY_ADD', newUser));
        //Update display table
        $scope.orgList = UsersFactory.query(function(response) {
        },function(error){
          displayRestError.display(error);            
        });
        $scope.filteredList = $scope.orgList;
      },function(error){
        displayRestError.display(error);            
      }); 
    }),
    function () {
      //console.log('Modal dismissed at: ' + new Date());
    }
  };
    
  //Update a User
  $scope.update = function (user, size) {
    var editModalInstance = $uibModal.open({
    templateUrl: 'partials/users/updateModal.html',
    controller: 'UupdateController',
    size: size,
    resolve: {user: function () {return user;}}
    });

    editModalInstance.result.then(function (updateUser) {
      UsersFactory.update(updateUser,function(response) {
 		alertService.success($filter('translate')('USERS.NOTIFY_UPDATE', updateUser));
        //Update display table
        $scope.orgList = UsersFactory.query(function(response) {
        },function(error){
          displayRestError.display(error);            
        });
        $scope.filteredList = $scope.orgList;
      },function(error){
        displayRestError.display(error);            
      });
    }), 
    function () {
     //console.log('Modal dismissed at: ' + new Date());
    }
  };
});


//Users Modal
myControllerModule.controller('UdeleteController', function ($scope, $modalInstance, $sce, user, $filter) {
  $scope.user = user;
  $scope.header = $filter('translate')('USERS.TITLE_DELETE');
  $scope.deleteMsg = $filter('translate')('USERS.MESSAGE_DELETE',user);
  $scope.remove = function() {
    $modalInstance.close($scope.user);
  };
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('UaddController', function ($scope, $modalInstance, TypesFactory, $filter) {
  $scope.user = {};
  $scope.header = $filter('translate')('USERS.TITLE_ADD')
  $scope.roles = TypesFactory.getUserRoles();
  $scope.add = function() {$modalInstance.close($scope.user); }
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});

myControllerModule.controller('UupdateController', function ($scope, $modalInstance, TypesFactory, user, $filter) {
  $scope.user = user;
  $scope.header = $filter('translate')('USERS.TITLE_UPDATE');
  $scope.roles = TypesFactory.getUserRoles();
  $scope.update = function() {$modalInstance.close($scope.user);}
  $scope.cancel = function () { $modalInstance.dismiss('cancel'); }
});
