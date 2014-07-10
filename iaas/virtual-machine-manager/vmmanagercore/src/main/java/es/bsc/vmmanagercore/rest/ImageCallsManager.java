package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.model.ImageUploaded;

import javax.ws.rs.WebApplicationException;

/**
 * This class implements the REST calls that are related with VM images.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class ImageCallsManager {

    private Gson gson = new Gson();
    private static JsonParser parser = new JsonParser();
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
        JsonArray jsonImagesArray = new JsonArray();
        for (ImageUploaded image: vmManager.getVmImages()) {
            jsonImagesArray.add(parser.parse(gson.toJson(image, ImageUploaded.class)));
        }
        JsonObject result = new JsonObject();
        result.add("images", jsonImagesArray);
        return result.toString();
    }

    /**
     * Uploads an image to the infrastructure.
     *
     * @param imageInfo the JSON document that contains the description of the image to be uploaded.
     * @return the ID of the image
     */
    public String uploadImage(String imageInfo) {
        // Read the input JSON
        ImageToUpload imageToUpload = gson.fromJson(imageInfo, ImageToUpload.class);

        // Throw a 400 exception if the format of the JSON is not correct
        if (imageToUpload.getName() == null || imageToUpload.getUrl() == null) {
            throw new WebApplicationException(400);
        }

        // Create the image and return its ID
        String id = vmManager.createVmImage(imageToUpload);
        JsonObject idJson = new JsonObject();
        idJson.addProperty("id", id);
        return idJson.toString();
    }

    /**
     * Returns a JSON document that contains the information of a particular image.
     *
     * @param imageId the ID of the image
     * @return the JSON document
     */
    public String getImage(String imageId) {
        // Throw error if the image does not exist
        if (!VmmRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds())) {
            throw new WebApplicationException(404);
        }

        // Return JSON representation of the image
        return gson.toJson(vmManager.getVmImage(imageId));
    }

    /**
     * Deletes an image from the infrastructure
     *
     * @param imageId the ID of the image
     */
    public void deleteImage(String imageId) {
        // Throw error if the image does not exist
        if (!VmmRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds())) {
            throw new WebApplicationException(404);
        }

        // Delete the image
        vmManager.deleteVmImage(imageId);
    }

}
