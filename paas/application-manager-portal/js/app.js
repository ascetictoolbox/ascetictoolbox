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
      templateUrl: 'partials/applications_list.html'//,
      //controller: 'MainCtrl as ctrl'
    })
    .when('/applications/:applicationName/deployments', {
      templateUrl: 'partials/deployments_list.html',
      controller: 'DeploymentsController as deploymentsCtrl'
    })
   .otherwise({redirectTo: '/'});
  }]);
  


