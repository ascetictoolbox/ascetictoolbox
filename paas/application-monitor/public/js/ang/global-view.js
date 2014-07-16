(function() {
	var globalView = angular.module("GlobalView",[]);

    globalView.controller("MetaController", ["$http","$scope", function($http, $scope) {
        $scope.meta = '{"osName":"Loading...","osVersion":"Loading...","arch":"Loading...","cpus":"Loading..."}';
        $http.get("/status/meta").success(function(data) {
            $scope.meta = data;
        });
    }]);

})();