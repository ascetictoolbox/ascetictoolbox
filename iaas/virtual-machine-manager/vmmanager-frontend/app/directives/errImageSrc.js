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
        .module('vmmanager.directives')
        .directive('errImageSrc', ErrImageSrc);

    /**
     * This directive is used to check whether a Zabbix graph could be loaded.
     * If the user is not logged in Zabbix, a toast with an error is shown and the img element is
     * removed. This way, the image of a broken link is not shown.
     */
    function ErrImageSrc() {
        return {
            link: function (scope, element) {
                element.bind('error', function () {
                    toastr.error('You need to be logged in Zabbix to see the graphs.');
                    element.remove();
                });
            }
        }
    }

})();
