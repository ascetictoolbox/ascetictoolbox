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

	appip.controller("GraphicsPanelController", ["$modal","$scope","$interval", function($modal, $scope, $interval) {

        // kills all the $interval instances when the global page changes (this controller is closed)
        $scope.$watch('page',function() {
            for(var i in $scope.panels) {
                $interval.cancel($scope.panels[i].theInterval);
            }
        });

        $scope.panels = { };
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
                $scope.panels[$scope.idCounter] = form;
                $scope.idCounter++;
            }, function() {
            });


		};

        $scope.removePanel = function(id) {
            console.log("deleting : ");
            console.log($scope.panels[id].theInterval);
            $interval.cancel($scope.panels[id].theInterval);
            delete $scope.panels[id];
        };

	}]);

    appip.controller("NewPanelFrame", ["$scope","$modalInstance","valor", function($scope,$modalInstance,valor) {
        $scope.form = {
            appId : "SinusApp",
            nodeId : "",
            instanceId : "",
            description : "",
            metric : "metric"
        }
        $scope.valor = valor;

        $scope.ok = function() {
            $modalInstance.close($scope.form);
        }

        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        }
    }]);

    appip.controller("TimeSeriesController", ["$scope", "$interval", function($scope, $interval) {

    }]);

    var MAX_TIME = 15*60*1000;
    var STEP_TIME = 5*1000;

    appip.directive("hcSeries",function($http) {
        return {
            restrict : 'E',
            replace : true,
            scope: true,
            controller :function ($scope, $element, $attrs, $interval, $http) {
                $scope.info.theInterval = $interval(function() {
                    // update graph with new info since last query
                    console.log("tick: " + $scope.info.theInterval.$$intervalId);
                    var queryLastMetric = [{"$match":{
                        "appId":$scope.info.appId,
                        "timestamp": {"$gt" : $scope.latestTimestamp}
                    }},
                    {"$group" : {
                        "_id" : null,
                        "latestTimestamp" : { "$max" : "$timestamp" },
                        "data" : {"$avg": "$data." + $scope.info.metric}
                    }}];
                    $http.post("/query",JSON.stringify(queryLastMetric))
                        .success(function(ret) {
                            if(ret.length > 0) {
                                $scope.latestTimestamp = ret[0].latestTimestamp;
                                var series = $scope.chart.series[0];
                                series.addPoint([$scope.latestTimestamp,ret[0].data], true, true);
                            }
                        });
                },STEP_TIME); // todo: kill timer when panel is closed or page is changed
                console.log($scope.info.theInterval);
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

                // get last timestamp of an application
                var getLastTimestamp =
                    [{"$match":{"appId":$scope.info.appId}},
                        {"$group" : { "_id": null,
                            "last": {"$max": "$timestamp"}
                        }}];

                $http.post("/query",JSON.stringify(getLastTimestamp))
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

                        $http.post("/query",JSON.stringify(groupTimestamps)).
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


                /*var query = '[{"$match":{"appId":"SinusApp"}},'+
                 '{"$group" : {'+
                 '"_id" : { "$subtract" : ["$timestamp" , {"$mod" : [ "$timestamp", 100000 ] }]},'+
                 '"data" : {"$avg": "$data.metric"}'+
                 '}}]';*/


                /*scope.$watch("items", function (newValue) {
                    chart.series[0].setData(newValue, true);
                }, true);*/
            }

        });

    /*

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        $scope.highchartsNG = {
            chart: {
                animation : false
            },
            title: {
                text: 'USD to EUR exchange rate from 2006 through 2008'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Exchange rate'
                }
            },
            legend: {
                enabled: false
            },

            series: [{
                data: (function () {
                    // generate an array of random data
                    var data = [],
                        time = (new Date()).getTime(),
                        i;

                    for (i = -19; i <= 0; i += 1) {
                        data.push({
                            x: time + i * 1000,
                            y: Math.random()
                        });
                    }
                    return data;
                }())
            }]
        };

        $interval(function() {
            var x = (new Date()).getTime(), // current time
                y = Math.random();
            console.log(x + " - " , y);
            var series = $scope.highchartsNG.series[0];
            series.data.shift();
            series.data.push({"x":x,"y":y});
            //series.addPoint([x, y], true, true);
        },3000);





        /*{
            options: {
                chart: {
                    type: 'bar'
                }
            },
            series: [{
                data: [10, 15, 12, 8, 7]
            }],
            title: {
                text: 'Hello'
            },
            loading: false
        }
    }]);
*/
})();
