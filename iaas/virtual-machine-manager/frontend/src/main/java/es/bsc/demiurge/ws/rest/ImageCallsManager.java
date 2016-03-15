/**
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

package es.bsc.demiurge.ws.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.bsc.demiurge.core.models.images.ListImagesUploaded;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.manager.VmManager;
import es.bsc.demiurge.core.models.images.ImageToUpload;

/**
 * This class implements the REST calls that are related with VM images.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ImageCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;
    private RestInputValidator inputValidator = new RestInputValidator();

    /**
     * Class constructor.
     *
     * @param vmManager the VM manager
     */
    public ImageCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    /**
     * Returns a JSON document that contains the information of all the images of the infrastructure.
     *
     * @return the JSON document
     */
    public String getAllImages() {
        return gson.toJson(new ListImagesUploaded(vmManager.getVmImages()));
    }

    /**
     * Uploads an image to the infrastructure.
     *
     * @param imageInfo the JSON document that contains the description of the image to be uploaded.
     * @return the ID of the image
     */
    public String uploadImage(String imageInfo) throws CloudMiddlewareException {
        inputValidator.checkImageDescription(gson.fromJson(imageInfo, ImageToUpload.class));
        String imageId = vmManager.createVmImage(gson.fromJson(imageInfo, ImageToUpload.class));
        return getJsonWithImageId(imageId);
    }

    /**
     * Returns a JSON document that contains the information of a particular image.
     *
     * @param imageId the ID of the image
     * @return the JSON document
     */
    public String getImage(String imageId) {
        inputValidator.checkImageExists(imageId, vmManager.getVmImagesIds());
        return gson.toJson(vmManager.getVmImage(imageId));
    }

    /**
     * Deletes an image from the infrastructure.
     *
     * @param imageId the ID of the image
     */
    public void deleteImage(String imageId) {
        inputValidator.checkImageExists(imageId, vmManager.getVmImagesIds());
        vmManager.deleteVmImage(imageId);
    }

    /**
     * Returns a JSON document that contains the ID of an image.
     *
     * @param id the image ID
     * @return the JSON document
     */
    private String getJsonWithImageId(String id) {
        JsonObject idJson = new JsonObject();
        idJson.addProperty("id", id);
        return idJson.toString();
    }

}
