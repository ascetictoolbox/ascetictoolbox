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

angular.module('vmmanager.controllers').controller('SchedulingAlgCtrl', [ '$http', '$scope', function($http, $scope) {
    
    var schedulingAlgCtrl = this;

    $scope.loadSchedulingAlg = function() {
        $http({method: 'GET', url: base_url + "scheduling_algorithms"}).
            success(function(data) {
                schedulingAlgCtrl.algorithms = data["scheduling_algorithms"];
            });
    };

    $scope.getCurrentSchedulingAlg = function() {
        $http({method: 'GET', url: base_url + "scheduling_algorithms/current"}).
            success(function(data) {
                schedulingAlgCtrl.currentAlgorithm = data.name
            })
    };

    $scope.changeSchedulingAlg = function(newSchedulingAlg) {
        $http({method: 'PUT', url: base_url + "scheduling_algorithms/current", data: {"algorithm": newSchedulingAlg}}).
            success(function() {
                $scope.getCurrentSchedulingAlg();
            })
    };

    schedulingAlgCtrl.algorithms = [];
    schedulingAlgCtrl.currentAlgorithm = "";
    $scope.loadSchedulingAlg();
    $scope.getCurrentSchedulingAlg();

}]);