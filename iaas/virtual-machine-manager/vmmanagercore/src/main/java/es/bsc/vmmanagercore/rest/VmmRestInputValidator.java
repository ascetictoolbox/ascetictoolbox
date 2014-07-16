package es.bsc.vmmanagercore.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.bsc.vmmanagercore.model.ImageToUpload;

import javax.ws.rs.WebApplicationException;
import java.util.Arrays;
import java.util.List;

/**
 * Validates the input of the REST calls.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmmRestInputValidator {

    private List<String> validActions = Arrays.asList("rebootHard", "rebootSoft", "start", "stop", "suspend", "resume");

    public void checkVmDescriptions(JsonObject vmsJson) {
        if (vmsJson.get("vms") == null) {
            throw new WebApplicationException(400);
        }

        String[] requiredParams = {"name", "image", "cpus", "ramMb", "diskGb", "applicationId"};
        JsonArray vmsJsonArray = vmsJson.getAsJsonArray("vms");
        for (JsonElement vmJsonElement: vmsJsonArray) {
            JsonObject vmJson = vmJsonElement.getAsJsonObject();
            for (String requiredParam: requiredParams) {

                // Check that the required parameters have been included
                if (vmJson.get(requiredParam) == null) {
                    throw new WebApplicationException(400);
                }

                // Check that CPUs, ramMb, and diskGb have non-negative values
                if (requiredParam.equals("cpus") || requiredParam.equals("ramMb") ||
                        requiredParam.equals("diskGb")) {
                    if (vmJson.get(requiredParam).getAsInt() < 0) {
                        throw new WebApplicationException(400);
                    }
                }

                // Check that the name is not empty
                if (requiredParam.equals("name")) {
                    if (vmJson.get(requiredParam).getAsString().equals("")) {
                        throw new WebApplicationException(400);
                    }
                }

            }
        }
    }

    public void checkVmExists(boolean vmExists) {
        if (!vmExists) {
            throw new WebApplicationException(404);
        }
    }

    public void checkImageExists(String imageId, List<String> imagesIds) {
        if (!imagesIds.contains(imageId)) {
            throw new WebApplicationException(404);
        }
    }

    public void checkImageDescription(ImageToUpload imageToUpload) {
        if (imageToUpload.getName() == null || imageToUpload.getUrl() == null) {
            throw new WebApplicationException(400);
        }
    }

    public void checkJsonActionFormat(JsonObject jsonObject) {
        if (jsonObject.get("action") == null) {
            throw new WebApplicationException(400);
        }
        if (!validActions.contains(jsonObject.get("action").getAsString())) {
            throw new WebApplicationException(400);
        }
    }

}
