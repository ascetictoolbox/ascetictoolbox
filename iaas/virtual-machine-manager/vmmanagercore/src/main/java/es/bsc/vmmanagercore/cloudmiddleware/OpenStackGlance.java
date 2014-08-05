package es.bsc.vmmanagercore.cloudmiddleware;

import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.utils.HttpUtils;
import org.apache.commons.validator.UrlValidator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    public String createImageFromUrl(ImageToUpload imageToUpload) {
        //build the headers of the HTTP request
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Auth-Token", token);
        headers.put("x-image-meta-container_format", "bare");
        headers.put("User-Agent", "python-glanceclient");
        headers.put("x-image-meta-is_public", "True");
        //headers.put("x-glance-api-copy-from", imageToUpload.getUrl());
        if (new UrlValidator().isValid(imageToUpload.getUrl())) {
            headers.put("x-image-meta-location", imageToUpload.getUrl());
        }
        headers.put("Content-Type", "application/octet-stream");
        headers.put("x-image-meta-disk_format", "qcow2");
        headers.put("x-image-meta-name", imageToUpload.getName());

        /*
        String filePath = null;
        if (!new UrlValidator().isValid(imageToUpload.getUrl())) {
            filePath = imageToUpload.getUrl();
        }

        String responseContent = HttpUtils.executeHttpRequest("POST",
                HttpUtils.buildURI("http", openStackIp, glancePort, "/v1/images"), headers, "", filePath);
        */

        String responseContent;
        if (new UrlValidator().isValid(imageToUpload.getUrl())) {
            responseContent = HttpUtils.executeHttpRequest("POST",
                    HttpUtils.buildURI("http", openStackIp, glancePort, "/v1/images"), headers, "", null);

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
            String glanceCommandOutput = executeCommand("glance --os-username vm.manager --os-password vmmanager14 " +
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
        //build the headers of the HTTP request
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Auth-Token", token);

        //execute the HTTP request
        HttpUtils.executeHttpRequest("DELETE",
                HttpUtils.buildURI("http", openStackIp, glancePort, "/v2/images/" + imageId), headers, "", null);
    }

    /**
     * Checks whether an image is active. In OpenStack, an image can be in different statuses: active, deleted,
     * queued, etc. An image is only fully available if it is in the active state.
     *
     * @param imageId the id of the image.
     * @return true if the image is active, false otherwise.
     */
    public boolean imageIsActive(String imageId) {
        //build the headers of the HTTP request
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Auth-Token", token);
        headers.put("User-Agent", "python-glanceclient");
        headers.put("Content-Type", "application/octet-stream");

        //execute the HTTP request
        String responseContent = HttpUtils.executeHttpRequest("GET",
                HttpUtils.buildURI("http", openStackIp, glancePort, "/v2/images/" + imageId), headers, "", null);

        //get the image status
        String imageStatus = "";
        try {
            imageStatus = new ObjectMapper().readTree(responseContent).get("status").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageStatus.equals("active");
    }

    /**
     * Gets a token needed to perform requests to the OpenStack API.
     *
     * @return Token needed for authentication.
     */
    private String getToken() {
        //build the headers of the HTTP request
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");

        //build the parameters of the HTTP request
        String params = "{\"auth\":{\"passwordCredentials\":"
                + "{\"username\":" + "\"" + keyStoneUser + "\""
                + ", \"password\":" + "\"" + keyStonePassword + "\"}"
                + ", \"tenantId\":" + "\"" + keyStoneTenantId + "\"}}";

        //execute the HTTP request
        String responseContent = HttpUtils.executeHttpRequest("POST",
                HttpUtils.buildURI("http", openStackIp, keyStonePort, "/v2.0/tokens"), headers, params, null);

        //get the token
        JsonNode tokenJson;
        try {
            tokenJson = new ObjectMapper().readTree(responseContent).get("access").get("token").get("id");
        } catch (Exception e) {
            throw new RuntimeException("Could not login to the Glance service.");
        }
       return tokenJson.asText();
    }

    private String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

}
