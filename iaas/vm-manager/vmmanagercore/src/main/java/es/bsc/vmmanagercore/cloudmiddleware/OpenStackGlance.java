package es.bsc.vmmanagercore.cloudmiddleware;

import java.net.URI;
import java.util.HashMap;

import org.apache.commons.validator.UrlValidator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.utils.HttpUtils;

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
	private String token;
	
	public OpenStackGlance() {
		VmManagerConfiguration conf = VmManagerConfiguration.getInstance();
		this.openStackIp = conf.openStackIP;
		this.glancePort = conf.glancePort;
		this.keyStonePort = conf.keyStonePort;
		this.keyStoneUser = conf.keyStoneUser;
		this.keyStonePassword = conf.keyStonePassword;
		this.keyStoneTenantId = conf.keyStoneTenantId;
		
		//get the token needed for authentication
		token = getToken();
	}

	public String createImageFromUrl(ImageToUpload imageToUpload) {
		//check that the URL received is valid
		UrlValidator urlValidator = new UrlValidator();
		if (!urlValidator.isValid(imageToUpload.getUrl())) { 
			throw new IllegalArgumentException("The URL received to create the image is not valid");
		}
		
		//build the URI of the HTTP request
		URI uri = HttpUtils.buildURI("http", openStackIp, glancePort, "/v1/images");
		
		//build the headers of the HTTP request
		HashMap<String, String> headers = new HashMap<>();
		headers.put("X-Auth-Token", token);
		headers.put("x-image-meta-container_format", "bare");
		headers.put("User-Agent", "python-glanceclient");
		headers.put("x-image-meta-is_public", "True");
		headers.put("x-glance-api-copy-from", imageToUpload.getUrl());
		headers.put("Content-Type", "application/octet-stream");
		headers.put("x-image-meta-disk_format", "qcow2");
		headers.put("x-image-meta-name", imageToUpload.getName());
		
		//execute the HTTP request
		String responseContent = HttpUtils.executeHttpRequest("POST", uri, headers, "");
		
		//return the image ID
		ObjectMapper mapper = new ObjectMapper();
		JsonNode imageIdJson = null;
		try {
			imageIdJson = mapper.readTree(responseContent).get("image").get("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imageIdJson.asText();
	}
	
	public void deleteImage(String imageId) {
		//build the URI of the HTTP request
		URI uri = HttpUtils.buildURI("http", openStackIp, glancePort, "/v2/images/" + imageId);
		
		//build the headers of the HTTP request
		HashMap<String, String> headers = new HashMap<>();
		headers.put("X-Auth-Token", token);
		
		//execute the HTTP request
		HttpUtils.executeHttpRequest("DELETE", uri, headers, "");
	}
	
	public boolean imageIsActive(String imageId) {
		//build the URI of the HTTP request
		URI uri = HttpUtils.buildURI("http", openStackIp, glancePort, "/v2/images/" + imageId);
		
		//build the headers of the HTTP request
		HashMap<String, String> headers = new HashMap<>();
		headers.put("X-Auth-Token", token);
		headers.put("User-Agent", "python-glanceclient");
		headers.put("Content-Type", "application/octet-stream");
		
		//execute the HTTP request
		String responseContent = HttpUtils.executeHttpRequest("GET", uri, headers, "");
		
		//get the image status
		ObjectMapper mapper = new ObjectMapper();
		String imageStatus = "";
		try {
			imageStatus = mapper.readTree(responseContent).get("status").asText();
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
		//build the URI of the HTTP request
		URI uri = HttpUtils.buildURI("http", openStackIp, keyStonePort, "/v2.0/tokens");
		
		//build the headers of the HTTP request
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Content-type", "application/json");
		
		//build the parameters of the HTTP request
		String params = "{\"auth\":{\"passwordCredentials\":"
				+ "{\"username\":" + "\"" + keyStoneUser + "\""
				+ ", \"password\":" + "\"" + keyStonePassword + "\"}"
				+ ", \"tenantId\":" + "\"" + keyStoneTenantId + "\"}}";
		
		//execute the HTTP request
		String responseContent = HttpUtils.executeHttpRequest("POST", uri, headers, params);
		
		//get the token
		ObjectMapper mapper = new ObjectMapper();
		JsonNode tokenJson = null;
		try {
			tokenJson = mapper.readTree(responseContent).get("access").get("token").get("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tokenJson.asText();
	}
	
}
