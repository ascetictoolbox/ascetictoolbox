/*
 * Author: Mario Macias (Barcelona Supercomputing Center). 2014
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 */

(function() {
	var appip = angular.module("Metrics",["ui.bootstrap"]);

	appip.time = 0;

	appip.controller("GraphicsPanelController", ["$modal","$scope","$interval","$http", function($modal, $scope, $interval,$http) {


        // kills all the $interval instances when the global page changes (this controller is closed)
        $scope.$watch('page',function() {
            for(var i in $scope.panels) {
                $interval.cancel($scope.panels[i].theInterval);
            }
        });

        $scope.panels = { };

        $http.get("/gui/metricPanel").success(function(recvPanels) {
            for(var i in recvPanels) {
                var p = recvPanels[i];
                $scope.panels[p["_id"].$oid] = p;
            }
        });

        $scope.idCounter = 0;

        $scope.addNewPanel = function() {
            var modalInstance = $modal.open({
                templateUrl: 'newPanelForm.html',
                controller : "NewPanelFrame",
                resolve : {
                    valor : function() {return "Un valor aqui";}
                }
            });

            modalInstance.result.then(function(form) {
                $http.post("/gui/metricPanel",JSON.stringify(form), { headers : {"Content-Type":"application/json", "Accept":"application/json"}})
                    .success(function(panelId) {
                        $scope.panels[panelId] = form;
                    });

            }, function() {
            });

		};

        $scope.removePanel = function(id) {
            console.log("deleting : " + id);
            $http.delete("/gui/metricPanel/"+id)
                .success(function() {
                    console.log($scope.panels[id].theInterval);
                    $interval.cancel($scope.panels[id].theInterval);
                    delete $scope.panels[id];
                });
        };

	}]);

    appip.controller("NewPanelFrame", ["$http", "$scope","$modalInstance","valor", function($http,$scope,$modalInstance,valor) {
        $scope.form = {
            appId : "",
            nodeId : "",
            deploymentId : "",
            description : "",
            metric : ""
        }
        $scope.valor = valor;

        $scope.ok = function() {
            $modalInstance.close($scope.form);
        }

        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        }
        $scope.appsDeployments = [];
        $http.get("/deployments").then(function (response) {
                $scope.appsDeployments = response.data;
            });
        $scope.getApps = function() {

            var ret = [];
            for(var a in $scope.appsDeployments) {
                ret.push(a);
            }
            return ret;
        }
        $scope.getDeployments = function() {
            var ret = $scope.appsDeployments[$scope.form.appId];
            if(ret) {
                return ret;
            } else {
                return [];
            }
        }
        $scope.metrics = [];
        $scope.onAppSelected = function() {
            console.log("Seleccionado: " + $scope.form.appId);
            var getLastMetric =
                [{"$match":{"appId":$scope.form.appId}},
                    {"$sort":{ "timestamp":1}},
                    {"$group" : { "_id": null,
                        "data": {"$last": "$data"}
                    }}];
            $http.post("/query",JSON.stringify(getLastMetric), { headers : {"Content-Type":"application/json", "Accept":"application/json"}})
                .success(function(lastMetric) {
                    console.log("successs!!!");

                    // show only metrics that parse
                    $scope.metrics = getAllMetrics(lastMetric[0].data);
                }).error(function(err) {
                    console.log("error " + JSON.stringify(err));
                });
        }

    }]);

    function getAllMetrics(metrics) {
        var paths = [];
        for(var m in metrics) {
            if(metrics[m] instanceof Object) {
                var ppaths = getAllMetrics(metrics[m]);
                if(ppaths) for(var p in ppaths) {
                    paths.push(m + "." + ppaths[p]);
                }
            } else if(typeof metrics[m] == "number") { // only adds properties that parse into a number
                paths.push(m);
            }
        }
        return paths;
    }


    appip.controller("TimeSeriesController", ["$scope", "$interval", function($scope, $interval) {

    }]);

    var MAX_TIME = 45*60*1000;
    var STEP_TIME = 5*1000;

    appip.directive("hcSeries",function($http) {
        return {
            restrict : 'E',
            replace : true,
            scope: true,
            controller :function ($scope, $element, $attrs, $interval, $http) {
                $scope.info.theInterval = $interval(function() {
                    // update graph with new info since last query
                    var queryLastMetric = [{"$match":{
                        "appId":$scope.info.appId,
                        "timestamp": {"$gt" : $scope.latestTimestamp}
                    }},
                    {"$group" : {
                        "_id" : null,
                        "latestTimestamp" : { "$max" : "$timestamp" },
                        "data" : {"$avg": "$data." + $scope.info.metric}
                    }}];
                    $http.post("/query",JSON.stringify(queryLastMetric), { headers : {"Content-Type":"application/json", "Accept":"application/json"}})
                        .success(function(ret) {
                            if(ret.length > 0) {
                                $scope.latestTimestamp = ret[0].latestTimestamp;
                                var series = $scope.chart.series[0];
                                series.addPoint([$scope.latestTimestamp,ret[0].data], true, true);
                            }
                        });
                },STEP_TIME); // todo: kill timer when panel is closed or page is changed
            },
            link : function($scope, $element, $attrs, $interval) {

                $element.append('<div id="panel'+$scope.id+'">not working {{id}}</div>');

                var mierdaca = $scope.id;
                var chartInfo = {
                    chart: {
                        renderTo : 'panel'+$scope.id,
                        animation : false
                    },
                    title: {
                        text: $scope.info.description
                    },
                    xAxis: {
                        type: 'datetime'
                    },
                    yAxis: {
                        title: {
                            text: $scope.info.metric
                        }
                    },
                    legend: {
                        enabled: false
                    },
                    plotOptions: {

                        series: {
                            animation: false,
                            marker : {
                                enabled: false
                            }

                        }
                    },

                    series: [{
                        type: 'area',
                        data: null // cargar aquí dinámicamente toda esta mierda
                    }]
                };

                // get last timestamp of an application∫∫∫
                var getLastTimestamp =
                    [{"$match":{"appId":$scope.info.appId}},
                        {"$group" : { "_id": null,
                            "last": {"$max": "$timestamp"}
                        }}];

                $http.post("/query",JSON.stringify(getLastTimestamp), { headers : {"Content-Type":"application/json", "Accept":"application/json"}})
                    .success(function(ret) {
                        // get all metrics in the last MAX_TIME aggregated in STEP_TIME intervals
                        $scope.latestTimestamp = ret[0].last;
                        var groupTimestamps =
                            [{"$match":{
                                "appId": $scope.info.appId,
                                "timestamp": {"$gt" : $scope.latestTimestamp - MAX_TIME}
                            }},
                            {"$group" : {
                                "_id" : { "$subtract" : ["$timestamp" , {"$mod" : [ "$timestamp", STEP_TIME ] }]},
                                "data" : {"$avg": "$data." + $scope.info.metric}
                            }}];

                        $http.post("/query",JSON.stringify(groupTimestamps), { headers : {"Content-Type":"application/json", "Accept":"application/json"}}).
                            success(function(ret) {
                                var minDataSize = MAX_TIME / STEP_TIME;
                                var data = [],
                                    time = 0,
                                    i;

                                // metrics will be inserted sorted
                                for( i in ret ) {
                                    var ipos = 0;
                                    while(ipos < data.length && ret[i]._id > data[ipos].x) {
                                        ipos++;
                                    }
                                    data.splice(ipos,0,{
                                                            x: ret[i]._id,  //time
                                                            y: ret[i].data
                                                        });
                                }
                                // fill the rest of gaps if the received data does not fill the MAX_TIME window
                                for(i = 0 ; i < minDataSize - ret.length ; i++) {
                                    data.splice(0,0, { x: (data[0].x-STEP_TIME) , y:0 });
                                }
                                chartInfo.series[0].data = data;
                                $scope.chart = new Highcharts.Chart(chartInfo);
                            });
                        });

                    }

            }
        });

})();
