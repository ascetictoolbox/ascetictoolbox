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

package es.bsc.demiurge.openstackjclouds;

import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.models.images.ImageToUpload;
import es.bsc.demiurge.core.utils.CommandExecutor;
import es.bsc.demiurge.core.utils.HttpUtils;
import org.apache.commons.validator.UrlValidator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/** 
 * This class is a connector for the OpenStack image service (Glance). It is needed because
 * the version of JClouds that we are using does not implement the communication with Glance.
 * 
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es).
 */
public class OpenStackGlance {

    private final OpenStackCredentials openStackCredentials;
	private final Logger log = LogManager.getLogger(OpenStackGlance.class);

    // I have not implemented a mechanism to reuse tokens.
    // This might make things a bit slower.

    /**
     * Class constructor.
     */
    public OpenStackGlance(OpenStackCredentials openStackCredentials) {
        this.openStackCredentials = openStackCredentials;
    }

    /**
     * Uploads an image to OpenStack. The image is downloaded from a given URL.
     *
     * @param imageToUpload the image to upload
     * @return the ID of the image just created. This ID is the same as the ID assigned in OpenStack.
     */
    public String createImageFromUrl(ImageToUpload imageToUpload) throws CloudMiddlewareException {
        String responseContent;
        if (new UrlValidator().isValid(imageToUpload.getUrl())) {
            responseContent = HttpUtils.executeHttpRequest("POST",
                    HttpUtils.buildURI("http",
                            openStackCredentials.getOpenStackIP(),
                            openStackCredentials.getGlancePort(),
                            "/v1/images"),
                    getHeadersForCreateImageRequest(imageToUpload), "");
			log.debug("Creating image from Valid URL " + imageToUpload.getUrl()+ " :\n"+ responseContent);
            return getIdFromCreateImageImageResponse(responseContent);
        }
        else {


            try {
                String glanceCommandOutput = CommandExecutor.executeCommand(
                        "glance --os-username " + openStackCredentials.getKeyStoneUser() +
                        " --os-password " + openStackCredentials.getKeyStonePassword() + " " +
                        "--os-tenant-id " + openStackCredentials.getKeyStoneTenantId() + " " +
                        "--os-auth-url http://" + openStackCredentials.getOpenStackIP() + ":35357/v2.0 " +
                        "image-create --name=" + imageToUpload.getName() + " " +
                        "--disk-format=" + getImageFormat(imageToUpload.getUrl()) + " " +
                        "--container-format=bare --is-public=True " +
                        "--file " + imageToUpload.getUrl());

                log.debug("Creating image from non-valid URL. Glance command:\n"+glanceCommandOutput);
                String outputIdLine = glanceCommandOutput.split(System.getProperty("line.separator"))[9];
                String id = outputIdLine.split("\\|")[2]; // Get the line where that specifies the ID
                return id.substring(1, id.length() - 1); // Remove first and last characters (spaces)
            } catch (Exception e) {
                throw new CloudMiddlewareException("Error creating image from " + imageToUpload.getUrl()+ ": " + e.getMessage(),e);
            }
        }
    }

    /**
     * Returns the headers for the call used to created an image
     *
     * @param imageToUpload the image to be uploaded
     * @return the headers
     */
    private Map<String, String> getHeadersForCreateImageRequest(ImageToUpload imageToUpload) {
        Map<String, String> result = new HashMap<>();
        result.put("X-Auth-Token", getToken());
        result.put("x-image-meta-container_format", "bare");
        result.put("User-Agent", "python-glanceclient");
        result.put("x-image-meta-is_public", "True");
        if (new UrlValidator().isValid(imageToUpload.getUrl())) {
            result.put("x-image-meta-location", imageToUpload.getUrl());
        }
        result.put("Content-Type", "application/octet-stream");
        // For the moment, assume that if the image comes from a URL the format is qcow2
        result.put("x-image-meta-disk_format", "qcow2");
        result.put("x-image-meta-name", imageToUpload.getName());
        return result;
    }

    /**
     * Extracts the ID of the image from the response of the call used to create an image.
     *
     * @param response the response of the call
     * @return the ID of the image
     */
    private String getIdFromCreateImageImageResponse(String response) {
        JsonNode imageIdJson;
        try {
            imageIdJson = new ObjectMapper().readTree(response).get("image").get("id");
        } catch (Exception e) {
            throw new RuntimeException("There was a problem while uploading an image.");
        }
        return imageIdJson.asText();
    }

