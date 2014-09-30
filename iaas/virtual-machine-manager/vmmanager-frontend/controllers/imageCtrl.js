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

angular
    .module('vmmanager.controllers')
    .controller('ImageCtrl', [ 'ImageService', ImageCtrl ]);

function ImageCtrl(ImageService) {
    var imageCtrl = this;

    imageCtrl.loadImages = function() {
        ImageService
            .getImages()
            .then(function(response) {
                imageCtrl.images = response["data"]["images"];
            });
    };

    imageCtrl.deleteImage = function(imageId) {
        ImageService
            .deleteImage(imageId)
            .then(function() {
                imageCtrl.loadImages();
            });
    };

    imageCtrl.uploadImage = function(imageName, imageUrl) {
        ImageService
            .uploadImage(imageName, imageUrl)
            .then(function() {
                imageCtrl.refresh();
                $('#imageModal').modal('hide'); // TODO: This should be done using a directive
            });
    };

    imageCtrl.changeColumnSort = function(criteriaIndex, reverse) {
        imageCtrl.columnSort = { criteria: imageCtrl.sortingCriteria[criteriaIndex], reverse: reverse };
    };

    imageCtrl.refresh = function() {
        imageCtrl.loadImages();
    };

    imageCtrl.imageAttributes = ["Name", "ID", "State", "Actions"];
    imageCtrl.images = [];

    // Table sorting
    imageCtrl.sortingCriteria = ["name", "id", "status"];
    imageCtrl.columnSort = { criteria:imageCtrl.sortingCriteria[0], reverse:false };

    imageCtrl.loadImages();
}