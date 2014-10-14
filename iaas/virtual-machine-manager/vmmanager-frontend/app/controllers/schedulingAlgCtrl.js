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
        .controller('SchedulingAlgCtrl', SchedulingAlgCtrl);

    /* @ngInject */
    function SchedulingAlgCtrl(SchedulingAlgService, $q, $scope) {

        var schedulingAlgCtrl = this;
        $scope.loading = true;
        schedulingAlgCtrl.algorithms = [];
        schedulingAlgCtrl.currentAlgorithm = '';
        schedulingAlgCtrl.changeSchedulingAlg = changeSchedulingAlg;

        activate();

        function activate() {
            loadSchedulingAlgsInfo();
        }

        function changeSchedulingAlg(newSchedulingAlg) {
            toastr.info('Changing scheduling algorithm...');
            SchedulingAlgService
                .changeCurrentSchedulingAlg(newSchedulingAlg)
                .then(
                    function() {
                        schedulingAlgCtrl.getCurrentSchedulingAlg();
                        toastr.success('Scheduling algorithm changed.');
                    },
                    function() {
                        toastr.error('Error while changing the scheduling algorithm.');
                    }
                );
        }

        /** Loads the scheduling algorithms available and the current one */
        function loadSchedulingAlgsInfo() {
            $q.all([SchedulingAlgService.getSchedulingAlgorithms(), SchedulingAlgService.getCurrentSchedulingAlg()])
                .then(
                    function(responses) {
                        schedulingAlgCtrl.algorithms = responses[0].data.scheduling_algorithms;
                        schedulingAlgCtrl.currentAlgorithm = responses[1].data.name;
                        toastr.success('Scheduling algorithms loaded.');
                        $scope.loading = false;
                    },
                    function() {
                        toastr.error('Could not load the current scheduling algorithm.');
                        $scope.loading = false;
                    }
                );
        }

    }
    SchedulingAlgCtrl.$inject = ['SchedulingAlgService', '$q', '$scope'];

})();