    /**
     * Deletes an image from the OpenStack infrastructure.
     *
     * @param imageId the ID of the image to be deleted.
     */
    public void deleteImage(String imageId) {
        HttpUtils.executeHttpRequest("DELETE",
                HttpUtils.buildURI("http",
                        openStackCredentials.getOpenStackIP(),
                        openStackCredentials.getGlancePort(),
                        "/v2/images/" + imageId),
                getHeadersForDeleteImageRequest(), "");
    }

    /**
     * Returns the headers for the call used to delete an image.
     *
     * @return the headers.
     */
    private Map<String, String> getHeadersForDeleteImageRequest() {
        Map<String, String> result = new HashMap<>();
        result.put("X-Auth-Token", getToken());
        return result;
    }

    /**
     * Checks whether an image is active. In OpenStack, an image can be in different statuses: active, deleted,
     * queued, etc. An image is only fully available if it is in the active state.
     *
     * @param imageId the id of the image.
     * @return true if the image is active, false otherwise.
     */
    public boolean imageIsActive(String imageId) {
        String responseContent = HttpUtils.executeHttpRequest("GET",
                HttpUtils.buildURI("http",
                        openStackCredentials.getOpenStackIP(),
                        openStackCredentials.getGlancePort(),
                        "/v2/images/" + imageId),
                getHeadersForImageIsActiveRequest(), "");
        return imageIsActiveFromResponse(responseContent);
    }

    /**
     * Returns the headers for the call used to check whether an image is active.
     *
     * @return the headers.
     */
    private Map<String, String> getHeadersForImageIsActiveRequest() {
        Map<String, String> result = new HashMap<>();
        result.put("X-Auth-Token", getToken());
        result.put("User-Agent", "python-glanceclient");
        result.put("Content-Type", "application/octet-stream");
        return result;
    }

    /**
     * Checks whether an image is active from the response of the call used to check whether an image is active.
     *
     * @param response the response of the call
     * @return True if the image is active, false otherwise.
     */
    private boolean imageIsActiveFromResponse(String response) {
        String imageStatus = "";
        try {
            imageStatus = new ObjectMapper().readTree(response).get("status").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageStatus.equals("active");
    }

    /**
     * Gets a token needed to perform requests to the OpenStack API.
     *
     * @return Token needed for authentication
     */
    private String getToken() {
        String responseContent = HttpUtils.executeHttpRequest("POST",
                HttpUtils.buildURI("http",
                        openStackCredentials.getOpenStackIP(),
                        openStackCredentials.getKeyStonePort(),
                        "/v2.0/tokens"),
                getHeadersForGetTokenRequest(),
                getParamsForGetTokenRequest());
        return getTokenFromGetTokenResponse(responseContent);
    }

    /**
     * Returns the headers for the call used to get a token.
     *
     * @return the headers
     */
    private Map<String, String> getHeadersForGetTokenRequest() {
        Map<String, String> result = new HashMap<>();
        result.put("Content-type", "application/json");
        return result;
    }

    /**
     * Returns the parameters for the call used to get a token.
     *
     * @return the headers
     */
    private String getParamsForGetTokenRequest() {
        return "{\"auth\":{\"passwordCredentials\":"
                + "{\"username\":" + "\"" + openStackCredentials.getKeyStoneUser() + "\""
                + ", \"password\":" + "\"" + openStackCredentials.getKeyStonePassword() + "\"}"
                + ", \"tenantId\":" + "\"" + openStackCredentials.getKeyStoneTenantId() + "\"}}";
    }

    /**
     * Extracts the token from the response of the call used to get the token.
     *
     * @param response the response of the call
     * @return the token
     */
    private String getTokenFromGetTokenResponse(String response) {
        JsonNode tokenJson;
        try {
            tokenJson = new ObjectMapper().readTree(response).get("access").get("token").get("id");
        } catch (Exception e) {
            throw new RuntimeException("Could not login to the Glance service.");
        }
        return tokenJson.asText();
    }

    /**
     * Returns the format of an image
     *
     * @param path the path of the image
     * @return the format of the image
     */
    private String getImageFormat(String path) throws CloudMiddlewareException {
        // This uses qemu-utils. The host where the VMM is running needs to have qemu-utils installed.
        // Is there any way we can get rid of this dependency?

        try {
            String qemuInfoOutput = CommandExecutor.executeCommand("qemu-img info " + path);
            // The result of the qemu-img command executed contains in the second line "file format: <format>"
            return qemuInfoOutput.split(System.getProperty("line.separator"))[1].split(":")[1].substring(1);
        } catch(Exception e) {
            throw new CloudMiddlewareException(e.getMessage(), e);
        }
    }

}
