(function() {
	var appip = angular.module("Dashboard",["ui.bootstrap"]);

	appip.time = 0;

	appip.controller("GraphicsPanelController", ["$log",function($log) {
		this.panels = {"0":{"title":"Test Panel"}};
		this.idCounter = 1;

		this.addNewPanel = function() {
			this.panels[this.idCounter] = {"title":"hola tron " + this.idCounter};
			this.idCounter++;
		};

		this.removePanel = function(panelId) {
			delete this.panels[panelId];
		};
	}]);

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


	appip.controller("AppsListController", ["$http","$log",function($http,$log) {
		var self = this;
		this.appsTree = {};


		// todo: echar un ojo a esto: http://angular-ui.github.io/bootstrap/
		// Typeahead

		$http.get("/api/").success(function(data) {
			self.appsTree = data;
		});

/*		var self = this;
		this.time = appip.time;

		var incTime = function() {
			appip.time++;
			self.time = appip.time;

		}

		var stopTime = $interval(incTime, 1000);

		$scope.$on("$destroy", function() {
			$interval.cancel(stopTime);
		});
*/
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
