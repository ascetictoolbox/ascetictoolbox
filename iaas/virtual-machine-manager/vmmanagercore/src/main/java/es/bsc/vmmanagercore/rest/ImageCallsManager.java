package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.model.ListImagesUploaded;

/**
 * This class implements the REST calls that are related with VM images.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ImageCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

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
    public String uploadImage(String imageInfo) {
        VmmRestInputValidator.checkImageDescription(gson.fromJson(imageInfo, ImageToUpload.class));
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
        VmmRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds());
        return gson.toJson(vmManager.getVmImage(imageId));
    }

    /**
     * Deletes an image from the infrastructure.
     *
     * @param imageId the ID of the image
     */
    public void deleteImage(String imageId) {
        VmmRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds());
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
