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
	var appip = angular.module("Metrics",["ui.bootstrap"]);

	appip.time = 0;

	appip.controller("GraphicsPanelController", function() {
		this.panels = {"0":{"title":"Test Panel"}};
		this.idCounter = 1;

		this.addNewPanel = function() {
			this.panels[this.idCounter] = {"title":"hola tron " + this.idCounter};
			this.idCounter++;
		};

		this.removePanel = function(panelId) {
			delete this.panels[panelId];
		};
	});

	// Cache for typeahead controller.
	// TODO: refresh this cache every X seconds
	appip.appsNodesList = null;

	appip.controller("TypeAheadCtrl", ["$scope","$http","$log", function($scope, $http, $log) {
		$scope.selected = undefined;
		$scope.getMetrics = function (input) {
			var path = input.split("/");
			$log.log("--> " + path.join(" . "));
			if(appip.appsNodesList == null) {
				// populates application/node tree
				return $http.get("/api").then(function (res) {
					appip.appsNodesList = {};
					// converts it to tree format
					for(var app in res.data) {
						appip.appsNodesList[app] = {};
						for(var node in res.data[app]) {
							appip.appsNodesList[app][res.data[app][node]] = null;
						}
					}
					return matchInput();
				});
			}
			if(path.length >= 3) {
				var appK = getIgnoreCase(appip.appsNodesList, path[0]);
				if(appK != null) {
					var nodeK = getIgnoreCase(appip.appsNodesList[appK], path[1]);
					if (appip.appsNodesList[appK][nodeK] == null) {
						return $http.get("/api/"+appK+"/"+nodeK).then(function (res) {
							appip.appsNodesList[appK][nodeK] = res.data.values;
							return matchInput();
						});
					}
				}
			}
			return matchInput();

			function matchInput() {
				var services = [];

				// find options subtree
				var subtree = appip.appsNodesList;
				var pathIdx;
				var urlPath = "";
				for(pathIdx = 0 ; pathIdx < path.length-1 ; pathIdx++) {
					var r = new RegExp(path[pathIdx],"i");
					var key = getIgnoreCase(subtree,path[pathIdx]);
					if(key != null) {
						urlPath += encodeURIComponent(key) + "/";
						subtree = subtree[key];
					} else {
						break;
					}
				}

				// find choice from last path
				if(subtree instanceof Array || subtree instanceof Object) {
					for (var key in subtree) {
						var r = new RegExp(path[pathIdx], "i");
						if (r.test(key)) {
							services.push(urlPath + encodeURIComponent(key));
						}
					}
					services.sort();
				}
				return services;
			}
		};
	}]);

	function getIgnoreCase(tree, field) {
		field = field.toLowerCase();
		for(var f in tree) {
			if(field == f.toLowerCase()) {
				return f;
			}
		}
		return null;
	}
})();
