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


// This directive is based on this tutorial: http://ankursethi.in/2013/07/loading-spinners-with-angularjs-and-spin-js/
(function () {

    angular
        .module('vmmanager.directives')
        .directive('loadingSpinner', LoadingSpinner);

    function LoadingSpinner() {
        return {
            restrict: 'A',
            replace: true,
            transclude: true,
            scope: {
                loading: '=loadingSpinner'
            },
            templateUrl: 'app/views/common/loadingSpinner.html',
            link: function() {
                var opts = {
                    lines: 10, // The number of lines to draw
                    length: 7, // The length of each line
                    width: 4, // The line thickness
                    radius: 12, // The radius of the inner circle
                    corners: 1, // Corner roundness (0..1)
                    rotate: 0, // The rotation offset
                    direction: 1, // 1: clockwise, -1: counterclockwise
                    color: '#000', // #rgb or #rrggbb or array of colors
                    speed: 1.5, // Rounds per second
                    trail: 60, // Afterglow percentage
                    shadow: false, // Whether to render a shadow
                    hwaccel: false, // Whether to use hardware acceleration
                    className: 'spinner', // The CSS class to assign to the spinner
                    zIndex: 2e9, // The z-index (defaults to 2000000000)
                    top: '82%', // Top position relative to parent
                    left: '50%' // Left position relative to parent
                };
                var target = document.getElementById('my-loading-spinner-container');
                var spinner = new Spinner(opts).spin(target);
            }
        };
    }

})();

