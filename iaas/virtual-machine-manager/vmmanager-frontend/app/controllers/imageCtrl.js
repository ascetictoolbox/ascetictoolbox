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
        .controller('ImageCtrl', ImageCtrl);

    /* @ngInject */
    function ImageCtrl(ImageService, $scope) {
        var imageCtrl = this;
        $scope.loading = true;
        imageCtrl.imageAttributes = ['Name', 'ID', 'State', 'Actions'];
        imageCtrl.images = [];
        imageCtrl.sortingCriteria = ['name', 'id', 'status'];
        imageCtrl.columnSort = { criteria:imageCtrl.sortingCriteria[0], reverse:false };

        imageCtrl.loadImages = loadImages;
        imageCtrl.deleteImage = deleteImage;
        imageCtrl.uploadImage = uploadImage;
        imageCtrl.changeColumnSort = changeColumnSort;

        activate();

        function activate() {
            imageCtrl.loadImages();
        }

        function loadImages() {
            ImageService
                .getImages()
                .then(
                    function(response) {
                        imageCtrl.images = response.data.images;
                        toastr.success('List of images loaded.');
                        $scope.loading = false;
                    },
                    function() {
                        toastr.error('Could not load the images');
                        $scope.loading = false;
                    });
        }

        function deleteImage(imageId) {
            toastr.info('Deleting image...');
            ImageService
                .deleteImage(imageId)
                .then(
                    function() {
                        imageCtrl.loadImages();
                        toastr.success('Image Deleted.');
                    },
                    function() {
                        toastr.error('Could not delete the image.');
                    });
        }

        function uploadImage(imageName, imageUrl) {
            $('#imageModal').modal('hide'); // TODO: This should be done using a directive
            toastr.info('Uploading image...');
            ImageService
                .uploadImage(imageName, imageUrl)
                .then(
                    function() {
                        imageCtrl.loadImages();
                        toastr.success('Image Uploaded.');
                    },
                    function() {
                        toastr.error('Could not upload the image.');
                    });
        }

        function changeColumnSort(criteriaIndex, reverse) {
            imageCtrl.columnSort = { criteria: imageCtrl.sortingCriteria[criteriaIndex], reverse: reverse };
        }

    }
    ImageCtrl.$inject = ['ImageService', '$scope'];

})();