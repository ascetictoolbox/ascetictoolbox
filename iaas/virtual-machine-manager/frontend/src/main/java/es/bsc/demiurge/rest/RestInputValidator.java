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

package es.bsc.demiurge.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import es.bsc.demiurge.core.models.images.ImageToUpload;

import javax.ws.rs.WebApplicationException;
import java.util.Arrays;
import java.util.List;

/**
 * Validates the input of the REST calls.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class RestInputValidator {

    private List<String> validActions = Arrays.asList("migrate", "rebootHard", "rebootSoft", "start",
            "stop", "suspend", "resume");

    public void checkVmDescriptions(JsonObject vmsJson) {
        if (vmsJson.get("vms") == null) {
            throw new WebApplicationException(400);
        }

        String[] requiredParams = {"name", "image", "cpus", "ramMb", "diskGb"};
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
        // The JSON format for a migration action is a bit different.
        // It needs an "options" array that has to contain the field "destinationHostName".
        if (jsonObject.get("action").getAsString().equals("migrate")) {
            if (jsonObject.get("options") == null) {
                throw new WebApplicationException(400);
            }
            JsonArray optionsArray = (JsonArray) jsonObject.get("options");
            JsonObject destinationHostNameObject = (JsonObject)optionsArray.get(0);
            if (destinationHostNameObject.get("destinationHostName") == null) {
                throw new WebApplicationException(400);
            }
        }
    }

    public void checkHostExists(boolean hostExists) {
        if (!hostExists) {
            throw new WebApplicationException(400);
        }
    }

}
