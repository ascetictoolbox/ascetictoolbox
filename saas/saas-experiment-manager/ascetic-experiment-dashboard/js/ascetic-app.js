/*
    Main Routing module
*/
(function(){

var asceticApp = angular.module('asceticApp', [
  'ngRoute',
  'ngResource',
  'asceticControllers'
]);

asceticApp.config(['$routeProvider',
  function($routeProvider) {
      
    $routeProvider.
      when('/dashboard', {
        templateUrl: 'partials/dashboard.html',
        controller: 'DashboardCtrl'
      }).
      when('/metrics', {
        templateUrl: 'partials/metrics.html',
        controller: 'MetricsCtrl'
      }).
      when('/kpis', {
        templateUrl: 'partials/kpis.html',
        controller: 'KPIsCtrl'
      }).
      when('/generator', {
        templateUrl: 'partials/generator.html',
        controller: 'GeneratorCtrl'
      }).
      otherwise({
        redirectTo: '/kpis'
      });
  }]);
})();