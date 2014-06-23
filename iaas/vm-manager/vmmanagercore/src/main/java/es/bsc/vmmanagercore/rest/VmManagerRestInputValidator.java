package es.bsc.vmmanagercore.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates the input of the REST calls.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerRestInputValidator {

    private static List<String> validActions =
            Arrays.asList("rebootHard", "rebootSoft", "start", "stop", "suspend", "resume");

    public static boolean checkVmDescriptions(JsonObject vmsJson) {
        if (vmsJson.get("vms") == null) {
            return false;
        }

        String[] requiredParams = {"name", "image", "cpus", "ramMb", "diskGb", "applicationId"};
        JsonArray vmsJsonArray = vmsJson.getAsJsonArray("vms");
        for (JsonElement vmJsonElement: vmsJsonArray) {
            JsonObject vmJson = vmJsonElement.getAsJsonObject();
            for (String requiredParam: requiredParams) {

                // Check that the required parameters have been included
                if (vmJson.get(requiredParam) == null) {
                    return false;
                }

                // Check that CPUs, ramMb, and diskGb have non-negative values
                if (requiredParam.equals("cpus") || requiredParam.equals("ramMb") ||
                        requiredParam.equals("diskGb")) {
                    if (vmJson.get(requiredParam).getAsInt() < 0) {
                        return false;
                    }
                }

                // Check that the name is not empty
                if (requiredParam.equals("name")) {
                    if (vmJson.get(requiredParam).getAsString().equals("")) {
                        return false;
                    }
                }

            }
        }

        return true;
    }

    public static boolean checkImageExists(String imageId, ArrayList<String> imagesIds) {
        return imagesIds.contains(imageId);
    }

    public static boolean isValidAction(String action) {
        return validActions.contains(action);
    }

    public static boolean checkImageDescription(JsonNode imageDescription) {
        String[] necessaryParams = {"name", "url"};
        for (String necessaryParam: necessaryParams) {
            // Check that the required parameters have been included
            if (!imageDescription.has(necessaryParam)) {
                return false;
            }
        }
        return true;
    }

}
