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