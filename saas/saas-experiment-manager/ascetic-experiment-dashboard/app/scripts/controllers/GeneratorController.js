
angular.module('asceticApp').controller('GeneratorCtrl', ['$scope', '$http',
  function ($scope, $http) {
      $scope.app = "";
      $scope.instance = "";
      $scope.node = "";
      $scope.metrics = "";
      $scope.min = 0;
      $scope.max = 100;
      $scope.n = 0;
      $scope.generateData = function () {

          for(i=0; i< parseInt($scope.n) ;i++) {
              var names = $scope.metrics.split(" ");
              var metrix = {};
              for(j in names) {
                metrix[names[j]] = getRandomInt(parseInt($scope.min), parseInt($scope.max));
              }
              var d = { appId: $scope.app, instanceId: $scope.instance, nodeId: $scope.node, data: metrix };
              sendData(d);
              sleep(1000);
              console.log("Sending item "+i);
          }
      }

      function sendData(data) {
        $http.post("/event",JSON.stringify(data), { headers : {"Content-Type":"application/json", "Accept":"application/json"}})
            .success(function(result) {
                console.log(result);
            }).error(function(err) {
                     console.log("error " + JSON.stringify(err));
            });
      }
  }]);
