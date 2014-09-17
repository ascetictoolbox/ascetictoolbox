(function() {
	var appip = angular.module("Metrics",["ui.bootstrap"]);

	appip.time = 0;

	appip.controller("GraphicsPanelController", ["$modal","$scope", function($modal, $scope) {
        $scope.panels = {"0":{"title":"Test Panel"}};
        $scope.idCounter = 1;

        $scope.addNewPanel = function() {
            console.log("Ke pasa tron");

            var modalInstance = $modal.open({
                templateUrl: 'newPanelForm.html',
                controller : "NewPanelFrame",
                resolve : {
                    valor : function() {return "Un valor aqui";}
                }
            });

            modalInstance.result.then(function(form) {
                console.log("vete a la mierda");
                console.log(form);
            }, function() {
                console.log("cabronazo");
            });

            $scope.panels[this.idCounter] = {"title":"hola tron " + this.idCounter};
            $scope.idCounter++;
		};

	}]);

    appip.controller("NewPanelFrame", ["$scope","$modalInstance","valor", function($scope,$modalInstance,valor) {
        $scope.form = {
            appId : "",
            nodeId : "",
            instanceId : "",
            metric : ""
        }
        $scope.valor = valor;

        $scope.ok = function() {
            $modalInstance.close($scope.form);
        }

        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        }
    }]);

})();
