angular.module('asceticApplicationManagerPortalApp', ['ngResource'])
  .controller('MainCtrl', ['$scope','ApplicationService', function($scope, ApplicationService) {
    var self = this;

    var response = ApplicationService.query();
    
    response.$promise.then(function(data){
      $scope.applications = data.items.application; //Changed data.data.topics to data.topics
    });

    console.log(response);
  
  }])
  .factory('ApplicationService', ['$resource', function ($resource) {
    return $resource('http://localhost:8080/application-manager/applications/:application',{application: "@name"},  {
      query: {
        isArray: false,
        method: 'GET',
        headers: {'Accept': 'application/json'}
      }
    });
  }]);