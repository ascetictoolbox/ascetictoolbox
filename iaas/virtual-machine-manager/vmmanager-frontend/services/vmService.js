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

angular
    .module('vmmanager.services')
    .factory('VmService', [ '$http', VmService ] );

function VmService($http) {
    var VmService = { };
    VmService.getVms = function() {
        return $http({method: 'GET', url: base_url + "vms"});
    };
    VmService.deployVm = function(name, imageId, cpus, ramMb, diskGb, appId) {
        var dataNewVm = {
            vms: [
                {
                    name: name,
                    image: imageId,
                    cpus: cpus,
                    ramMb: ramMb,
                    diskGb: diskGb,
                    applicationId: appId
                }
            ]};
        return $http({method: 'POST', url: base_url + "vms/", data: dataNewVm});
    };
    VmService.deleteVm = function(vmId) {
        return $http({method: 'DELETE', url: base_url + "vms/" + vmId});
    };
    VmService.performActionOnVm = function(vmId, apiAction) {
        return $http({method: 'PUT', url: base_url + "vms/" + vmId, data: {"action": apiAction}});
    };
    return VmService;
}