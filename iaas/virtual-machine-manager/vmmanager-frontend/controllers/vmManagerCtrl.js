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
    .module('vmmanager.controllers')
    .controller('VmManagerController', [ 'VmService', vmManagerCtrl ]);

function vmManagerCtrl(VmService) {
    var vmmanager = this;

    vmmanager.loadVms = function() {
        VmService
            .getVms()
            .then(function(response) {
                vmmanager.vms = response["data"]["vms"];
                convertVmsStringDates();
            });
    };

    vmmanager.deleteVm = function(vmId) {
        VmService
            .deleteVm(vmId)
            .then(function() {
                vmmanager.loadVms();
            });
    };

    vmmanager.performActionVm = function(vmId, apiAction) {
        VmService
            .performActionOnVm(vmId, apiAction)
            .then(function() {
                vmmanager.loadVms();
            });
    };

    vmmanager.changeColumnSort = function(criteriaIndex, reverse) {
        vmmanager.columnSort = { criteria: vmmanager.sortingCriteria[criteriaIndex], reverse: reverse };
    };

    // Performs an action on a VM (destroy, reboot, etc.)
    vmmanager.performAction = function(vmId, action) {
        if (action == "Destroy") { // DELETE REST call
            vmmanager.deleteVm(vmId);
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
            vmmanager.performActionVm(vmId, apiAction);
        }
    };

    vmmanager.refresh = function() {
        vmmanager.loadVms();
    };

    vmmanager.newVm = function(name, imageId, cpus, ramMb, diskGb, appId) {
        VmService
            .deployVm(name, imageId, cpus, ramMb, diskGb, appId)
            .then(function() {
                vmmanager.refresh();
                $('#myModal').modal('hide'); // TODO: This should be done using a directive
            });
    };

    // Transforms the dates from string to Date so they can be sorted
    function convertVmsStringDates() {
        vmmanager.vms.forEach(function(vm) {
            vm.created = new Date(vm.created);
        });
    }

    vmmanager.vmAttributes = ["Name", "ID", "Image", "CPUs", "RAM(MB)",
        "Disk(GB)", "State", "IP", "Host", "Created", "App ID", "Actions"];
    vmmanager.vmActions = ["Destroy", "Hard reboot", "Soft reboot",
        "Start", "Stop", "Suspend", "Resume"];

    // Table sorting
    vmmanager.sortingCriteria = ["name", "id", "image", "cpus", "ramMb", "diskGb", "state", "ipAddress", "hostName",
        "created", "applicationId"];
    vmmanager.columnSort = { criteria:vmmanager.sortingCriteria[9], reverse:true };

    // Initialize the existing VMs to empty, because this array will be checked before performing the REST request
    vmmanager.vms = [];

    vmmanager.loadVms();
}