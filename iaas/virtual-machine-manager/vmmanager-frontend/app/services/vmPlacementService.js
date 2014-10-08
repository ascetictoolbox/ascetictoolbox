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
        .factory('VmPlacementService', VmPlacementService);

    /* @ngInject */
    function VmPlacementService($http, BASE_URL) {
        return {
            getConstructionHeuristics: getConstructionHeuristics,
            getLocalSearchAlgs: getLocalSearchAlgs,
            getVmPlacement: getVmPlacement
        };

        function getConstructionHeuristics() {
            return $http({method: 'GET', url: BASE_URL + 'vm_placement/construction_heuristics'});
        }

        function getLocalSearchAlgs() {
            return $http({method: 'GET', url: BASE_URL + 'vm_placement/local_search_algorithms'});
        }

        function getVmPlacement(timeLimit, constructionHeuristic, localSearchAlgName, localSearchAlgOptions) {
            var placementRequest = {
                timeLimitSeconds: timeLimit,
                constructionHeuristicName: constructionHeuristic,
                localSearchAlgorithm: {
                    name: localSearchAlgName,
                    options: localSearchAlgOptions
                }
            };
            return $http({method: 'PUT', url: BASE_URL + 'vm_placement/recommended_plan', data: placementRequest});
        }
    }
    VmPlacementService.$inject = ['$http', 'BASE_URL'];

})();