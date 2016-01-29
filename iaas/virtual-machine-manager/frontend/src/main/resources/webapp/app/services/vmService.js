/*
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

(function () {

    angular
        .module('vmmanager.services')
        .factory('VmService', VmService);

    /* @ngInject */
    function VmService($http, BASE_URL) {
        return {
            getVms: getVms,
            deployVm: deployVm,
            deleteVm: deleteVm,
            performActionOnVm: performActionOnVm
        };

        function getVms() {
            return $http({method: 'GET', url: BASE_URL + 'vms'});
        }

        function deployVm(vm) {
            return $http({method: 'POST', url: BASE_URL + 'vms/', data: vm});
        }

        function deleteVm(vmId) {
            return $http({method: 'DELETE', url: BASE_URL + 'vms/' + vmId});
        }

        function performActionOnVm(vmId, apiAction) {
            return $http({method: 'PUT', url: BASE_URL + 'vms/' + vmId, data: {action: apiAction}});
        }
    }
    VmService.$inject = ['$http', 'BASE_URL'];

})();