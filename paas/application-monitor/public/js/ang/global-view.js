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
	var globalView = angular.module("GlobalView",[]);

    globalView.controller("MetaController", ["$http","$scope", function($http, $scope) {
        $scope.meta = '{"osName":"Loading...","osVersion":"Loading...","arch":"Loading...","cpus":"Loading..."}';
        $http.get("/status/meta").success(function(data) {
            $scope.meta = data;
        });
    }]);

    globalView.controller("AppsListController", ["$http","$interval",function($http, $interval) {
        var self = this;
        this.appsTree = {};


        // todo: echar un ojo a esto: http://angular-ui.github.io/bootstrap/
        // Typeahead

        var loadApps = function() {
            var start = - 5*60*1000; // look for activity in the last 5 minutes
            $http.get("/apps?start="+start).success(function(data) {
                self.appsTree = data;
            });
        }
        $interval(loadApps,5000);
        loadApps();
    }]);


})();