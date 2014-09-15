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

'use strict';

var vmmanager = angular.module('vmmanager', ['vmmanager.controllers', 'ngRoute', 'ngAnimate']);

vmmanager.config(function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl : 'views/dashboard.html'
        })
        .when('/virtual_machines', {
            templateUrl: 'views/virtualMachines.html'
        })
        .when('/images', {
           templateUrl: 'views/images.html'
        })
        .when('/scheduling_algorithms', {
            templateUrl: 'views/schedulingAlgorithms.html'
        })
        .when('/hosts', {
            templateUrl: 'views/hosts.html'
        })
        .when('/logs', {
            templateUrl: 'views/logs.html'
        });
});

// TODO: This should be in the logs controller
vmmanager.filter('formatLogMessage', function() {
    return function(logMessage) {
        return logMessage.split("--id:")[0]; // Hide the ID associated with the deployment
    }
});