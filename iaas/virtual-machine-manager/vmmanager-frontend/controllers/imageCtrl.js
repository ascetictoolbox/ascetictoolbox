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

angular.module('vmmanager.controllers').controller('ImageCtrl', [ '$http', '$scope', function($http, $scope) {
    
    var imageCtrl = this;
        
    // Gets the information about the images in the system
    $scope.loadImages = function() {
        $http({method: 'GET', url: base_url + "images"}).
            success(function(data) {
                imageCtrl.images = data.images;
            })
    };

    $scope.deleteImage = function(imageId) {
        $http({method: 'DELETE', url: base_url + "images/" + imageId}).
            success(function() {
                $scope.loadImages(); // Reload the data
            })
    };

    $scope.uploadImage = function(imageName, imageUrl) {
        var dataNewImage = {
                name: imageName,
                url: imageUrl
            };

        $http({method: 'POST', url: base_url + "images/", data: dataNewImage}).
            success(function() {
                $scope.refresh();
                $('#imageModal').modal('hide'); // TODO: This should be done using a directive
            })
        };

    $scope.refresh = function() {
        $scope.loadImages();
    };

    imageCtrl.imageAttributes = ["Name", "ID", "State", "Actions"];
    imageCtrl.images = [];

    $scope.loadImages();

}]);