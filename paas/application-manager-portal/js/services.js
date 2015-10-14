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
//  Services to communicate with the Application Manager

angular.module('asceticApplicationManagerPortalApp.services', [])
  
  .factory('ApplicationService', ['$resource', function($resource) {

    var service = $resource('http://localhost:8080/application-manager/applications/:name',{name: "@name"},  {
      query: {
        isArray: false,
        method: 'GET',
        headers: {'Accept': 'application/json'}
      }
    });

    return service;
  }])

  .factory('DeploymentService', ['$resource', function($resource) {

    var service = $resource('http://localhost:8080/application-manager/applications/:name/deployments/:id',{name: "@name", id: "@id"},  {
      query: {
        isArray: false,
        method: 'GET',
        headers: {'Accept': 'application/json'}
      }
    });

    return service;
  }]);