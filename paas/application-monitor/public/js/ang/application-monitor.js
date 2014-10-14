/*
 * Author: Mario Macias (Barcelona Supercomputing Center). 2014
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 */

(function() {

	var appm = angular.module('ApplicationMonitor',["Metrics","GlobalView"]);

	appm.controller('PageController',  ["$scope",function($scope) {

        $scope.page = 'global.html';
        $scope.setPage = function(newPage) {
            $scope.page = newPage;
		};
	}]);
})();

