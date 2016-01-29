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

(function () {

    angular
        .module('vmmanager', [
            // Custom modules
            'vmmanager.controllers',
            'vmmanager.services',
            'vmmanager.filters',
            'vmmanager.directives',
            // Angular modules
            'ngRoute'])
        .config(configRoutes);

    function configRoutes($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl : 'app/views/dashboard.html',
                controller: 'ChartsCtrl',
                controllerAs: 'charts'
            })
            .when('/virtual_machines', {
                templateUrl: 'app/views/virtualMachines.html',
                controller: 'VmManagerController',
                controllerAs: 'vms'
            })
            .when('/images', {
                templateUrl: 'app/views/images.html',
                controller: 'ImageCtrl',
                controllerAs: 'images'
            })
            .when('/scheduling_algorithms', {
                templateUrl: 'app/views/schedulingAlgorithms.html',
                controller: 'SchedulingAlgCtrl',
                controllerAs: 'schedulingAlgorithms'
            })
            .when('/self_adaptation', {
                templateUrl: 'app/views/selfAdaptation.html',
                controller: 'SelfAdaptationCtrl',
                controllerAs: 'selfAdaptation'
            })
            .when('/hosts/', {
                templateUrl: 'app/views/hosts.html',
                controller: 'HostCtrl',
                controllerAs: 'hosts'
            })
            .when('/logs', {
                templateUrl: 'app/views/logs.html',
                controller: 'LogsCtrl',
                controllerAs: 'logs'
            })
            .when('/zabbix', {
                templateUrl: 'app/views/zabbix.html',
                controller: 'ZabbixCtrl',
                controllerAs: 'zabbix'
            })
            .when('/zabbix/:host', {
                templateUrl: 'app/views/zabbix.html',
                controller: 'ZabbixCtrl',
                controllerAs: 'zabbix'
            })
            .otherwise({
                redirectTo: '/'
            });
    }

})();