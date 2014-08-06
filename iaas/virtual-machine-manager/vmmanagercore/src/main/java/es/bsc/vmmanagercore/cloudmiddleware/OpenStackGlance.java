package es.bsc.vmmanagercore.cloudmiddleware;

import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.utils.CommandExecutor;
import es.bsc.vmmanagercore.utils.HttpUtils;
import org.apache.commons.validator.UrlValidator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/** 
 * This class is a connector for the OpenStack image service (Glance). It is needed because
 * the version of JClouds that we are using does not implement the communication with Glance.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es).
 * 
 */
public class OpenStackGlance {

    private String openStackIp;
    private int glancePort;
    private int keyStonePort;
    private String keyStoneUser;
    private String keyStonePassword;
    private String keyStoneTenantId;
    private String token; // token needed for authentication
    private CommandExecutor commandExecutor = new CommandExecutor();

    /**
     * Class constructor.
     */
    public OpenStackGlance() {
        VmManagerConfiguration conf = VmManagerConfiguration.getInstance();
        this.openStackIp = conf.openStackIP;
        this.glancePort = conf.glancePort;
        this.keyStonePort = conf.keyStonePort;
        this.keyStoneUser = conf.keyStoneUser;
        this.keyStonePassword = conf.keyStonePassword;
        this.keyStoneTenantId = conf.keyStoneTenantId;
        token = getToken();
    }

    /**
     * Uploads an image to OpenStack. The image is downloaded from a given URL.
     *
     * @param imageToUpload the image to upload
     * @return the ID of the image just created. This ID is the same as the ID assigned in OpenStack.
     */
    //TODO I need to refactor this function.
    public String createImageFromUrl(ImageToUpload imageToUpload) {
        //build the headers of the HTTP request
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Auth-Token", token);
        headers.put("x-image-meta-container_format", "bare");
        headers.put("User-Agent", "python-glanceclient");
        headers.put("x-image-meta-is_public", "True");
        if (new UrlValidator().isValid(imageToUpload.getUrl())) {
            headers.put("x-image-meta-location", imageToUpload.getUrl());
        }
        headers.put("Content-Type", "application/octet-stream");
        headers.put("x-image-meta-disk_format", "qcow2");
        headers.put("x-image-meta-name", imageToUpload.getName());

        String responseContent;
        if (new UrlValidator().isValid(imageToUpload.getUrl())) {
            responseContent = HttpUtils.executeHttpRequest("POST",
                    HttpUtils.buildURI("http", openStackIp, glancePort, "/v1/images"), headers, "");

            //return the image ID
            JsonNode imageIdJson;
            try {
                imageIdJson = new ObjectMapper().readTree(responseContent).get("image").get("id");
            } catch (Exception e) {
                throw new RuntimeException("There was a problem while uploading an image.");
            }
            return imageIdJson.asText();
        }
        else {
            String glanceCommandOutput = commandExecutor.executeCommand(
                    "glance --os-username vm.manager --os-password vmmanager14 " +
                    "--os-tenant-id f559470b483c48f18479bd039400b007 " +
                    "--os-auth-url http://130.149.248.39:35357/v2.0 " +
                    "image-create --name=" + imageToUpload.getName() + " " +
                    "--disk-format=qcow2 --container-format=bare --is-public=True " +
                    "--file " + imageToUpload.getUrl());
            String outputIdLine = glanceCommandOutput.split(System.getProperty("line.separator"))[9];
            String id = outputIdLine.split("\\|")[2]; // Get the line where that specifies the ID
            return id.substring(1, id.length() - 1); // Remove first and last characters (spaces)
        }
    }

    /**
     * Deletes an image from the OpenStack infrastructure.
     *
     * @param imageId the ID of the image to be deleted.
     */
    public void deleteImage(String imageId) {
        HttpUtils.executeHttpRequest("DELETE",
                HttpUtils.buildURI("http", openStackIp, glancePort, "/v2/images/" + imageId),
                getHeadersForDeleteImageRequest(), "");
    }

    /**
     * Returns the headers for the call used to delete an image.
     *
     * @return the headers.
     */
    private Map<String, String> getHeadersForDeleteImageRequest() {
        Map<String, String> result = new HashMap<>();
        result.put("X-Auth-Token", token);
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
                HttpUtils.buildURI("http", openStackIp, glancePort, "/v2/images/" + imageId),
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
        result.put("X-Auth-Token", token);
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
        String responseContent = HttpUtils.executeHttpRequest("POST", HttpUtils.buildURI("http", openStackIp,
                keyStonePort, "/v2.0/tokens"), getHeadersForGetTokenRequest(), getParamsForGetTokenRequest());
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
                + "{\"username\":" + "\"" + keyStoneUser + "\""
                + ", \"password\":" + "\"" + keyStonePassword + "\"}"
                + ", \"tenantId\":" + "\"" + keyStoneTenantId + "\"}}";
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

}
