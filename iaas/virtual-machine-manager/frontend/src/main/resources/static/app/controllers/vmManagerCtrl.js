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
        .module('vmmanager.controllers')
        .controller('VmManagerController', vmManagerCtrl);

    /* @ngInject */
    function vmManagerCtrl(VmService) {

        var vmmanager = this;
        vmmanager.loading = true;
        vmmanager.vmAttributes = ['Name', 'ID', 'Image', 'CPUs', 'RAM(MB)',
            'Disk(GB)', 'State', 'IP', 'Host', 'Created', 'App ID', 'Actions'];
        vmmanager.vmActions = ['Destroy', 'Hard reboot', 'Soft reboot', 'Start', 'Stop', 'Suspend', 'Resume'];
        vmmanager.sortingCriteria = ['name', 'id', 'image', 'cpus', 'ramMb', 'diskGb', 'state', 'ipAddress', 'hostName',
            'created', 'applicationId'];
        vmmanager.columnSort = { criteria:vmmanager.sortingCriteria[9], reverse:true };
        vmmanager.vms = [];

        vmmanager.deleteVm = deleteVm;
        vmmanager.performActionVm = performActionVm;
        vmmanager.changeColumnSort = changeColumnSort;
        vmmanager.performAction = performAction;
        vmmanager.newVm = newVm;
        vmmanager.refreshVmList = refreshVmList;

        activate();

        function activate() {
            loadVms();
        }

        function loadVms() {
            VmService
                .getVms()
                .then(
                    function(response) {
                        vmmanager.vms = response.data.vms;
                        convertVmsStringDates();
                        vmmanager.loading = false;
                        toastr.success('List of VMs loaded.');
                    },
                    function() {
                        toastr.error('Could not load the list of VMs.');
                        vmmanager.loading = false;
                    });
        }

        function deleteVm(vmId) {
            VmService
                .deleteVm(vmId)
                .then(
                    function() {
                        loadVms();
                        toastr.success('VM deleted.');
                    },
                    function() {
                        toastr.error('Could not delete the VM.');
                    });
        }

        function performActionVm(vmId, apiAction) {
            VmService
                .performActionOnVm(vmId, apiAction)
                .then(
                    function() {
                        loadVms();
                        toastr.success('Action performed on VM.');
                    },
                    function() {
                        toastr.error('Could not perform the action on the VM.');
                    });
        }

        function changeColumnSort(criteriaIndex, reverse) {
            vmmanager.columnSort = { criteria: vmmanager.sortingCriteria[criteriaIndex], reverse: reverse };
        }

        // Performs an action on a VM (destroy, reboot, etc.)
        function performAction(vmId, action) {
            toastr.info('Performing action on VM...');
            if (action === 'Destroy') { // DELETE REST call
                vmmanager.deleteVm(vmId);
            }
            else { // PUT REST calls
                var apiAction = '';
                if (action === 'Hard reboot') {
                    apiAction = 'rebootHard';
                }
                else if (action === 'Soft reboot') {
                    apiAction = 'rebootSoft';
                }
                else if (action === 'Start') {
                    apiAction = 'start';
                }
                else if (action === 'Stop') {
                    apiAction = 'stop';
                }
                else if (action === 'Suspend') {
                    apiAction = 'suspend';
                }
                else if (action === 'Resume') {
                    apiAction = 'resume';
                }
                vmmanager.performActionVm(vmId, apiAction);
            }
        }

        function newVm(name, imageId, cpus, ramMb, diskGb, appId) {
            $('#myModal').modal('hide'); // TODO: This should be done using a directive
            toastr.info('Deploying VM...');

            var newVm = {
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

            VmService
                .deployVm(newVm)
                .then(
                    function() {
                        loadVms();
                        toastr.success("VM deployed.");
                    },
                    function() {
                        toastr.error("Could not deploy VM.");
                    });

        }

        // Transforms the dates from string to Date so they can be sorted
        function convertVmsStringDates() {
            vmmanager.vms.forEach(function(vm) {
                vm.created = new Date(vm.created);
            });
        }

        function refreshVmList() {
            toastr.info('Refreshing list of VMs...');
            loadVms();
        }

    }
    vmManagerCtrl.$inject = ['VmService'];

})();