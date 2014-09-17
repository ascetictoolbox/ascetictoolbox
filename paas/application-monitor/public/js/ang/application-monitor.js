(function() {

	var appm = angular.module('ApplicationMonitor',["Metrics","GlobalView"]);

	appm.controller('PageController',  function() {
		this.page = 'metrics.html';
		this.setPage = function(newPage) {
			this.page = newPage;
		};
	});
})();

