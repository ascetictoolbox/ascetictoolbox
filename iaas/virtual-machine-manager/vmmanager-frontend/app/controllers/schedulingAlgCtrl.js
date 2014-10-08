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
    function SchedulingAlgCtrl(SchedulingAlgService) {

        var schedulingAlgCtrl = this;
        schedulingAlgCtrl.algorithms = [];
        schedulingAlgCtrl.currentAlgorithm = '';

        schedulingAlgCtrl.loadSchedulingAlg = loadSchedulingAlg;
        schedulingAlgCtrl.getCurrentSchedulingAlg = getCurrentSchedulingAlg;
        schedulingAlgCtrl.changeSchedulingAlg = changeSchedulingAlg;

        activate();

        function activate() {
            schedulingAlgCtrl.loadSchedulingAlg();
            schedulingAlgCtrl.getCurrentSchedulingAlg();
        }

        function loadSchedulingAlg() {
            SchedulingAlgService
                .getSchedulingAlgorithms()
                .then(
                    function(response) {
                        schedulingAlgCtrl.algorithms = response.data.scheduling_algorithms;
                        toastr.success('Scheduling algorithms loaded.');
                    },
                    function() {
                        toastr.error('Could not load the scheduling algorithms.');
                    });

        }

        function getCurrentSchedulingAlg() {
            SchedulingAlgService
                .getCurrentSchedulingAlg()
                .then(
                    function(response) {
                        schedulingAlgCtrl.currentAlgorithm = response.data.name;
                        toastr.success('Current scheduling algorithm loaded.');
                    },
                    function() {
                        toastr.error('Could not load the current scheduling algorithm.');
                    });
        }

        function changeSchedulingAlg(newSchedulingAlg) {
            toastr.info('Changing scheduling algorithm.');
            SchedulingAlgService
                .changeCurrentSchedulingAlg(newSchedulingAlg)
                .then(
                    function() {
                        schedulingAlgCtrl.getCurrentSchedulingAlg();
                        toastr.success('Scheduling algorithm changed.');
                    },
                    function() {
                        toastr.error('Error while changing the scheduling algorithm.');
                    });
        }

    }
    SchedulingAlgCtrl.$inject = ['SchedulingAlgService'];

})();