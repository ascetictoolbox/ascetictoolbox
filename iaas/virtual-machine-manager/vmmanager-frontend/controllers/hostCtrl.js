/*
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

angular.module('vmmanager.controllers').controller('HostCtrl', [ '$http', '$scope', function($http, $scope) {

    var hostCtrl = this;

    $scope.loadHosts = function() {
        $http({method: 'GET', url: base_url + "nodes"})
            .success(function(data) {
                hostCtrl.hosts = data["nodes"];
                $scope.calculateTotalPowerConsumption();
                $scope.drawPowerChart();
                $scope.drawServersUsageChart();
            });
    };

    $scope.calculateTotalPowerConsumption = function() {
        var totalPowerConsumption = 0;
        hostCtrl.hosts.forEach(function(host) {
            totalPowerConsumption += host["currentPower"];
        });
        hostCtrl.totalPowerConsumption = totalPowerConsumption;
    };

    $scope.refresh = function() {
        $scope.loadHosts();
        $scope.calculateTotalPowerConsumption();
    };

    // This function draws a pie chart using the Highcharts library
    $scope.drawPowerChart = function() {
        var powerData = [];
        hostCtrl.hosts.forEach(function(host) {
            powerData.push([host["hostname"], host["currentPower"]]);
        });

        var chart = new Highcharts.Chart({
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: 1,//null,
                plotShadow: false,
                renderTo: 'power-chart'
            },
            title: {
                text: 'Cluster Consumption: ' + hostCtrl.totalPowerConsumption.toFixed(2) + ' W'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                        style: {
                            color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                        }
                    }
                }
            },
            series: [{
                type: 'pie',
                name: 'Power Consumption',
                data: powerData
            }]
        });
    };

    // This function draws a 3d scatterplot using the Highcharts library
    $scope.drawServersUsageChart = function() {
        var usageData = [];
        hostCtrl.hosts.forEach(function(host) {
            usageData.push([Math.min(100, 100*(host["assignedCpus"]/host["totalCpus"])),
                Math.min(100,100*(host["assignedMemoryMb"]/host["totalMemoryMb"])),
                Math.min(100,100*(host["assignedDiskGb"]/host["totalDiskGb"]))]);
        });

        // Set up the chart
        var chart = new Highcharts.Chart({
            chart: {
                renderTo: 'servers-usage',
                margin: 100,
                type: 'scatter',
                options3d: {
                    enabled: true,
                    alpha: 10,
                    beta: 30,
                    depth: 250,
                    viewDistance: 5,

                    frame: {
                        bottom: { size: 1, color: 'rgba(0,0,0,0.02)' },
                        back: { size: 1, color: 'rgba(0,0,0,0.04)' },
                        side: { size: 1, color: 'rgba(0,0,0,0.06)' }
                    }
                }
            },
            title: {
                text: 'Servers usage'
            },
            subtitle: {
                text: 'x axis = cpu usage (%), y axis = ram usage (%), z = disk usage(%)'
            },
            plotOptions: {
                scatter: {
                    width: 100,
                    height: 100,
                    depth: 100
                }
            },
            yAxis: {
                min: 0,
                max: 100,
                title: {
                    text: "RAM"
                }
            },
            xAxis: {
                min: 0,
                max: 100,
                gridLineWidth: 1,
                title: {
                    text: "CPU"
                }
            },
            zAxis: {
                min: 0,
                max: 100,
                title: {
                    text: "Disk"
                }
            },
            legend: {
                enabled: false
            },
            series: [{
                name: 'Usage',
                colorByPoint: true,
                data: usageData
            }]
        });


        // Add mouse events for rotation
        $(chart.container).bind('mousedown.hc touchstart.hc', function (e) {
            e = chart.pointer.normalize(e);

            var posX = e.pageX,
                posY = e.pageY,
                alpha = chart.options.chart.options3d.alpha,
                beta = chart.options.chart.options3d.beta,
                newAlpha,
                newBeta,
                sensitivity = 5; // lower is more sensitive

            $(document).bind({
                'mousemove.hc touchdrag.hc': function (e) {
                    // Run beta
                    newBeta = beta + (posX - e.pageX) / sensitivity;
                    newBeta = Math.min(100, Math.max(-100, newBeta));
                    chart.options.chart.options3d.beta = newBeta;

                    // Run alpha
                    newAlpha = alpha + (e.pageY - posY) / sensitivity;
                    newAlpha = Math.min(100, Math.max(-100, newAlpha));
                    chart.options.chart.options3d.alpha = newAlpha;

                    chart.redraw(false);
                },
                'mouseup touchend': function () {
                    $(document).unbind('.hc');
                }
            });
        });
    };

    hostCtrl.hostAttributes = ["Host", "CPUs", "CPUs used (%)", "RAM(MB)", "RAM used (%)",
        "Disk(GB)", "Disk used (%)", "Current Power (W)"];
    hostCtrl.hosts = [];
    hostCtrl.totalPowerConsumption = 0;

    $scope.loadHosts();

    $( "#power-chart" ).empty();

}]);