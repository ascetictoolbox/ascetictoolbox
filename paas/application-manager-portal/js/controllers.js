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
  
  .controller('AppsCtrl', ['$scope','ApplicationService', function($scope, ApplicationService) {
    var self = this;

    var response = ApplicationService.query();
    
    response.$promise.then(function(data) {
      $scope.applications = data.items.application; //Changed data.data.topics to data.topics
    });

    //console.log(response);
  }])

  .controller('DeploymentsController', [ '$scope', '$routeParams', 'DeploymentService', function($scope, $routeParams, DeploymentService) {
      var self = this;
      $scope.applicationName = $routeParams.applicationName; 

      var response = DeploymentService.query({name: $scope.applicationName, status: 'DEPLOYED'});

      response.$promise.then(function(data) {
        $scope.deployments = data.items.deployment; //Changed data.data.topics to data.topics
      });

      console.log(response);
  }])

  .controller('DeploymentController', [ '$scope', '$routeParams', '$timeout', 'DeploymentService', 
                                        function($scope, $routeParams, $timeout, DeploymentService) {
    var self = this;
    self.applicationName = $routeParams.applicationName; 
    self.deploymentId = $routeParams.deploymentId; 

    $scope.deployment = [];

    (function tick() {
         DeploymentService.query(
          {name: self.applicationName, id: self.deploymentId, status: 'DEPLOYED'}, 
          function(deployment){
             $scope.deployment = deployment;
             $timeout(tick, 10000);
         });
    })();
  }])

  .controller('CreateDeploymentController', [ '$scope', '$location', 'ApplicationService', 
                                              function($scope, $location, ApplicationService) {
    var self = this;
    $scope.data = '<xml/>';
    
    $scope.add = function() {
      var f = document.getElementById('file').files[0],
      r = new FileReader();
      r.onloadend = function(e) {
        $scope.data = e.target.result;
      }
      r.readAsBinaryString(f);
    }

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