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

	appip.controller("GraphicsPanelController", ["$modal","$scope", function($modal, $scope) {
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

	}]);

    appip.controller("NewPanelFrame", ["$scope","$modalInstance","valor", function($scope,$modalInstance,valor) {
        $scope.form = {
            appId : "",
            nodeId : "",
            instanceId : "",
            metric : ""
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

    appip.directive("hcSeries",function() {
        return {
            restrict : 'E',
            replace : true,
            scope: true,
            controller :function ($scope, $element, $attrs, $interval) {
                $interval(function() {
                    var x = (new Date()).getTime(), // current time
                        y = Math.random();
                    var series = $scope.chart.series[0];
                    series.addPoint([x, y], true, true);
                },1000); // todo: kill timer when panel is closed or page is changed
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
                        data: (function () {
                            // generate an array of random data
                            var data = [],
                                time = (new Date()).getTime(),
                                i;

                            for (i = -100; i <= 0; i += 1) {
                                data.push({
                                    x: time + i * 1000,
                                    y: Math.random()
                                });
                            }
                            return data;
                        }())
                    }]
                };
                $scope.chart = new Highcharts.Chart(chartInfo);

                /*scope.$watch("items", function (newValue) {
                    chart.series[0].setData(newValue, true);
                }, true);*/
            }

        };
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
