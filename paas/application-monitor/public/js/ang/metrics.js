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
                console.log("cabronazo");
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
        console.log("3--> " + $scope.panel);

    }]);

    appip.directive("hcSeries",function() {
        return {
            restrict : 'E',
            replace : true,
            scope: true,
            controller :function ($scope, $element, $attrs) {
                console.log($scope.id);
                console.log($scope.info);
            },
            link : function($scope, $element, attrs) {
                $element.append('<div id="panel'+$scope.id+'">not working {{id}}</div>');

                    console.log("--> " + $scope.id);
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
                            animation: false
                        }
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
                console.log(chartInfo);
                var chart = new Highcharts.Chart(chartInfo);
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
