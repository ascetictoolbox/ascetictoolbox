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
        .controller('SidebarCtrl', SidebarCtrl);

    /* @ngInject */
    function SidebarCtrl($location, $rootScope) {

        var sidebarCtrl = this;
        sidebarCtrl.sectionIsActive = sectionIsActive;

        activate();

        function activate() {
            if (!sidebarCtrl.section) {
                sidebarCtrl.section = $location.url().substr(1);
            }
        }

        function sectionIsActive(section) {
            return sidebarCtrl.section === section;
        }

        // Watches for a change in the URL to change the active section in the sidebar accordingly
        $rootScope.$on('$locationChangeStart', function(event, next) {
            var section = next.split("#/")[1];
            if (!section || section === '') {
                section = 'dashboard';
            }
            if (section === 'hosts/') {
                section = 'hosts';
            }
            if (section.indexOf('zabbix') === 0) {
                section = 'zabbix';
            }
            if (section === 'virtual_machines') {
                section = 'vms';
            }
            sidebarCtrl.section = section;
        });

    }
    SidebarCtrl.$inject = ['$location', '$rootScope'];

})();