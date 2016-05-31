asceticControllers.controller('MetricsCtrl', ['$scope', '$http',
  function ($scope, $http) {
      $scope.selectedApp = "";
      $scope.selectedNode = "";
      $scope.accessors = [];
      $scope.allTheData = [];
      $scope.lines = [];
      $scope.lineNumber = 0;
      $scope.lineColors = ["#AC1014", "#FDD526","#73C509","#0980BA","#65286B"];
      $http.get("/apps").success(function(data) {
        $scope.apps = [];
        for (var prop in data) {
          // important check that this is objects own property
          // not from prototype prop inherited
          if(data.hasOwnProperty(prop)){
             $scope.apps.push({name: prop, nodes:data[prop]});
          }
       }
    });

    $scope.onAppSelected = function() {
      console.log($scope.selectedApp);
    };

    $scope.onNodeSelected = function() {
             var getLastMetric =
                 [{"$match":{"appId":$scope.selectedApp.name}},
                     {"$sort":{ "timestamp":1}},
                     {"$group" : { "_id": null,
                         "data": {"$last": "$data"}
                     }}];
             $http.post("/query",JSON.stringify(getLastMetric), { headers : {"Content-Type":"application/json", "Accept":"application/json"}})
                 .success(function(lastMetric) {
                     console.log(lastMetric);
                     // show only metrics that parse
                     $scope.metrics = getAllMetrics(lastMetric[0].data);
                 }).error(function(err) {
                     console.log("error " + JSON.stringify(err));
                 });
         }


    function mergeData(data, attributes) {
         var x = $scope.lineNumber;
         $scope.lineNumber+=attributes.length ;

         var limit = data.length;
         for(var i=0; i < limit; i++) {
            if(i >= $scope.allTheData.length) {
                    $scope.allTheData.push({ time: data[i].timestamp });
                    for(var j = 0; j < $scope.accessors.length; j++) {
                        $scope.allTheData[i][$scope.accessors[j]] = null;
                    }
            }
            for(var a = 0; a < attributes.length; a++) {
                $scope.allTheData[i][(x+a)+"-"+attributes[a]] = data[i]['data'][attributes[a]];
            }

         }
         for(var a = 0; a < attributes.length; a++) {
                $scope.accessors.push((x+a)+"-"+attributes[a]);
         }

    }

    $scope.removeLine = function(lineIndex) {
        var newData = [];

        for(d in $scope.allTheData) {
            delete $scope.allTheData[d][$scope.accessors[lineIndex]];
        }
        $scope.accessors.splice(lineIndex,1);
        $scope.lines.splice(lineIndex,1);
        console.log($scope.accessors);
        $("#morris-chart").empty();
        Morris.Line({
                element: 'morris-chart',
                data: $scope.allTheData,
                continuousLine: true,
                lineColors: $scope.lineColors,
                xkey: 'time',
                ykeys: $scope.accessors,
                labels: $scope.accessors,
                pointSize: 2,
                hideHover: 'auto',
                resize: true
        });
    }

    $scope.addLine = function() {
            var getAllMeasuresForMetric =
                 [{"$match":{"appId":$scope.selectedApp.name, "nodeId": $scope.selectedNode }},
                     {"$sort":{ "timestamp":1}}
                     ];
             $http.post("/query",JSON.stringify(getAllMeasuresForMetric), { headers : {"Content-Type":"application/json", "Accept":"application/json"}})
                 .success(function(measures) {
                    var data = [];
                    for(m in measures) {
                        var v = measures[m].data[$scope.selectedMetric] ;
                        var ts = measures[m].timestamp ;
                        if(v) {
                            var x = {'timestamp': ts };
                            x[$scope.selectedMetric] = v ;
                            data.push(v);
                        }
                    }
                    console.log(measures);
                    mergeData(measures,[$scope.selectedMetric]);
                    $scope.lines.push({name: $scope.selectedMetric, nodeId:$scope.selectedNode })
                    $scope.measures = data ;
                    $("#morris-chart").empty();
                    console.log($scope.allTheData);
                    Morris.Line({
                        element: 'morris-chart',
                        data: $scope.allTheData,
                        continuousLine: true,
                        lineColors: $scope.lineColors,
                        xkey: 'time',
                        ykeys: $scope.accessors,
                        labels: $scope.accessors,
                        pointSize: 2,
                        hideHover: 'auto',
                        resize: true
                  });
                 }).error(function(err) {
                     console.log("error " + JSON.stringify(err));
                 });


    }}]);
