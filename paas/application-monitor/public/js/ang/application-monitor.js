(function() {

	var appm = angular.module('ApplicationMonitor',["Metrics","GlobalView"]);

	appm.controller('PageController',  function() {
		this.page = 'global.html';
		this.setPage = function(newPage) {
			this.page = newPage;
		};
	});
})();

