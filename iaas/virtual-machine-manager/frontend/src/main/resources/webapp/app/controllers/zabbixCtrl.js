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

// TODO: remove this. To rely in zabbix gui
// or, otherwise, to rely in an intermediate layer that connects between different
// monitoring solutinos
(function () {

    angular
        .module('vmmanager.controllers')
        .controller('ZabbixCtrl', ZabbixCtrl);

    /* @ngInject */
    function ZabbixCtrl($route) {
        var zabbixCtrl = this;

        // These arrays contains the hosts for each of the clusters that we use and their Zabbix IDs.
        // It is not a good solution to have these values hardcoded here, but it will be enough for now.
        var y1Hosts = {
            'Asok09' : 608,
            'Asok10' : 611,
            'Asok11' : 612,
            'Asok12' : 613
        };
        var y2TestingHosts = {
            'wally152' : 10140,
            'wally153' : 10141,
            'wally154' : 10142
        };
        var y2StableHosts = {
            'wally155' : 10143,
            'wally157' : 10112,
            'wally158' : 10113,
            'wally159' : 10114,
            'wally160' : 10115,
            'wally161' : 10116,
            'wally162' : 10117,
            'wally163' : 10118,
            'wally164' : 10119,
            'wally165' : 10120,
            'wally166' : 10121,
            'wally167' : 10122,
            'wally168' : 10123,
            'wally169' : 10124,
            'wally170' : 10125,
            'wally171' : 10126,
            'wally172' : 10127,
            'wally173' : 10128,
            'wally174' : 10129,
            'wally175' : 10130,
            'wally176' : 10131,
            'wally177' : 10132,
            'wally178' : 10133,
            'wally179' : 10134,
            'wally180' : 10135,
            'wally181' : 10136,
            'wally182' : 10137,
            'wally193' : 10111,
            'wally195' : 10110,
            'wally196' : 10109,
            'wally197' : 10108,
            'wally198' : 10107
        };
        var zabbixIp = '192.168.3.199'; // This should not be hardcoded...

        zabbixCtrl.hosts = Object.keys(y2TestingHosts); // This should not be hardcoded...
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

        // TODO: it seems that for y2 this URL does not longer work because there is not a chart with power vs cpu
        // usage configured in Zabbix. chart2.php should be changed to the new one once it is configured.
        function getGraphLink(hostname, width, height, seconds) {
            return 'http://' + zabbixIp + '/zabbix/chart2.php?graphid=' + getZabbixId(hostname).toString() +
                '&width=' + width.toString() + '&height=' + height.toString() + '&period=' + seconds.toString();
        }

        /**
         * This function returns the ID of the graph for the specified host.
         * This is specific for Ascetic's case.
         */
        function getZabbixId(hostname) {
            if (y1Hosts[hostname]) {
                return y1Hosts[hostname];
            }
            if (y2TestingHosts[hostname]) {
                return y2TestingHosts[hostname];
            }
            return y2StableHosts[hostname];
        }
    }
    ZabbixCtrl.$inject = ['$route'];

})();