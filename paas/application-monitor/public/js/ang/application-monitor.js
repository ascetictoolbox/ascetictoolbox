(function() {

	var appm = angular.module('ApplicationMonitor',["Dashboard","GlobalView","Dashboard"]);

	appm.controller('PageController',  function() {
		this.page = 'global.html';
		console.log("quepasaColega");
		this.setPage = function(newPage) {
			this.page = newPage;
		};
	});
})();

