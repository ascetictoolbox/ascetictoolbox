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
        .controller('SelfAdaptationCtrl', SelfAdaptationCtrl);

    /* @ngInject */
    function SelfAdaptationCtrl(VmPlacementService, SelfAdaptationService, $q) {

        var selfAdaptation = this;
        selfAdaptation.loading = true;
        selfAdaptation.constructionHeuristics = [];
        selfAdaptation.localSearchAlgs = [];
        selfAdaptation.options = {
            vmDeployedOpts: {
                constrHeuristic: {},
                localSearchAlg: {
                    options: {}
                },
                maxExecTimeSeconds: 0
            },
            vmDestroyedOpts: {
                localSearchAlg: {
                    options: {}
                },
                maxExecTimeSeconds: 0
            },
            periodicOpts: {
                localSearchAlg: {
                    options: {}
                },
                timeIntervalMinutes: {},
                maxExecTimeSeconds: 0
            }
        };

        selfAdaptation.inputOptionIsActive = inputOptionIsActive;
        selfAdaptation.showOptions = showOptions;
        selfAdaptation.saveOptions = saveOptions;
        selfAdaptation.getLocalSearchAlgNames = getLocalSearchAlgNames;
		selfAdaptation.engageManual = engageManual;

        /* Every scheduling algorithm has different configuration options (size, accepted count limit, etc.).
           The following constants define those options.*/
        var ALGS_WITHOUT_SIZE = ['Hill Climbing', 'Simulated Annealing'];
        var ALGS_WITHOUT_ACC_COUNT_LIMIT = ['Hill Climbing', 'Late Acceptance',
            'Step Counting Hill Climbing', 'Simulated Annealing'];
        var ALGS_WITHOUT_INITIAL_HARD_TEMP = ['Hill Climbing', 'Late Acceptance', 'Late Simulated Annealing',
            'Step Counting Hill Climbing', 'Late Simulated Annealing', 'Tabu Search'];
        var ALGS_WITHOUT_INITIAL_SOFT_TEMP = ['Hill Climbing', 'Late Acceptance', 'Late Simulated Annealing',
            'Step Counting Hill Climbing', 'Late Simulated Annealing', 'Tabu Search'];

        activate();

        function activate() {
            loadSchedulingAlgsInfoAndSelfAdaptationOpts();
        }

        function loadSchedulingAlgsInfoAndSelfAdaptationOpts() {
            $q.all([VmPlacementService.getConstructionHeuristics(),
                    VmPlacementService.getLocalSearchAlgs(),
                    SelfAdaptationService.getSelfAdaptationOptions()])
                .then(
                    function(responses) {
                        selfAdaptation.constructionHeuristics= responses[0].data;
                        selfAdaptation.localSearchAlgs = responses[1].data;
                        loadSelfAdaptationOpts(responses[2].data);
                        selfAdaptation.localSearchAlgs.unshift({ name: "None", options: []});
                        toastr.success('Self adaptation options loaded.');
                        selfAdaptation.loading = false;
                    },
                    function() {
                        toastr.error('Could not load self adaptation options.');
                        selfAdaptation.loading = false;
                    }
                );
        }

        function inputOptionIsActive(localSearchAlgName, option) {
            if (!localSearchAlgName || localSearchAlgName === 'None') {
                return false;
            }
            else if (option === 'size') {
                return ALGS_WITHOUT_SIZE.indexOf(localSearchAlgName) === -1;
            }
            else if (option === 'acceptedCountLimit') {
                return ALGS_WITHOUT_ACC_COUNT_LIMIT.indexOf(localSearchAlgName) === -1;
            }
            else if (option === 'initialHardTemp') {
                return ALGS_WITHOUT_INITIAL_HARD_TEMP.indexOf(localSearchAlgName) === -1;
            }
            else if (option === 'initialSoftTemp') {
                return ALGS_WITHOUT_INITIAL_SOFT_TEMP.indexOf(localSearchAlgName) === -1;
            }
        }

        function showOptions(localSearchAlgName) {
            return localSearchAlgName && (localSearchAlgName !== "None" && localSearchAlgName !== 'Hill Climbing');
        }

        function getLocalSearchAlgNames() {
            var result = [];
            selfAdaptation.localSearchAlgs.forEach(function(localSearchAlg) {
                result.push(localSearchAlg.name);
            });
            return result;
        }

		function engageManual() {
			SelfAdaptationService.engageManual()
				.then(
				function() {
					toastr.success('Self adaptation manually triggered');
				},
				function() {
					toastr.error('Error triggering self-adaptation manually');
				}
			);
		}

        function saveOptions() {
            toastr.info('Saving self-adaptation options...');
            var selfAdaptationOpts = { // Create empty self-adaptation options object
                afterVmDeploymentSelfAdaptationOps: {
                    constructionHeuristic: {},
                    localSearchAlgorithm: {},
                    maxExecTimeSeconds: 0
                },
                afterVmDeleteSelfAdaptationOps: {
                    localSearchAlgorithm: {},
                    maxExecTimeSeconds: 0
                },
                periodicSelfAdaptationOps: {
                    localSearchAlgorithm: {},
                    timeIntervalMinutes: 0,
                    maxExecTimeSeconds: 0
                }
            };
            selfAdaptationOpts = setSelfAdaptationOpts(selfAdaptationOpts, selfAdaptation.options.vmDeployedOpts,
                    selfAdaptation.options.vmDestroyedOpts, selfAdaptation.options.periodicOpts);
            SelfAdaptationService.saveSelfAdaptationOptions(selfAdaptationOpts)
                .then(
                    function() {
                        toastr.success('Self adaptation options saved.');
                    },
                    function() {
                        toastr.error('Could not save self adaptation options.');
                    }
                );

        }

        function setSelfAdaptationOpts(selfAdaptationOpts, vmDeployedOpts, vmDestroyedOpts, periodicOpts) {
            var result = selfAdaptationOpts;
            result = setAfterVmDeploymentOptions(result, vmDeployedOpts);
            result = setAfterVmDeleteOptions(result, vmDestroyedOpts);
            result = setPeriodicOptions(result, periodicOpts);
            return result;
        }

        function setAfterVmDeploymentOptions(selfAdaptationOpts, vmDeployedOpts) {
            var result = selfAdaptationOpts;
            if (vmDeployedOpts.constrHeuristic) {
                result.afterVmDeploymentSelfAdaptationOps.constructionHeuristic = vmDeployedOpts.constrHeuristic;
            }
            if (vmDeployedOpts.localSearchAlg && vmDeployedOpts.localSearchAlg.name !== 'None') {
                result.afterVmDeploymentSelfAdaptationOps.localSearchAlgorithm = vmDeployedOpts.localSearchAlg;
                deleteUnnecessaryLocalSearchOpts(result.afterVmDeploymentSelfAdaptationOps.localSearchAlgorithm);
                result.afterVmDeploymentSelfAdaptationOps.maxExecTimeSeconds = vmDeployedOpts.maxExecTimeSeconds;
            }
            return result;
        }

        function setAfterVmDeleteOptions(selfAdaptationOpts, vmDestroyedOpts) {
            var result = selfAdaptationOpts;
            if (vmDestroyedOpts.localSearchAlg && vmDestroyedOpts.localSearchAlg.name !== 'None') {
                result.afterVmDeleteSelfAdaptationOps.localSearchAlgorithm = vmDestroyedOpts.localSearchAlg;
                deleteUnnecessaryLocalSearchOpts(result.afterVmDeleteSelfAdaptationOps.localSearchAlgorithm);
                result.afterVmDeleteSelfAdaptationOps.maxExecTimeSeconds = vmDestroyedOpts.maxExecTimeSeconds;
            }
            return result;
        }

        function setPeriodicOptions(selfAdaptationOpts, periodicOpts) {
            var result = selfAdaptationOpts;
            if (periodicOpts.localSearchAlg && periodicOpts.localSearchAlg.name !== 'None') {
                result.periodicSelfAdaptationOps.localSearchAlgorithm = periodicOpts.localSearchAlg;
                deleteUnnecessaryLocalSearchOpts(result.periodicSelfAdaptationOps.localSearchAlgorithm);
                result.periodicSelfAdaptationOps.timeIntervalMinutes = parseInt(periodicOpts.timeIntervalMinutes);
                result.periodicSelfAdaptationOps.maxExecTimeSeconds = periodicOpts.maxExecTimeSeconds;
            }
            return result;
        }

        function loadSelfAdaptationOpts(options) {
            // After VM deployment
            selfAdaptation.constructionHeuristics.forEach(function(heuristic) {
                if (heuristic.name === options.afterVmDeploymentSelfAdaptationOps.constructionHeuristic.name) {
                    selfAdaptation.options.vmDeployedOpts.constrHeuristic = heuristic;
                }
            });
            if (options.afterVmDeploymentSelfAdaptationOps.localSearchAlgorithm &&
                    options.afterVmDeploymentSelfAdaptationOps.localSearchAlgorithm.name) {
                selfAdaptation.options.vmDeployedOpts.localSearchAlg =
                        options.afterVmDeploymentSelfAdaptationOps.localSearchAlgorithm;
                selfAdaptation.options.vmDeployedOpts.maxExecTimeSeconds =
                    options.afterVmDeploymentSelfAdaptationOps.maxExecTimeSeconds;
            }
            else {
                selfAdaptation.options.vmDeployedOpts.localSearchAlg.name = 'None';
                selfAdaptation.options.vmDeployedOpts.maxExecTimeSeconds = 0;
            }

            // After VM deleted
            if (options.afterVmDeleteSelfAdaptationOps.localSearchAlgorithm &&
                    options.afterVmDeleteSelfAdaptationOps.localSearchAlgorithm.name) {
                selfAdaptation.options.vmDestroyedOpts.localSearchAlg =
                        options.afterVmDeleteSelfAdaptationOps.localSearchAlgorithm;
                selfAdaptation.options.vmDestroyedOpts.maxExecTimeSeconds =
                        options.afterVmDeleteSelfAdaptationOps.maxExecTimeSeconds;
            }
            else {
                selfAdaptation.options.vmDestroyedOpts.localSearchAlg.name = 'None';
                selfAdaptation.options.vmDestroyedOpts.maxExecTimeSeconds = 0;
            }

            // Periodic
            if (options.periodicSelfAdaptationOps.localSearchAlgorithm &&
                    options.periodicSelfAdaptationOps.localSearchAlgorithm.name) {
                selfAdaptation.options.periodicOpts.localSearchAlg =
                        options.periodicSelfAdaptationOps.localSearchAlgorithm;
                selfAdaptation.options.periodicOpts.timeIntervalMinutes =
                        options.periodicSelfAdaptationOps.timeIntervalMinutes;
                selfAdaptation.options.periodicOpts.maxExecTimeSeconds =
                        options.periodicSelfAdaptationOps.maxExecTimeSeconds;
            }
            else {
                selfAdaptation.options.periodicOpts.localSearchAlg.name = 'None';
                selfAdaptation.options.periodicOpts.timeIntervalMinutes = 0;
                selfAdaptation.options.periodicOpts.maxExecTimeSeconds = 0;
            }

        }

        /* Each local search algorithm has its own options.
         The form may contain non-valid data and we need to 'clean' it. */
        function deleteUnnecessaryLocalSearchOpts(localSearchAlg) {
            if (ALGS_WITHOUT_SIZE.indexOf(localSearchAlg.name) !== -1) {
                delete localSearchAlg.options.size;
            }
            if (ALGS_WITHOUT_ACC_COUNT_LIMIT.indexOf(localSearchAlg.name) !== -1) {
                delete localSearchAlg.options.acceptedCountLimit;
            }
            if (ALGS_WITHOUT_INITIAL_HARD_TEMP.indexOf(localSearchAlg.name) !== -1) {
                delete localSearchAlg.options.initialHardTemp;
            }
            if (ALGS_WITHOUT_INITIAL_SOFT_TEMP.indexOf(localSearchAlg.name) !== -1) {
                delete localSearchAlg.options.initialSoftTemp;
            }
        }

    }
    SelfAdaptationCtrl.$inject = ['VmPlacementService', 'SelfAdaptationService', '$q'];

})();
