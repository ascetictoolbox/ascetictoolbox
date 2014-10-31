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
        .controller('VmPlacementCtrl', VmPlacementCtrl);

    /* @ngInject */
    function VmPlacementCtrl(VmPlacementService) {

        var vmPlacement = this;
        vmPlacement.constructionHeuristics = [];
        vmPlacement.localSearchAlgs = [];
        vmPlacement.recommendedPlan = [];
        vmPlacement.getVmPlacement = getVmPlacement;
        vmPlacement.executeVmPlacement = executeVmPlacement;
        vmPlacement.inputOptionIsActive = inputOptionIsActive;
        vmPlacement.showOptions = showOptions;

        activate();

        function activate() {
            getConstructionHeuristics();
            getLocalSearchAlgs();
        }

        function getConstructionHeuristics() {
            VmPlacementService
                .getConstructionHeuristics()
                .then(
                    function(response) {
                        vmPlacement.constructionHeuristics = response.data;
                    },
                    function() {
                        toastr.error('Could not load the construction heuristics.');
                    });
        }

        function getLocalSearchAlgs() {
            VmPlacementService
                .getLocalSearchAlgs()
                .then(
                    function(response) {
                        vmPlacement.localSearchAlgs = response.data;
                    },
                    function() {
                        toastr.error('Could not load the local search algorithms.');
                    });
        }

        function getVmPlacement(timeLimit, heuristic, localSearchAlg, optionSize, optionAcceptedCountLimit,
                optionInitialHardTemp, optionInitialSoftTemp) {
            toastr.info('Calculating a new deployment plan...');
            var localSearchAlgOptions;
            switch (localSearchAlg) {
                case 'Hill Climbing':
                    localSearchAlgOptions = {};
                    break;
                case 'Late Acceptance':
                    localSearchAlgOptions = {size: optionSize};
                    break;
                case 'Late Simulated Annealing':
                    localSearchAlgOptions = {size: optionSize, acceptedCountLimit: optionAcceptedCountLimit};
                    break;
                case 'Simulated Annealing':
                    localSearchAlgOptions = {initialHardTemp: optionInitialHardTemp,
                            initialSoftTemp: optionInitialSoftTemp};
                    break;
                case 'Step Counting Hill Climbing':
                    localSearchAlgOptions = {size: optionSize};
                    break;
                case 'Tabu Search':
                    localSearchAlgOptions = {size: optionSize, acceptedCountLimit: optionAcceptedCountLimit};
                    break;
                default:
                    break;
            }

            var placementRequest = {
                timeLimitSeconds: timeLimit,
                constructionHeuristicName: heuristic,
                localSearchAlgorithm: {
                    name: localSearchAlg,
                    options: localSearchAlgOptions
                }
            };

            VmPlacementService
                .getVmPlacement(placementRequest)
                .then(
                    function(response) {
                        vmPlacement.recommendedPlan = response.data.plan;
                        toastr.success('Got a recommended plan for VM placement.');
                    },
                    function() {
                        toastr.error('Could not get a recommended plan for VM placement');
                    });

            $('#vmPlacementModal').modal('hide'); // TODO: This should be done using a directive
        }

        function executeVmPlacement() {
            var placementToExecute = [];
            $.map(vmPlacement.recommendedPlan, function(value, index) { // Convert to the format needed
                placementToExecute.push({vmId: index, hostname: value});
            });

            VmPlacementService
                .executeVmPlacement(placementToExecute)
                .then(
                    function() {
                       toastr.success('Deployment plan executed correctly.');
                    },
                    function() {
                        toastr.error('Error while executing the deployment plan');
                    });
        }

        function inputOptionIsActive(localSearchAlgName, option) {
            if (option === 'size') {
                return localSearchAlgName === 'Late Acceptance' || localSearchAlgName === 'Late Simulated Annealing'
                        || localSearchAlgName === 'Step Counting Hill Climbing'
                        || localSearchAlgName === 'Tabu Search';
            }
            else if (option === 'acceptedCountLimit') {
                return localSearchAlgName === 'Late Simulated Annealing' || localSearchAlgName === 'Tabu Search';
            }
            else if (option === 'initialHardTemp' || option === 'initialSoftTemp') {
                return localSearchAlgName === 'Simulated Annealing';
            }
        }

        function showOptions(localSearchAlg) {
            return !localSearchAlg || !localSearchAlg.name ? false : localSearchAlg.name !== 'Hill Climbing';
        }

    }
    VmPlacementCtrl.$inject = ['VmPlacementService'];

})();
