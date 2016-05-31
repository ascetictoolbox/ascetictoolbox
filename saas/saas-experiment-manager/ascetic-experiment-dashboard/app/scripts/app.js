/*
    Main Routing module
*/
(function(){

var asceticApp = angular.module('asceticApp', [
  'ngRoute',
  'ngResource',
  'asceticControllers',
  'xml'
]);

asceticApp
  .config(function ($httpProvider) {
    $httpProvider.interceptors.push('xmlHttpInterceptor');
  });

asceticApp.config(['$routeProvider',
  function($routeProvider) {

    $routeProvider.
      when('/dashboard', {
        templateUrl: 'views/partials/dashboard.html',
        controller: 'DashboardCtrl'
      }).
      when('/metrics', {
        templateUrl: 'views/partials/metrics.html',
        controller: 'MetricsCtrl'
      }).
      when('/kpis', {
        templateUrl: 'views/partials/kpis.html',
        controller: 'KPIsCtrl'
      }).
      when('/generator', {
        templateUrl: 'views/partials/generator.html',
        controller: 'GeneratorCtrl'
      }).
      otherwise({
        redirectTo: '/dashboard'
      });
  }]);
})();
