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

angular.module('vmmanager.controllers').controller('VmManagerController', [ '$http', '$scope', function($http, $scope) {
    
    var vmmanager = this;

    // Gets the information about the VMs deployed in the system
    $scope.loadVms = function() {
        $http({method: 'GET', url: base_url + "vms"}).
            success(function(data) {
                vmmanager.vms = data.vms;
            })
    };

    $scope.deleteVm = function(vmId) {
        $http({method: 'DELETE', url: base_url + "vms/" + vmId}).
            success(function() {
                $scope.loadVms(); // Reload the data
            })
    };

    $scope.performActionVm = function(vmId, apiAction) {
        $http({method: 'PUT', url: base_url + "vms/" + vmId, data: {"action": apiAction}}).
            success(function() {
                $scope.loadVms(); // Reload the data
            })
    };

    // Perfoms an action on a VM (destroy, reboot, etc.)
    $scope.performAction = function(vmId, action) {
        if (action == "Destroy") { // DELETE REST call
                $scope.deleteVm(vmId);
        }
        else { // PUT REST calls
            var apiAction = "";
            if (action == "Hard reboot") {
                apiAction = "rebootHard";
            }
            else if (action == "Soft reboot") {
                apiAction = "rebootSoft";
            }
            else if (action == "Start") {
                apiAction = "start";
            }
            else if (action == "Stop") {
                apiAction = "stop";
            }
            else if (action == "Suspend") {
                apiAction = "suspend";
            }
            else if (action == "Resume") {
                apiAction = "resume";
            }
            $scope.performActionVm(vmId, apiAction);
        }
    };

    $scope.refresh = function() {
        $scope.loadVms();
    };

    $scope.newVm = function(name, imageId, cpus, ramMb, diskGb, appId) {
        var dataNewVm = {vms: [{
            name: name,
            image: imageId,
            cpus: cpus,
            ramMb: ramMb,
            diskGb: diskGb,
            applicationId: appId }]};

        $http({method: 'POST', url: base_url + "vms/", data: dataNewVm}).
            success(function() {
                $scope.refresh();
                $('#myModal').modal('hide'); // TODO: This should be done using a directive
            })
    };

    vmmanager.vmAttributes = ["Name", "ID", "Image", "CPUs", "RAM(MB)",
        "Disk(GB)", "State", "IP", "Host", "Created", "App ID", "Actions"];
    vmmanager.vmActions = ["Destroy", "Hard reboot", "Soft reboot",
        "Start", "Stop", "Suspend", "Resume"];

    // Initialize the existing VMs to empty, because this array will be 
    // checked before performing the REST request
    vmmanager.vms = []; 
        
    $scope.loadVms();

}]);