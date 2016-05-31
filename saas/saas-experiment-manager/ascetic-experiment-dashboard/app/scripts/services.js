

var sbkBasePath = "http://localhost:8080"

angular.module('asceticApp')
  .service('SkbAPI', function($http){
    var self = new Object();

    function query(url,callback){
      console.log("getting "+sbkBasePath+url+"...")
      $http.get(sbkBasePath+url).success(callback).error(function(){
        console.log("error during get on "+url);
      });
    }

    self.snapshots = function(expId,callback){
      query("/snapshots?expId="+expId,callback);
    }

    return self;
  });

angular.module('asceticApp')
  .factory('PaasAPI', function($resource,AppManagerHost,AppMonitorHost) {
    var self = new Object;

    self.applications = $resource(AppManagerHost + "/applications/",{},{
  	  query: {method: 'GET', isArray:false},
      headers : {"Content-Type":"text/plain", "Accept":"application/json"}
    });

    self.deployments = $resource(AppManagerHost + "/applications/:appId/deployments/", { appId :"0"});

    self.events = $resource(AppMonitorHost + "/query", {
          find: {
              method: 'POST',
              headers : {"Content-Type":"text/plain", "Accept":"application/json"}
          }
      });

    self.vms = $resource(AppManagerHost + "/applications/:appId/deployments/:deploymentId/vms/", { appId :"0", deploymentId : "0"});

    return self;
  })
  ;
