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
//  Configuration of the Portal that defines the workflow and layout

angular.module('asceticApplicationManagerPortalApp', ['ngRoute', 'ngResource', 'asceticApplicationManagerPortalApp.services', 'asceticApplicationManagerPortalApp.controllers'])

  .config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/', {
      templateUrl: 'partials/applications_list.html',
      controller: 'AppsCtrl as ctrl'
    })
    .when('/new_deployment', {
      templateUrl: 'partials/new_deployment.html',
      controller: 'CreateDeploymentController as ctrl'
    })
    .when('/applications/:applicationName/deployments', {
      templateUrl: 'partials/deployments_list.html',
      controller: 'DeploymentsController as ctrl'
    })
    .when('/applications/:applicationName/deployments/:deploymentId', {
      templateUrl: 'partials/deployment.html',
      controller: 'DeploymentController as ctrl'
    })
   .otherwise({redirectTo: '/'});
  }])

  .directive('prettyprint', function() {
    return {
        restrict: 'xml',
        link: function postLink(scope, element, attrs) {
              var str1 = String(element.html());
              str1.replace(/\n/g,"<br/>");
              element.html(prettyPrintOne(str1,'',true));
        }
    };
  })

  .directive('onReadFile', function ($parse) {
    return {
      restrict: 'A',
      scope: false,
      link: function(scope, element, attrs) {
        var fn = $parse(attrs.onReadFile);
            
        element.on('change', function(onChangeEvent) {
          var reader = new FileReader();
                
          reader.onload = function(onLoadEvent) {
            scope.$apply(function() {
              fn(scope, {$fileContent:onLoadEvent.target.result});
            });
          };

          reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
        });
      }
    };
  });
  


