//
// 
// Copyright 2015 ATOS SPAIN S.A. 
// 
// Licensed under the Apache License, Version 2.0 (the License);
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
// @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
// e-mail david.garciaperez@atos.net 
// 
//  Controllers to show the different infortmation in the Portal

angular.module('asceticApplicationManagerPortalApp.controllers', [])

  //.controller('MainCtrl', ['$scope', function($scope) {
  //  var self = this;

  //  $scope.applicationNameUrl;
  //}])
  
  .controller('AppsCtrl', ['$scope', '$rootScope', '$routeParams', 'ApplicationService', 
                            function($scope, $rootScope, $routeParams, ApplicationService) {
    var self = this;
    $scope.hideAllUrl = true;
    $rootScope.applicationNameUrl = $routeParams.applicationName;
    $rootScope.deploymentId = $routeParams.deploymentId;

    var response = ApplicationService.query();
    
    response.$promise.then(function(data) {
      $scope.applications = data.items.application; //Changed data.data.topics to data.topics
    });

    //console.log(response);
  }])

  .controller('DeploymentsController', [ '$scope', '$rootScope', '$routeParams', '$timeout', 'DeploymentService', 
                                          function($scope, $rootScope, $routeParams, $timeout, DeploymentService) {
      var self = this;
      $scope.applicationName = $routeParams.applicationName;
      $rootScope.applicationNameUrl = $routeParams.applicationName;
      $rootScope.deploymentId = $routeParams.deploymentId;

      $scope.loadDeployments = function() {
        var response = DeploymentService.query({name: $scope.applicationName, status: 'DEPLOYED'});

        response.$promise.then(function(data) {
          $scope.deployments = data.items.deployment; //Changed data.data.topics to data.topics
        });
      };

      $scope.deleteDeployment = function(id) {
        console.log(id);
        DeploymentService.deleteDeployment({name: $scope.applicationName, id: id});
        $timeout(function() { 
          $scope.loadDeployments(); 
        }, 10000);
        
      }
      
      $scope.loadDeployments();
  }])

  .controller('DeploymentController', [ '$scope', '$rootScope', '$routeParams', '$timeout', 'DeploymentService',
                                        function($scope, $rootScope, $routeParams, $timeout, DeploymentService) {
    var self = this;
    self.applicationName = $routeParams.applicationName;
    self.deploymentId = $routeParams.deploymentId;
    $rootScope.applicationNameUrl = self.applicationName;
    $rootScope.deploymentId = self.deploymentId;

    $scope.deployment = [];

    (function tick() {
         DeploymentService.query(
          {name: self.applicationName, id: self.deploymentId, status: 'DEPLOYED'},
          function(deployment){
             $scope.deployment = deployment;
             $timeout(tick, 10000);
         });
    })();

    $scope.deleteDeployment = function(id) {
        console.log(id);
        DeploymentService.deleteDeployment({name: self.applicationName, id: id});
    }
  }])

  .controller('CreateDeploymentController', [ '$scope', '$rootScope', '$routeParams', '$location', 'ApplicationService',
                                              function($scope, $rootScope, $routeParams, $location, ApplicationService) {
    var self = this;
    $scope.data = '<xml/>';
    $rootScope.applicationNameUrl = $routeParams.applicationName;
    $rootScope.deploymentId = $routeParams.deploymentId;

    $scope.showContent = function($fileContent){
        $scope.data = $fileContent;
    };

    $scope.post = function() {

      console.log($scope.data)

      var response = ApplicationService.save($scope.data);

      response.$promise.then(function(data) {

        // TODO I should add some error handling here... 
        $location.path("/applications/" + data.name + "/deployments/" + data.deployments.deployment[0].id);
      });
      //console.log(response);
    }

  }]); 

