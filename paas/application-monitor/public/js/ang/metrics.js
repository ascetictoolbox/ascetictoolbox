(function() {
	var appip = angular.module("Metrics",["ui.bootstrap"]);

	appip.time = 0;

	appip.controller("GraphicsPanelController", ["$modal","$scope", function($modal,$scope) {
		this.panels = {"0":{"title":"Test Panel"}};
		this.idCounter = 1;

		this.addNewPanel = function() {
            $modal.open({
                templateUrl: 'newPanelForm.html',
                resolve : {
                    something : "something here"
                }
            });

			this.panels[this.idCounter] = {"title":"hola tron " + this.idCounter};
			this.idCounter++;
		};

		this.removePanel = function(panelId) {
			delete this.panels[panelId];
		};
	}]);

})();
