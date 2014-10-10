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
        .module('vmmanager.controllers')
        .controller('ZabbixCtrl', ZabbixCtrl);

    /* @ngInject */
    function ZabbixCtrl($route) {
        var zabbixCtrl = this;
        zabbixCtrl.hosts = ['Asok09', 'Asok10', 'Asok11', 'Asok12']; // TODO: this should not be hardcoded
        zabbixCtrl.tab = zabbixCtrl.hosts[0];

        zabbixCtrl.tabIsActive = tabIsActive;
        zabbixCtrl.getGraphLink = getGraphLink;

        activate();

        function activate() {
            setInitialTab();
        }

        function setInitialTab() {
            if ($route.current.params.host) {
                var indexHostSelected = zabbixCtrl.hosts.indexOf($route.current.params.host);
                if (indexHostSelected === -1) {
                    indexHostSelected = 0;
                }
                zabbixCtrl.tab = zabbixCtrl.hosts[indexHostSelected];
            }
        }

        function tabIsActive(tab) {
            return zabbixCtrl.tab === tab;
        }

        function getGraphLink(hostname, width, height, seconds) {
            return 'http://10.4.0.15/zabbix/chart2.php?graphid=' + getZabbixId(hostname).toString() +
                    '&width=' + width.toString() + '&height=' + height.toString() + '&period=' + seconds.toString();
        }

        /**
         * This function returns the ID of the graph for the specified host.
         * This is specific for Ascetic's case.
         */
        function getZabbixId(hostname) {
            switch(hostname) {
                case 'Asok09':
                    return 608;
                case 'Asok10':
                    return 611;
                case 'Asok11':
                    return 612;
                case 'Asok12':
                    return 613;
                default:
                    return null;
            }
        }
    }
    ZabbixCtrl.$inject = ['$route'];

})();