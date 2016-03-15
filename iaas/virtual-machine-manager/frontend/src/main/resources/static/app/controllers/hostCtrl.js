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
        .controller('HostCtrl', HostCtrl);

    /* @ngInject */
    function HostCtrl(HostService) {

        var hostCtrl = this;
        hostCtrl.loading = true;
        hostCtrl.hostAttributes = ['Host', 'CPUs', 'CPUs used (%)', 'RAM(MB)', 'RAM used (%)',
            'Disk(GB)', 'Disk used (%)', 'Current Power (W)'];
        hostCtrl.hosts = [];
        hostCtrl.sortingCriteria = ['hostname', 'totalCpus', 'assignedCpus/totalCpus', 'totalMemoryMb',
            'assignedMemoryMb/totalMemoryMb', 'totalDiskGb', 'assignedDiskGb/totalDiskGb', 'currentPower'];
        hostCtrl.columnSort = { criteria:hostCtrl.sortingCriteria[0], reverse:false };

        hostCtrl.changeColumnSort = changeColumnSort;
        hostCtrl.refreshHostList = refreshHostList;

        activate();

        function activate() {
            loadHosts();
        }

        function loadHosts() {
            HostService
                .getHosts()
                .then(
                    function(response) {
                        hostCtrl.hosts = response.data.nodes;
                        toastr.success('List of Hosts loaded.');
                        hostCtrl.loading = false;
                    },
                    function() {
                        toastr.error('Could not load the hosts.');
                        hostCtrl.loading = false;
                    });
        }

        function changeColumnSort(criteriaIndex, reverse) {
            hostCtrl.columnSort = { criteria: hostCtrl.sortingCriteria[criteriaIndex], reverse: reverse };
        }

        function refreshHostList() {
            toastr.info('Refreshing list of hosts...');
            loadHosts();
        }

    }
    HostCtrl.$inject = ['HostService'];

})();