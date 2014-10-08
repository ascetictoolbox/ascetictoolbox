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
    function SidebarCtrl($location) {

        var sidebarCtrl = this;
        sidebarCtrl.section = $location.url().substr(1);
        sidebarCtrl.sectionIsActive = sectionIsActive;
        sidebarCtrl.setSection = setSection;

        activate();

        function activate() {
            if (!sidebarCtrl.section) { // If we are on the root, we set the section to 'dashboard'
                sidebarCtrl.section = 'dashboard';
            }
        }

        function sectionIsActive(section) {
            return sidebarCtrl.section === section;
        }

        function setSection (section) {
            sidebarCtrl.section = section;
        }

    }
    SidebarCtrl.$inject = ['$location'];

})();